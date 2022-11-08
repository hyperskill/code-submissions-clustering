package org.jetbrains.research.code.submissions.clustering.cli.models

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

open class AbstractGraphBuilderArgs(parser: ArgParser) {
    val language by parser.storing(
        "-l", "--language",
        help = "Programming language of code submissions"
    )
    val outputDir by parser.storing(
        "-o", "--outputDir",
        help = "Directory to store all output files",
    )
    val binaryInput by parser.storing(
        "-b", "--binaryInput",
        help = "Directory storing previously serialized graph"
    ).default<String?>(null)
    val serializeGraph by parser.flagging(
        "--serialize",
        help = "Save submissions graph and its clustered structure to binary files"
    )
    val saveCSV by parser.flagging(
        "--saveCSV",
        help = "Save unified solutions to .csv file"
    )
    val visualize by parser.flagging(
        "--visualize",
        help = "Save submissions graph and its clustered structure visualization to .png files"
    )
    val saveClusters by parser.flagging(
        "--saveClusters",
        help = "Save submissions graph clusters to .txt file"
    )
    val clusteringResult by parser.flagging(
        "--clusteringResult",
        help = "Save the result of clustering to .csv.gz file"
    )
}
