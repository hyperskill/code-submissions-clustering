package org.jetbrains.research.code.submissions.clustering.load.context

import org.jetbrains.research.code.submissions.clustering.load.distance.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.load.unifiers.Unifier

/**
 * @property unifier unifier to use while operating submissions graph
 * @property codeDistanceMeasurer object to measure distance between code strings
 */
data class SubmissionsGraphContext<T>(
    val unifier: Unifier,
    val codeDistanceMeasurer: CodeDistanceMeasurerBase<T>,
)
