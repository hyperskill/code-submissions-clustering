package org.jetbrains.research.code.submissions.clustering.impl.util.logging

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.research.ml.ast.transformations.Transformation
import kotlin.system.measureTimeMillis

class TransformationsStatisticsBuilder {
    private val transformationsNumber = mutableMapOf<String, Int>()
    private val transformationsExecTime = mutableMapOf<String, Long>()

    suspend fun forwardApplyMeasuredWithTimeout(transformation: Transformation, psiTree: PsiFile, timeout: Long) {
        val previousTree = ApplicationManager.getApplication().runReadAction<PsiElement> {
            psiTree.copy()
        }
        val executionTime = withTimeout(timeout) {
            measureTimeMillis {
                ApplicationManager.getApplication().invokeAndWait({
                    ApplicationManager.getApplication().runWriteAction {
                        transformation.forwardApply(psiTree)
                    }
                }, ModalityState.NON_MODAL)
            }
        }

        var isApplied = false
        ApplicationManager.getApplication().invokeAndWait({
            isApplied = ApplicationManager.getApplication().runReadAction<Boolean> {
                !(previousTree?.textMatches(psiTree) ?: false)
            }
        }, ModalityState.NON_MODAL)
        if (isApplied) {
            transformationsNumber[transformation.key] =
                transformationsNumber.getOrDefault(transformation.key, 0) + 1
        }
        transformationsExecTime[transformation.key] =
            transformationsExecTime.getOrDefault(transformation.key, 0) + executionTime
    }

    fun buildStatistics(transformations: List<Transformation>): String = buildString {
        appendLine("Transformations statistics:")
        transformations.forEach {
            appendLine(
                buildTransformationStats(
                    it.key,
                    transformationsExecTime.getOrDefault(it.key, 0),
                    transformationsNumber.getOrDefault(it.key, 0)
                )
            )
        }
    }

    private fun buildTransformationStats(key: String, execTime: Long, number: Int): String = buildString {
        append(key.padEnd(LOG_PADDING))
        append("$execTime ms".padEnd(LOG_PADDING))
        append("$number times applied")
    }

    companion object {
        const val LOG_PADDING = 30
    }
}
