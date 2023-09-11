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

## Setting up IJ code server

IJ code server ([code-submissions-clustering-ij](code-submissions-clustering-ij/README.md)) is responsible for unification and distance calculation that require working with Program Structure Interface (PSI).

To set up IJ code server configuration, specify servers' ports and programming language in [`code-submissions-clustering-ij/src/main/resources/config.json`](code-submissions-clustering-ij/src/main/resources/config.json):

```json
{
  "servers": [
    {
      "port": 8000,
      "language": "PYTHON"
    }
  ]
}
```

Then make sure that you have [Docker](https://docs.docker.com/get-docker/) installed and build docker image: `Build IJ server docker image` OR run the following command from the project root:
```
docker build -t ij-server:latest -f code-submissions-clustering-ij/Dockerfile .
```

Finally, start servers using Python script: from the root of `scripts` run
```
pip3 install -r requirements.txt
python3 -m src.server.start_ij_servers
```

The script will indicate when all configured IJ servers are ready to listen.

Optionally, you can specify a path to the server config and save logs from the server to the specified directory:

```
python3 -m src.server.start_ij_servers --config=</path/to/config/file> --logs_dir=</path/to/logs/dir>
```

## Getting clustering results

After the servers have started, configure and run `Run cluster command` run configuration to cluster submissions.

### Required arguments

| Argument          | Description                                          |
|-------------------|------------------------------------------------------|
| **inputFile**     | Input .csv file with code submissions                |
| **distanceLimit** | Max distance between two vertices inside one cluster |
| **outputDir**     | Directory to store all output files                  |
| **language**      | Programming language of code submissions             |

### Optional arguments

| Argument                | Description                                                                                                                                                                |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **--binaryInput=[DIR]** | Directory storing previously serialized graph (containing `graph.bin` and `clusters.bin`)                                                                                  |
| **--serialize**         | Save submissions graph and its clustered structure to binary files (`/serialization` folder)                                                                               |
| **--saveCSV**           | Save unified solutions to .csv file (`graph.csv`)                                                                                                                          |
| **--visualize**         | Save submissions graph and its clustered structure visualization to .png files (`/visualization` folder). Requires [Graphviz installation](https://graphviz.org/download/) |
| **--saveClusters**      | Save submissions graph clusters to .txt file (`/txt/clusters.txt`)                                                                                                         |
| **--clusteringResult**  | Save the result of clustering to .csv.gz file (`clustering.csv.gz`)                                                                                                        |

## Usage examples

To cluster code submissions from .csv file and save clustering result to .csv.gz file use `clusteringResult` flag:

```
./gradlew run --args="cluster </path/to/submissions.csv> <distance limit> --outputDir=</path/to/output/dir> --language=PYTHON --clusteringResult"
```

To serialize resulting graph and its current clustered structure for future use add `serialize` flag:
```
./gradlew run --args="cluster </path/to/submissions.csv> <distance limit> --outputDir=</path/to/output/dir> --language=PYTHON --serialize"
```

You can then use preprocessed data from previous serialization for new clustering using `binaryInput`:

```
./gradlew run --args="cluster </path/to/submissions.csv> <distance limit> --outputDir=</path/to/output/dir> --language=PYTHON --binaryInput=</path/to/serialization>
```

## Clustering result overview

Clustering result constructed with `--clusteringResult` flag stores dataframe containing:

| Column        | Description                                                                                             |
|---------------|---------------------------------------------------------------------------------------------------------|
| submission_id | Id of code submission.                                                                                  |
| cluster_id    | Id of cluster containing the submission. Cluster ids are unique within a step, indexing begins from 0.  |
| position      | Position of the submission within the cluster according to the quality of code. Indexing begins from 0. |

Records are sorted by `submission_id`.

An example of clustering result:

| **submission_id**  | **cluster_id** | **position**  |
|:------------------:|:--------------:|:-------------:|
|      55931834      |       1        |       0       |
|      55960731      |       2        |       0       |
|      55984329      |       0        |       0       |
|      56095922      |       0        |       1       |
|      56102714      |       2        |       1       |
|      56105930      |       0        |       2       |
|      56105969      |       0        |       3       |
|      56107617      |       0        |       4       |
|      56117553      |       2        |       2       |
|      56123571      |       0        |       5       |
|      56133878      |       0        |       6       |
|      56156455      |       2        |       3       |