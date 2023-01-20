from enum import Enum, unique

from src.utils.models.script_parameters import BaseRunnerParameters


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'inputFile'
    OUTPUT_DIR = 'outputDir'
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
    def get_all_flags(script_params: BaseRunnerParameters):
        return {
            TaskFlagArgs.SERIALIZE: script_params.serialize,
            TaskFlagArgs.SAVE_CSV: script_params.save_csv,
            TaskFlagArgs.VISUALIZE: script_params.visualize,
            TaskFlagArgs.SAVE_CLUSTERS: script_params.save_clusters,
            TaskFlagArgs.CLUSTERING_RESULT: script_params.clustering_result,
        }
