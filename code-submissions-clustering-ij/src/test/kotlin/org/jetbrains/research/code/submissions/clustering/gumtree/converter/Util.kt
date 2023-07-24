package org.jetbrains.research.code.submissions.clustering.gumtree.converter

import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.converter.label
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.converter.preOrder

fun PsiElement.isEqualTreeStructure(treeCtx: TreeContext): Boolean {
    val psiPreOrder = ApplicationManager.getApplication().runReadAction<List<PsiElement>> {
        this.preOrder().toList()
    }
    val treeCtxPreOrder = treeCtx.root.preOrder().toList()

    if (psiPreOrder.size != treeCtxPreOrder.size) {
        return false
    }
    return psiPreOrder.zip(treeCtxPreOrder).all { (psi, tree) ->
        psi.isSameStructure(tree)
    }
}

@Suppress("ReturnCount")
fun PsiElement.isSameStructure(
    tree: Tree?,
): Boolean {
    tree ?: return false
    // Compare type
    if (this.elementType.toString() != tree.type.toString()) {
        return false
    }
    // Compare labels
    if (this.label != tree.label) {
        return false
    }
    return true
}
