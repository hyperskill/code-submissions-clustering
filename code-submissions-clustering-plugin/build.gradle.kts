group = rootProject.group
version = rootProject.version

plugins {
    application
}

dependencies {
    implementation(project(":code-submissions-clustering-core"))
    implementation(project(":code-submissions-clustering-ij"))
    // Need for CLI
    implementation(libs.kotlin.argparser)
    implementation(libs.dataframe)
    implementation(libs.clikt)
}

application {
    mainClass.set("org.jetbrains.research.code.submissions.clustering.cli.MainKt")
}
