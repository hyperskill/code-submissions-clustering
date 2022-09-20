# Loading submissions graphs

To load submissions graph(s) from .csv file, run load_submissions_graph.py with following arguments:

### Required arguments

| Argument        | Description                                                                                                                                                                                     |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **input_file**  | Input .csv file storing submissions for a step (or multiple steps).                                                                                                                             |
| **output_path** | Output directory to store all output files. In basic configuration a log file, <br/>a new .csv and a string representation of resulting graph for every step in **input_file** will be created. |
| **language**    | Programming language of code submissions.                                                                                                                                                       |

### Optional arguments

| Argument        | Description                                                                                              |
|-----------------|----------------------------------------------------------------------------------------------------------|
| **--serialize** | Serialize resulting graph to .bin file using [protobuf](https://developers.google.com/protocol-buffers). |
| **--saveCSV**   | Save unified submissions and corresponding list of submission ids to .csv file.                          |

### Usage example

```
python3 load_submissions_graph.py /path/to/input/file /path/to/output/dir PYTHON --serialize --saveCSV
```
