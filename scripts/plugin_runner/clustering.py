"""Cluster code submissions script.

This script allows user to cluster code submissions for multiple steps from directory [csv_dir]
containing .csv files with submissions for specified steps. Distance limit between resulting
clusters is configured by 3 arguments: [min_distance_limit], [max_distance_limit] and
[step_distance_limit].

If submissions graphs for specified steps have been already loaded and serialized, the resulting
serialization can be provided with [preprocess_dir argument]. [preprocess_dir] must be a directory
containing serialization for all steps in [csv_dir] locating in the following path:
[preprocess_dir]/[step_id]/serialization.

By default, script creates a single file for every step:
* [output_path]/[step_id]/[distance_limit]/txt/graph.txt containing string representation of built
submissions graph

Optional arguments:
* --serialize: create [output_path]/[step_id]/[distance_limit]/serialization folder containing
serialized submissions graph and its serialized clustered structure
* --saveCSV: create [output_path]/[step_id]/[distance_limit]/graph.csv containing
result of submissions unification
* --visualize: create [output_path]/[step_id]/[distance_limit]/visualization folder containing
submissions graph and its clustered structure visualization
* --saveClusters: create [output_path]/[step_id]/[distance_limit]/txt/clusters.txt containing string
representation of resulting clustering for unified code submissions
* --clusteringResult: create [output_path]/[step_id]/[distance_limit]/clustering.csv.gz archive
containing single .csv file with resulting clustering for initial code submissions
"""

import argparse
import os.path

from plugin_runner.utils import configure_parser
from utils.file_utils import create_dir, list_files
from utils.logger_utils import set_logger
from utils.runners.clustering_runner import ClusteringRunner
from utils.steps_processing_utils import process_steps

SERIALIZATION_DIR = 'serialization'


def build_solutions_file_name(step_id: int, args: argparse.Namespace) -> str:
    """
    Build .csv file name storing submissions for step step_id.

    :param step_id: step id
    :param args: script arguments
    :return: built .csv file name
    """
    return f'{args.csv_dir}/{step_id}.csv'


def build_output_dir_name(step_id: int, args: argparse.Namespace) -> str:
    """
    Build directory name to store submissions graph output files for step step_id.

    :param step_id: step id
    :param args: script arguments
    :return: built directory name
    """
    return f'{args.output_path}/{step_id}/{args.distance_limit}'


def build_binary_input_file_name(step_id: int, args: argparse.Namespace) -> str:
    """
    Build directory name containing submissions graph serialization for step step_id.

    :param step_id: step id
    :param args: script arguments
    :return: built directory name
    """
    return f'{args.preprocess_dir}/{step_id}/{SERIALIZATION_DIR}'


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument(
        'csv_dir',
        help='Input directory with .csv files containing code submissions',
    )
    parser.add_argument(
        '--preprocess_dir',
        help='Input directory with preprocessed data',
    )
    parser.add_argument(
        'min_distance_limit',
        help='Min distance limit',
        type=int,
    )
    parser.add_argument(
        'max_distance_limit',
        help='Max distance limit',
        type=int,
    )
    parser.add_argument(
        'step_distance_limit',
        help='Distance limit step',
        type=int,
    )

    configure_parser(parser)
    args = parser.parse_args()

    create_dir(args.output_path)
    logger = set_logger(args.output_path)

    step_ids = [int(os.path.splitext(f)[0]) for f in list_files(args.csv_dir)]
    for step_id in step_ids:
        create_dir(f'{args.output_path}/{step_id}')

    task_runner = ClusteringRunner()

    for dl in range(args.min_distance_limit, args.max_distance_limit, args.step_distance_limit):
        args.distance_limit = dl
        logger.info(f'Clustering code submissions with distance limit: {dl}')
        process_steps(
            step_ids, task_runner, args, logger,
            build_solutions_file_name=build_solutions_file_name,
            build_output_dir_name=build_output_dir_name,
            build_binary_input_file_name=build_binary_input_file_name,
        )
