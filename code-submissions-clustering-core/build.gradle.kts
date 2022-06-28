group = rootProject.group
version = rootProject.version

dependencies {
    implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
        version {
            branch = "master"
        }
    }
}