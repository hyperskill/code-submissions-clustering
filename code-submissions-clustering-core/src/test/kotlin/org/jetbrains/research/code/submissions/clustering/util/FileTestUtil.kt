package org.jetbrains.research.code.submissions.clustering.util

import java.io.File

/**
 * @property value
 */
enum class Extension(val value: String) {
    PY(".py"),
    ;
}

enum class Type {
    INPUT, OUTPUT
}

/**
 * @property type
 */
class TestFileFormat(private val prefix: String, private val extension: Extension, val type: Type) {
    fun check(file: File): TestFile? {
        val number = "(?<=${prefix}_)\\d+(?=(_.*)?${extension.value})".toRegex().find(file.name)?.value
            ?.toInt()
        return number?.let { TestFile(file, type, number) }
    }

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun match(testFile: TestFile): Boolean = testFile.type == type

    /**
     * @property file
     * @property type
     * @property number
     */
    data class TestFile(val file: File, val type: Type, val number: Number)
}

object FileTestUtil {
    val File.content: String
        get() = this.readText().removeSuffix("\n")

    /**
     * We assume the format of the test files will be:
     *
     * inPrefix_i_anySuffix.inExtension
     * outPrefix_i_anySuffix.outExtension,
     *
     * where:
     * inPrefix and outPrefix are set in [inFormat] and [outFormat] together with extensions,
     * i is a number; two corresponding input and output files should have the same number,
     * suffixes can by any symbols not necessary the same for the corresponding files.
     *
     * @param folder
     * @param inFormat
     * @param outFormat
     */
    fun getInAndOutFilesMap(
        folder: String,
        inFormat: TestFileFormat = TestFileFormat("in", Extension.PY, Type.INPUT),
        outFormat: TestFileFormat? = null
    ): Map<File, File?> {
        val (files, folders) = File(folder).listFiles().orEmpty().partition { it.isFile }

        // Process files in the given folder
        val inAndOutFilesGrouped = files.mapNotNull { inFormat.check(it) ?: outFormat?.check(it) }.groupBy { it.number }
        val inAndOutFilesMap = inAndOutFilesGrouped.map { (number, fileInfoList) ->
            val (f1, f2) = outFormat?.let {
                require(fileInfoList.size == 2) { "There are less or more than 2 test files with number $number" }
                fileInfoList.sortedBy { it.type }.zipWithNext().first()
            } ?: run {
                require(fileInfoList.size == 1) { "There are less or more than 1 test files with number $number" }
                Pair(fileInfoList.first(), null)
            }
            require(inFormat.match(f1)) { "The input file does not match the input format" }
            outFormat?.let {
                require(f2 != null && outFormat.match(f2)) { "The output file does not match the output format" }
            }
            f1.file to f2?.file
        }.sortedBy { it.first.name }.toMap()

        outFormat?.let {
            require(inAndOutFilesMap.values.mapNotNull { it }.size == inAndOutFilesMap.values.size) { "Output tests" }
        }

        // Process all other nested files
        return folders.sortedBy { it.name }.map { getInAndOutFilesMap(it.absolutePath, inFormat, outFormat) }
            .fold(inAndOutFilesMap) { a, e -> a.plus(e) }
    }
}
