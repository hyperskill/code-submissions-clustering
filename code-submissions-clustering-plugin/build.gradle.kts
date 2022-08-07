import org.jetbrains.intellij.tasks.RunIdeTask

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":code-submissions-clustering-core"))
    // Need for CLI
    implementation(libs.kotlin.argparser)
    implementation(libs.dataframe)
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
        args = mutableListOf<String>().apply {
            add("load")
            input?.let { add("--input_file=$it") }
            output?.let { add("--output_path=$it") }
            if (project.hasProperty("serialize")) {
                add("--serialize")
            }
            if (project.hasProperty("saveCSV")) {
                add("--saveCSV")
            }
        }
    }
}
