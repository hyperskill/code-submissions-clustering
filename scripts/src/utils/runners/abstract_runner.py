from abc import ABC, abstractmethod
from pathlib import Path
from typing import Tuple, Dict, Any

from src.utils.run_process_utils import run_in_subprocess


class AbstractRunner(ABC):
    """Abstract runner."""

    WORKING_DIR = Path(__file__).parent.parent.parent.parent

    @abstractmethod
    def build_arguments(self, *args, **kwargs) \
            -> Tuple[Dict[Any, Any], Dict[Any, bool]]:
        """Build arguments for CLI."""
        pass

    @abstractmethod
    def configure_cmd(
            self,
            named_args: Dict[Any, Any],
            flag_args: Dict[Any, bool],
    ) -> str:
        """Build command to execute."""
        pass

    def run(self, *args, **kwargs) -> str:
        """Run process and return its stderr."""
        named_args, flag_args = self.build_arguments(*args, **kwargs)
        cmd = self.configure_cmd(named_args, flag_args)
        return_code, stdout, stderr = run_in_subprocess(cmd.split(), self.WORKING_DIR)
        if return_code != 0:
            return ''
        return stderr
