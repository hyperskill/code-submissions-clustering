package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree.GumTreeDistanceMeasurer
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.junit.Ignore
import org.junit.jupiter.api.AfterEach
import java.awt.EventQueue

@Ignore
open class ParametrizedBaseWithUnifierTest(testDataRoot: String) : ParametrizedBaseWithPythonSdkTest(testDataRoot) {
    init {
        mockProject ?: run {
            EventQueue.invokeAndWait {
                super.setUp()
            }
            mockProject = project
            mockPsiManager = psiManager
            mockContext = SubmissionsGraphContext(
                PyUnifier(mockProject!!, mockPsiManager!!, toSetSdk = false),
                GumTreeDistanceMeasurer(GumTreeGraphContextBuilder.getPythonTreeGenerator())
            )
        }
    }

    @AfterEach
    override fun tearDown() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            deleteTmpProjectFiles(getTmpProjectDir(toCreateFolder = false))
        }
    }

    protected fun ProtoSubmissionsGraph.assertEquals(other: ProtoSubmissionsGraph) {
        assertEquals(this.verticesList.sortedBy { it.id }, other.verticesList.sortedBy { it.id })
        // TODO: sort by id pairs (from.id, to.id)
        assertEquals(this.edgesList.sortedBy { it.from.id }, other.edgesList.sortedBy { it.from.id })
    }

    companion object {
        var mockProject: Project? = null
        var mockPsiManager: PsiManager? = null
        lateinit var mockContext: SubmissionsGraphContext<out Any>
    }
}
