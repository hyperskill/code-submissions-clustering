package org.jetbrains.research.code.submissions.clustering.model

class SubmissionsNode(submission: Submission) {
    val code: String = submission.code
    val stepId: Int = submission.stepId
    val idList: MutableSet<Int> = mutableSetOf(submission.id)

    constructor(oldNode: SubmissionsNode, submission: Submission) : this(submission) {
        idList.addAll(oldNode.idList)
    }

    override fun equals(other: Any?): Boolean = other is SubmissionsNode && other.code == code

    override fun hashCode(): Int = code.hashCode()

    override fun toString(): String = "\nSubmissionsNode(code = \n$code, \nidList = ${idList.toList().sorted()})"
}
