package org.jetbrains.research.code.submissions.clustering.server

import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsEdge

sealed interface CodeServerRequest

data class UnifyRequest(val submissionCode: SubmissionCode) : CodeServerRequest
data class CalcDistRequest(val submissionsEdge: SubmissionsEdge) : CodeServerRequest
