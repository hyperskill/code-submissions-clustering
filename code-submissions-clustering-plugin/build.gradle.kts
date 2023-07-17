import org.jetbrains.intellij.tasks.RunIdeTask
import java.nio.file.Paths

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":code-submissions-clustering-core"))
    implementation(project(":code-submissions-clustering-ij"))
    // Need for CLI
    implementation(libs.kotlin.argparser)
    implementation(libs.dataframe)
}

abstract class BaseCLITask : RunIdeTask() {
    @get:Input
    abstract val taskName: Property<String>

    private val inputFile: String? by project
    private val language: String? by project
    private val outputDir: String? by project
    private val binaryInput: String? by project

    init {
        jvmArgs = listOf(
            "-Dgt.pp.path=${Paths.get(project.parent!!.projectDir.toString(), "libs", "pythonparser")}",
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Xmx5048m",
            "-Xms5048m",
        )
        standardInput = System.`in`
        standardOutput = System.`out`
    }

    fun setArgs(block: MutableList<String>.() -> Unit = {}) {
        args = mutableListOf<String>().apply {
            add(taskName.get())
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
            block()
        }
    }
}

tasks {
    val loadTaskName = "load"
    register<BaseCLITask>(loadTaskName) {
        taskName.set(loadTaskName)
        setArgs()
        dependsOn(build)
    }

    val calculateDistTaskName = "calculate-dist"
    register<BaseCLITask>(calculateDistTaskName) {
        taskName.set(calculateDistTaskName)
        setArgs()
        dependsOn(build)
    }

    val clusterTaskName = "cluster"
    register<BaseCLITask>(clusterTaskName) {
        val distanceLimit: String? by project
        taskName.set(clusterTaskName)
        setArgs {
            distanceLimit?.let {
                add("--distanceLimit=$it")
            }
        }
        dependsOn(build)
    }
}
