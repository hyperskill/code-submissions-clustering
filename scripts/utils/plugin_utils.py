import subprocess
from abc import ABC, abstractmethod
from enum import Enum, unique
from typing import Any, Dict, Tuple
from argparse import Namespace


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'input'
    OUTPUT_PATH = 'output'


@unique
class TaskFlagArgs(Enum):
    SERIALIZE = 'serialize'
    SAVE_CSV = 'saveCSV'


class AbstractTaskRunner(ABC):
    """Abstract gradle task runner"""

    task_name: str
    task_prefix = ':code-submissions-clustering-plugin:'

    @abstractmethod
    def build_arguments(self, *args, **kwargs) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        """Build arguments for task CLI"""
        pass

    def configure_cmd(self, named_args: Dict[TaskNamedArgs, Any], flag_args: Dict[TaskFlagArgs, bool]) -> str:
        """Build command to execute"""
        cmd = f'./gradlew {self.task_prefix}{self.task_name}'
        for arg_name, arg_value in named_args.items():
            cmd = cmd + f' -P{arg_name.value}={str(arg_value)} '
        for arg_name, arg_value in flag_args.items():
            if arg_value:
                cmd = cmd + f' -P{arg_name.value}'
        return cmd

    def run(self, *args, **kwargs):
        """Run task"""
        named_args, flag_args = self.build_arguments(*args, **kwargs)
        cmd = self.configure_cmd(named_args, flag_args)
        subprocess.run(cmd.split(), stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


class LoadRunner(AbstractTaskRunner):
    """'load' task runner"""

    task_name = 'load'

    def build_arguments(
            self,
            step_id: int,
            script_arguments: Namespace,
            **kwargs
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        named_args = {
            TaskNamedArgs.INPUT_FILE: kwargs['build_solutions_file_name'](step_id, script_arguments),
            TaskNamedArgs.OUTPUT_PATH: kwargs['build_output_dir_name'](step_id, script_arguments),
        }
        flag_args = {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize,
            TaskFlagArgs.SAVE_CSV: script_arguments.saveCSV,
        }
        return named_args, flag_args


class CalculateDistRunner(AbstractTaskRunner):
    """'calculate-dist' task runner"""

    task_name = 'calculate-dist'

    def build_arguments(
            self,
            step_id,
            script_arguments,
            **kwargs
    ) -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool]]:
        named_args = {
            TaskNamedArgs.INPUT_FILE: kwargs['build_initial_graph_filename'](step_id, script_arguments),
            TaskNamedArgs.OUTPUT_PATH: kwargs['build_output_dir_name'](step_id, script_arguments),
        }
        flag_args = {
            TaskFlagArgs.SERIALIZE: script_arguments.serialize
        }
        return named_args, flag_args
