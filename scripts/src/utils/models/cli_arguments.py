import argparse
from dataclasses import dataclass

from src.utils.file_utils import get_absolute_path


@dataclass
class ClusteringArguments:
    def __init__(self, args: argparse.Namespace):
        self.input_file = get_absolute_path(args.input_file)
        self.output_path = get_absolute_path(args.output_path)
        self.language = args.language
        self.distance_limit = args.distanceLimit
        self.binary_input = get_absolute_path(args.binaryInput)
        self.save_csv = args.saveCSV
        self.serialize = args.serialize
        self.visualize = args.visualize
        self.save_clusters = args.saveClusters
        self.clustering_result = args.clusteringResult
