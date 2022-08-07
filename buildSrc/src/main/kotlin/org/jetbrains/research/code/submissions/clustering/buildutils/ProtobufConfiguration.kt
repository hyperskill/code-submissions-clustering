package org.jetbrains.research.code.submissions.clustering.buildutils

import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

fun Project.configureProtobuf() {
    apply<ProtobufPlugin>()
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:3.0.0"
        }
    }
}