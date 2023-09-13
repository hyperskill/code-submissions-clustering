package org.jetbrains.research.code.submissions.clustering.impl.unifiers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import mu.KotlinLogging
import io.ktor.utils.io.*
import org.jetbrains.research.code.submissions.clustering.impl.util.logging.TransformationsStatisticsBuilder
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.reformatInWriteAction
import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.ml.ast.transformations.Transformation

/**
 * Abstract unifier producing unifying transformations over code submissions.
 * @property project project to use
 */
abstract class AbstractUnifier(
    private val project: Project
) : Unifier {
    private val logger = KotlinLogging.logger { Unit }
    private val statisticsLogger = KotlinLogging.logger("TransformationsStatsLogger")
    abstract val language: Language
    abstract val singleRunTransformations: List<Transformation>
    abstract val repeatingTransformations: List<Transformation>
    protected abstract val psiFileFactory: PsiFileFactory

    @Suppress("TooGenericExceptionCaught")
    private fun PsiFile.applyTransformations(
        transformations: List<Transformation>,
        statsBuilder: TransformationsStatisticsBuilder,
        previousTree: PsiElement? = null,
    ) {
        logger.debug { "Tree Started: ${this.text}" }

        val psiDocumentManager = this.project.service<PsiDocumentManager>()
        val document = psiDocumentManager.getDocument(this)
        try {
            transformations.filter { it !in skipTransformations }.forEach {
                applyTransformation(it, this, document, psiDocumentManager, statsBuilder)
            }
        } catch (e: Throwable) {
            logger.error {
                """Transformation error {$e}: 
                        |Previous Code=${previousTree?.text}
                        |Current Code=${this.text}
                        |""".trimMargin()
            }
        }
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA")
    override suspend fun Submission.unify(): Submission {
        val statsBuilder = TransformationsStatisticsBuilder()
        skipTransformations = mutableSetOf()
        statisticsLogger.info { "Unification: STEP_ID=$stepId ID=${info.id}" }
        logger.debug { "Unification: STEP_ID=$stepId ID=${info.id}" }
        val code = this.code.let { code ->
            val psi = psiFileFactory.getPsiFile(code)
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    var iterationNumber = 0
                    do {
                        ++iterationNumber
                        val previousTree = psi.copy()
                        psi.applyTransformations(repeatingTransformations, statsBuilder, previousTree)
                        logger.debug { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
                        logger.debug { "Current text[$iterationNumber]:\n${psi.text}\n\n" }
                    } while (!previousTree.textMatches(psi.text) && iterationNumber <= MAX_ITERATIONS)
                    singleRunTransformations.forEach {
                        isFinishedWithTimeout(it, psi, statsBuilder)
                    }
                    logger.debug { "Tree Ended[[$iterationNumber]]: ${psi.text}\n\n\n" }
                    statisticsLogger.info { "Total iterations number: $iterationNumber" }
                }
            }
            psi.reformatInWriteAction().text.also {
                psiFileFactory.releasePsiFile(psi)
            }
        }
        statisticsLogger.info {
            statsBuilder.buildStatistics(singleRunTransformations + repeatingTransformations)
        }
        return this.copy(code = code)
    }

    private fun applyTransformation(
        transformation: Transformation,
        psiTree: PsiFile,
        document: Document?,
        psiDocumentManager: PsiDocumentManager,
        statsBuilder: TransformationsStatisticsBuilder,
    ) {
        logger.debug { "Transformation Started: ${transformation.key}" }
        if (!isFinishedWithTimeout(transformation, psiTree, statsBuilder)) {
            logger.warn { "Transformation Skipped: ${transformation.key}" }
        } else {
            logger.debug { "Transformation Ended: ${transformation.key}" }
            document?.let {
                psiDocumentManager.commitDocument(document)
            }
        }
    }

    private fun isFinishedWithTimeout(
        transformation: Transformation,
        psiTree: PsiFile,
        statsBuilder: TransformationsStatisticsBuilder,
    ): Boolean {
        ProgressIndicatorUtils.withTimeout(TIMEOUT_MS) {
            statsBuilder.forwardApplyMeasured(transformation, psiTree)
        } ?: run {
            // Skip transformation with timeout in further iterations
            skipTransformations.add(transformation)
            logger.warn { "Timeout reached for ${transformation.key}" }
            return false
        }
        return true
    }

    companion object {
        const val MAX_ITERATIONS = 50
        const val TIMEOUT_MS: Long = 10000
        var skipTransformations = mutableSetOf<Transformation>()
    }
}
