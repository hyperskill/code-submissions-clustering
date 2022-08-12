package org.jetbrains.research.code.submissions.clustering.load

/**
 * @property unifier unifier to use while operating submissions graph
 * @property codeDistanceMeasurer object to measure distance between code strings
 */
data class SubmissionsGraphContext(
    val unifier: AbstractUnifier,
    val codeDistanceMeasurer: CodeDistanceMeasurer
)
