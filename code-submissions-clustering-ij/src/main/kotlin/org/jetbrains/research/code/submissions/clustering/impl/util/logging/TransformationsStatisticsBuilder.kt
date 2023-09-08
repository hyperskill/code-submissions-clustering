package org.jetbrains.research.code.submissions.clustering.impl.util.logging

import com.intellij.psi.PsiFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.research.ml.ast.transformations.Transformation
import kotlin.system.measureTimeMillis

class TransformationsStatisticsBuilder {
    private val transformationsNumber = mutableMapOf<String, Int>()
    private val transformationsExecTime = mutableMapOf<String, Long>()

    fun forwardApplyMeasuredWithTimeout(transformation: Transformation, psiTree: PsiFile, timeout: Long) {
        val previousTree = psiTree.copy()
        val executionTime = measureTimeMillis {
            runBlocking {
                withTimeout(timeout) {
                    transformation.forwardApply(psiTree)
                }
            }
        }
        val isApplied = !(previousTree?.textMatches(psiTree) ?: false)
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
