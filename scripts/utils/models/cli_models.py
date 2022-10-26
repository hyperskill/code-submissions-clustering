from argparse import Namespace
from enum import Enum, unique


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'input'
    OUTPUT_PATH = 'output'
    LANGUAGE = 'lang'
    BINARY_INPUT = 'binaryInput'


@unique
class TaskFlagArgs(Enum):
    SERIALIZE = 'serialize'
    SAVE_CSV = 'saveCSV'
    VISUALIZE = 'visualize'
    SAVE_CLUSTERS = 'saveClusters'
    CLUSTERING_RESULT = 'clusteringResult'

    @staticmethod
    def get_all_flags(script_arguments: Namespace):
        return {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize,
            TaskFlagArgs.SAVE_CSV: script_arguments.saveCSV,
            TaskFlagArgs.VISUALIZE: script_arguments.visualize,
            TaskFlagArgs.SAVE_CLUSTERS: script_arguments.saveClusters,
            TaskFlagArgs.CLUSTERING_RESULT: script_arguments.clusteringResult,
        }
