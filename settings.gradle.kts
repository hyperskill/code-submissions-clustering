import java.net.URI

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

rootProject.name = "code-submissions-clustering"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/ast-transformations.git")) {
        producesModule("org.jetbrains.research.ml.ast.transformations:ast-transformations-core")
    }
}

include(
    "code-submissions-clustering-core",
    "code-submissions-clustering-plugin",
    "code-submissions-clustering-ij"
)
