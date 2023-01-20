from dataclasses import dataclass


@dataclass
class ScriptArgument:
    name: str
    help_text: str
    is_optional: bool = False
    is_flag: bool = False
    data_type: type = str


class BaseScriptArguments:
    def __init__(self):
        self._args = [
            ScriptArgument(
                'output_path',
                'Output directory to store folders with output files',
            ),
            ScriptArgument(
                'language',
                'Programming language of code submissions',
            ),
            ScriptArgument(
                'binaryInput',
                'Directory storing previously serialized graph',
                is_optional=True,
            ),
            ScriptArgument(
                'serialize',
                'Save submissions graph to binary file',
                is_optional=True,
                is_flag=True,
                data_type=bool,
            ),
            ScriptArgument(
                'saveCSV',
                'Save unified solutions to .csv file',
                is_optional=True,
                is_flag=True,
                data_type=bool,
            ),
            ScriptArgument(
                'visualize',
                'Visualize the solution graph into png format (can work slow)',
                is_optional=True,
                is_flag=True,
                data_type=bool,
            ),
            ScriptArgument(
                'saveClusters',
                'Save clusters to .txt file',
                is_optional=True,
                is_flag=True,
                data_type=bool,
            ),
            ScriptArgument(
                'clusteringResult',
                'Save the result of clustering to .csv.gz file',
                is_optional=True,
                is_flag=True,
                data_type=bool,
            ),
        ]

    def __iter__(self):
        return iter(self._args)


class LoadScriptArguments(BaseScriptArguments):
    def __init__(self):
        super().__init__()
        self._args = [
            ScriptArgument(
                'input_file',
                'Input .csv file with code solutions for a set of steps',
            ),
        ] + self._args


class CalculateDistScriptArguments(BaseScriptArguments):
    def __init__(self):
        super().__init__()
        self._args = [
            ScriptArgument(
                'input_path',
                'Input directory storing serialized submissions graphs',
            ),
        ] + self._args


class ClusterScriptArguments(BaseScriptArguments):
    def __init__(self):
        super().__init__()
        self._args = [
            ScriptArgument(
                'csv_dir',
                'Input directory with .csv files containing code submissions',
            ),
            ScriptArgument(
                'min_distance_limit',
                'Min distance limit',
                data_type=int,
            ),
            ScriptArgument(
                'max_distance_limit',
                'Max distance limit',
                data_type=int,
            ),
            ScriptArgument(
                'step_distance_limit',
                'Distance limit step',
                data_type=int,
            ),
        ] + self._args
