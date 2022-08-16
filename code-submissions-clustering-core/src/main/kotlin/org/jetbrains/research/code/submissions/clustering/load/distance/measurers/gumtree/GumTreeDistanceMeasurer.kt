package org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.load.unifiers.createTempProject
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.util.createPsiFile

abstract class GumTreeDistanceMeasurerBase : CodeDistanceMeasurerBase<List<Action>>() {
    override fun List<Action>.calculateWeight() = this.size

    abstract fun String.parseTree(): TreeContext

    private fun calculateDistance(source: TreeContext, target: TreeContext) = Matcher(source, target).getEditActions()

    final override fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias): List<Action> {
        val source = graph.getEdgeSource(edge).code.parseTree()
        val target = graph.getEdgeTarget(edge).code.parseTree()
        return calculateDistance(source, target)
    }
}

class GumTreeDistanceMeasurerByParser(
    private val treeGenerator: TreeGenerator,
) : GumTreeDistanceMeasurerBase() {
    override fun List<Action>.calculateWeight() = this.size

    @Suppress("WRONG_OVERLOADING_FUNCTION_ARGUMENTS")
    override fun String.parseTree() = this.parseTree(treeGenerator)

    private fun String.parseTree(treeGenerator: TreeGenerator) =
        treeGenerator.generateFrom().string(this) ?: error("Can not parse code: $this")
}

class GumTreeDistanceMeasurerByPsi(
    project: Project = createTempProject(),
    private val psiManager: PsiManager = PsiManager.getInstance(project),
) : GumTreeDistanceMeasurerBase() {
    // TODO: fix it. We have to be sure that we use unique ids for all dummy files
    @Suppress("MagicNumber", "MAGIC_NUMBER")
    private val idFactory = IdentifierFactoryImpl(2000)
    override fun String.parseTree() =
        getTreeContext(this.createPsiFile(idFactory.uniqueIdentifier(), Language.PYTHON, psiManager))
}
