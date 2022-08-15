package org.jetbrains.research.code.submissions.clustering.load.context.builder

import org.jetbrains.research.code.submissions.clustering.model.SubmissionNodeID

/**
 * Factory to create an identifier unique to factory instance
 */
interface SubmissionNodeIdentifierFactory {
    /**
     * Creates unique solution space vertex identifier
     *
     * @return unique identifier
     */
    fun uniqueIdentifier(): SubmissionNodeID
}

class SubmissionNodeIdentifierFactoryImpl : SubmissionNodeIdentifierFactory {
    private var counter = 0

    override fun uniqueIdentifier(): SubmissionNodeID = counter++
}
