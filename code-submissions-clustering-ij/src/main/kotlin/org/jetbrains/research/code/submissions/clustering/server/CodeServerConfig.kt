package org.jetbrains.research.code.submissions.clustering.server

import kotlinx.serialization.Serializable

@Serializable
data class CodeServerOrchestratorConfig(
    val servers: List<CodeServerConfig>
)

@Serializable
data class CodeServerConfig(
    val port: Int,
    val language: String,
)
