import logging
import os
import sys


def set_logger(output_path: str) -> logging.Logger:
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    log_file = f"{output_path}/log.txt"
    if not os.path.exists(log_file):
        os.mknod(log_file)

    output_file_handler = logging.FileHandler(log_file)
    stdout_handler = logging.StreamHandler(sys.stdout)
    logger.addHandler(output_file_handler)
    logger.addHandler(stdout_handler)

    return logger
