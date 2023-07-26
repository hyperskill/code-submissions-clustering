package org.jetbrains.research.code.submissions.clustering.server

import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsEdge

sealed interface CodeServerRequest

/**
 * @property submissionCode submission code
 */
data class UnifyRequest(val submissionCode: SubmissionCode) : CodeServerRequest

/**
 * @property submissionsEdge pair of submissions codes
 */
data class CalcDistRequest(val submissionsEdge: SubmissionsEdge) : CodeServerRequest

object ClearRequest : CodeServerRequest
