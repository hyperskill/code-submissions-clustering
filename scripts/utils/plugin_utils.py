import subprocess
from abc import ABC, abstractmethod
from argparse import Namespace
from enum import Enum, unique
from typing import Any, Dict, Tuple


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'input'
    OUTPUT_PATH = 'output'
    LANGUAGE = 'lang'


@unique
class TaskFlagArgs(Enum):
    SERIALIZE = 'serialize'
    SAVE_CSV = 'saveCSV'


class AbstractTaskRunner(ABC):
    """Abstract gradle task runner."""

    task_name: str
    task_prefix = ':code-submissions-clustering-plugin:'

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
        completed_process = subprocess.run(cmd.split(), capture_output=True)
        stderr_output = completed_process.stderr
        if stderr_output is None:
            return ""
        return stderr_output.decode('utf-8')


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
        flag_args = {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize,
            TaskFlagArgs.SAVE_CSV: script_arguments.saveCSV,
        }
        return named_args, flag_args
