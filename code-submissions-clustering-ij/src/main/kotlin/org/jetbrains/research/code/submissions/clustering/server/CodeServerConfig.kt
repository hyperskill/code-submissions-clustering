package org.jetbrains.research.code.submissions.clustering.server

import kotlinx.serialization.Serializable

/**
 * @property servers IJ code server configs
 */
@Serializable
data class CodeServerOrchestratorConfig(
    val servers: List<CodeServerConfig>
)

/**
 * @property port port
 * @property language programming language of code submissions
 */
@Serializable
data class CodeServerConfig(
    val port: Int,
    val language: String,
)
