group = rootProject.group
version = rootProject.version

dependencies {
    implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
        version {
            branch = "master"
        }
    }
    implementation(libs.dataframe)
    implementation(libs.jgrapht.core)
    implementation(libs.gumtreediff.core)
    implementation(libs.gumtreediff.gen.python)
    // Need for tests
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.platform.console)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
