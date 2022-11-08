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
        val inputFile: String? by project
        val language: String? by project
        val outputDir: String? by project
        val binaryInput: String? by project
        args = mutableListOf<String>().apply {
            add("load")
            inputFile?.let { add("--inputFile=$it") }
            language?.let { add("--language=$it") }
            outputDir?.let { add("--outputDir=$it") }
            binaryInput?.let { add("--binaryInput=$it") }
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
            if (project.hasProperty("clusteringResult")) {
                add("--clusteringResult")
            }
        }
    }
    register("calculate-dist", BaseCLITask::class) {
        dependsOn(build)
        val inputFile: String? by project
        val language: String? by project
        val outputDir: String? by project
        val binaryInput: String? by project
        args = mutableListOf<String>().apply {
            add("calculate-dist")
            inputFile?.let { add("--inputFile=$it") }
            language?.let { add("--language=$it") }
            outputDir?.let { add("--outputDir=$it") }
            binaryInput?.let { add("--binaryInput=$it") }
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
            if (project.hasProperty("clusteringResult")) {
                add("--clusteringResult")
            }
        }
    }
    register("cluster", BaseCLITask::class) {
        dependsOn(build)
        val inputFile: String? by project
        val language: String? by project
        val distanceLimit: String? by project
        val outputDir: String? by project
        val binaryInput: String? by project
        args = mutableListOf<String>().apply {
            add("cluster")
            inputFile?.let { add("--inputFile=$it") }
            language?.let { add("--language=$it") }
            distanceLimit?.let { add("--distanceLimit=$it") }
            outputDir?.let { add("--outputDir=$it") }
            binaryInput?.let { add("--binaryInput=$it") }
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
            if (project.hasProperty("clusteringResult")) {
                add("--clusteringResult")
            }
        }
    }
}
