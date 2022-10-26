from argparse import ArgumentParser

from utils.models.cli_models import TaskFlagArgs


def configure_parser(parser: ArgumentParser):
    parser.add_argument(
        'output_path',
        help='Output directory to store folders with output files',
    )
    parser.add_argument(
        'language',
        help='Programming language of code submissions',
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
