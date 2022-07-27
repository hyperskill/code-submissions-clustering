package org.jetbrains.research.code.submissions.clustering.load.unifiers

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.load.Language
import org.jetbrains.research.code.submissions.clustering.load.createTempProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.transformations.anonymization.AnonymizationTransformation
import org.jetbrains.research.ml.ast.transformations.augmentedAssignment.AugmentedAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.commentsRemoval.CommentsRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.comparisonUnification.ComparisonUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.constantfolding.ConstantFoldingTransformation
import org.jetbrains.research.ml.ast.transformations.deadcode.DeadCodeRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.expressionUnification.ExpressionUnificationTransformation
import org.jetbrains.research.ml.ast.transformations.ifRedundantLinesRemoval.IfRedundantLinesRemovalTransformation
import org.jetbrains.research.ml.ast.transformations.multipleOperatorComparison.MultipleOperatorComparisonTransformation
import org.jetbrains.research.ml.ast.transformations.multipleTargetAssignment.MultipleTargetAssignmentTransformation
import org.jetbrains.research.ml.ast.transformations.outerNotElimination.OuterNotEliminationTransformation
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject

/**
 * Python-specific unifier.
 * @property project project to use (creates new by default)
 * @property psiManager PSI manager to use (gets instance from new project by default)
 */
class PyUnifier(
    override val project: Project = createTempProject(),
    override val psiManager: PsiManager = PsiManager.getInstance(project),
    isSdkSet: Boolean = false
) : AbstractUnifier() {
    override val language = Language.PYTHON
    override val transformations = listOf(
        AnonymizationTransformation,
        AugmentedAssignmentTransformation,
        CommentsRemovalTransformation,
        ComparisonUnificationTransformation,
        ConstantFoldingTransformation,
        DeadCodeRemovalTransformation,
        ExpressionUnificationTransformation,
        IfRedundantLinesRemovalTransformation,
        MultipleOperatorComparisonTransformation,
        MultipleTargetAssignmentTransformation,
        OuterNotEliminationTransformation,
    )

    init {
        if (!isSdkSet) {
            setSdkToProject(project, getTmpProjectDir())
        }
    }
}
