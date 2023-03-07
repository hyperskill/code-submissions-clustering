import json
import zipfile
from os.path import join
from pathlib import Path
from typing import Tuple, Dict, Any, List

import pandas as pd

from src.utils.file_utils import create_dir
from src.utils.runners.abstract_runner import AbstractRunner

RESULTS_FOLDER_NAME = 'results'
RESULT_ARCHIVE_NAME = 'result'
SUBMISSIONS_FOLDER_NAME = 'submissions'

language_to_extension = {
    'python3': 'py',
}


def get_clusters(step_id: int, threshold: float, output_dir: Path) -> List[List[int]]:
    """
    Parse clusters from JPlag results

    :param step_id: step id
    :param threshold: clustering threshold
    :param output_dir: directory storing all output files (including JPlag result)
    :return: list of all found clusters (as lists of submissions ids)
    """
    def cluster_members_to_submission_ids(members: List[str]) -> List[int]:
        return [int(Path(sub_file).stem) for sub_file in members]

    result_archive_path = join(
        output_dir,
        RESULTS_FOLDER_NAME,
        str(step_id),
        str(threshold),
        f'{RESULT_ARCHIVE_NAME}.zip',
    )
    with zipfile.ZipFile(result_archive_path) as z:
        with z.open('overview.json') as f:
            data = json.load(f)

    return [cluster_members_to_submission_ids(cl['members']) for cl in data['clusters']]


def write_clustering_result(
        clusters: List[List[int]],
        step_id: int,
        threshold: float,
        output_dir: Path,
):
    """
    Write clustering result in Code Submissions Clustering format

    :param step_id: step id
    :param threshold: clustering threshold
    :param clusters: list of all found clusters (as lists of submissions ids)
    :param output_dir: directory storing all output files
    """
    clustering_result_df = pd.DataFrame({
        'submission_id': [i for cl in clusters for i in cl],
        'cluster_id': [cl_i for cl_i, cl in enumerate(clusters) for _ in cl],
        'position': [m_i for cl in clusters for m_i, _ in enumerate(cl)],
    })

    clustering_result_path = join(
        output_dir,
        RESULTS_FOLDER_NAME,
        str(step_id),
        str(threshold),
        'clustering.csv.gz',
    )
    clustering_result_df.to_csv(clustering_result_path, compression='gzip', index=False)


def write_clusters(
        clusters: List[List[int]],
        step_id: int,
        threshold: float,
        output_dir: Path,
        language: str,
):
    submissions_path = join(output_dir, SUBMISSIONS_FOLDER_NAME, str(step_id))
    clusters_file = join(output_dir, RESULTS_FOLDER_NAME, str(step_id), str(threshold), 'clusters.txt')

    clusters_sep = '=' * 30
    solutions_sep = f'\n{"-" * 60}\n'

    with open(clusters_file, 'w') as f:
        for i, ids in enumerate(clusters):
            f.write(f'\n{clusters_sep} Cluster {i} {clusters_sep}\n\n')
            for sol_id in ids:
                sol_file = join(submissions_path, f'{sol_id}.{language_to_extension[language]}')
                with open(sol_file) as sol_f:
                    f.write(''.join(sol_f.readlines()))
                    f.write(solutions_sep)


class JPlagRunner(AbstractRunner):
    """JPlag runner"""

    def build_arguments(
            self,
            step_id: int,
            language: str,
            output_dir: Path,
            jar_dir: Path,
            threshold: float,
            *args,
            **kwargs,
    ) -> Tuple[Dict[str, Any], Dict[str, bool]]:
        submissions_path = join(output_dir, SUBMISSIONS_FOLDER_NAME, str(step_id))
        result_dir = join(
            output_dir,
            RESULTS_FOLDER_NAME,
            str(step_id),
            str(threshold),
            RESULT_ARCHIVE_NAME,
        )
        named_args = {
            'jarDir': jar_dir,
            'rootDir': submissions_path,
            '-l': language,
            '-n': -1,
            '-r': result_dir,
            '--cluster-alg': 'AGGLOMERATIVE',
            '--cluster-agglomerative-inter-cluster-similarity': 'MIN',
            '--cluster-agglomerative-threshold': threshold,
        }
        flag_args = {
            '--cluster-pp-none': True,
        }
        return named_args, flag_args

    def configure_cmd(
            self,
            named_args: Dict[str, Any],
            flag_args: Dict[str, bool],
    ) -> str:
        cmd = 'java -jar'
        for arg_name, arg_value in named_args.items():
            if not arg_name.startswith('-'):
                cmd = cmd + f' {arg_value}'
            else:
                cmd = cmd + f' {arg_name} {arg_value}'
        for arg_name, arg_value in flag_args.items():
            if arg_value:
                cmd = cmd + f' {arg_name}'
        return cmd

    def run(
            self,
            step_id: int,
            language: str,
            output_dir: Path,
            jar_dir: Path,
            threshold: float,
            *args,
            **kwargs,
    ) -> str:
        create_dir(join(output_dir, RESULTS_FOLDER_NAME, str(step_id), str(threshold)))
        stderr = AbstractRunner.run(self, step_id, language, output_dir, jar_dir, threshold)

        try:
            clusters = get_clusters(step_id, threshold, output_dir)
        except FileNotFoundError:
            raise RuntimeError('JPlag did not execute successfully: not enough valid submissions')

        write_clustering_result(clusters, step_id, threshold, output_dir)
        write_clusters(clusters, step_id, threshold, output_dir, language)
        return stderr
