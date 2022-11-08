@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY")

package org.jetbrains.research.code.submissions.clustering.cli.models

import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph

data class Writer(
    val writer: SubmissionsGraph.(String) -> Unit,
    val toWrite: Boolean = false,
)
