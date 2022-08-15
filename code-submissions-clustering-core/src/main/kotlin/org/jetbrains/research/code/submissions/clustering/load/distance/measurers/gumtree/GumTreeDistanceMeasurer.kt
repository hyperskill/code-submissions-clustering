package org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge

class GumTreeDistanceMeasurer(
    private val treeGenerator: TreeGenerator,
) : CodeDistanceMeasurerBase<List<Action>>() {
    override fun List<Action>.calculateWeight() = this.size

    private fun String.parseTree(treeGenerator: TreeGenerator): TreeContext =
        treeGenerator.generateFromString(this) ?: error("Can not parse code: $this")

    override fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias): List<Action> {
        val source = graph.getEdgeSource(edge).code.parseTree(treeGenerator)
        val target = graph.getEdgeTarget(edge).code.parseTree(treeGenerator)
        val matcher = Matcher(source, target)
        return matcher.getEditActions()
    }
}
