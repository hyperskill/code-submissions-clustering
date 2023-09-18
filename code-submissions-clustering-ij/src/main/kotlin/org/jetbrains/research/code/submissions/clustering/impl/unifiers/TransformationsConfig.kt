package org.jetbrains.research.code.submissions.clustering.impl.unifiers

import kotlinx.serialization.Serializable

/**
 * @property repeatingTransformations list of repeating transformations
 * @property singleTransformations list of single run transformations
 */
@Serializable
data class TransformationsConfig(
    val repeatingTransformations: List<String>,
    val singleTransformations: List<String>,
)
