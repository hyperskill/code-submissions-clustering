package org.jetbrains.research.code.submissions.clustering.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager

fun <T : PsiElement> T.reformatInWriteAction(): T {
    val codeStyleManager = CodeStyleManager.getInstance(project)
    return WriteCommandAction.runWriteCommandAction<T>(project) {
        @Suppress("UNCHECKED_CAST")
        codeStyleManager.reformat(this) as T
    }
}
