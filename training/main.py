import sqlite3
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import json
from datetime import datetime


def train_compatible_model(product_id=1):

    conn = sqlite3.connect("store.db")

    query = """
            SELECT s.date, \
                   s.quantity, \
                   strftime('%w', s.date) as day_of_week, \
                   strftime('%m', s.date) as month,
        strftime('%d', s.date) as day_of_month
            FROM Sells s
            WHERE s.id_product = ?
            ORDER BY s.date \
            """

    df = pd.read_sql(query, conn, params=(product_id,))
    conn.close()

    if len(df) < 10:
        print("Dados insuficientes")
        return

    df['date'] = pd.to_datetime(df['date'])
    df['day_of_week'] = df['day_of_week'].astype(int)
    df['month'] = df['month'].astype(int)
    df['day_of_month'] = df['day_of_month'].astype(int)


    reference_date = pd.Timestamp('2025-05-25')
    df['days_since_reference'] = (df['date'] - reference_date).dt.days

    df['rolling_avg_7'] = df['quantity'].rolling(window=7, min_periods=1).mean()
    df['rolling_avg_30'] = df['quantity'].rolling(window=30, min_periods=1).mean()

    df['lag_1'] = df['quantity'].shift(1)
    df['lag_7'] = df['quantity'].shift(7)

    df.fillna(0, inplace=True)

    features = [
        'day_of_week',
        'month',
        'day_of_month',
        'days_since_reference',
        'rolling_avg_7',
        'rolling_avg_30',
        'lag_1',
        'lag_7'
    ]

    X = df[features].values
    y = df['quantity'].values

    model = RandomForestRegressor(
        n_estimators=50,
        max_depth=10,
        random_state=42,
        n_jobs=-1
    )

    model.fit(X, y)

    initial_type = [('input', FloatTensorType([None, len(features)]))]

    onnx_model = convert_sklearn(
        model,
        initial_types=initial_type,
        target_opset=12
    )

    model_filename = f"model_compatible_{product_id}.onnx"
    with open(model_filename, "wb") as f:
        f.write(onnx_model.SerializeToString())

    metadata = {
        'product_id': product_id,
        'features': features,
        'feature_count': len(features),
        'input_name': 'input',
        'output_name': 'variable',
        'reference_date': '2025-05-25',
        'training_date': datetime.now().isoformat(),
        'data_points': len(df),
        'model_type': 'RandomForestRegressor',
        'notes': 'Features compatíveis com Java StockPredictor'
    }

    metadata_filename = f"metadata_{product_id}.json"
    with open(metadata_filename, 'w') as f:
        json.dump(metadata, f, indent=2)

    test_features = np.array([[
        2,
        3,
        15,
        435,
        5.2,
        4.8,
        6,
        5
    ]], dtype=np.float32)

    test_prediction = model.predict(test_features)[0]

    print(f"✅ Modelo treinado e salvo: {model_filename}")
    print(f"📊 Features: {features}")
    print(f"🔢 Test prediction: {test_prediction:.2f}")
    print(f"📁 Metadata: {metadata_filename}")

    return model_filename, metadata_filename


def get_all_products():
    conn = sqlite3.connect("store.db")
    query = "SELECT DISTINCT id_product FROM Sells"
    df = pd.read_sql(query, conn)
    conn.close()
    return df['id_product'].tolist()


if __name__ == "__main__":
    products = get_all_products()
    for product in products:
        try:
            train_compatible_model(product)
        except Exception as e:
            print(f"❌ Erro ao treinar produto {product}: {e}")