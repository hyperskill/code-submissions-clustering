package org.jetbrains.research.code.submissions.clustering.client

import kotlinx.coroutines.runBlocking
import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier
import org.jetbrains.research.code.submissions.clustering.model.Submission

class IjUnifier(private val clientImpl: CodeServerClientImpl) : Unifier {
    override fun Submission.unify(): Submission = runBlocking {
        clientImpl.unify(this@unify)
    }

    override fun clear() = Unit
}
