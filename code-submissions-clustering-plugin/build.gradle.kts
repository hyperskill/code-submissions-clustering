import org.jetbrains.intellij.tasks.RunIdeTask

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":code-submissions-clustering-core"))
    // Need for CLI
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlinx:dataframe:0.8.0")
}

open class BaseCLITask : RunIdeTask() {
    init {
        jvmArgs = listOf("-Djava.awt.headless=true", "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register("load", BaseCLITask::class) {
        dependsOn(build)
        val input: String? by project
        val output: String? by project
        args = listOfNotNull(
            "load",
            input?.let { "--input_file=$it" },
            output?.let { "--output_path=$it" }
        )
    }
}
