import pandas as pd


def read_df(input_file: str) -> pd.DataFrame:
    return pd.read_csv(input_file)


def write_df(df: pd.DataFrame, output_file: str):
    df.to_csv(output_file, index=False)
