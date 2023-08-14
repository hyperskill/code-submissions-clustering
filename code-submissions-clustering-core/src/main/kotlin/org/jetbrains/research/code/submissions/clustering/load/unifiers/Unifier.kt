package org.jetbrains.research.code.submissions.clustering.load.unifiers

import org.jetbrains.research.code.submissions.clustering.model.Submission

interface Unifier {
    suspend fun Submission.unify(): Submission
    suspend fun clear() = Unit
}
