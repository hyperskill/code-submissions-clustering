package org.jetbrains.research.code.submissions.clustering.util

import java.io.File

fun createFolder(path: String) {
    val file = File(path)
    if (file.exists() && file.isFile) {
        file.delete()
    }
    if (!file.exists()) {
        file.mkdirs()
    }
}

fun getTmpDirPath() = System.getProperty("java.io.tmpdir").removeSuffix("/")

fun getTmpProjectDir(toCreateFolder: Boolean = true): String {
    val path = "${getTmpDirPath()}/codeSubmissionsClusteringTmp"
    if (toCreateFolder) {
        createFolder(path)
    }
    return path
}

fun addFileToProject(
    projectPath: String,
    fileName: String,
    fileContext: String = ""
): File {
    val filePath = "$projectPath/$fileName"
    val file = File(filePath)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(fileContext)
    return file
}
