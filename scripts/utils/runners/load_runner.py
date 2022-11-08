from argparse import Namespace
from typing import Any, Dict, Tuple

from utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from utils.runners.abstract_task_runner import AbstractTaskRunner, get_common_named_arguments


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
        named_args = get_common_named_arguments(step_id, script_arguments, **kwargs)
        return named_args, TaskFlagArgs.get_all_flags(script_arguments)
