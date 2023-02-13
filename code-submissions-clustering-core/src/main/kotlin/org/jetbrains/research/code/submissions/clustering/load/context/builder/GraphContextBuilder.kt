package org.jetbrains.research.code.submissions.clustering.load.context.builder

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext

interface GraphContextBuilder<T> {
    fun buildContext(): SubmissionsGraphContext<T>
}
