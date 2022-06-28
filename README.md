[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
![Gradle Build](https://github.com/hyperskill/code-submissions-clustering/workflows/Gradle%20Build/badge.svg?branch=main)
[![Run deteKT](https://github.com/hyperskill/code-submissions-clustering/actions/workflows/detekt.yml/badge.svg)](https://github.com/hyperskill/code-submissions-clustering/actions/workflows/detekt.yml)
[![Run diKTat](https://github.com/hyperskill/code-submissions-clustering/actions/workflows/diktat.yml/badge.svg)](https://github.com/hyperskill/code-submissions-clustering/actions/workflows/diktat.yml)


# Code submissions clustering

The main goal of this project is to cluster and range submissions from the Hyperskill platform
The baseline will be based on an approach with a graph of solutions.
We will use [transformations](https://github.com/JetBrains-Research/ast-transformations) of the source code
to unify student submissions. Next, we will find the difference between them and highlit the clusters.
The first ranking version will be based on simple heuristics.