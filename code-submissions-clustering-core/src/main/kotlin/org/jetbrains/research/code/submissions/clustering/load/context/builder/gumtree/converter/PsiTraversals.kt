@file:Suppress("TOO_LONG_FUNCTION", "WRONG_NEWLINES")

package org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter

import com.intellij.psi.PsiElement
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.PsiToGumTreeConverter.isWhiteSpace
import org.jetbrains.research.code.submissions.clustering.util.getElementChildren
import java.util.*

fun PsiElement.preOrder(toIgnoreWhiteSpaces: Boolean = true): Iterable<PsiElement> = object : Iterable<PsiElement> {
    override fun iterator(): Iterator<PsiElement> {
        return object : Iterator<PsiElement> {
            val currentNodes: Stack<PsiElement> = Stack()

            init {
                if (!this@preOrder.needToIgnore(toIgnoreWhiteSpaces)) {
                    currentNodes.add(this@preOrder)
                }
            }

            override fun hasNext() = currentNodes.size != 0

            override fun next(): PsiElement {
                if (!hasNext()) {
                    throw NoSuchElementException("Iterator is empty!")
                }
                val c = currentNodes.pop()
                currentNodes.addAll(c.getElementChildren(toIgnoreWhiteSpaces).reversed())
                return c
            }
        }
    }
}

fun PsiElement.postOrder(toIgnoreWhiteSpaces: Boolean = true): Iterable<PsiElement> = object : Iterable<PsiElement> {
    override fun iterator(): Iterator<PsiElement> {
        return object : Iterator<PsiElement> {
            val currentNodes: Stack<PsiElement> = Stack()

            init {
                if (!this@postOrder.needToIgnore(toIgnoreWhiteSpaces)) {
                    currentNodes.add(this@postOrder)
                }
                if (hasNext()) {
                    addAllUntilLeftLeaf()
                }
            }

            override fun hasNext() = currentNodes.size != 0

            override fun next(): PsiElement {
                if (!hasNext()) {
                    throw NoSuchElementException("Iterator is empty!")
                }
                val c = currentNodes.pop()
                if (hasNext() && currentNodes.peek().getElementChildren(toIgnoreWhiteSpaces).lastOrNull() != c) {
                    addAllUntilLeftLeaf()
                }
                return c
            }

            private fun addAllUntilLeftLeaf() {
                var peek = currentNodes.peek()
                while (peek.children.isNotEmpty()) {
                    val childrenToAdd = peek.getElementChildren(toIgnoreWhiteSpaces).reversed()
                    if (childrenToAdd.isNotEmpty()) {
                        currentNodes.addAll(childrenToAdd)
                        peek = currentNodes.peek()
                    } else {
                        break
                    }
                }
            }
        }
    }
}

private fun PsiElement.needToIgnore(toIgnoreWhiteSpaces: Boolean) = this.isWhiteSpace && toIgnoreWhiteSpaces
