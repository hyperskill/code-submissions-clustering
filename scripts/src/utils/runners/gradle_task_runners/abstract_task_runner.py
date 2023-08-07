import platform
from abc import abstractmethod
from pathlib import Path
from typing import Any, Dict, Tuple, List

from src.utils.models.cli_models import TaskFlagArgs, TaskNamedArgs
from src.utils.models.script_parameters import BaseRunnerParameters
from src.utils.runners.abstract_runner import AbstractRunner


def get_common_named_arguments(
        step_id: int,
        script_params: BaseRunnerParameters,
        input_file_name_in_kwargs: str,
        **kwargs,
) -> Dict[TaskNamedArgs, str]:
    named_args = {
        TaskNamedArgs.INPUT_FILE:
            kwargs[input_file_name_in_kwargs](step_id, script_params),
        TaskNamedArgs.OUTPUT_DIR:
            kwargs['build_output_dir_name'](step_id, script_params),
        TaskNamedArgs.LANGUAGE: script_params.language,
    }
    if script_params.binary_input is not None:
        named_args[TaskNamedArgs.BINARY_INPUT] = kwargs['build_binary_input_file_name'](
            step_id, script_params,
        )
    return named_args


class AbstractTaskRunner(AbstractRunner):
    """Abstract gradle task runner."""

    task_name: str

    # The root folder of the initial project
    WORKING_DIR = Path(__file__).parent.parent.parent.parent.parent.parent

    @abstractmethod
    def build_arguments(self, *args, **kwargs) \
            -> Tuple[Dict[TaskNamedArgs, Any], Dict[TaskFlagArgs, bool], List[TaskNamedArgs]]:
        """Build arguments for task CLI."""
        pass

    def configure_cmd(
            self,
            named_args: Dict[TaskNamedArgs, Any],
            flag_args: Dict[TaskFlagArgs, bool],
            positional_args: List[TaskNamedArgs] = None,
    ) -> List[str]:
        """Build command to execute."""
        if platform.system() == 'Windows':
            cmd = 'cmd /c gradlew.bat'.split()
        else:
            cmd = ['./gradlew']
        cmd += ['run']
        cmd_args = f'--args={self.task_name}'
        for arg_name in positional_args:
            cmd_args = cmd_args + f' {named_args[arg_name]}'
        for arg_name, arg_value in named_args.items():
            if arg_name in positional_args:
                continue
            cmd_args = cmd_args + f' --{arg_name.value}={str(arg_value)}'
        for arg_name, arg_value in flag_args.items():
            if arg_value:
                cmd_args = cmd_args + f' --{arg_name.value}'
        cmd = cmd + [cmd_args]
        return cmd
