from argparse import Namespace
from dataclasses import dataclass
from typing import Optional, Any, Dict

from src.utils.file_utils import get_absolute_path


@dataclass(frozen=True)
class BaseRunnerParameters:
    # Required arguments
    output_path: str
    language: str
    # Optional arguments
    binary_input: Optional[str]
    serialize: bool
    save_csv: bool
    visualize: bool
    save_clusters: bool
    clustering_result: bool

    @classmethod
    def _get_args(cls, args: Namespace) -> Dict[str, Any]:
        return {
            'output_path': get_absolute_path(args.output_path),
            'language': args.language,
            'binary_input': get_absolute_path(args.binaryInput),
            'serialize': args.serialize,
            'save_csv': args.saveCSV,
            'visualize': args.visualize,
            'save_clusters': args.saveClusters,
            'clustering_result': args.clusteringResult,
        }

    @classmethod
    def from_args(cls, args: Namespace):
        return cls(**cls._get_args(args))


@dataclass(frozen=True)
class LoadSubmissionsGraphParameters(BaseRunnerParameters):
    input_file: str

    @classmethod
    def _get_args(cls, args: Namespace) -> Dict[str, Any]:
        args_dict = BaseRunnerParameters._get_args(args)
        args_dict['input_file'] = get_absolute_path(args.input_file)
        return args_dict


@dataclass(frozen=True)
class CalculateDistancesParameters(BaseRunnerParameters):
    input_path: str

    @classmethod
    def _get_args(cls, args: Namespace) -> Dict[str, Any]:
        args_dict = BaseRunnerParameters._get_args(args)
        args_dict['input_path'] = get_absolute_path(args.input_path)
        return args_dict


@dataclass(frozen=True)
class ClusteringParameters(BaseRunnerParameters):
    csv_dir: str
    min_distance_limit: int
    max_distance_limit: int
    step_distance_limit: int

    # Distance limit in range(min_distance_limit, max_distance_limit, step_distance_limit)
    # This parameter is not parsed from script arguments directly but is used to
    # iterate over distance limits from min_distance_limit to max_distance_limit
    distance_limit: int

    @classmethod
    def _get_args(cls, args: Namespace, distance_limit: int = None) -> Dict[str, Any]:
        args_dict = BaseRunnerParameters._get_args(args)
        args_dict['csv_dir'] = get_absolute_path(args.csv_dir)
        args_dict['min_distance_limit'] = int(args.min_distance_limit)
        args_dict['max_distance_limit'] = int(args.max_distance_limit)
        args_dict['step_distance_limit'] = int(args.step_distance_limit)
        if distance_limit is not None:
            args_dict['distance_limit'] = distance_limit
        else:
            args_dict['distance_limit'] = args_dict['min_distance_limit']
        return args_dict

    @classmethod
    def from_args(cls, args: Namespace, distance_limit: int = None):
        return cls(**cls._get_args(args, distance_limit))
