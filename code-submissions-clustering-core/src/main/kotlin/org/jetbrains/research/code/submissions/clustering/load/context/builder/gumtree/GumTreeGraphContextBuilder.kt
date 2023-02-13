package org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.GraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.load.unifiers.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.getProjectBuilder
import org.jetbrains.research.code.submissions.clustering.util.psi.PsiFileFactory

typealias GumTreeGraphContext = SubmissionsGraphContext<List<Action>>

class GumTreeGraphContextBuilder : GraphContextBuilder<List<Action>> {
    @Suppress("UnusedPrivateMember")
    private val treeGeneratorByLanguage = mapOf<Language, () -> TreeGenerator>(
        Language.PYTHON to { getPythonTreeGenerator() },
    )
    private lateinit var language: Language

    fun setLanguage(language: Language): GumTreeGraphContextBuilder {
        this.language = language
        return this
    }

    private fun getUnifier(psiFileFactory: PsiFileFactory, project: Project): AbstractUnifier = when (language) {
        Language.PYTHON -> PyUnifier(psiFileFactory, project)
    }

    private fun getPythonTreeGenerator(): PythonTreeGenerator {
        GumTreeParserUtil.checkSetup()
        return PythonTreeGenerator()
    }

    override fun buildContext(): GumTreeGraphContext {
        val project = getProjectBuilder(language).buildProject()
        val psiManager = PsiManager.getInstance(project)
        val psiFileFactory = PsiFileFactory(language, psiManager)

        return SubmissionsGraphContext(
            getUnifier(psiFileFactory, project),
            GumTreeDistanceMeasurerByPsi(psiFileFactory),
        )
    }
}
