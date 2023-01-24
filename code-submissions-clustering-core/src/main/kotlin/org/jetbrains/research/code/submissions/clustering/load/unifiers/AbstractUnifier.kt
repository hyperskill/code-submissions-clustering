package org.jetbrains.research.code.submissions.clustering.load.unifiers

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.util.asPsiFile
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.code.submissions.clustering.util.reformatInWriteAction
import org.jetbrains.research.ml.ast.transformations.Transformation
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

/**
 * Abstract unifier producing unifying transformations over code submissions.
 * @property project project to use
 * @property psiManager PSI manager to use
 */
abstract class AbstractUnifier(
    private val project: Project, private val psiManager: PsiManager, private val anonymization: Transformation? = null
) {
    private val logger = Logger.getLogger(javaClass.name)
    abstract val language: Language
    abstract val transformations: List<Transformation>
    private val codeToUnifiedCode = HashMap<String, String>()
    private val transformationsNumber = mutableMapOf<String, Int>()
    private val transformationsExecTime = mutableMapOf<String, Long>()

    private fun Transformation.forwardApplyMeasured(psiTree: PsiFile) {
        val previousTree = psiTree.copy()
        val executionTime = measureTimeMillis {
            forwardApply(psiTree)
        }
        val isApplied = !(previousTree?.textMatches(psiTree) ?: false)
        if (isApplied) {
            transformationsNumber[key] = transformationsNumber.getOrDefault(key, 0) + 1
        }
        transformationsExecTime[key] = transformationsExecTime.getOrDefault(key, 0) + executionTime
    }

    @Suppress("TooGenericExceptionCaught")
    private fun PsiFile.applyTransformations(transformations: List<Transformation>, previousTree: PsiElement? = null) {
        logger.fine { "Tree Started: ${this.text}" }

        val psiDocumentManager = this.project.service<PsiDocumentManager>()
        val document = this.let { psiDocumentManager.getDocument(it) }
        try {
            transformations.forEach {
                document?.let {
                    psiDocumentManager.commitDocument(document)
                }
                logger.finer { "Transformation Started: ${it.key}" }
                it.forwardApplyMeasured(this)
                logger.finer { "Transformation Ended: ${it.key}" }
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
    fun Submission.unify(): Submission {
        clearStats()
        val code = codeToUnifiedCode.getOrDefault(this.code, this.code.asPsiFile(language, psiManager) {
            ApplicationManager.getApplication().invokeAndWait {
                ApplicationManager.getApplication().runWriteAction {
                    anonymization?.forwardApplyMeasured(it)
                    var iterationNumber = 0
                    do {
                        ++iterationNumber
                        val previousTree = it.copy()
                        it.applyTransformations(transformations, previousTree)
                        logger.finer { "Previous text[$iterationNumber]:\n${previousTree.text}\n" }
                        logger.finer { "Current text[$iterationNumber]:\n${it.text}\n\n" }
                    } while (!previousTree.textMatches(it.text) && iterationNumber <= MAX_ITERATIONS && codeToUnifiedCode[it.text] == null)
                    logger.fine { "Tree Ended[[$iterationNumber]]: ${it.text}\n\n\n" }
                    logger.info { "Total iterations number: $iterationNumber" }
                }
            }
            codeToUnifiedCode.getOrPut(it.text) { it.reformatInWriteAction().text }
        })
        logger.info { logTransformationStats() }
        return this.copy(code = code)
    }

    fun clearCache() = codeToUnifiedCode.clear()

    private fun clearStats() {
        transformationsExecTime.clear()
        transformationsNumber.clear()
    }

    private fun logTransformationStats(): String = buildString {
        appendLine("Transformations statistics:")
        val logTransformations = anonymization?.let {
            listOf(it) + transformations
        } ?: transformations
        logTransformations.forEach {
            appendLine(
                logStats(
                    it.key,
                    transformationsExecTime.getOrDefault(it.key, 0),
                    transformationsNumber.getOrDefault(it.key, 0)
                )
            )
        }
    }

    private fun logStats(key: String, execTime: Long, number: Int): String = buildString {
        append(key.padEnd(LOG_PADDING))
        append("$execTime ms".padEnd(LOG_PADDING))
        append("$number times applied")
    }

    companion object {
        const val LOG_PADDING = 30
        const val MAX_ITERATIONS = 50
    }
}

fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
