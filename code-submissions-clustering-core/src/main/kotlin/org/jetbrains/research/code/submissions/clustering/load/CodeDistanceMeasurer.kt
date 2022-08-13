package org.jetbrains.research.code.submissions.clustering.load

interface CodeDistanceMeasurer {
    fun computeDistance(code: String, otherCode: String): Int
}
