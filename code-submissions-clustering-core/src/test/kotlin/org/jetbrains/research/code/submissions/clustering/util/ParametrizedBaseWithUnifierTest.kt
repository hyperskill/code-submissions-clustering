package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
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
            unifier = PyUnifier(mockProject!!, mockPsiManager!!, toSetSdk = false)
        }
    }

    @AfterEach
    override fun tearDown() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            deleteTmpProjectFiles(getTmpProjectDir(toCreateFolder = false))
        }
    }

    companion object {
        var mockProject: Project? = null
        var mockPsiManager: PsiManager? = null
        lateinit var unifier: AbstractUnifier
    }
}
