import tempfile
from pathlib import Path

SCRIPTS_ROOT_FOLDER = Path(__file__).parent.parent.parent
RESOURCES_FOLDER = Path(__file__).parent / 'resources'
TMP_FOLDER = Path(tempfile.gettempdir()) / 'test_code_submissions_clustering_output'
