package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.util.Identifier
import java.util.concurrent.ConcurrentSkipListSet

/**
 * @property code submissions code
 * @property stepId submissions step id
 * @property submissionsList list of submission infos corresponding to [code]
 * @property id
 */
data class SubmissionsNode(
    val id: Identifier,
    val code: String,
    val stepId: Int,
    val submissionsList: ConcurrentSkipListSet<SubmissionInfo>
) : Comparable<SubmissionsNode> {
    constructor(
        id: Identifier,
        code: String,
        stepId: Int,
        submissionsList: MutableSet<SubmissionInfo>
    ) : this(
        id,
        code,
        stepId,
        ConcurrentSkipListSet(submissionsList)
    )

    constructor(submission: Submission, id: Identifier) : this(
        id,
        submission.code,
        submission.stepId,
        ConcurrentSkipListSet(listOf(submission.info))
    )

    override fun compareTo(other: SubmissionsNode): Int = id - other.id

    override fun equals(other: Any?): Boolean = other is SubmissionsNode && other.code == code

    override fun hashCode(): Int = code.hashCode()

    override fun toString(): String =
        "\nSubmissionsNode(code = \n$code, \nidList = ${submissionsList.map { it.id }.sorted()})"
}
