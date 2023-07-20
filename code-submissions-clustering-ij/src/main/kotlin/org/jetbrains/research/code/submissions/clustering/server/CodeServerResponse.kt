package org.jetbrains.research.code.submissions.clustering.server

import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsWeight

sealed interface CodeServerResponse

/**
 * @property submissionCode submission code
 */
data class UnifyResponse(val submissionCode: SubmissionCode) : CodeServerResponse

/**
 * @property submissionsWeight weight between two submissions codes
 */
data class CalcDistResponse(val submissionsWeight: SubmissionsWeight) : CodeServerResponse
