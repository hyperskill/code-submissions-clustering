package org.jetbrains.research.code.submissions.clustering.util.parallel

import java.util.concurrent.*
import java.util.stream.Collectors
import kotlin.math.min

class ParallelContext : AutoCloseable {
    private val service: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    override fun close() {
        service.shutdownNow()
    }

    /**
     * Process [values] and accumulate results parallel.
     *
     * @param values values to process
     * @param accumulatorFactory creates initial accumulator
     * @param processor processes value and accumulates result
     * @param combiner combines parallel processed accumulators
     */
    fun <V, A> runParallel(
        values: List<V>,
        accumulatorFactory: () -> A,
        processor: (V, A) -> A,
        combiner: (A, A) -> A
    ): A = runParallel<V, Unit?, A>(
        values,
        { value, _, accumulator -> processor(value, accumulator) },
        null,
        accumulatorFactory,
        combiner
    )

    /**
     * Process [values] and accumulate results parallel.
     *
     * @param values values to process
     * @param processor processes value and accumulates result
     * @param context context
     * @param accumulatorFactory creates initial accumulator
     * @param combiner combines parallel processed accumulators
     */
    fun <V, C, A> runParallel(
        values: List<V>,
        processor: ParallelProcessor<V, C, A>,
        context: C,
        accumulatorFactory: () -> A,
        combiner: (A, A) -> A
    ): A = runParallel(
        values,
        processor,
        accumulatorFactory,
        { context },
        combiner
    )

    /**
     * Process [values] and accumulate results parallel.
     *
     * @param values values to process
     * @param processor processes value and accumulates result
     * @param accumulatorFactory creates initial accumulator
     * @param contextFactory creates context
     * @param combiner combines parallel processed accumulators
     */
    fun <V, C, A> runParallel(
        values: List<V>,
        processor: ParallelProcessor<V, C, A>,
        accumulatorFactory: () -> A,
        contextFactory: () -> C,
        combiner: (A, A) -> A
    ): A {
        val tasks = splitValues(values).stream().sequential()
            .map { list ->
                Task(
                    list,
                    accumulatorFactory,
                    contextFactory,
                    processor
                )
            }.collect(Collectors.toList())
        val results = tasks
            .map(service::submit)
        return results.stream().sequential()
            .map(Companion::getResult)
            .reduce(combiner)
            .orElseGet(accumulatorFactory)
    }

    /**
     * Single parallel task for processing values and accumulating results.
     *
     * @param values values to process
     * @param accumulatorFactory creates accumulator
     * @param contextFactory creates context
     * @param processor processes value and accumulates result
     */
    private inner class Task<V, C, A>(
        private val values: List<V>,
        private val accumulatorFactory: () -> A,
        private val contextFactory: () -> C,
        private val processor: ParallelProcessor<V, C, A>
    ) : Callable<A> {
        override fun call(): A {
            val context = contextFactory()
            var accumulator = accumulatorFactory()
            for (value in values) {
                accumulator = processor.process(value, context, accumulator)
            }
            return accumulator
        }
    }

    companion object {
        private const val MAX_BLOCKS_COUNT = 4

        /**
         * Split values in at most [MAX_BLOCKS_COUNT] blocks.
         */
        private fun <V> splitValues(values: List<V>): List<List<V>> {
            if (values.isEmpty()) {
                return listOf(values)
            }
            val lists: MutableList<List<V>> = ArrayList()
            val valuesCount = values.size
            val blocksCount = min(MAX_BLOCKS_COUNT, values.size)
            val blockSize = (valuesCount - 1) / blocksCount + 1  // round up
            var blockStart = 0
            while (blockStart < valuesCount) {
                lists.add(values.subList(blockStart, min(blockStart + blockSize, valuesCount)))
                blockStart += blockSize
            }
            return lists
        }

        @Suppress("TooGenericExceptionThrown")
        private fun <V> getResult(future: Future<V>): V {
            while (true) {
                try {
                    return future.get()
                } catch (e: InterruptedException) {
                    continue
                } catch (e: ExecutionException) {
                    throw RuntimeException(e)
                }
            }
        }
    }
}
