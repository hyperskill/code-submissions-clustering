package org.jetbrains.research.code.submissions.clustering.gumtree.converter

import junit.framework.TestCase
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.converter.getTreeContext
import org.jetbrains.research.code.submissions.clustering.util.FileTestUtil.getInAndOutFilesMap
import org.jetbrains.research.code.submissions.clustering.util.ParametrizedBaseWithUnifierTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
@Ignore
class PsiTreeConverterTest : ParametrizedBaseWithUnifierTest(getResourcesRootPath(::PsiTreeConverterTest)) {
    @JvmField
    @Parameterized.Parameter(0)
    var inSourceFile: File? = null

    @Test
    fun compareTreeStructureTest() {
        val inFilePsi = mockFixture!!.configureByFile(inSourceFile!!.absolutePath)
        TestCase.assertTrue(inFilePsi.isEqualTreeStructure(getTreeContext(inFilePsi)))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0})")
        fun getTestData(): List<Array<File>> {
            val files = getInAndOutFilesMap(
                getResourcesRootPath(::PsiTreeConverterTest),
            )
            return files.map { f -> arrayOf(f.key) }.toList()
        }
    }
}
