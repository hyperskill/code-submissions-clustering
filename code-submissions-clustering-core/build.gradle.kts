import java.nio.file.Paths

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
    implementation(libs.zip4j)

    implementation("it.unimi.dsi:fastutil-core:8.5.11")
    implementation("com.google.guava:guava:31.1-jre")

    // Need for tests
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.platform.console)
}

tasks {
    test {
//        useJUnitPlatform()
        jvmArgs = listOf(
            "-Dgt.pp.path=${Paths.get(project.parent!!.projectDir.toString(), "libs", "pythonparser")}"
        )
    }
}
