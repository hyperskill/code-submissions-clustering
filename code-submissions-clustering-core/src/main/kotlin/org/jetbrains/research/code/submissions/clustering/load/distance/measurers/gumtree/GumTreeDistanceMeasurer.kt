package org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.load.unifiers.createTempProject
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.util.asPsiFile
import org.jetbrains.research.code.submissions.clustering.util.trimCode

abstract class GumTreeDistanceMeasurerBase : CodeDistanceMeasurerBase<List<Action>>() {
    override fun List<Action>.calculateWeight() = this.size

    abstract fun String.parseTree(context: SubmissionsGraphContext<*>): TreeContext

    private fun calculateDistance(source: TreeContext, target: TreeContext) = Matcher(source, target).getEditActions()

    final override fun computeFullDistance(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
        context: SubmissionsGraphContext<*>
    ): List<Action> {
        val source = graph.getEdgeSource(edge).code.parseTree(context)
        val target = graph.getEdgeTarget(edge).code.parseTree(context)
        return calculateDistance(source, target)
    }
}

class GumTreeDistanceMeasurerByParser(
    private val treeGenerator: TreeGenerator,
) : GumTreeDistanceMeasurerBase() {
    override fun List<Action>.calculateWeight() = this.size

    @Suppress("WRONG_OVERLOADING_FUNCTION_ARGUMENTS")
    override fun String.parseTree(context: SubmissionsGraphContext<*>) = this.parseTree(treeGenerator)

    private fun String.parseTree(treeGenerator: TreeGenerator) =
        treeGenerator.generateFrom().string(this) ?: error("Can not parse code: $this")
}

class GumTreeDistanceMeasurerByPsi(
    project: Project = createTempProject(),
    private val psiManager: PsiManager = PsiManager.getInstance(project),
) : GumTreeDistanceMeasurerBase() {
    private val codeToTreeContext: HashMap<String, TreeContext> = HashMap()
    override fun String.parseTree(context: SubmissionsGraphContext<*>): TreeContext {
        val trimmedCode = this.trimCode()
        return codeToTreeContext[trimmedCode] ?: run {
            val psi =
                context.codeToUnifiedPsi.getOrDefault(trimmedCode, this.asPsiFile(Language.PYTHON, psiManager) { it })
            getTreeContext(psi)
        }
    }
}
