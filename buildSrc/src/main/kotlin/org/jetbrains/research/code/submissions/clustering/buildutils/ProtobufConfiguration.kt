package org.jetbrains.research.code.submissions.clustering.buildutils

import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.apply

fun makeOSSpecificDependency(dependency: String): String = when {
    OperatingSystem.current().isMacOsX -> "$dependency:osx-x86_64"
    OperatingSystem.current().isWindows -> "$dependency:windows-x86_64"
    else -> dependency
}

fun Project.configureProtobuf() {
    apply<ProtobufPlugin>()
    protobuf {
        protoc {
            artifact = makeOSSpecificDependency("com.google.protobuf:protoc:3.19.4")
        }
    }
}