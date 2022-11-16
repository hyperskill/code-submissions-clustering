import time


def time_to_str(secs: float) -> str:
    return time.strftime('%Hh %Mm %Ss', time.gmtime(secs))
