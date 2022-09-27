package org.jetbrains.research.code.submissions.clustering.load.context

import com.intellij.psi.PsiFile
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.load.unifiers.AbstractUnifier

/**
 * @property unifier unifier to use while operating submissions graph
 * @property codeDistanceMeasurer object to measure distance between code strings
 */
data class SubmissionsGraphContext<T>(
    val unifier: AbstractUnifier,
    val codeDistanceMeasurer: CodeDistanceMeasurerBase<T>,
    val codeToUnifiedPsi: HashMap<String, PsiFile> = HashMap(),
)
