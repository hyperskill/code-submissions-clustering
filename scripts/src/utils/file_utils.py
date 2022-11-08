import os
from pathlib import Path
from typing import List, Optional


def create_dir(path: str):
    if not os.path.exists(path):
        os.makedirs(path)


def list_dirs(path: str) -> List[str]:
    dirs = []
    for file in os.listdir(path):
        path_to_file = os.path.join(path, file)
        if os.path.isdir(path_to_file):
            dirs.append(file)
    return dirs


def get_absolute_path(path: Optional[str]) -> Optional[str]:
    if path is None:
        return None
    return str(Path(path).resolve())
