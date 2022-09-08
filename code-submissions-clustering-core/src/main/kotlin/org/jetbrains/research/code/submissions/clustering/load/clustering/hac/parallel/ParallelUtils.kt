package org.jetbrains.research.code.submissions.clustering.load.clustering.hac.parallel

object ParallelUtils {
    fun <V> combineLists(first: MutableList<V>, second: MutableList<V>): MutableList<V> {
        if (first.size < second.size) {
            return combineLists(second, first)
        }
        first.addAll(second)
        return first
    }
}
