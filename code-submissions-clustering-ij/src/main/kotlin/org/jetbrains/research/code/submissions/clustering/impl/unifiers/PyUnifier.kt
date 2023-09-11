package org.jetbrains.research.code.submissions.clustering.impl.unifiers

import com.intellij.openapi.project.Project
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.ml.ast.transformations.Transformation
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
    transformationsConfig: TransformationsConfig = defaultTransformationsConfig
) : AbstractUnifier(project) {
    override val language = Language.PYTHON
    override val repeatingTransformations = transformationsConfig.repeatingTransformations.map {
        PyTransformations.valueOf(it).transformation
    }
    override val singleRunTransformations: List<Transformation> = transformationsConfig.singleTransformations.map {
        PyTransformations.valueOf(it).transformation
    }

    /**
     * @property transformation Python transformation
     */
    enum class PyTransformations(val transformation: Transformation) {
        ANONYMIZATION(AnonymizationTransformation),
        AUGMENTED_ASSIGNMENT(AugmentedAssignmentTransformation),
        COMMENTS_REMOVAL(CommentsRemovalTransformation),
        COMPARISON_UNIFICATION(ComparisonUnificationTransformation),
        CONSTANT_FOLDING(ConstantFoldingTransformation),
        DEAD_CODE_REMOVAL(DeadCodeRemovalTransformation),
        EMPTY_LINES_REMOVAL(EmptyLinesRemovalTransformation),
        EXPRESSION_UNIFICATION(ExpressionUnificationTransformation),
        IF_REDUNDANT_LINES_REMOVAL(IfRedundantLinesRemovalTransformation),
        MULTIPLE_OPERATOR_COMPARISON(MultipleOperatorComparisonTransformation),
        MULTIPLE_TARGET_ASSIGNMENT(MultipleTargetAssignmentTransformation),
        OUTER_NOT_ELIMINATION(OuterNotEliminationTransformation),
        ;
    }

    companion object {
        val defaultTransformationsConfig = TransformationsConfig(
            repeatingTransformations = PyTransformations.values().map { it.name },
            singleTransformations = listOf()
        )
    }
}
