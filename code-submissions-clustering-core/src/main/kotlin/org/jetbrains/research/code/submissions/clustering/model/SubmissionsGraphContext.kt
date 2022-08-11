package org.jetbrains.research.code.submissions.clustering.model

import com.github.gumtreediff.gen.TreeGenerator
import com.github.gumtreediff.gen.python.PythonTreeGenerator
import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.util.Language

/**
 * @property language code submissions' language
 */
data class SubmissionsGraphContext(val language: Language) {
    val unifier = unifierByLanguage[language]!!
    val treeGenerator = treeGeneratorByLanguage[language]!!

    companion object {
        private val unifierByLanguage = mapOf<Language, AbstractUnifier>(
            Language.PYTHON to PyUnifier(),
        )
        private val treeGeneratorByLanguage = mapOf<Language, TreeGenerator>(
            Language.PYTHON to PythonTreeGenerator(),
        )
    }
}
