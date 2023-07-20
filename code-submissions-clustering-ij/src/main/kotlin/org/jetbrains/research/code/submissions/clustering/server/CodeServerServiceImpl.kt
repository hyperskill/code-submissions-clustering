package org.jetbrains.research.code.submissions.clustering.server

import org.jetbrains.research.code.submissions.clustering.CodeServerGrpcKt
import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.SubmissionsWeight
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import java.util.logging.Logger

class CodeServerServiceImpl(language: Language) : CodeServerGrpcKt.CodeServerCoroutineImplBase() {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    private val graphContext = GumTreeGraphContextBuilder().setLanguage(language).buildContext()

    @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
    override suspend fun unify(request: SubmissionCode): SubmissionCode {
        logger.info("Receive request: \n${request.code}")
        val code = try {
            graphContext.unifier.run {
                createMockSubmission(request.code).unify().code
            }
        } catch (e: Throwable) {
            logger.severe(e.toString())
            throw e
        }
        logger.info("Unification finished")
        return SubmissionCode.newBuilder().setCode(code).build()
    }

    override suspend fun calculateWeight(request: SubmissionsEdge): SubmissionsWeight {
        val weight = (graphContext.codeDistanceMeasurer as GumTreeDistanceMeasurerByPsi).run {
            val source = request.from.code.parseTree()
            val target = request.to.code.parseTree()
            calculateDistance(source, target).calculateWeight()
        }
        return SubmissionsWeight.newBuilder().setWeight(weight).build()
    }

    private fun createMockSubmission(code: String) = Submission(SubmissionInfo(0, 0), 0, code)
}
