package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.util.ParametrizedBaseWithUnifierTest
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class LoadGraphTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testLoadGraphFromDataFrame(dataFrame: DataFrame<*>, expectedGraphRepresentation: String) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            assertEquals(
                expectedGraphRepresentation,
                dataFrame.loadGraph(unifier).buildStringRepresentation()
            )
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION")
        @JvmStatic
        fun getTestData(): List<Arguments> = listOf(
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(emptySequence()),
                "([], [])"
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "print(1)",
                ),
                "([\n" +
                    "SubmissionsNode(code = \n" +
                    "print(1)\n, \n" +
                    "idList = [1])], [])"
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "y = 1\n",
                    2, 1000, "var = 1\n",
                    3, 1000, "a=1\n",
                ),
                "([\n" +
                    "SubmissionsNode(code = \n" +
                    "v1 = 1\n, \n" +
                    "idList = [1, 2, 3])], [])"
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "y =         1\n",
                    2, 1000, "var       = 1\n",
                    3, 1000, "a=1\n",
                ),
                "([\n" +
                    "SubmissionsNode(code = \n" +
                    "v1 = 1\n, \n" +
                    "idList = [1, 2, 3])], [])"
            ),
        )
    }
}
