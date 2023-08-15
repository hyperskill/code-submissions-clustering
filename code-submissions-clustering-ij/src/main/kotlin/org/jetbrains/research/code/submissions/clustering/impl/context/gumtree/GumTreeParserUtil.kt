package org.jetbrains.research.code.submissions.clustering.impl.context.gumtree

import mu.KotlinLogging
import net.lingala.zip4j.ZipFile
import org.jetbrains.research.code.submissions.clustering.util.Command
import org.jetbrains.research.code.submissions.clustering.util.getTmpDirPath
import org.jetbrains.research.code.submissions.clustering.util.runProcessBuilder
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * [GumTreeParserUtil] is created for parser setup before using GumTree with python code.
 * Also [checkSetup] should be called before running tests.
 */
object GumTreeParserUtil {
    private val LOG = KotlinLogging.logger {}
    private const val PYTHONPARSER_PROPERTY = "gt.pp.path"
    private const val PARSER_REPO_ZIP_URL =
        "https://github.com/JetBrains-Research/pythonparser/archive/master.zip"
    private const val PARSER_ZIP_NAME = "master.zip"

    // Relative path in the parser repository
    private const val PARSER_REPO_FOLDER = "pythonparser-master"
    private const val PARSER_RELATIVE_PATH = "$PARSER_REPO_FOLDER/src/main/python/pythonparser/pythonparser_3.py"
    private const val PARSER_NAME = "pythonparser"
    val targetParserPath = "${getTmpDirPath()}/$PARSER_NAME"
    val parserRepoPath = "${getParserRepoParentFolder()}/$PARSER_REPO_FOLDER"
    val parserZipPath = "${getParserRepoParentFolder()}/$PARSER_ZIP_NAME"

    /**
     * Get a parent folder of parser repository, which is stored in this project in the resources folder
     */
    private fun getParserRepoParentFolder(): String = getTmpDirPath()

    /**
     * Update parser zip file
     */
    private fun updateParserZip() {
        val zipFilePath = Paths.get("${getParserRepoParentFolder()}/$PARSER_ZIP_NAME")
        LOG.info("Updating the current master zip")
        val file: InputStream = URL(PARSER_REPO_ZIP_URL).openStream()
        Files.copy(file, zipFilePath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun unzipParserRepo(zipParserRepoPath: String = parserZipPath) {
        LOG.info("Unzipping the folder with the repository")
        val zipFile = ZipFile(zipParserRepoPath)
        val parserRepositoryPath = getParserRepoParentFolder()
        File(parserRepoPath).deleteRecursively()
        zipFile.extractAll(parserRepositoryPath)
    }

    /**
     * Makes incoming file executable
     *
     * @param targetFile - file, which file system permissions will be
     * changed to "executable".
     */
    private fun makeFileExecutable(targetFile: File) {
        LOG.info("Making parser file executable")
        runProcessBuilder(Command(listOf("chmod", "+x", targetFile.absolutePath)))
    }

    /**
     * Check if parser file is in the target place and it is executable.
     * if not - makes it so.
     */
    private fun checkParserFile(
        parserFilePath: String,
        targetPath: String,
        toUpdateTargetFile: Boolean,
        toAddIntoSystemPath: Boolean = true
    ) {
        val targetFile = File(targetPath)
        if (!targetFile.exists() || toUpdateTargetFile) {
            LOG.info("Parser file will be created in $targetPath")
            val parserFile = File(parserFilePath)
            parserFile.copyTo(File(targetPath), overwrite = true)
            makeFileExecutable(targetFile)
        } else {
            LOG.info("Parser file already exists in $targetPath")
        }
        if (toAddIntoSystemPath) {
            LOG.info("Adding parser path into system path")
            System.setProperty(PYTHONPARSER_PROPERTY, targetPath)
        }
    }

    private fun String.exists() = File(this).exists()

    /*
     * Check if pythonparser files are valid
     */
    fun checkSetup(toUpdateRepo: Boolean = false) {
        LOG.info("Checking correctness of a parser setup")
        val parserPath = "${getParserRepoParentFolder()}/$PARSER_RELATIVE_PATH"

        if (toUpdateRepo || !parserPath.exists()) {
            LOG.info("There is no parser repo (${!parserPath.exists()}) or it needs to be updated ($toUpdateRepo)")
            if (toUpdateRepo || !parserZipPath.exists()) {
                LOG.info("There is no parser zip (${!parserZipPath.exists()}) or it needs to be updated ($toUpdateRepo)")
                updateParserZip()
            }
            unzipParserRepo()
        }
        checkParserFile(parserPath, targetParserPath, toUpdateRepo)
    }
}
