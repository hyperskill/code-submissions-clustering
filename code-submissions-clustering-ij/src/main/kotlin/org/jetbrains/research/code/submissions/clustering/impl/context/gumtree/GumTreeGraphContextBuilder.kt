package org.jetbrains.research.code.submissions.clustering.impl.context.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.TransformationsConfig
import org.jetbrains.research.code.submissions.clustering.impl.util.getProjectBuilder
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.PsiFileFactory
import org.jetbrains.research.code.submissions.clustering.load.context.GraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.model.Language

typealias GumTreeGraphContext = SubmissionsGraphContext<List<Action>>

class GumTreeGraphContextBuilder : GraphContextBuilder<List<Action>> {
    @Suppress("UnusedPrivateMember")
    private val treeGeneratorByLanguage = mapOf<Language, () -> TreeGenerator>(
        Language.PYTHON to { getPythonTreeGenerator() },
    )
    private lateinit var language: Language
    private lateinit var transformationsConfig: TransformationsConfig

    fun setLanguage(language: Language): GumTreeGraphContextBuilder {
        this.language = language
        return this
    }

    fun configureTransformations(transformationsConfig: TransformationsConfig): GumTreeGraphContextBuilder {
        this.transformationsConfig = transformationsConfig
        return this
    }

    private fun getUnifier(psiFileFactory: PsiFileFactory, project: Project): AbstractUnifier = when (language) {
        Language.PYTHON -> PyUnifier(psiFileFactory, project, transformationsConfig)
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
