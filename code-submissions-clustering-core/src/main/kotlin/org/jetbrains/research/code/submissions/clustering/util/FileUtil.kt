package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString

fun File.deleteFromProject() {
    ApplicationManager.getApplication().runWriteAction {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(this)
        virtualFile?.delete(null)
        delete()
    }
}

fun createFolder(path: Path) {
    val file = path.toFile()
    if (file.exists() && file.isFile) {
        file.delete()
    }
    if (!file.exists()) {
        file.mkdirs()
    }
}

fun getTmpDirPath(): String = System.getProperty("java.io.tmpdir")

fun getTmpProjectDir(toCreateFolder: Boolean = true): String {
    val folderName = "codeSubmissionsClusteringTmp"
    val path = Path(getTmpDirPath()) / folderName
    if (toCreateFolder) {
        createFolder(path)
    }
    return path.pathString
}

fun addFileToProject(
    projectPath: String,
    fileName: String,
    fileContent: String = ""
): File {
    val filePath = Path(projectPath) / fileName
    val file = filePath.toFile()
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(fileContent)
    return file
}
