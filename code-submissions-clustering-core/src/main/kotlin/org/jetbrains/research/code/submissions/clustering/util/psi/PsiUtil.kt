package org.jetbrains.research.code.submissions.clustering.util.psi

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.PsiToGumTreeConverter.filterWhiteSpaces

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

// TODO: add other symbols?
fun String.trimCode() = this.trim(' ', '\n', '\r')
