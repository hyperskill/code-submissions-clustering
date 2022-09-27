from enum import Enum, unique


@unique
class TaskNamedArgs(Enum):
    INPUT_FILE = 'input'
    OUTPUT_PATH = 'output'
    LANGUAGE = 'lang'


@unique
class TaskFlagArgs(Enum):
    SERIALIZE = 'serialize'
    SAVE_CSV = 'saveCSV'
