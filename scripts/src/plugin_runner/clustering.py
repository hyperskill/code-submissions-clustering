"""Cluster code submissions script.

This script allows user to cluster code submissions for multiple steps from directory [csv_dir]
containing .csv files with submissions for specified steps. With a single run of the script multiple
distance limits might be used for clustering. Distance limit between resulting clusters is
configured by 3 arguments: [min_distance_limit], [max_distance_limit] and [step_distance_limit].

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

import os.path

from src.plugin_runner.utils import ScriptArgsParser
from src.utils.file_utils import create_dir, list_files
from src.utils.logger_utils import set_logger
from src.utils.models.script_arguments import ClusterScriptArguments
from src.utils.models.script_parameters import ClusteringParameters
from src.utils.runners.gradle_task_runners.clustering_runner import ClusteringRunner
from src.utils.steps_processing_utils import process_steps

SERIALIZATION_DIR = 'serialization'


def build_solutions_file_name(step_id: int, params: ClusteringParameters) -> str:
    """
    Build .csv file name storing submissions for step step_id.

    :param step_id: step id
    :param params: script arguments
    :return: built .csv file name
    """
    return f'{params.csv_dir}/{step_id}.csv'


def build_output_dir_name(step_id: int, params: ClusteringParameters) -> str:
    """
    Build directory name to store submissions graph output files for step step_id.

    :param step_id: step id
    :param params: script arguments
    :return: built directory name
    """
    return f'{params.output_path}/{step_id}/{params.distance_limit}'


def build_binary_input_file_name(step_id: int, params: ClusteringParameters) -> str:
    """
    Build directory name containing submissions graph serialization for step step_id.

    :param step_id: step id
    :param params: script arguments
    :return: built directory name
    """
    return f'{params.binary_input}/{step_id}/{SERIALIZATION_DIR}'


if __name__ == '__main__':
    parser = ScriptArgsParser(ClusterScriptArguments())
    args = parser.parse_args()

    params = ClusteringParameters.from_args(args)

    create_dir(params.output_path)
    logger = set_logger(params.output_path)

    step_ids = [int(os.path.splitext(f)[0]) for f in list_files(params.csv_dir)]
    for step_id in step_ids:
        create_dir(f'{params.output_path}/{step_id}')

    task_runner = ClusteringRunner()

    for dl in range(
            params.min_distance_limit,
            params.max_distance_limit,
            params.step_distance_limit,
    ):
        params = ClusteringParameters.from_args(args, distance_limit=dl)
        logger.info(f'Clustering code submissions with distance limit: {dl}')
        process_steps(
            step_ids, task_runner, logger, params,
            build_solutions_file_name=build_solutions_file_name,
            build_output_dir_name=build_output_dir_name,
            build_binary_input_file_name=build_binary_input_file_name,
        )
