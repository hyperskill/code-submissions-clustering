from argparse import ArgumentParser

from src.utils.models.cli_models import TaskFlagArgs, TaskNamedArgs


def configure_parser(parser: ArgumentParser):
    parser.add_argument(
        TaskNamedArgs.OUTPUT_PATH.value,
        help='Output directory to store folders with output files',
    )
    parser.add_argument(
        TaskNamedArgs.LANGUAGE.value,
        help='Programming language of code submissions',
    )
    parser.add_argument(
        f'--{TaskNamedArgs.BINARY_INPUT.value}',
        help='Directory storing previously serialized graph',
    )
    parser.add_argument(
        f'--{TaskFlagArgs.SERIALIZE.value}', action='store_true',
        help='Save submissions graph to binary file',
    )
    parser.add_argument(
        f'--{TaskFlagArgs.SAVE_CSV.value}', action='store_true',
        help='Save unified solutions to .csv file',
    )
    parser.add_argument(
        f'--{TaskFlagArgs.VISUALIZE.value}', action='store_true',
        help='Visualize the solution graph into png format (can work slow)',
    )
    parser.add_argument(
        f'--{TaskFlagArgs.SAVE_CLUSTERS.value}', action='store_true',
        help='Save clusters to .txt file',
    )
    parser.add_argument(
        f'--{TaskFlagArgs.CLUSTERING_RESULT.value}', action='store_true',
        help='Save the result of clustering to .csv.gz file',
    )