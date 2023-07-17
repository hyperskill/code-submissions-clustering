package org.jetbrains.research.code.submissions.clustering.buildutils

import com.google.protobuf.gradle.*
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
        plugins {
            id("grpc") {
                artifact = makeOSSpecificDependency("io.grpc:protoc-gen-grpc-java:1.31.1")
            }
            id("grpckt") {
                artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
            }
        }
        generateProtoTasks {
            all().forEach {
                it.plugins {
                    id("grpc")
                    id("grpckt")
                }
            }
        }
    }
}