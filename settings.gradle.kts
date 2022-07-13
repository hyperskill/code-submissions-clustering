import java.net.URI

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

include("code-submissions-clustering-core")
include("code-submissions-clustering-plugin")
