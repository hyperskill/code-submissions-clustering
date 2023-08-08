package org.jetbrains.research.code.submissions.clustering.client

import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier
import org.jetbrains.research.code.submissions.clustering.model.Submission

class IjUnifier(private val orchestrator: CodeServerOrchestrator) : Unifier {
    override suspend fun Submission.unify(): Submission = orchestrator.unify(this@unify)

    override suspend fun clear() {
        orchestrator.clearUnifier()
    }
}
