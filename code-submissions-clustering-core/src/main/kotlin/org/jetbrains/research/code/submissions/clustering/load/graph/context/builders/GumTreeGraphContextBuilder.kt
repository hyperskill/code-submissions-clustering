package org.jetbrains.research.code.submissions.clustering.load.graph.context.builders

import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.code.submissions.clustering.load.AbstractGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.GumTreeDistanceMeasurer
import org.jetbrains.research.code.submissions.clustering.model.Language

object GumTreeGraphContextBuilder : AbstractGraphContextBuilder() {
    private val treeGeneratorByLanguage = mapOf<Language, () -> TreeGenerator>(
        Language.PYTHON to { PythonTreeGenerator() },
    )
    override fun getContext(language: Language): SubmissionsGraphContext = SubmissionsGraphContext(
        unifierByLanguage.getValue(language)(),
        GumTreeDistanceMeasurer(treeGeneratorByLanguage.getValue(language)())
    )
}
