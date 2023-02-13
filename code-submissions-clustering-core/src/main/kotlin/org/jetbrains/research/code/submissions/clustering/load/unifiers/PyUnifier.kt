package org.jetbrains.research.code.submissions.clustering.load.unifiers

import com.intellij.openapi.project.Project
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.psi.PsiFileFactory
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.ast.transformations.augmentedAssignment.AugmentedAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.commentsRemoval.CommentsRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.comparisonUnification.ComparisonUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.constantfolding.ConstantFoldingTransformation
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.emptyLinesRemoval.EmptyLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.expressionUnification.ExpressionUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.ifRedundantLinesRemoval.IfRedundantLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.multipleOperatorComparison.MultipleOperatorComparisonTransformation
import org.jetbrains.research.ml.ast.transformations.multipleTargetAssignment.MultipleTargetAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.outerNotElimination.OuterNotEliminationTransformation

/**
 * Python-specific unifier.
 * @property psiFileFactory
 */
class PyUnifier(
    override val psiFileFactory: PsiFileFactory,
    project: Project,
) : AbstractUnifier(project, AnonymizationTransformation) {
    override val language = Language.PYTHON
    override val transformations = listOf(
        AugmentedAssignmentTransformation,
        CommentsRemovalTransformation,
        ComparisonUnificationTransformation,
        ConstantFoldingTransformation,
        DeadCodeRemovalTransformation,
        EmptyLinesRemovalTransformation,
        ExpressionUnificationTransformation,
        IfRedundantLinesRemovalTransformation,
        MultipleOperatorComparisonTransformation,
        MultipleTargetAssignmentTransformation,
        OuterNotEliminationTransformation,
    )
}
