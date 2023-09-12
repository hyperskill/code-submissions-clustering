package org.jetbrains.research.code.submissions.clustering.impl.util.logging

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withTimeout
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.MyTimeoutCancellationException
import org.jetbrains.research.ml.ast.transformations.Transformation
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis


class TransformationsStatisticsBuilder {
    private val transformationsNumber = mutableMapOf<String, Int>()
    private val transformationsExecTime = mutableMapOf<String, Long>()

    suspend fun forwardApplyMeasuredWithTimeout(transformation: Transformation, psiTree: PsiFile, timeout: Long) {
        val previousTree = ApplicationManager.getApplication().runReadAction<PsiElement> {
            psiTree.copy()
        }

//        println("current thread: ${Thread.currentThread()}")

        val executionTime = withTimeout(timeout) {
//            val pool = Executors.newFixedThreadPool(1)
            println("execution start: ${transformation.key}")
            future {
//                println("async thread: ${Thread.currentThread()}")
                measureTimeMillis {
                     ApplicationManager.getApplication().invokeAndWait({
//                        println("invoke thread: ${Thread.currentThread()}")
                         ProgressIndicatorUtils.withTimeout(timeout) {
                             transformation.forwardApply(psiTree)
                         } ?: throw MyTimeoutCancellationException("${transformation.key} with timeout!")
                     }, ModalityState.NON_MODAL)
                }
//                println("measureTimeMillis finished")
//                ms
            }.await()
        }

        println("Await reached for ${transformation.key}")

        var isApplied: Boolean? = null
        ApplicationManager.getApplication().invokeAndWait({
            isApplied = ApplicationManager.getApplication().runReadAction<Boolean> {
                !(previousTree?.textMatches(psiTree) ?: false)
            }
        }, ModalityState.NON_MODAL)
        require(isApplied != null) { "isApplied was not calculated!" }
        if (isApplied!!) {
            transformationsNumber[transformation.key] = transformationsNumber.getOrDefault(transformation.key, 0) + 1
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
