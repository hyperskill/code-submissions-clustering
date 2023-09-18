package org.jetbrains.research.code.submissions.clustering.cli.models

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import org.jetbrains.research.code.submissions.clustering.model.Language
import java.io.File
import java.nio.file.Path

class AbstractGraphBuilderOptions : OptionGroup(
    name = "Common Options"
) {
    val outputDir by option(
        "-o", "--outputDir",
        help = "Directory to store all output files (required)",
    ).required()
    val language by option(
        "-l", "--language",
        help = "Programming language of code submissions (required)",
    ).enum<Language>().required()
    val binaryInput: Path? by option(
        "-b", "--binaryInput",
        help = "Directory storing previously serialized graph",
    ).path(mustExist = true)
    val configFile: File by option(
        "-c", "--config",
        help = "Path to IJ code server config file"
    ).file(mustExist = true).default(
        File("../code-submissions-clustering-ij/src/main/resources/server-config.json")
    )
}

class AbstractGraphBuilderFlags : OptionGroup(
    name = "Flags"
) {
    val serializeGraph: Boolean by option(
        "--serialize",
        help = "Save submissions graph and its clustered structure to binary files",
    ).flag()
    val saveCSV: Boolean by option(
        "--saveCSV",
        help = "Save unified solutions to .csv file",
    ).flag()
    val visualize: Boolean by option(
        "--visualize",
        help = "Save submissions graph and its clustered structure visualization to .png files",
    ).flag()
    val saveClusters: Boolean by option(
        "--saveClusters",
        help = "Save submissions graph clusters to .txt file",
    ).flag()
    val clusteringResult: Boolean by option(
        "--clusteringResult",
        help = "Save the result of clustering to .csv.gz file",
    ).flag()
}
