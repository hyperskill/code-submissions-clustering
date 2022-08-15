package org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree.GumTreeDistanceMeasurer
import org.jetbrains.research.code.submissions.clustering.load.context.builder.AbstractGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.model.Language

typealias GumTreeGraphContext = SubmissionsGraphContext<List<Action>>

object GumTreeGraphContextBuilder : AbstractGraphContextBuilder<List<Action>>() {
    private val treeGeneratorByLanguage = mapOf<Language, () -> TreeGenerator>(
        Language.PYTHON to { getPythonTreeGenerator() },
    )

    fun getPythonTreeGenerator(): PythonTreeGenerator {
        GumTreeParserUtil.checkSetup()
        return PythonTreeGenerator()
    }

    override fun getContext(language: Language): GumTreeGraphContext = SubmissionsGraphContext(
        unifierByLanguage.getValue(language)(),
        GumTreeDistanceMeasurer(treeGeneratorByLanguage.getValue(language)())
    )
}
