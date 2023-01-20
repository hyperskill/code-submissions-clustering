"""Distance calculation script.

This script allows user to modify already serialized submissions graph(s) stored in [input_path]
by calculating distances between every pair of submissions nodes and adding edges according to
calculated distances.
By default, script creates 1 file for every serialized graph:
* [output_path]/[step_id]/graph.txt containing string representation of modified submissions graph
Optional arguments:
* --serialize: additionally creates [output_path]/[step_id]/graph.bin containing new serialized
submissions graph
"""
from src.plugin_runner.utils import ScriptArgsParser
from src.utils.file_utils import create_dir, list_dirs
from src.utils.logger_utils import set_logger
from src.utils.models.script_arguments import CalculateDistScriptArguments
from src.utils.models.script_parameters import CalculateDistancesParameters
from src.utils.runners.calculate_dist_runner import CalculateDistRunner
from src.utils.steps_processing_utils import process_steps

SERIALIZATION_DIR = 'serialization'


def build_initial_graph_filename(step_id: int, params: CalculateDistancesParameters) -> str:
    """
    Build .bin file name storing input serialized submissions graph for step step_id.

    :param step_id: step id
    :param params: script arguments
    :return: .bin file name
    """
    return f'{params.input_path}/{step_id}/{SERIALIZATION_DIR}/graph.bin'


def build_output_dir_name(step_id: int, params: CalculateDistancesParameters) -> str:
    """
    Build directory name to store submissions graph output files for step step_id.

    :param step_id: step id
    :param params: script arguments
    :return: built directory name
    """
    return f'{params.output_path}/{step_id}'


if __name__ == '__main__':
    parser = ScriptArgsParser(CalculateDistScriptArguments())
    params = CalculateDistancesParameters(parser.parse_args())

    create_dir(params.output_path)
    logger = set_logger(params.output_path)

    step_ids = [int(d) for d in list_dirs(params.input_path)]
    task_runner = CalculateDistRunner()

    process_steps(
        step_ids, task_runner, params, logger,
        build_initial_graph_filename=build_initial_graph_filename,
        build_output_dir_name=build_output_dir_name,
    )
