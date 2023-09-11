import json
import sys
import time
from argparse import ArgumentParser
from pathlib import Path

import grpc

from src.utils.logger_utils import set_logger
from src.utils.run_process_utils import run_in_subprocess

PROJECT_ROOT = Path(__file__).parent.parent.parent.parent
IJ_ROOT = PROJECT_ROOT / 'code-submissions-clustering-ij'
PATH_TO_DOCKERFILE = IJ_ROOT / 'Dockerfile'
PATH_TO_CONFIG = IJ_ROOT / 'src' / 'main' / 'resources' / 'server-config.json'
IMAGE_NAME = 'ij-server:latest'
BASE_PORT = 50051
TIMEOUT_SEC = 1


def start_server(port, language):
    cmd = [
        'docker',
        'run',
        '-d',
        '-p',
        f'{port}:{BASE_PORT}',
        '--rm',
        IMAGE_NAME,
        f'-Planguage={language}',
    ]
    run_in_subprocess(cmd, cwd=PROJECT_ROOT)


def grpc_server_on(port) -> bool:
    try:
        with grpc.insecure_channel(f'localhost:{port}') as channel:
            grpc.channel_ready_future(channel).result(timeout=TIMEOUT_SEC)
            return True
    except grpc.FutureTimeoutError:
        return False


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument('--config', help='Path to config file', default=PATH_TO_CONFIG)
    args = parser.parse_args()

    logger = set_logger()

    try:
        with open(args.config) as f:
            config = json.load(f)

        for s in config['servers']:
            logger.info(f"Starting docker container for IJ code server on port {s['port']}...")
            start_server(s['port'], s['language'])

        unavailable_ports = [s['port'] for s in config['servers']]
        it = 0

        while unavailable_ports:
            it += 1
            time.sleep(5)
            for port in unavailable_ports:
                if grpc_server_on(port):
                    logger.info(f'* IJ code server on port {port} is ready')
                    unavailable_ports = [p for p in unavailable_ports if p != port]
                else:
                    if it % 6 == 0:
                        logger.info(f'IJ code server on port {port} is not started yet, waiting...')

        logger.info('All IJ code servers are ready')

    except Exception as e:
        logger.error(e)
        logger.info('Process is stopped due to above exception. Quiting program...')
        sys.exit(1)
