from enum import Enum, unique

from src.utils.models.cli_arguments import ClusteringArguments


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'inputFile'
    OUTPUT_DIR = 'outputDir'
    OUTPUT_PATH = 'output_path'
    LANGUAGE = 'language'
    BINARY_INPUT = 'binaryInput'
    DISTANCE_LIMIT = 'distanceLimit'


@unique
class TaskFlagArgs(Enum):
    SERIALIZE = 'serialize'
    SAVE_CSV = 'saveCSV'
    VISUALIZE = 'visualize'
    SAVE_CLUSTERS = 'saveClusters'
    CLUSTERING_RESULT = 'clusteringResult'

    @staticmethod
    def get_all_flags(script_arguments: ClusteringArguments):
        return {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize,
            TaskFlagArgs.SAVE_CSV: script_arguments.save_csv,
            TaskFlagArgs.VISUALIZE: script_arguments.visualize,
            TaskFlagArgs.SAVE_CLUSTERS: script_arguments.save_clusters,
            TaskFlagArgs.CLUSTERING_RESULT: script_arguments.clustering_result,
        }
