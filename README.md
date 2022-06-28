# Code submissions clustering

The main goal of this project is to cluster and range submissions from the Hyperskill platform
The baseline will be based on an approach with a graph of solutions.
We will use [transformations](https://github.com/JetBrains-Research/ast-transformations) of the source code
to unify student submissions. Next, we will find the difference between them and highlit the clusters.
The first ranking version will be based on simple heuristics.