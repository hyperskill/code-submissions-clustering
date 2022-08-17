package org.jetbrains.research.code.submissions.clustering.gumtree.converter

import junit.framework.TestCase
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.util.Extension
import org.jetbrains.research.code.submissions.clustering.util.FileTestUtil.getInAndOutFilesMap
import org.jetbrains.research.code.submissions.clustering.util.ParametrizedBaseWithUnifierTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class PsiTreeConverterTest : ParametrizedBaseWithUnifierTest(getResourcesRootPath(::PsiTreeConverterTest)) {
    @ParameterizedTest
    @MethodSource("getTestData")
    @DisplayName("Compare tree structure test")
    fun compareTreeStructureTest(inSourceFile: File) {
        val inFilePsi = mockFixture!!.configureByText("dummy${Extension.PY.value}", inSourceFile.readText())
        TestCase.assertTrue(inFilePsi.isEqualTreeStructure(getTreeContext(inFilePsi)))
    }

    companion object {
        @JvmStatic
        fun getTestData(): List<Arguments> {
            val files = getInAndOutFilesMap(
                getResourcesRootPath(::PsiTreeConverterTest),
            )
            return files.map { f -> Arguments.of(f.key) }.toList()
        }
    }
}
