package org.jetbrains.research.code.submissions.clustering.load.clustering.hac.parallel

fun interface ParallelProcessor<V, C, A> {
    fun process(value: V, context: C, accumulator: A): A
}
