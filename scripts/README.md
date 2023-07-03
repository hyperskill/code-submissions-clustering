# Code submissions clustering scripts

*This readme contains instructions on code submissions clustering scripts.
If you would like to run external tools preserving the same input and output format, consider [external_tools_runner](/src/external_tools_runner) folder.*

Here is a brief instruction on how to use code submissions clustering scripts.

The process consists of two main steps - loading submissions graphs from the data and clustering loaded submissions graphs.
Loading submissions graph includes: 
* unification of the submissions;
* building a submissions graph as a complete graph on unified submissions;
* calculation of the distances between the vertices.

Clustering a submissions graph is a process of building a complete graph on clusters of the unified submissions.

Loading a submissions graph is a time-consuming task, thus, loading and clustering are separated into two different scripts, 
so that the loading result may be obtained once and used for clustering multiple times.

You can find the usage instructions for both scripts below.

## Loading submissions graphs

To load submissions graph(s) from .csv file, run load_submissions_graph.py with following arguments:

### Required arguments

| Argument          | Description                                                                                                                                                                                     |
|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **input_file**    | Input .csv file storing submissions for a step (or multiple steps).                                                                                                                             |
| **output_path**   | Output directory to store all output files. In basic configuration a log file, <br/>a new .csv and a string representation of resulting graph for every step in **input_file** will be created. |
| **language**      | Programming language of code submissions.                                                                                                                                                       |

### Optional arguments

| Argument                | Description                                                                                                                     |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| **--binaryInput [DIR]** | Directory storing previously serialized graph.                                                                                  |
| **--serialize**         | Serialize resulting graph to .bin file using [protobuf](https://developers.google.com/protocol-buffers).                        |
| **--saveCSV**           | Save unified submissions and corresponding list of submission ids to .csv file.                                                 |
| **--visualize**         | Visualize the solution graph into png format (can work slow). Requires [Graphviz installation](https://graphviz.org/download/). |
| **--saveClusters**      | Save clusters to .txt file.                                                                                                     |
| **--clusteringResult**  | Save the result of clustering to .csv.gz file.                                                                                  |

### Usage example

Run the following command from the **root** of the **scripts** folder:

```
python3 -m src.plugin_runner.load_submissions_graph /path/to/input/file /path/to/output/dir PYTHON --serialize --saveCSV --saveClusters --clusteringResult
```

**Note**: you need to install all requirements from the 
[requirements](./requirements.txt) file before launching this script:

```text
pip3 install -r requirements.txt
```

## Clustering code submissions

To cluster code submissions, run clustering.py with following arguments (note that with a single run of the script multiple distance limits might be used for clustering):

### Required arguments

| Argument                | Description                                                                                                                                                                          |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **csv_dir**             | Directory storing input .csv file(s) with code submissions for a step (or multiple steps).                                                                                           |
| **min_distance_limit**  | An integer number specifying minimum distance limit between clusters. You can use 15 for the test running.                                                                           |
| **max_distance_limit**  | An integer number specifying maximum distance limit between clusters (not included). You can use 230 for the test running.                                                           |
| **step_distance_limit** | An integer number specifying the incrementation of distance limit. You can use 15 for the test running.                                                                              |
| **output_path**         | Output directory to store all output files. In basic configuration a log file <br/> and a string representation of resulting graph for every step in **input_file** will be created. |
| **language**            | Programming language of code submissions.                                                                                                                                            |

### Optional arguments

| Argument                | Description                                                                                                                     |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| **--binaryInput [DIR]** | Directory storing previously serialized graph.                                                                                  |
| **--serialize**         | Serialize resulting graph to .bin file using [protobuf](https://developers.google.com/protocol-buffers).                        |
| **--saveCSV**           | Save unified submissions and corresponding list of submission ids to .csv file.                                                 |
| **--visualize**         | Visualize the solution graph into png format (can work slow). Requires [Graphviz installation](https://graphviz.org/download/). |
| **--saveClusters**      | Save clusters to .txt file.                                                                                                     |
| **--clusteringResult**  | Save the result of clustering to .csv.gz file.                                                                                  |

Keep it in mind that in case you do not provide `--binaryInput` argument the submissions graphs will be loaded again 
for each distance limit in the range. That is much more time-consuming than reading the graphs from binary files, thus,
it is highly recommended to load and serialize submissions graphs before running the clustering script.

### Usage example

Run the following command from the **root** of the **scripts** folder:

```
python3 -m src.plugin_runner.clustering /path/to/csv/dir 10 100 20 /path/to/output/dir PYTHON --serialize --saveClusters --clusteringResult
```

**Note**: you need to install all requirements from the
[requirements](./requirements.txt) file before launching this script:

```text
pip3 install -r requirements.txt
```

## Building a pipeline

As have been said, loading submissions graphs and clustering should be done sequentially. Here is an example of
the whole process of clustering code submissions.

Firstly, load the submissions graphs from the submissions dataset and serialize them for future use:

```
python3 -m src.plugin_runner.load_submissions_graph /path/to/data.csv /path/to/load_output PYTHON --serialize
```

These are the contents of the passed load output directory after running the `load_submissions_graph` script:

```
.
├── log.txt
├── output
│    ├── [step id]
│    │   ├── serialization
│    │   │   ├── clusters.bin
│    │   │   └── graph.bin
│    │   └── txt
│    │       └── graph.txt
│    ├── ...
│    └── ...
└── solutions
    ├── [step id].csv
    ├── ...
    └── ...
```

Now it is pretty convenient to launch clustering of the serialized submissions graphs (add more optional arguments for 
advanced clustering analysis):

```
python3 -m src.plugin_runner.clustering /path/to/load_output/solutions 15 230 15 /path/to/clustering_output PYTHON --binaryInput=/path/to/load_output/output --clusteringResult
```

These are the contents of the passed clustering output directory after running the `clustering` script:

```
.
├── [step id]
│    └── 15
│        ├── clustering.csv.gz
│        └── txt
│            └── graph.txt
│    ├── ...
│    └── 225
│        ├── clustering.csv.gz
│        └── txt
│            └── graph.txt
├── ...
├── ...
└── log.txt
```
