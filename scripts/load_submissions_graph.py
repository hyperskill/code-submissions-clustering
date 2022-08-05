import argparse
import logging
import os
import subprocess
import sys
import time
from typing import List

import pandas as pd

SOLUTIONS_DIR_NAME = "solutions"
OUTPUT_DIR_NAME = "output"


def build_solutions_file_name(step_id: int, output_path: str) -> str:
    return f"{output_path}/{SOLUTIONS_DIR_NAME}/{step_id}.csv"


def build_output_dir_name(step_id: int, output_path: str) -> str:
    return f"{output_path}/{OUTPUT_DIR_NAME}/{step_id}"


def configure_load_cmd(input_file: str, output_dir: str, serialize: bool, save_csv: bool) -> str:
    cmd = f"./gradlew :code-submissions-clustering-plugin:load -Pinput={input_file} -Poutput={output_dir}"
    if serialize:
        cmd = cmd + " -Pserialize"
    if save_csv:
        cmd = cmd + " -PsaveCSV"
    return cmd


def parse_solutions(input_file: str, output_path: str) -> List[int]:
    df_all_solutions = pd.read_csv(input_file)
    unique_steps = df_all_solutions['step_id'].unique()
    for step in unique_steps:
        output_file = build_solutions_file_name(step, output_path)
        df_all_solutions[df_all_solutions['step_id'] == step].to_csv(output_file, index=False)
    return unique_steps


def run_submissions_graph_load(step_id: int, output_path: str, serialize: bool, save_csv: bool):
    input_file = build_solutions_file_name(step_id, output_path)
    output_dir = build_output_dir_name(step_id, output_path)
    cmd = configure_load_cmd(input_file, output_dir, serialize, save_csv)
    subprocess.run(cmd.split(), stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


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


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_file", help="Input .csv file with code solutions for a set of steps")
    parser.add_argument("output_path", help="Output directory to store folders with output files")
    parser.add_argument("--serialize", action="store_true", help="Save submissions graph to binary file")
    parser.add_argument("--saveCSV", action="store_true", help="Save unified solutions to .csv file")

    args = parser.parse_args()

    # Create directories
    if not os.path.exists(args.output_path):
        os.makedirs(args.output_path)
    solutions_path = f"{args.output_path}/{SOLUTIONS_DIR_NAME}"
    if not os.path.exists(solutions_path):
        os.makedirs(solutions_path)
    output_dir = f"{args.output_path}/{OUTPUT_DIR_NAME}"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    logger = set_logger(args.output_path)
    total_execution_time = 0

    logger.info(f"Start parsing {args.input_file}...")
    start = time.time()
    try:
        step_ids = parse_solutions(args.input_file, args.output_path)
    except Exception as e:
        logger.error(e)
        logger.info("Parsing is stopped due to above exception. Quiting program...")
        sys.exit(1)
    end = time.time()
    logger.info(f"Parsing finished in {time.strftime('%Hh %Mm %Ss', time.gmtime(end - start))}")
    total_execution_time += end - start

    for i, step_id in enumerate(step_ids):
        logger.info(f"Progress status: {i}/{len(step_ids)}")
        logger.info(f"Operating step {step_id}...")
        start = time.time()
        try:
            run_submissions_graph_load(step_id, args.output_path, args.serialize, args.saveCSV)
        except Exception as e:
            logger.error(e)
            logger.info(f"Operating step {step_id} is stopped due to above exception")
        else:
            end = time.time()
            logger.info(f"Step {step_id} operated in {time.strftime('%Hh %Mm %Ss', time.gmtime(end - start))}")
            total_execution_time += end - start
    logger.info(f"All steps operated. "
                f"Loading is finished in {time.strftime('%Hh %Mm %Ss', time.gmtime(total_execution_time))}")
