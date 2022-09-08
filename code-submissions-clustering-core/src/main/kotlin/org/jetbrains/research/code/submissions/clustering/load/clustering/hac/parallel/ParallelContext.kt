package org.jetbrains.research.code.submissions.clustering.load.clustering.hac.parallel

import java.util.concurrent.*
import kotlin.math.min

class ParallelContext : AutoCloseable {
    private val service: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    override fun close() {
        service.shutdownNow()
    }

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

    fun <V, C, A> runParallel(
        values: List<V>,
        processor: ParallelProcessor<V, C, A>,
        accumulatorFactory: () -> A,
        contextFactory: () -> C,
        combiner: (A, A) -> A
    ): A {
        val tasks = splitValues(values)
            .map { list ->
                Task(
                    list,
                    accumulatorFactory,
                    contextFactory,
                    processor
                )
            }
        val results = tasks.map { task ->
            service.submit(task)
        }
        return results.stream().sequential()
            .map(Companion::getResult)
            .reduce(combiner)
            .orElseGet(accumulatorFactory)
    }

    private inner class Task<V, C, A>(
        private val values: List<V>,
        private val accumulatorFactory: () -> A,
        private val contextFactory: () -> C,
        processor: ParallelProcessor<V, C, A>
    ) : Callable<A> {
        private val processor: ParallelProcessor<V, C, A>

        init {
            this.processor = processor
        }

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
        private const val MIN_BLOCKS_COUNT = 4

        private fun <V> splitValues(values: List<V>): List<List<V>> {
            if (values.isEmpty()) {
                return listOf(values)
            }
            val lists: MutableList<List<V>> = ArrayList()
            val valuesCount = values.size
            val blocksCount = min(MIN_BLOCKS_COUNT, values.size)
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
                } catch (e: ExecutionException) {
                    throw RuntimeException(e)
                }
            }
        }
    }
}
