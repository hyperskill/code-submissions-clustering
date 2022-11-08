# Loading submissions graphs

To load submissions graph(s) from .csv file, run load_submissions_graph.py with following arguments:

### Required arguments

| Argument        | Description                                                                                                                                                                                     |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **input_file**  | Input .csv file storing submissions for a step (or multiple steps).                                                                                                                             |
| **output_path** | Output directory to store all output files. In basic configuration a log file, <br/>a new .csv and a string representation of resulting graph for every step in **input_file** will be created. |
| **language**    | Programming language of code submissions.                                                                                                                                                       |

### Optional arguments

| Argument                | Description                                                                                              |
|-------------------------|----------------------------------------------------------------------------------------------------------|
| **--binaryInput [DIR]** | Directory storing previously serialized graph.                                                           |
| **--serialize**         | Serialize resulting graph to .bin file using [protobuf](https://developers.google.com/protocol-buffers). |
| **--saveCSV**           | Save unified submissions and corresponding list of submission ids to .csv file.                          |
| **--visualize**         | Visualize the solution graph into png format (can work slow).                                            |
| **--saveClusters**      | Save clusters to .txt file.                                                                              |
| **--clusteringResult**  | Save the result of clustering to .csv.gz file.                                                           |

### Usage example

Run the following command from the **root** of the **scripts** folder:

```
python3 -m plugin_runner.load_submissions_graph /path/to/input/file /path/to/output/dir PYTHON --serialize --saveCSV --saveClusters
```

**Note**: you need to install all requirements from the 
[requirements](./requirements.txt) file before launching this script:

```text
pip3 install -r requirements.txt
```
