package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SerializationTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testSerializeGraph(dataFrame: DataFrame<*>) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val graph = dataFrame.loadGraph(unifier)
            val bytes = graph.toProto().toByteArray()
            val deserializedGraph = ProtoSubmissionsGraph.parseFrom(bytes).toGraph()
            assertEquals(
                graph.buildStringRepresentation(),
                deserializedGraph.buildStringRepresentation()
            )
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION")
        @JvmStatic
        fun getTestData(): List<DataFrame<*>> = listOf(
            dataFrameOf("id", "step_id", "code")(emptySequence()),
            dataFrameOf("id", "step_id", "code")(
                1, 1000, "print(1)",
            ),
            dataFrameOf("id", "step_id", "code")(
                1, 1000, "y = 1\n",
                2, 1000, "var = 1\n",
                3, 1000, "a=1\n",
            ),
            dataFrameOf("id", "step_id", "code")(
                1, 1000, "y =         1\n",
                2, 1000, "var       = 1\n",
                3, 1000, "a=1\n",
            ),
        )
    }
}
