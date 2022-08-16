package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

fun File.deleteFromProject() {
    ApplicationManager.getApplication().runWriteAction {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(this)
        virtualFile?.delete(null)
        delete()
    }
}

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
    val folderName = "codeSubmissionsClusteringTmp"
    val path = "${getTmpDirPath()}/$folderName"
    if (toCreateFolder) {
        createFolder(path)
    }
    return path
}

fun addFileToProject(
    projectPath: String,
    fileName: String,
    fileContent: String = ""
): File {
    val filePath = "$projectPath/$fileName"
    val file = File(filePath)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(fileContent)
    return file
}

fun deleteTmpProjectFiles(projectPath: String) {
    File(projectPath).walkBottomUp().forEach {
        it.deleteFromProject()
    }
}
