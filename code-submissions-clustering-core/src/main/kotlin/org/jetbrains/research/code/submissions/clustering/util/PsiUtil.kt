package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.PsiToGumTreeConverter.filterWhiteSpaces
import org.jetbrains.research.code.submissions.clustering.model.Language

val PsiElement.isLeaf: Boolean
    get() = this.children.isEmpty()

fun <T : PsiElement> T.reformatInWriteAction(): T {
    val codeStyleManager = CodeStyleManager.getInstance(project)
    return WriteCommandAction.runWriteCommandAction<T>(project) {
        @Suppress("UNCHECKED_CAST")
        codeStyleManager.reformat(this) as T
    }
}

fun PsiElement.getElementChildren(toIgnoreWhiteSpaces: Boolean): List<PsiElement> = if (toIgnoreWhiteSpaces) {
    this.children.filterWhiteSpaces().toList()
} else {
    this.children.toList()
}

inline fun <reified T> String.asPsiFile(language: Language, psiManager: PsiManager, block: (PsiFile) -> T): T {
    val basePath = getTmpProjectDir(toCreateFolder = false)
    val fileName = "dummy.${language.extension}"
    val file = addFileToProject(basePath, fileName, fileContent = this)
    val psi = ApplicationManager.getApplication().runWriteAction<PsiFile> {
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: throw NoSuchFileException(
            file, reason = "Virtual file cannot be created because file was not found in the local file system"
        )
        psiManager.findFile(virtualFile)
    }
    val result = block(psi)
    file.deleteFromProject()
    return result
}

// TODO: add other symbols?
fun String.trimCode() = this.trim(' ', '\n')