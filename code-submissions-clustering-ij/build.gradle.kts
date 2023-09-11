import org.jetbrains.intellij.tasks.RunIdeTask
import java.nio.file.Paths

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":code-submissions-clustering-core"))
    implementation(libs.ast.transformations.core)
    implementation(libs.kotlin.argparser)
    implementation(libs.zip4j)
    implementation(libs.gumtreediff.core)
    implementation(libs.gumtreediff.gen.python)

    // Need for tests
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.platform.console)
}

abstract class BaseCLITask : RunIdeTask() {
    @get:Input
    abstract val taskName: Property<String>

    init {
        jvmArgs = listOf(
            "-Dgt.pp.path=${Paths.get(project.parent!!.projectDir.toString(), "libs", "pythonparser")}",
            "-Djava.awt.headless=true",
            "--add-exports",
            "java.base/jdk.internal.vm=ALL-UNNAMED",
            "-Xmx5048m",
            "-Xms256m",
        )
        standardInput = System.`in`
        standardOutput = System.`out`
    }

    fun setLogsDir(logsDir: String) {
        jvmArgs = mutableListOf<String>().apply {
            add("-Dlogs_dir=$logsDir")
            jvmArgs?.let { addAll(it) }
        }
    }

    fun setArgs(block: MutableList<String>.() -> Unit = {}) {
        args = mutableListOf<String>().apply {
            add(taskName.get())
            block()
        }
    }
}

tasks {
    test {
        jvmArgs = listOf(
            "-Dgt.pp.path=${Paths.get(project.parent!!.projectDir.toString(), "libs", "pythonparser")}"
        )
    }

    val serverTaskName = "ij-code-server"
    register(serverTaskName, BaseCLITask::class) {
        taskName.set(serverTaskName)
        dependsOn(build)
        val port: String? by project
        val language: String? by project
        val logsDir: String? by project
        setLogsDir(logsDir ?: project.parent!!.projectDir.toString())
        setArgs {
            port?.let { add("--port=$it") }
            language?.let { add("--language=$it") }
        }
    }
}
