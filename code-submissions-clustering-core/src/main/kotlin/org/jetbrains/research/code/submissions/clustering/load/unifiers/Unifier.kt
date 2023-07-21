package org.jetbrains.research.code.submissions.clustering.load.unifiers

import org.jetbrains.research.code.submissions.clustering.model.Submission

interface Unifier {
    fun Submission.unify(): Submission
    fun clear() = Unit
}
