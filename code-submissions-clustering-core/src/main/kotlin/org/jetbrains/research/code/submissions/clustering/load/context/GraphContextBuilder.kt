package org.jetbrains.research.code.submissions.clustering.load.context

interface GraphContextBuilder<T> {
    fun buildContext(): SubmissionsGraphContext<T>
}
