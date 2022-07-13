package org.jetbrains.research.code.submissions.clustering.load

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.PythonFileType
import org.jetbrains.research.code.submissions.clustering.Submission
import org.jetbrains.research.code.submissions.clustering.util.addPyFileToProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.transformations.Transformation
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

private const val MAX_ITERATIONS: Int = 5

/**
 * Class for unifying code solutions using ast transformations.
 */
class Unifier {

    private val project: Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
        ?: error("Internal error: the temp project was not created")
    private val psiManager: PsiManager = PsiManager.getInstance(project)

    init {
        setSdkToProject(project, getTmpProjectDir())
    }

    private fun String.createPsiFile(id: Int): PsiFile {
        return ApplicationManager.getApplication().runWriteAction<PsiFile> {
            val basePath = getTmpProjectDir(toCreateFolder = false)
            val fileName = "dummy$id." + PythonFileType.INSTANCE.defaultExtension
            val file = addPyFileToProject(basePath, fileName, fileContext = this)
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            psiManager.findFile(virtualFile!!)
        }
    }

    private fun PsiFile.applyTransformations(transformations: List<Transformation>): Boolean {
        val prevPsi = this.copy()
        ApplicationManager.getApplication().invokeAndWait {
            ApplicationManager.getApplication().runWriteAction {
                transformations.forEach {
                    it.forwardApply(this)
                }
            }
        }
        return prevPsi.text != this.text
    }

    private val allTransformations: List<Transformation>
        get() = listOf(
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

    fun Submission.unify(): Submission {
        val psi = this.code.createPsiFile(this.id)

        var iterationsCounter = 0
        do {
            iterationsCounter += 1
            if (!psi.applyTransformations(allTransformations)) {
                break
            }
        } while (iterationsCounter <= MAX_ITERATIONS)

        return this.copy(code = psi.text)
    }
}