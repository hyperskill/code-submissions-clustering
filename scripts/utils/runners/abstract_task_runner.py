from abc import ABC, abstractmethod
from pathlib import Path
from typing import Any, Dict, Tuple

from utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from utils.run_process_utils import run_in_subprocess


class AbstractTaskRunner(ABC):
    """Abstract gradle task runner."""

    task_name: str
    task_prefix = ':code-submissions-clustering-plugin:'

    # The root folder of the initial project
    PROJECT_DIR = Path(__file__).parent.parent.parent.parent

    @abstractmethod
    def build_arguments(self, *args, **kwargs) \
            -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for task CLI."""
        pass

    def configure_cmd(
            self,
            named_args: Dict[TaskNamedArgs, Any],
            flag_args: Dict[TaskFlagArgs, bool],
    ) -> str:
        """Build command to execute."""
        cmd = f'./gradlew {self.task_prefix}{self.task_name}'
        for arg_name, arg_value in named_args.items():
            cmd = cmd + f' -P{arg_name.value}={str(arg_value)} '
        for arg_name, arg_value in flag_args.items():
            if arg_value:
                cmd = cmd + f' -P{arg_name.value}'
        return cmd

    def run(self, *args, **kwargs) -> str:
        """Run task and return process stderr."""
        named_args, flag_args = self.build_arguments(*args, **kwargs)
        cmd = self.configure_cmd(named_args, flag_args)

        return_code, stdout = run_in_subprocess(cmd.split(), self.PROJECT_DIR)
        if return_code != 0:
            return ''
        return stdout
