package org.jetbrains.research.code.submissions.clustering.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Main : CliktCommand(name = "csc") {
    override fun run() = Unit
}

fun main(args: Array<String>) =
    Main()
        .subcommands(
            Load(),
            CalculateDistance(),
            Cluster()
        ).main(args)
