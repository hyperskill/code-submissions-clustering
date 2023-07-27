package org.jetbrains.research.code.submissions.clustering.impl.unifiers

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.research.code.submissions.clustering.impl.util.logging.TransformationsStatisticsBuilder
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.reformatInWriteAction
import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.transformations.Transformation
import java.util.logging.Logger

/**
 * Abstract unifier producing unifying transformations over code submissions.
 * @property project project to use
 */
abstract class AbstractUnifier(
    private val project: Project, private val anonymization: Transformation? = null
) : Unifier {
    private val logger = Logger.getLogger(javaClass.name)
    abstract val language: Language
    abstract val transformations: List<Transformation>
    private val codeToUnifiedCode = HashMap<String, String>()
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
            transformations.forEach {
                logger.finer { "Transformation Started: ${it.key}" }
                statsBuilder.forwardApplyMeasured(it, this)
                logger.finer { "Transformation Ended: ${it.key}" }
                document?.let {
                    psiDocumentManager.commitDocument(document)
                }
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
    override fun Submission.unify(): Submission {
        val statsBuilder = TransformationsStatisticsBuilder()
        val code = this.code.let { code ->
            val psi = psiFileFactory.getPsiFile(code)
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    anonymization?.let { anon ->
                        statsBuilder.forwardApplyMeasured(anon, psi)
                    }
                    var iterationNumber = 0
                    do {
                        ++iterationNumber
                        val previousTree = psi.copy()
                        psi.applyTransformations(transformations, statsBuilder, previousTree)
                        logger.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
                        logger.finer { "Current text[$iterationNumber]:\n${psi.text}\n\n" }
                    } while (!previousTree.textMatches(psi.text) && iterationNumber <= MAX_ITERATIONS)
                    logger.fine { "Tree Ended[[$iterationNumber]]: ${psi.text}\n\n\n" }
                    logger.info { "Total iterations number: $iterationNumber" }
                }
            }
            psi.reformatInWriteAction().text.also {
                psiFileFactory.releasePsiFile(psi)
            }
        }
        logger.info {
            statsBuilder.buildStatistics(listOfNotNull(anonymization) + transformations)
        }
        return this.copy(code = code)
    }

    fun clearCache() = codeToUnifiedCode.clear()

    override fun clear() = clearCache()

    companion object {
        const val MAX_ITERATIONS = 50
    }
}

fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
