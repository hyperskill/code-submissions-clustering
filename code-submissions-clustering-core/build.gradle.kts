group = rootProject.group
version = rootProject.version

dependencies {
    implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
        version {
            branch = "master"
        }
    }
    implementation("org.jgrapht:jgrapht-core:1.5.1")
}