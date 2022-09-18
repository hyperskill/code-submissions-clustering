import org.jetbrains.intellij.tasks.RunIdeTask
import java.nio.file.Paths

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
        jvmArgs = listOf(
            "-Dgt.pp.path=${Paths.get(project.parent!!.projectDir.toString(), "libs", "pythonparser")}",
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED"
        )
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register("load", BaseCLITask::class) {
        dependsOn(build)
        val input: String? by project
        val lang: String? by project
        val output: String? by project
        args = mutableListOf<String>().apply {
            add("load")
            input?.let { add("--input_file=$it") }
            lang?.let { add("--language=$it") }
            output?.let { add("--output_path=$it") }
            if (project.hasProperty("serialize")) {
                add("--serialize")
            }
            if (project.hasProperty("saveCSV")) {
                add("--saveCSV")
            }
            if (project.hasProperty("visualize")) {
                add("--visualize")
            }
            if (project.hasProperty("saveClusters")) {
                add("--saveClusters")
            }
        }
    }
    register("calculate-dist", BaseCLITask::class) {
        dependsOn(build)
        val input: String? by project
        val lang: String? by project
        val output: String? by project
        args = mutableListOf<String>().apply {
            add("calculate-dist")
            input?.let { add("--input_file=$it") }
            lang?.let { add("--language=$it") }
            output?.let { add("--output_path=$it") }
            if (project.hasProperty("serialize")) {
                add("--serialize")
            }
            if (project.hasProperty("saveCSV")) {
                add("--saveCSV")
            }
            if (project.hasProperty("visualize")) {
                add("--visualize")
            }
            if (project.hasProperty("saveClusters")) {
                add("--saveClusters")
            }
        }
    }
}
