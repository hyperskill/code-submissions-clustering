package org.jetbrains.research.code.submissions.clustering.util.parallel

object ParallelUtils {
    fun <V> MutableList<V>.combineWith(other: MutableList<V>): MutableList<V> {
        if (this.size < other.size) {
            return other.combineWith(this)
        }
        this.addAll(other)
        return this
    }
}
