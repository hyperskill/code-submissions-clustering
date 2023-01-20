from argparse import Namespace
from dataclasses import dataclass

from src.utils.file_utils import get_absolute_path


@dataclass
class BaseRunnerParameters:
    def __init__(self, args: Namespace):
        # Required arguments
        self.output_path = get_absolute_path(args.output_path)
        self.language = args.language
        # Optional arguments
        self.binary_input = get_absolute_path(args.binaryInput)
        self.serialize = args.serialize
        self.save_csv = args.saveCSV
        self.visualize = args.visualize
        self.save_clusters = args.saveClusters
        self.clustering_result = args.clusteringResult


@dataclass
class LoadSubmissionsGraphParameters(BaseRunnerParameters):
    def __init__(self, args: Namespace):
        super().__init__(args)
        self.input_file = get_absolute_path(args.input_file)


@dataclass
class CalculateDistancesParameters(BaseRunnerParameters):
    def __init__(self, args: Namespace):
        super().__init__(args)
        self.input_path = get_absolute_path(args.input_path)


@dataclass
class ClusteringParameters(BaseRunnerParameters):
    def __init__(self, args: Namespace):
        super().__init__(args)
        self.csv_dir = args.csv_dir
        self.min_distance_limit = int(args.min_distance_limit)
        self.max_distance_limit = int(args.max_distance_limit)
        self.step_distance_limit = int(args.step_distance_limit)

        # Distance limit in range(min_distance_limit, max_distance_limit, step_distance_limit)
        # This parameter is not parsed from script arguments directly but is used to
        # iterate over distance limits from min_distance_limit to max_distance_limit
        self.distance_limit = self.min_distance_limit
