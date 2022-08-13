package org.jetbrains.research.code.submissions.clustering.gumtree

import com.github.gumtreediff.gen.python.PythonTreeGenerator
import com.github.gumtreediff.tree.TreeContext
import org.jetbrains.research.code.submissions.clustering.load.graph.context.builders.GumTreeParserUtil
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

/*
* Check if python parser setup works correctly
* */
internal class PythonParserSetupTest : PythonGumTreeBaseTest() {
    private val srcFile: String = javaClass.getResource("source.py")?.path
        ?: error("Can not find \"source.py\" resources for PythonParserSetupTest class")
    private val parserRepoFile = File(GumTreeParserUtil.parserRepoPath)
    private val parserZipFile = File(GumTreeParserUtil.parserZipPath)
    private val targetParserFile = File(GumTreeParserUtil.targetParserPath)

    // Parse a python file via GumTree
    private fun getTreeContext(): TreeContext = PythonTreeGenerator().generateFrom().string(File(srcFile).readText())

    private fun getLastModified() = listOf(parserZipFile, parserRepoFile, targetParserFile).map { it.lastModified() }

    @Test
    @DisplayName("Check if parser setup works without both parser zip and parser repo")
    fun testEmptyParser() {
        parserRepoFile.deleteRecursively()
        parserZipFile.deleteRecursively()
        GumTreeParserUtil.checkSetup()
        // Parser zip file should be updated and parser repo should be unzipped
        assertParserFies()
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    @DisplayName("Check if parser setup works without parser repo")
    fun testParserWithoutRepo() {
        assert(parserZipFile.exists()) { PARSER_ZIP_FILE_EXIST_ERROR }
        parserRepoFile.deleteRecursively()

        val expectedLastModified = parserZipFile.lastModified()
        GumTreeParserUtil.checkSetup()
        val actualLastModified = parserZipFile.lastModified()

        // Parser repo file should be unzipped from existing zip file, which shouldn't be changed
        assert(parserRepoFile.exists()) { PARSER_REPO_FILE_EXIST_ERROR }
        assert(expectedLastModified == actualLastModified)
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    @DisplayName("Check if parser setup works without parser zip")
    fun testParserWithoutZip() {
        assert(parserRepoFile.exists()) { PARSER_REPO_FILE_EXIST_ERROR }
        parserZipFile.deleteRecursively()

        val expectedLastModified = parserRepoFile.lastModified()
        GumTreeParserUtil.checkSetup()
        val actualLastModified = parserRepoFile.lastModified()

        // Parser zip file shouldn't exist if there was parser repo, which shouldn't be changed
        assert(!parserZipFile.exists())
        assert(expectedLastModified == actualLastModified)
        assertDoesNotThrow(::getTreeContext)
    }

    @Test
    @DisplayName("Check if updates works with already existing parser zip and repo")
    fun testFullParser() {
        assertParserFies()
        val lastModifiedBeforeSetup = getLastModified()
        GumTreeParserUtil.checkSetup(true)
        val lastModifiedAfterSetup = getLastModified()

        // Parser zip file and repo should exist and should be changed together with targetParserFile
        assertParserFies()
        assert(lastModifiedBeforeSetup.zip(lastModifiedAfterSetup).all { it.first != it.second })
        assertDoesNotThrow(::getTreeContext)
    }

    private fun assertParserFies() {
        assert(parserRepoFile.exists()) { PARSER_REPO_FILE_EXIST_ERROR }
        assert(parserZipFile.exists()) { PARSER_ZIP_FILE_EXIST_ERROR }
    }

    companion object {
        private const val PARSER_REPO_FILE_EXIST_ERROR = "Python parser repo file does not exist!"
        private const val PARSER_ZIP_FILE_EXIST_ERROR = "Python parser zip file does not exist!"
    }
}
