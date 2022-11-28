package org.jetbrains.research.code.submissions.clustering.model

/**
 * Data class representing basic information about code submission.
 *
 * @property info submission info consisting of id and quality of code
 * @property stepId step id
 * @property code submission code
 */
data class Submission(val info: SubmissionInfo, val stepId: Int, val code: String)

/**
 * @property id submission id
 * @property quality submission quality of code
 */
data class SubmissionInfo(val id: Int, val quality: Int) : Comparable<SubmissionInfo> {
    override fun compareTo(other: SubmissionInfo): Int {
        if (quality != other.quality) {
            return other.quality - quality
        }
        return id - other.id
    }
}
