package org.jetbrains.research.code.submissions.clustering.util.parallel

/**
 * Interface for processing values and accumulating results using context.
 *
 * @param V values type
 * @param C context type
 * @param A accumulator type
 */
fun interface ParallelProcessor<V, C, A> {
    fun process(value: V, context: C, accumulator: A): A
}
