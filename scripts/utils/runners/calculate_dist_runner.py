from typing import Any, Dict, Tuple

from utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from utils.runners.abstract_task_runner import AbstractTaskRunner


class CalculateDistRunner(AbstractTaskRunner):
    """'calculate-dist' task runner."""

    task_name = 'calculate-dist'

    def build_arguments(
            self,
            step_id,
            script_arguments,
            **kwargs,
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for 'calculate-dist' CLI."""
        named_args = {
            TaskNamedArgs.INPUT_FILE:
                kwargs['build_initial_graph_filename'](step_id, script_arguments),
            TaskNamedArgs.OUTPUT_PATH:
                kwargs['build_output_dir_name'](step_id, script_arguments),
            TaskNamedArgs.LANGUAGE: script_arguments.language,
        }
        return named_args, TaskFlagArgs.get_all_flags(script_arguments)
