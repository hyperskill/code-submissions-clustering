package org.jetbrains.research.code.submissions.clustering.util

import com.jetbrains.python.PythonFileType

/**
 * Enum class for possible code submissions' languages.
 * @property extension corresponding file extension
 */
enum class Language(val extension: String) {
    PYTHON(PythonFileType.INSTANCE.defaultExtension),
    ;
}
