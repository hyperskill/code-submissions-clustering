from src.external_tools_runner.jplag import download_jplag
from src.tests import SCRIPTS_ROOT_FOLDER, RESOURCES_FOLDER, TMP_FOLDER
from src.utils.file_utils import list_files
from src.utils.run_process_utils import run_in_subprocess


def test_incorrect_arguments():
    cmd = [
        'python3',
        '-m',
        'src.external_tools_runner.jplag',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code != 0
    assert stderr is not None
    assert stderr != ''


def test_correct_arguments_required():
    cmd = [
        'python3',
        '-m',
        'src.external_tools_runner.jplag',
        str(RESOURCES_FOLDER / 'solutions' / '1000.csv'),
        str(TMP_FOLDER),
        'PYTHON',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code == 0
    assert stderr is None
    assert [file for file in list_files(SCRIPTS_ROOT_FOLDER) if file.endswith('.jar')]


def test_correct_arguments_optional():
    cmd = [
        'python3',
        '-m',
        'src.external_tools_runner.jplag',
        str(RESOURCES_FOLDER / 'solutions' / '1000.csv'),
        str(TMP_FOLDER),
        'PYTHON',
        '--threshold_min=0.2',
        '--threshold_max=0.25',
        '--threshold_step=0.1',
        f'--jplag_jar_directory={download_jplag(TMP_FOLDER)}',
    ]

    return_code, _, stderr = run_in_subprocess(cmd, cwd=SCRIPTS_ROOT_FOLDER)

    assert return_code == 0
    assert stderr is None
