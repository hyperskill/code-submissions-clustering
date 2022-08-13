package org.jetbrains.research.code.submissions.clustering.load

import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.model.Language

abstract class AbstractGraphContextBuilder {
    protected val unifierByLanguage = mapOf<Language, () -> AbstractUnifier>(
        Language.PYTHON to { PyUnifier() },
    )
    abstract fun getContext(language: Language): SubmissionsGraphContext
}
