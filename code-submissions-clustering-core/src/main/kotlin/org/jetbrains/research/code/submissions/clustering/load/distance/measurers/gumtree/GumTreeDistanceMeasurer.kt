package org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree

import com.github.gumtreediff.actions.model.*
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.util.psi.trimCode

abstract class GumTreeDistanceMeasurerBase : CodeDistanceMeasurerBase<List<Action>>() {
    private fun Tree.toMapKey() = this.toString()

    @Suppress("MagicNumber", "MAGIC_NUMBER")
    private fun Action.toNumber() = when (this) {
        is Move -> 0
        is TreeInsert, is Insert -> 1
        is TreeDelete, is Delete -> 2
        else -> 3
    }

    private fun Tree.calculateWeight() = this.metrics.size

    private fun Action.calculateWeight() = this.node.calculateWeight()

    @Suppress("NestedBlockDepth", "NO_BRACES_IN_CONDITIONALS_AND_LOOPS")
    override fun List<Action>.calculateWeight(): Int {
        // Sometimes we have actions about adding and deleting almost the same subtrees,
        // in these cases we calculate only different parts of the subtrees
        val insertedNodeToState = mutableMapOf<String, State>()
        var weight = 0
        val sortedActions = this.sortedBy { it.toNumber() }
        for (action in sortedActions) {
            when (action) {
                is Move -> weight += 1
                is TreeInsert, is Insert -> {
                    action.node.preOrder().forEach {
                        val node = it.toMapKey()
                        insertedNodeToState.putIfAbsent(node, State.DIFFERENT)
                        weight += 1
                    }
                }
                is TreeDelete, is Delete -> {
                    action.node.preOrder().forEach {
                        val node = it.toMapKey()
                        if (node in insertedNodeToState.keys) {
                            if (insertedNodeToState[node] == State.DIFFERENT) {
                                insertedNodeToState[node] = State.SAME
                            }
                            insertedNodeToState[node]!!.count += 1
                        }
                        weight += 1
                    }
                }
                else -> weight += action.calculateWeight()
            }
        }
        // Don't take into account the same vertices (which were added and then removed)
        weight -= insertedNodeToState.values.filter { it == State.SAME }.fold(0) { acc, x -> acc + x.count }
        return kotlin.math.abs(weight)
    }

    abstract fun String.parseTree(): TreeContext

    fun calculateDistance(source: TreeContext, target: TreeContext) = Matcher(source, target).getEditActions()

    final override fun computeFullDistance(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
    ): List<Action> {
        val source = graph.getEdgeSource(edge).code.parseTree()
        val target = graph.getEdgeTarget(edge).code.parseTree()
        return calculateDistance(source, target)
    }

    // An enum class to indicate if the same ot the different node was changed in a subtree
    @Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY")
    private enum class State(var count: Int) {
        DIFFERENT(0),
        SAME(0),
        ;
    }
}

class GumTreeDistanceMeasurerByParser(
    private val treeGenerator: TreeGenerator,
) : GumTreeDistanceMeasurerBase() {
    @Suppress("WRONG_OVERLOADING_FUNCTION_ARGUMENTS")
    override fun String.parseTree() = this.parseTree(treeGenerator)

    private fun String.parseTree(treeGenerator: TreeGenerator) =
        treeGenerator.generateFrom().string(this) ?: error("Can not parse code: $this")
}

class GumTreeDistanceMeasurerByPsi(
    private val psiFileFactory: PsiFileFactory,
) : GumTreeDistanceMeasurerBase() {
    private val codeToTreeContext: HashMap<String, TreeContext> = HashMap()
    override fun String.parseTree(): TreeContext {
        val trimmedCode = this.trimCode()
        return codeToTreeContext.getOrPut(trimmedCode) {
            val psi = psiFileFactory.getPsiFile(this)
            val treeContext = getTreeContext(psi)
            psiFileFactory.releasePsiFile(psi)
            treeContext
        }
    }
}
