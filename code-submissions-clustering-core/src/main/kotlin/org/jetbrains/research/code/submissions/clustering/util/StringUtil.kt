package org.jetbrains.research.code.submissions.clustering.util

fun String.normalize() = this.replace("\r\n", "\n").replace("\r", "\n")
