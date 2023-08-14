import logging
import sys
from os.path import exists, join
from pathlib import Path


def set_logger(output_path: str = None) -> logging.Logger:
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    stdout_handler = logging.StreamHandler(sys.stdout)
    logger.addHandler(stdout_handler)

    if output_path:
        log_file = join(output_path, 'log.txt')
        if not exists(log_file):
            Path(log_file).touch()
        output_file_handler = logging.FileHandler(log_file)
        logger.addHandler(output_file_handler)

    return logger
