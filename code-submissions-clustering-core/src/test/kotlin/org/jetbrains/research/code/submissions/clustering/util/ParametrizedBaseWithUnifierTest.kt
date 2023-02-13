package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.psi.PsiFileFactory
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

    private fun List<ProtoSubmissionsNode>.sortedNodes() = this.sortedBy { it.id }

    private fun List<ProtoSubmissionsEdge>.sortedEdges() = this.sortedWith(compareBy({ it.from.id }, { it.to.id }))

    protected fun ProtoSubmissionsGraph.assertEquals(other: ProtoSubmissionsGraph) {
        assertEquals(this.verticesList.sortedNodes(), other.verticesList.sortedNodes())
        assertEquals(this.edgesList.sortedEdges(), other.edgesList.sortedEdges())
    }

    protected fun Clustering<SubmissionsNode>.assertEquals(other: Clustering<SubmissionsNode>) {
        assertEquals(this.clusters.sortedBy { it.first() }, other.clusters.sortedBy { it.first() })
    }

    companion object {
        var mockProject: Project? = null
        var mockPsiManager: PsiManager? = null
        var mockFixture: CodeInsightTestFixture? = null
        lateinit var psiFileFactory: PsiFileFactory
        lateinit var mockContext: SubmissionsGraphContext<out Any>
    }
}
