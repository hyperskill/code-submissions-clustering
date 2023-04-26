package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.ml.ast.util.sdk.setSdkToProject

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
            setSdkToProject(it, getTmpProjectDir())
        }
    }
}

fun getProjectBuilder(language: Language): ProjectBuilder = when (language) {
    Language.PYTHON -> PyProjectBuilder()
}

private fun createTempProject(): Project = ProjectUtil.openOrImport(getTmpProjectDir(), null, true)
    ?: error("Internal error: the temp project was not created")
