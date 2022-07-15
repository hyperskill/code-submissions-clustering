package org.jetbrains.research.code.submissions.clustering

/**
 * Data class representing basic information about code submission.
 *
 * @property id submission id
 * @property stepId step id
 * @property code submission code
 */
data class Submission(val id: Int, val stepId: Int, val code: String)
