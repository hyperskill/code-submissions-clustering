package org.jetbrains.research.code.submissions.clustering.gumtree

import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.GumTreeParserUtil
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/*
* Check if python parser setup works correctly
* */
@RunWith(PythonGumTreeRunner::class)
internal class PythonParserSetupTest {
    private val parserRepoFile = File(GumTreeParserUtil.parserRepoPath)
    private val parserZipFile = File(GumTreeParserUtil.parserZipPath)
    private val targetParserFile = File(GumTreeParserUtil.targetParserPath)

    private fun getLastModified() = listOf(parserZipFile, parserRepoFile, targetParserFile).map { it.lastModified() }

    @Test
    fun testEmptyParser() {
        // Check if parser setup works without both parser zip and parser repo

        parserRepoFile.deleteRecursively()
        parserZipFile.deleteRecursively()
        GumTreeParserUtil.checkSetup()
        // Parser zip file should be updated and parser repo should be unzipped
        assertParserFies()
    }

    @Test
    fun testParserWithoutRepo() {
        // Check if parser setup works without parser repo

        assert(parserZipFile.exists()) { PARSER_ZIP_FILE_EXIST_ERROR }
        parserRepoFile.deleteRecursively()

        val expectedLastModified = parserZipFile.lastModified()
        GumTreeParserUtil.checkSetup()
        val actualLastModified = parserZipFile.lastModified()

        // Parser repo file should be unzipped from existing zip file, which shouldn't be changed
        assert(parserRepoFile.exists()) { PARSER_REPO_FILE_EXIST_ERROR }
        assert(expectedLastModified == actualLastModified)
    }

    @Test
    fun testParserWithoutZip() {
        // Check if parser setup works without parser zip

        assert(parserRepoFile.exists()) { PARSER_REPO_FILE_EXIST_ERROR }
        parserZipFile.deleteRecursively()

        val expectedLastModified = parserRepoFile.lastModified()
        GumTreeParserUtil.checkSetup()
        val actualLastModified = parserRepoFile.lastModified()

        // Parser zip file shouldn't exist if there was parser repo, which shouldn't be changed
        assert(!parserZipFile.exists())
        assert(expectedLastModified == actualLastModified)
    }

    @Test
    fun testFullParser() {
        // Check if updates works with already existing parser zip and repo

        assertParserFies()
        val lastModifiedBeforeSetup = getLastModified()
        GumTreeParserUtil.checkSetup(true)
        val lastModifiedAfterSetup = getLastModified()

        // Parser zip file and repo should exist and should be changed together with targetParserFile
        assertParserFies()
        assert(lastModifiedBeforeSetup.zip(lastModifiedAfterSetup).all { it.first != it.second })
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
