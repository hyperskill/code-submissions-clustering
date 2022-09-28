from enum import Enum, unique


@unique
class SubmissionColumns(Enum):
    ID = 'id'
    STEP_ID = 'step_id'
    CODE = 'code'
