from typing import Any, Dict, Tuple

from src.utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from src.utils.models.script_parameters import ClusteringParameters
from src.utils.runners.gradle_task_runners.abstract_task_runner \
    import AbstractTaskRunner, get_common_named_arguments


class ClusteringRunner(AbstractTaskRunner):
    """'cluster' task runner."""

    task_name = 'cluster'

    def build_arguments(
            self,
            step_id: int,
            script_params: ClusteringParameters,
            **kwargs,
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for 'cluster' CLI."""
        named_args = get_common_named_arguments(
            step_id,
            script_params,
            'build_solutions_file_name',
            **kwargs,
        )
        named_args[TaskNamedArgs.DISTANCE_LIMIT] = script_params.distance_limit
        return named_args, TaskFlagArgs.get_all_flags(script_params)
