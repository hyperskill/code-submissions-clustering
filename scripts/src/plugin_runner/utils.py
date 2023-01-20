from argparse import ArgumentParser, Namespace

from src.utils.models.script_arguments import ScriptArgument


class ScriptArgsParser:
    def __init__(self, args_enum):
        self._parser = ArgumentParser()
        for arg in args_enum:
            self.add_argument(arg)

    def add_argument(self, arg: ScriptArgument):
        name = arg.name if not arg.is_optional else f'--{arg.name}'
        if arg.is_flag:

            self._parser.add_argument(name, help=arg.help_text, action='store_true')
        else:
            self._parser.add_argument(name, help=arg.help_text, type=arg.data_type)

    def parse_args(self) -> Namespace:
        return self._parser.parse_args()
