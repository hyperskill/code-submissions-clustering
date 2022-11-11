from argparse import Namespace
from typing import Any, Dict, Tuple

from utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from utils.runners.abstract_task_runner import AbstractTaskRunner


class ClusteringRunner(AbstractTaskRunner):
    """'cluster' task runner."""

    task_name = 'cluster'

    def build_arguments(
            self,
            step_id: int,
            script_arguments: Namespace,
            **kwargs,
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for 'cluster' CLI."""
        named_args = {
            TaskNamedArgs.INPUT_FILE:
                kwargs['build_solutions_file_name'](step_id, script_arguments),
            TaskNamedArgs.OUTPUT_PATH:
                kwargs['build_output_dir_name'](step_id, script_arguments),
            TaskNamedArgs.LANGUAGE: script_arguments.language,
            TaskNamedArgs.DISTANCE_LIMIT: script_arguments.distance_limit,
        }
        if script_arguments.preprocess_dir is not None:
            named_args[TaskNamedArgs.BINARY_INPUT] = kwargs['build_binary_input_file_name'](
                step_id, script_arguments,
            )
        return named_args, TaskFlagArgs.get_all_flags(script_arguments)
