import time
from logging import Logger
from typing import List

from src.utils.runners.abstract_runner import AbstractRunner
from src.utils.time_utils import time_to_str


def process_steps(
        step_ids: List[int],
        task_runner: AbstractRunner,
        logger: Logger,
        *args,
        **kwargs,
):
    """Run task_runner for every step from step_ids using arguments from script_arguments.

    :param step_ids: list of step ids to operate
    :param task_runner: runs specified gradle task
    :param script_params: arguments passed to initial python script
    :param logger: logger
    """
    total_execution_time = 0
    for i, step_id in enumerate(step_ids):
        logger.info(f'Progress status: {i}/{len(step_ids)}')
        logger.info(f'Operating step {step_id}...')
        start = time.time()
        try:
            stderr_output = task_runner.run(step_id, *args, **kwargs)
        except Exception as e:
            logger.error(e)
            logger.info(f'Operating step {step_id} is stopped due to above exception')
        else:
            end = time.time()
            logger.info(stderr_output)
            logger.info(f'Step {step_id} operated in {time_to_str(end - start)}')
            total_execution_time += end - start
    logger.info(f'All steps operated in {time_to_str(total_execution_time)}')
