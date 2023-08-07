from typing import Any, Dict, Tuple, List

from src.utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from src.utils.models.script_parameters import CalculateDistancesParameters
from src.utils.runners.gradle_task_runners.abstract_task_runner \
    import AbstractTaskRunner, get_common_named_arguments


class CalculateDistRunner(AbstractTaskRunner):
    """'calculate-dist' task runner."""

    task_name = 'calculate-dist'

    def build_arguments(
            self,
            step_id: int,
            script_params: CalculateDistancesParameters,
            **kwargs,
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool], List[TaskNamedArgs]]:
        """Build arguments for 'calculate-dist' CLI."""
        named_args = get_common_named_arguments(
            step_id,
            script_params,
            'build_initial_graph_filename',
            **kwargs,
        )
        return named_args, TaskFlagArgs.get_all_flags(script_params), [TaskNamedArgs.INPUT_FILE]
