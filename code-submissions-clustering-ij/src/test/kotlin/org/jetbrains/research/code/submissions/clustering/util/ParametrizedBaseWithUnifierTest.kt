package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionInfo
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
import org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.distance.calculateDistances
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.junit.Ignore

@Ignore
open class ParametrizedBaseWithUnifierTest(testDataRoot: String) : ParametrizedBaseWithPythonSdkTest(testDataRoot) {

    override fun setUp() {
        super.setUp()
        mockProject ?: run {
            mockProject = project
            mockPsiManager = psiManager
            mockFixture = myFixture
            psiFileFactory = PsiFileFactory(Language.PYTHON, psiManager)
            mockContext = SubmissionsGraphContext(
                PyUnifier(psiFileFactory, mockProject!!),
                GumTreeDistanceMeasurerByPsi(psiFileFactory)
            )
        }
    }

    private fun List<ProtoSubmissionsNode>.sortedNodes() = this.sortedBy { it.code }

    private fun List<ProtoSubmissionInfo>.sortedInfo() = this.sortedBy { it.id }

    private fun List<ProtoSubmissionsEdge>.sortedEdges() = this.sortedWith(compareBy({ it.from.code }, { it.to.code }))

    private fun ProtoSubmissionsNode.equalsNode(other: ProtoSubmissionsNode) =
        (this.stepId == other.stepId) and (this.code == other.code) and (this.infoList.sortedInfo() == other.infoList.sortedInfo())

    private fun List<ProtoSubmissionsNode>.assertEqualsNodes(other: List<ProtoSubmissionsNode>) {
        assert(this.size == other.size)
        this.sortedNodes().zip(other.sortedNodes()).forEach { (first, second) -> assert(first.equalsNode(second)) }
    }

    private fun List<ProtoSubmissionsEdge>.assertEqualsEdges(other: List<ProtoSubmissionsEdge>) {
        assert(this.size == other.size)
        this.sortedEdges().zip(other.sortedEdges()).forEach { (first, second) ->
            assert(first.weight == second.weight)
            assert(
                ((first.from.equalsNode(second.from)) and (first.to.equalsNode(second.to)))
                        or ((first.from.equalsNode(second.to)) and (first.to.equalsNode(second.from)))
            )
        }
    }

    protected fun ProtoSubmissionsGraph.assertEquals(other: ProtoSubmissionsGraph) {
        this.verticesList.assertEqualsNodes(other.verticesList)
        this.edgesList.assertEqualsEdges(other.edgesList)
    }

    protected fun Clustering<SubmissionsNode>.assertEquals(other: Clustering<SubmissionsNode>) {
        assertEquals(this.clusters.sortedBy { it.first() }, other.clusters.sortedBy { it.first() })
    }

    protected fun DataFrame<*>.loadGraph() = runBlocking {
        loadGraph(mockContext)
    }

    protected fun SubmissionsGraph.calculateDistances() = runBlocking {
        calculateDistances(mockContext)
    }

    companion object {
        var mockProject: Project? = null
        var mockPsiManager: PsiManager? = null
        var mockFixture: CodeInsightTestFixture? = null
        lateinit var psiFileFactory: PsiFileFactory
        lateinit var mockContext: SubmissionsGraphContext<out Any>

        fun clearFactory() {
            psiFileFactory.clearFactory()
        }
    }
}
