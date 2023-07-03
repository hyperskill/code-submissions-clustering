import logging
import sys
from os.path import exists, join
from pathlib import Path


def set_logger(output_path: str) -> logging.Logger:
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    log_file = join(output_path, 'log.txt')
    if not exists(log_file):
        Path(log_file).touch()

    output_file_handler = logging.FileHandler(log_file)
    stdout_handler = logging.StreamHandler(sys.stdout)
    logger.addHandler(output_file_handler)
    logger.addHandler(stdout_handler)

    return logger
