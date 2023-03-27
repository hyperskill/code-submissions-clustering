# Scripts for running external tools

Here is a brief instruction on how to use external tools scripts.

## JPlag

To cluster submissions using [JPlag](https://github.com/jplag/JPlag) with equivalent to code submissions clustering input and output (clusters and clustering result), 
run jplag.py with following arguments:

### Required arguments

| Argument          | Description                                                                                                                                                      |
|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **input_file**    | Input .csv file storing submissions for a step (or multiple steps).                                                                                              |
| **output_path**   | Output directory to store all output files. This is where a log file, <br/>a new .csv, clustering result (.csv.gz) and clusters overview (.txt) will be created. |
| **language**      | Programming language of code submissions.                                                                                                                        |

### Optional arguments

| Argument                        | Description                                                                                                                                  |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **--threshold_min [MIN]**       | An integer number specifying minimum JPlag threshold (default: 0.2).                                                                         |
| **--threshold_max [MAX]**       | An integer number specifying maximum JPlag threshold (not included, default: 0.25).                                                          |
| **--threshold_step [STEP]**     | An integer number specifying the incrementation of JPlag threshold (default: 0.1).                                                           |
| **--jplag_jar_directory [DIR]** | Path to JPlag .jar file. By default, .jar in scripts root folder is used (if exists), or the latest version of JPlag is downloaded and used. |

### Usage example

Run the following command from the **root** of the **scripts** folder:

```
python3 -m src.external_tools_runner.jplag /path/to/csv /path/to/output/dir python3
```

**Note**: you need to install all requirements from the
[requirements](/requirements.txt) file before launching this script:

```text
pip3 install -r requirements.txt
```

### Output structure

Imagine to run JPlag script with following arguments:

```
python3 -m src.external_tools_runner.jplag /path/to/csv /path/to/output/dir python3 --threshold_min=0.2 --threshold_max=0.9 --threshold_step=0.2
```

Then the structure of passed **output_path** is following:

```
.
├── [step id]
│    └── 0.2
│        ├── clustering.csv.gz
│        ├── clusters.txt
│        └── result.zip
│    ├── ...
│    └── 0.8
│        ├── clustering.csv.gz
│        ├── clusters.txt
│        └── result.zip
├── ...
└── log.txt
```

Here `clustering.csv.gz` and `clusters.txt` have format equivalent to code submissions clustering, 
and `result.zip` is JPlag core output.

Note that JPlag might not run successfully for some steps. 
This mostly happens because there are not enough code submissions of sufficient length.
In this case corresponding directories will be empty:

```
.
├── [step id]
│    └── 0.2
│        ├── clustering.csv.gz
│        ├── clusters.txt
│        └── result.zip
│    ├── ...
│    └── 0.8
│        ├── clustering.csv.gz
│        ├── clusters.txt
│        └── result.zip
├── ...
├── [step id]
│    └── 0.2
│    ├── ...
│    └── 0.8
├── ...
└── log.txt
```