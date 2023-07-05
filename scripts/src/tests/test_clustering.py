import sys

from src.tests import SCRIPTS_ROOT_FOLDER, RESOURCES_FOLDER, TMP_FOLDER
from src.utils.run_process_utils import run_in_subprocess


def test_incorrect_arguments():
    cmd = [
        sys.executable,
        '-m',
        'src.plugin_runner.clustering',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code != 0
    assert stderr is not None
    assert stderr != ''


def test_correct_arguments_required():
    cmd = [
        sys.executable,
        '-m',
        'src.plugin_runner.clustering',
        str(RESOURCES_FOLDER / 'solutions'),
        '10',
        '100',
        '20',
        str(TMP_FOLDER),
        'PYTHON',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code == 0
    assert stderr is None


def test_correct_arguments_optional():
    cmd = [
        sys.executable,
        '-m',
        'src.plugin_runner.clustering',
        str(RESOURCES_FOLDER / 'solutions'),
        '10',
        '100',
        '20',
        str(TMP_FOLDER),
        'PYTHON',
        '--binaryInput=' + str(RESOURCES_FOLDER / 'output'),
        '--serialize',
        '--saveCSV',
        '--visualize',
        '--saveClusters',
        '--clusteringResult',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code == 0
    assert stderr is None
