package org.jetbrains.research.code.submissions.clustering.impl.unifiers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import io.ktor.utils.io.*
import kotlinx.coroutines.TimeoutCancellationException
import org.jetbrains.research.code.submissions.clustering.impl.util.logging.TransformationsStatisticsBuilder
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.reformatInWriteAction
import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.ml.ast.transformations.Transformation
import java.util.logging.Logger

/**
 * Abstract unifier producing unifying transformations over code submissions.
 * @property project project to use
 */
abstract class AbstractUnifier(
    private val project: Project
) : Unifier {
    private val logger = Logger.getLogger(javaClass.name)
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
        logger.fine { "Tree Started: ${this.text}" }

        val psiDocumentManager = this.project.service<PsiDocumentManager>()
        val document = psiDocumentManager.getDocument(this)
        try {
            transformations.filter { it !in skipTransformations }.forEach {
                applyTransformation(it, this, document, psiDocumentManager, statsBuilder)
            }
        } catch (e: Throwable) {
            logger.severe {
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
        val code = this.code.let { code ->
            val psi = psiFileFactory.getPsiFile(code)
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    var iterationNumber = 0
                    do {
                        ++iterationNumber
                        val previousTree = psi.copy()
                        psi.applyTransformations(repeatingTransformations, statsBuilder, previousTree)
                        logger.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
                        logger.finer { "Current text[$iterationNumber]:\n${psi.text}\n\n" }
                    } while (!previousTree.textMatches(psi.text) && iterationNumber <= MAX_ITERATIONS)
                    singleRunTransformations.forEach {
                        isFinishedWithTimeout(it, psi, statsBuilder)
                    }
                    logger.fine { "Tree Ended[[$iterationNumber]]: ${psi.text}\n\n\n" }
                    logger.info { "Total iterations number: $iterationNumber" }
                }
            }
            psi.reformatInWriteAction().text.also {
                psiFileFactory.releasePsiFile(psi)
            }
        }
        logger.info {
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
        logger.fine { "Transformation Started: ${transformation.key}" }
        if (!isFinishedWithTimeout(transformation, psiTree, statsBuilder)) {
            logger.severe { "Transformation Skipped: ${transformation.key}" }
        } else {
            logger.fine { "Transformation Ended: ${transformation.key}" }
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
        try {
            statsBuilder.forwardApplyMeasuredWithTimeout(transformation, psiTree, TIMEOUT_MS)
        } catch (e: TimeoutCancellationException) {
            // Skip transformation with timeout in further iterations
            skipTransformations.add(transformation)
            logger.severe { "Timeout reached for ${transformation.key}: $e" }
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
