"""Load submissions graph script.

This script allows user to load submissions graph(s) from .csv file [input_file] with
submissions for specified step(s).
By default, script creates 2 files for every step:
* [output_path]/solutions/[step_id].csv containing all solutions for step [step_id]
* [output_path]/output/[step_id]/graph.txt containing string representation of built
submissions graph
Optional arguments:
* --serialize: additionally creates [output_path]/output/[step_id]/graph.bin containing
serialized submissions graph
* --saveCSV: additionally creates [output_path]/output/[step_id]/graph.csv containing
result of submissions unification
"""

import argparse
import sys
import time
from typing import List

from src.plugin_runner.utils import configure_parser
from src.utils.df_utils import read_df, write_df
from src.utils.file_utils import create_dir, get_absolute_path
from src.utils.logger_utils import set_logger
from src.utils.models.df_column_name import SubmissionColumns
from src.utils.runners.load_runner import LoadRunner
from src.utils.steps_processing_utils import process_steps
from src.utils.time_utils import time_to_str

SOLUTIONS_DIR_NAME = 'solutions'
OUTPUT_DIR_NAME = 'output'


def build_solutions_file_name(step_id: int, args: argparse.Namespace) -> str:
    """
    Build .csv file name to store submissions for step step_id.

    :param step_id: step id
    :param args: script arguments
    :return: built .csv file name
    """
    output_path = get_absolute_path(args.output_path)
    return f'{output_path}/{SOLUTIONS_DIR_NAME}/{step_id}.csv'


def build_output_dir_name(step_id: int, args: argparse.Namespace) -> str:
    """
    Build directory name to store submissions graph output files for step step_id.

    :param step_id: step id
    :param args: script arguments
    :return: built directory name
    """
    output_path = get_absolute_path(args.output_path)
    return f'{output_path}/{OUTPUT_DIR_NAME}/{step_id}'


def create_directories(output_path: str):
    """
    Create missing output directories.

    :param output_path: root output directory
    """
    absolute_path = get_absolute_path(output_path)
    solutions_path = f'{absolute_path}/{SOLUTIONS_DIR_NAME}'
    create_dir(solutions_path)
    output_dir = f'{absolute_path}/{OUTPUT_DIR_NAME}'
    create_dir(output_dir)


def parse_solutions(args: argparse.Namespace) -> List[int]:
    """
    Read input .csv file input_file and write submissions for every step in separate .csv file.

    :param args: script arguments
    :return: list of contained step ids
    """
    input_file = get_absolute_path(args.input_file)
    df_all_solutions = read_df(input_file)
    unique_steps = df_all_solutions[SubmissionColumns.STEP_ID.value].unique()
    for step in unique_steps:
        output_file = build_solutions_file_name(step, args)
        cur_df = df_all_solutions[df_all_solutions[SubmissionColumns.STEP_ID.value] == step]
        write_df(cur_df, output_file)
    return unique_steps


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument(
        'input_file',
        help='Input .csv file with code solutions for a set of steps',
    )
    configure_parser(parser)
    args = parser.parse_args()

    create_directories(get_absolute_path(args.output_path))
    logger = set_logger(get_absolute_path(args.output_path))
    total_execution_time = 0

    logger.info(f'Start parsing {get_absolute_path(args.input_file)}...')
    start = time.time()
    try:
        step_ids = parse_solutions(args)
    except Exception as e:
        logger.error(e)
        logger.info('Parsing is stopped due to above exception. Quiting program...')
        sys.exit(1)
    end = time.time()
    logger.info(f'Parsing finished in {time_to_str(end - start)}')
    total_execution_time += end - start

    task_runner = LoadRunner()

    process_steps(
        step_ids, task_runner, args, logger,
        build_solutions_file_name=build_solutions_file_name,
        build_output_dir_name=build_output_dir_name,
    )
