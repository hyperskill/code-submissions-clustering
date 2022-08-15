package org.jetbrains.research.code.submissions.clustering.load.context.builder

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.unifiers.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.model.Language

abstract class AbstractGraphContextBuilder<T> {
    protected val unifierByLanguage = mapOf<Language, () -> AbstractUnifier>(
        Language.PYTHON to { PyUnifier() },
    )
    abstract fun getContext(language: Language): SubmissionsGraphContext<T>
}
