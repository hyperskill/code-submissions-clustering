package org.jetbrains.research.code.submissions.clustering.load.distance

import com.github.gumtreediff.tree.TreeContext
import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.util.ParametrizedBaseWithUnifierTest
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class GumTreeDistanceTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {

    @JvmField
    @Parameterized.Parameter(0)
    var from: String? = null

    @JvmField
    @Parameterized.Parameter(1)
    var to: String? = null

    @JvmField
    @Parameterized.Parameter(2)
    var expectedDistance: Int? = null

    private fun String.getTreeContext(): TreeContext {
        val psi = psiFileFactory.getPsiFile(this)
        val treeContext = getTreeContext(psi)
        psiFileFactory.releasePsiFile(psi)
        return treeContext
    }

    @Test
    fun testCalculateDistances() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            clearFactory()
            requireNotNull(mockProject)
            requireNotNull(mockPsiManager)
            val measurer = GumTreeDistanceMeasurerByPsi(psiFileFactory)
            val actions = measurer.calculateDistance(from!!.getTreeContext(), to!!.getTreeContext())
            val dist = with(measurer) {
                actions.calculateWeight()
            }
            assertEquals(expectedDistance!!, dist)
        }
    }

    companion object {
        private const val EMPTY_CODE = ""

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1}, {2})")
        fun solutions() = GumTreeDistanceCodeSnippetsData.codeSnippets.map {
            listOf(
                // from, to, distance
                arrayOf(it.code1, it.code2, it.distance1),
                arrayOf(it.code2, it.code1, it.distance2),
                arrayOf(it.code1, it.code1, 0),
                arrayOf(it.code2, it.code2, 0)
            )
        }.flatten() + listOf(arrayOf(EMPTY_CODE, EMPTY_CODE, 0))
    }
}
