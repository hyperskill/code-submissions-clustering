package org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.actions.model.Move
import com.github.gumtreediff.actions.model.TreeDelete
import com.github.gumtreediff.actions.model.TreeInsert
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.load.unifiers.createTempProject
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.util.asPsiFile
import org.jetbrains.research.code.submissions.clustering.util.trimCode

abstract class GumTreeDistanceMeasurerBase : CodeDistanceMeasurerBase<List<Action>>() {
    private fun Tree.toMapKey() = this.toString()

    @Suppress("NestedBlockDepth", "NO_BRACES_IN_CONDITIONALS_AND_LOOPS")
    override fun List<Action>.calculateWeight(): Int {
        // Sometimes we have actions about adding and deleting almost the same subtrees,
        // in these cases we calculate only different parts of the subtries
        val insertedNodeToFreq = mutableMapOf<String, Int>()
        var weight = 0
        for (action in this) {
            when (action) {
                is Move -> weight += 1
                is TreeInsert -> {
                    action.node.preOrder().forEach {
                        val node = it.toMapKey()
                        insertedNodeToFreq.putIfAbsent(node, 0)
                        insertedNodeToFreq[node] = insertedNodeToFreq[node]!! + 1
                    }
                }
                is TreeDelete -> {
                    action.node.preOrder().forEach {
                        val node = it.toMapKey()
                        if (node in insertedNodeToFreq.keys) {
                            insertedNodeToFreq[node] = insertedNodeToFreq[node]!! - 1
                        }
                    }
                }
                else -> weight += action.node.metrics.size
            }
        }
        weight += insertedNodeToFreq.values.filter { it > 0 }.sum()
        return weight
    }

    abstract fun String.parseTree(): TreeContext

    private fun calculateDistance(source: TreeContext, target: TreeContext) = Matcher(source, target).getEditActions()

    final override fun computeFullDistance(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
    ): List<Action> {
        val source = graph.getEdgeSource(edge).code.parseTree()
        val target = graph.getEdgeTarget(edge).code.parseTree()
        return calculateDistance(source, target)
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
    project: Project = createTempProject(),
    private val psiManager: PsiManager = PsiManager.getInstance(project),
) : GumTreeDistanceMeasurerBase() {
    private val codeToTreeContext: HashMap<String, TreeContext> = HashMap()
    override fun String.parseTree(): TreeContext {
        val trimmedCode = this.trimCode()
        return codeToTreeContext.getOrPut(trimmedCode) {
            this.asPsiFile(
                Language.PYTHON,
                psiManager
            ) { getTreeContext(it) }
        }
    }
}
