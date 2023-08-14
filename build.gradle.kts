import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.research.code.submissions.clustering.buildutils.*

group = "org.jetbrains.research.code.submissions.clustering"
version = "1.0-SNAPSHOT"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    java
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.intellij)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.serialization)
}

allprojects {
    apply {
        apply {
            plugin("java")
            plugin("kotlin")
            plugin("org.jetbrains.intellij")
            plugin("org.jetbrains.dokka")
            plugin("org.jetbrains.kotlin.plugin.serialization")
        }
    }

    intellij {
        pluginName.set(properties("pluginName"))
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
        plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    repositories {
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        implementation(rootProject.libs.protobuf.java)
        implementation(rootProject.libs.grpc.stub)
        implementation(rootProject.libs.grpc.protobuf)
        implementation(rootProject.libs.grpc.netty)
        implementation(rootProject.libs.grpc.stub.kotlin)
        implementation(rootProject.libs.kotlinx.coroutines.core)
        implementation(rootProject.libs.kotlinx.serialization.json)

        implementation(rootProject.libs.plugin.utilities.core)
        implementation(rootProject.libs.plugin.utilities.test)
        implementation(rootProject.libs.plugin.utilities.python)
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
        }
        // According to this topic:
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010164960-Build-Intellij-plugin-in-IDEA-2019-1-2020-3?page=1#community_comment_360002517940
        withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
            .forEach { it.enabled = false }
    }

    configureProtobuf()
    configureDiktat()
    configureDetekt()
}

createDiktatTask()
createDetektTask()