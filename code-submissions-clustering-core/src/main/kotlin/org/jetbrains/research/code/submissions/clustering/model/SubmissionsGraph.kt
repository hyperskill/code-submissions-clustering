package org.jetbrains.research.code.submissions.clustering.model

import com.github.gumtreediff.actions.EditScript
import com.github.gumtreediff.actions.EditScriptGenerator
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.matchers.MappingStore
import com.github.gumtreediff.matchers.Matcher
import com.github.gumtreediff.matchers.Matchers
import com.github.gumtreediff.tree.Tree
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import org.jetbrains.research.code.submissions.clustering.util.Language
import org.jetbrains.research.code.submissions.clustering.util.toProto

/**
 * @property graph inner representation of submissions graph
 */
data class SubmissionsGraph(val graph: Graph<SubmissionsNode, DefaultWeightedEdge>) {
    fun buildStringRepresentation() = toProto().toString()
}

class GraphBuilder(private val submissionsGraphContext: SubmissionsGraphContext) {
    private val graph: Graph<SubmissionsNode, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)
    private val vertexByCode = HashMap<String, SubmissionsNode>()

    fun add(submission: Submission) {
        submissionsGraphContext.unifier.run {
            val unifiedSubmission = submission.unify()
            vertexByCode.compute(unifiedSubmission.code) { _, vertex ->
                vertex?.let {
                    // Update existing vertex
                    vertex.idList.add(unifiedSubmission.id)
                    vertex
                } ?:  // Add new vertex with single id
                SubmissionsNode(unifiedSubmission).also {
                    graph.addVertex(it)
                }
            }
        }
    }

    fun makeComplete() {
        vertexByCode.forEach { (code, vertex) ->
            vertexByCode.forEach innerLoop@ { (otherCode, otherVertex) ->
                if (code == otherCode) {
                    return@innerLoop
                }
                val edge: DefaultWeightedEdge = graph.addEdge(vertex, otherVertex)
                val dist = computeDistance(code, otherCode, submissionsGraphContext.treeGenerator)
                graph.setEdgeWeight(edge, dist.toDouble())
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)

    private fun computeDistance(code: String, otherCode: String, treeGenerator: TreeGenerator): Int {
        val source: Tree = treeGenerator.generateFrom().string(code).root
        val destination: Tree = treeGenerator.generateFrom().string(otherCode).root
        val defaultMatcher: Matcher = Matchers.getInstance().matcher
        val mappings: MappingStore = defaultMatcher.match(source, destination)
        val editScriptGenerator: EditScriptGenerator = SimplifiedChawatheScriptGenerator()
        val actions: EditScript = editScriptGenerator.computeActions(mappings)
        return actions.size()
    }
}

fun buildGraph(language: Language, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val submissionsGraphContext = SubmissionsGraphContext(language)
    val builder = GraphBuilder(submissionsGraphContext)
    return builder.apply(block).build()
}
