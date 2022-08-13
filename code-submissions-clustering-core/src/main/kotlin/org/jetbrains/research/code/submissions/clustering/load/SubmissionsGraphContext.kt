package org.jetbrains.research.code.submissions.clustering.load

import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.GumTreeParserUtil

/**
 * @property unifier unifier to use while operating submissions graph
 * @property treeGenerator tree generator to use while operating submissions graph
 */
data class SubmissionsGraphContext(
    val unifier: AbstractUnifier,
    val treeGenerator: TreeGenerator
)

object SubmissionsGraphContextBuilder {
    private val unifierByLanguage = mapOf<Language, () -> AbstractUnifier>(
        Language.PYTHON to { PyUnifier() },
    )
    private val treeGeneratorByLanguage = mapOf(
        Language.PYTHON to { getPythonTreeGenerator() },
    )

    fun getPythonTreeGenerator(): TreeGenerator {
        GumTreeParserUtil.checkSetup()
        return PythonTreeGenerator()
    }

    fun getContext(language: Language) =
        SubmissionsGraphContext(
            unifierByLanguage.getValue(language)(),
            treeGeneratorByLanguage.getValue(language)()
        )
}
