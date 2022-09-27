from argparse import Namespace
from typing import Any, Dict, Tuple

from utils.models.cli_models import TaskNamedArgs, TaskFlagArgs
from utils.runners.abstract_task_runner import AbstractTaskRunner


class LoadRunner(AbstractTaskRunner):
    """'load' task runner."""

    task_name = 'load'

    def build_arguments(
            self,
            step_id: int,
            script_arguments: Namespace,
            **kwargs,
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for 'load' CLI."""
        named_args = {
            TaskNamedArgs.INPUT_FILE:
                kwargs['build_solutions_file_name'](step_id, script_arguments),
            TaskNamedArgs.OUTPUT_PATH:
                kwargs['build_output_dir_name'](step_id, script_arguments),
            TaskNamedArgs.LANGUAGE: script_arguments.language,
        }
        flag_args = {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize,
            TaskFlagArgs.SAVE_CSV: script_arguments.saveCSV,
        }
        return named_args, flag_args