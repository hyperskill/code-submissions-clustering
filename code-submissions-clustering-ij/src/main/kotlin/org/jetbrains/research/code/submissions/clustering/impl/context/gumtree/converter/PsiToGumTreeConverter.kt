package org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.converter

import com.github.gumtreediff.tree.Tree
import com.github.gumtreediff.tree.TreeContext
import com.github.gumtreediff.tree.TypeSet
import com.intellij.psi.PsiElement
import org.jetbrains.research.code.submissions.clustering.impl.util.psi.getElementChildren
import java.util.*

object PsiToGumTreeConverter {
    private const val WHITE_SPACE_TYPE = "WHITE_SPACE"

    val PsiElement.isWhiteSpace: Boolean
        get() = this.node.elementType.toString() == WHITE_SPACE_TYPE

    /**
     * Convert PSI to GumTree, storing already converted GumTree parent nodes and corresponding PSI child nodes:
     *       PSI:              GumTree:
     *  | children of A |       | A |
     *  | children of B |       | B |
     *  | children of C |       | C |
     *        ....               ...
     *  | children of Z |       | Z |
     *
     *  On each iteration children from PSI are converted to GumTree format and added to GumTree parents one by one;
     *  their PSI children are added to the corresponding place in the PSI children collection.
     *
     *  If [toIgnoreWhiteSpaces] is True then ignore nodes with WHITE_SPACE_TYPE. It can be useful in several cases:
     *  - The WHITE_SPACE elements do not represent the actual difference between two PSI trees,
     *  as they are tree elements that are not related to the original Python AST.
     *  This means that for a better trees comparison in GumTree format, you can remove them from PSI.
     *  - The standard GumTree trees XML serializer uses XMLInputFactory to deserialize the tree.
     *  In general, the algorithm works as follow: an XMLEventReader is created, then you need to restore
     *  the vertex for each XMLEvent.
     *  The problem is that if the label in the XMLEvent was a newline character symbol
     *  (for example \n for Unix systems, and \r\n for Windows),
     *  then after deserialization it will be replaced by the space symbol.
     *  This is not a problem with GumTree, this is a problem with standard Java XML deserializer.
     *  In this case, it is better to remove WHITE_SPACE elements.
     *
     * @param psiRoot
     * @param toIgnoreWhiteSpaces
     */
    fun convertTree(psiRoot: PsiElement, toIgnoreWhiteSpaces: Boolean = true): TreeContext {
        val context = TreeContext().apply {
            root = this.createTree(psiRoot)
                .also { it.setMetadata(psiRoot.textOffset, psiRoot.textLength) }
        }

        val gumTreeParents: Queue<Tree> = LinkedList(listOf(context.root))
        val psiChildren: Queue<List<PsiElement>> = LinkedList(listOf(psiRoot.getElementChildren(toIgnoreWhiteSpaces)))

        while (psiChildren.isNotEmpty()) {
            val parent = gumTreeParents.poll()
            psiChildren.poll().forEach { child ->
                val tree = context.createTree(child)
                    .also { it.setMetadata(child.textOffset, child.textLength) }
                tree.setParentAndUpdateChildren(parent)
                gumTreeParents.add(tree)
                psiChildren.add(child.getElementChildren(toIgnoreWhiteSpaces))
            }
        }

        return context
    }

    fun Array<PsiElement>.filterWhiteSpaces(): List<PsiElement> = this.filter { !it.isWhiteSpace }

    // Create GumTree tree
    private fun TreeContext.createTree(psiTree: PsiElement): Tree {
        val type = TypeSet.type(psiTree.node.elementType.toString())
        return this.createTree(type, psiTree.label)
    }

    private fun Tree.setMetadata(pos: Int, length: Int) {
        this.pos = pos
        this.length = length
    }
}
