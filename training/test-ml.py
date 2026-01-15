import onnxruntime as ort
import numpy as np
import json
import sqlite3
import pandas as pd
from datetime import datetime, timedelta

def load_model(product_id):
    session = ort.InferenceSession(f"model_compatible_{product_id}.onnx")

    with open(f"metadata_{product_id}.json") as f:
        metadata = json.load(f)

    return session, metadata


def predict_stockout(product_id, initial_stock, history):

    session, metadata = load_model(product_id)

    stock = initial_stock
    date = datetime.now()
    days = 0

    reference_date = datetime.fromisoformat(metadata['reference_date'])

    while stock > 0:
        day_of_week = (date.weekday() + 6) % 7
        month = date.month
        day_of_month = date.day
        days_since_ref = (date - reference_date).days

        rolling_avg_7 = np.mean(history[-7:]) if len(history) >= 7 else np.mean(history)
        rolling_avg_30 = np.mean(history[-30:]) if len(history) >= 30 else np.mean(history)

        lag_1 = history[-1] if len(history) >= 1 else 0
        lag_7 = history[-7] if len(history) >= 7 else 0

        feature_map = {
            "day_of_week": day_of_week,
            "month": month,
            "day_of_month": day_of_month,
            "days_since_reference": days_since_ref,
            "rolling_avg_7": rolling_avg_7,
            "rolling_avg_30": rolling_avg_30,
            "lag_1": lag_1,
            "lag_7": lag_7
        }

        features = np.array(
            [[feature_map[f] for f in metadata['features']]],
            dtype=np.float32
        )

        demand = session.run(
            None,
            {metadata['input_name']: features}
        )[0][0][0]

        sales_today = round(demand, 3)

        print(f"Dia {days + 1}: demanda={demand:.2f}, vendas={sales_today}, stock={stock - sales_today}")

        stock -= sales_today
        if stock < 0:
            return days
        history.append(sales_today)

        date += timedelta(days=1)
        days += 1

    return days

def load_sales_history(product_id, limit=30):
    conn = sqlite3.connect("store.db")

    query = """
        SELECT quantity
        FROM Sells
        WHERE id_product = ?
        ORDER BY date DESC
        LIMIT ?
    """

    df = pd.read_sql(query, conn, params=(product_id, limit))
    conn.close()

    return df.iloc[::-1]['quantity'].tolist()


if __name__ == "__main__":
    history = load_sales_history(25, limit=30)
    print(predict_stockout(25, 20, history))
