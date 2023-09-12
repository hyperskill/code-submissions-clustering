package org.jetbrains.research.code.submissions.clustering.impl.util

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject
import kotlin.system.exitProcess

sealed interface ProjectBuilder {
    fun buildProject(): Project
}

class PyProjectBuilder : ProjectBuilder {
    private var toSetSdk: Boolean = true
    fun withSdk(set: Boolean): PyProjectBuilder {
        toSetSdk = set
        return this
    }

    override fun buildProject(): Project = createTempProject().also {
        if (toSetSdk) {
            ApplicationManager.getApplication().invokeLater({
                ApplicationManager.getApplication().runWriteAction {
                    if (!ApplicationManager.getApplication().isDispatchThread) {
                        exitProcess(1)
                    }
                    setSdkToProject(it, getTmpProjectDir())
                }
            }, ModalityState.NON_MODAL)
        }
    }
}

fun getProjectBuilder(language: Language): ProjectBuilder = when (language) {
    Language.PYTHON -> PyProjectBuilder()
}

private fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
