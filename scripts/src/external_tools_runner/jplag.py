import os
import re
import sys
from argparse import ArgumentParser
from os.path import join
from pathlib import Path
from typing import List

import pandas as pd
import wget
from github import Github

from src.utils.file_utils import create_dir
from src.utils.logger_utils import set_logger
from src.utils.models.df_column_name import SubmissionColumns
from src.utils.runners.external_tools_runners.jplag_runner import SUBMISSIONS_FOLDER_NAME, \
    JPlagRunner, RESULTS_FOLDER_NAME
from src.utils.steps_processing_utils import process_steps

WORKING_DIR = Path(__file__).parent.parent.parent


def download_jplag(directory) -> str:
    """
    Download the latest JPlag release to directory

    :param directory: directory to store JPlag
    :return: path to JPlag jar
    """
    g = Github()
    repo = g.get_repo('jplag/jplag')
    url = repo.get_latest_release().assets[0].browser_download_url
    resp = wget.download(url, out=str(directory.absolute()), bar=None)
    return resp


def get_jplag_jar_dir(jplag_jar_directory_arg) -> str:
    """Get path to JPlag jar

    Check if JPlag jar directory passed to script arguments is valid
    and download the latest JPlag release and return new directory if not

    :param jplag_jar_directory_arg: JPlag jar directory passed to script arguments
    :return: actual JPlag jar directory
    """
    jplag_jar_regex = re.compile('jplag-[0-9.]+-jar-with-dependencies.jar$')

    if jplag_jar_directory_arg is not None \
            and jplag_jar_regex.search(jplag_jar_directory_arg) is not None \
            and os.path.exists(jplag_jar_directory_arg):
        return jplag_jar_directory_arg

    return download_jplag(WORKING_DIR)


def create_submission_files(submissions_csv_file_path: str, output_dir: str) -> List[int]:
    """
    Create code submissions files from .csv file in JPlag form

    :param submissions_csv_file_path: path to .csv file storing code submissions
    :param output_dir: directory storing all output files

    :return list of all step ids
    """
    submissions_df = pd.read_csv(submissions_csv_file_path)
    steps = submissions_df[SubmissionColumns.STEP_ID.value].unique()

    submissions_dir = join(output_dir, SUBMISSIONS_FOLDER_NAME)
    for step in steps:
        step_dir = join(submissions_dir, str(step))
        create_dir(step_dir)
        step_df = submissions_df[submissions_df[SubmissionColumns.STEP_ID.value] == step]
        for _, row in step_df.iterrows():
            sub_id = row[SubmissionColumns.ID.value]
            with open(join(step_dir, f'{sub_id}.py'), 'w') as f:
                f.write(row[SubmissionColumns.CODE.value])

    return steps


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('submissions_csv_file_path',
                        help='Path to .csv file storing all submissions')
    parser.add_argument('output_directory',
                        help='Directory to store all output files')
    parser.add_argument('language',
                        help='Language of passed submissions {python3}')
    parser.add_argument('--jplag_jar_directory',
                        help='Path to JPlag jar file')
    args = parser.parse_args()

    create_dir(args.output_directory)
    logger = set_logger(args.output_directory)

    try:
        jplag_jar_dir = get_jplag_jar_dir(args.jplag_jar_directory)
    except Exception as e:
        logger.error(e)
        logger.error('Could not locate existing JPlag jar or download the latest release of JPlag')
        sys.exit(1)

    print(jplag_jar_dir)
    step_ids = create_submission_files(args.submissions_csv_file_path, args.output_directory)

    create_dir(join(args.output_directory, RESULTS_FOLDER_NAME))
    jplag_runner = JPlagRunner()
    process_steps(
        step_ids, jplag_runner, logger,
        args.language,
        args.output_directory,
        jplag_jar_dir,
    )
