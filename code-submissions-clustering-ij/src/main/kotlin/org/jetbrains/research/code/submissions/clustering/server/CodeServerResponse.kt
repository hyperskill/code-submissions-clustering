package org.jetbrains.research.code.submissions.clustering.server

import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsWeight

sealed interface CodeServerResponse

data class UnifyResponse(val submissionCode: SubmissionCode) : CodeServerResponse
data class CalcDistResponse(val submissionsWeight: SubmissionsWeight) : CodeServerResponse
