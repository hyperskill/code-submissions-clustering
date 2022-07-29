package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.awt.EventQueue

class LoadGraphTest : ParametrizedBaseWithPythonSdkTest(getTmpProjectDir()) {
    init {
        mockProject ?: run {
            EventQueue.invokeAndWait {
                super.setUp()
            }
            mockProject = project
            mockPsiManager = psiManager
            unifier = PyUnifier(mockProject!!, mockPsiManager!!, toSetSdk = false)
        }
    }

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
        private var mockProject: Project? = null
        private var mockPsiManager: PsiManager? = null
        private lateinit var unifier: AbstractUnifier

        @Suppress("WRONG_NEWLINES")
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
                    2, 1000, "y = 1\n",
                    3, 1000, "var = 1\n",
                    4, 1000, "a=1\n",
                ),
                "([\n" +
                        "SubmissionsNode(code = \n" +
                        "v1 = 1\n, \n" +
                        "idList = [2, 3, 4])], [])"
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    5, 1000, "y =         1\n",
                    6, 1000, "var       = 1\n",
                    7, 1000, "a=1\n",
                ),
                "([\n" +
                        "SubmissionsNode(code = \n" +
                        "v1 = 1\n, \n" +
                        "idList = [5, 6, 7])], [])"
            ),
        )
    }
}
