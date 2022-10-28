package org.jetbrains.research.code.submissions.clustering.load.distance

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.load.distance.measurers.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.ParametrizedBaseWithUnifierTest
import org.jetbrains.research.code.submissions.clustering.util.asPsiFile
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class GumTreeDistanceTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    private fun String.getTreeContext() =
        this.asPsiFile(
            Language.PYTHON,
            mockPsiManager!!
        ) { getTreeContext(it) }

    @ParameterizedTest
    @MethodSource("solutions")
    fun testCalculateDistances(from: String, to: String, expectedDistance: Int) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            requireNotNull(mockProject)
            requireNotNull(mockPsiManager)
            val measurer = GumTreeDistanceMeasurerByPsi(mockProject!!)
            val actions = measurer.calculateDistance(from.getTreeContext(), to.getTreeContext())
            val dist = with(measurer) {
                actions.calculateWeight()
            }
            assertEquals(expectedDistance, dist)
        }
    }

    companion object {
        private const val EMPTY_CODE = ""

        @JvmStatic
        fun solutions(): List<Arguments> = GumTreeDistanceCodeSnippetsData.codeSnippets.map {
            listOf(
                // from, to, distance
                Arguments.of(it.code1, it.code2, it.distance1),
                Arguments.of(it.code2, it.code1, it.distance2),
                Arguments.of(it.code1, it.code1, 0),
                Arguments.of(it.code2, it.code2, 0)
            )
        }.flatten() + Arguments.of(EMPTY_CODE, EMPTY_CODE, 0)
    }
}
