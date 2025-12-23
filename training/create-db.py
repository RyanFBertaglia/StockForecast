import sqlite3
import random
from datetime import datetime, timedelta
import numpy as np

random.seed(42)
np.random.seed(42)


class StoreDatabaseCreator:
    def __init__(self, db_name="store.db"):
        self.db_name = db_name
        self.conn = None
        self.product_cache = {}

    def create_database(self):
        """Cria estrutura do banco de dados"""
        print(f"🛠️ Criando banco de dados: {self.db_name}")

        self.conn = sqlite3.connect(self.db_name)
        self.conn.execute("PRAGMA foreign_keys = ON")
        cursor = self.conn.cursor()

        cursor.execute("""
        CREATE TABLE IF NOT EXISTS Products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            stock INTEGER DEFAULT 0
        )
        """)

        cursor.execute("""
        CREATE TABLE IF NOT EXISTS Sells (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_product INTEGER NOT NULL,
            date DATE NOT NULL,
            quantity INTEGER DEFAULT 1,
            FOREIGN KEY (id_product) REFERENCES Products(id)
        )
        """)

        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sells_date ON Sells(date)")
        cursor.execute("CREATE INDEX IF NOT EXISTS idx_sells_product ON Sells(id_product)")

        self.conn.commit()
        print("✅ Tabelas criadas com sucesso!")

    def generate_products(self, num_products=30):
        print(f"🛍️ Gerando {num_products} produtos...")

        products = [
            ("Mouse Gamer RGB", 45),
            ("Teclado Mecânico Redragon", 32),
            ("Monitor 24'' Samsung", 18),
            ("Notebook Dell i5", 9),
            ("Fone Bluetooth JBL", 28),
            ("Webcam Logitech C920", 22),
            ("Caixa de Som JBL Go", 37),
            ("Tablet Samsung A7", 15),
            ("Smartwatch Xiaomi", 41),
            ("Carregador Rápido 65W", 56),
            ("Caneta Esferográfica Azul (caixa 50)", 68),
            ("Caderno Universitário 200p", 42),
            ("Grampeador Stanley", 29),
            ("Pasta Catálogo A4", 33),
            ("Bloco de Notas Adesivas", 77),
            ("Garrafa Térmica 1L", 38),
            ("Jogo de Panelas 5pç", 12),
            ("Conjunto Talheres Inox", 25),
            ("Toalha de Mesa Retangular", 19),
            ("Jogo de Copos 6un", 31),
            ("HD Externo 1TB", 21),
            ("Pendrive 64GB", 53),
            ("Mouse Pad Gamer XXL", 47),
            ("Cooler para Notebook", 34),
            ("Cabo HDMI 2.0 2m", 62),
            ("Domain-Driven Design", 14),
            ("Clean Code", 17),
            ("Python Fluente", 23),
            ("Java Efetivo", 19),
            ("Arquitetura Limpa", 16),
        ]

        cursor = self.conn.cursor()
        cursor.executemany("""
            INSERT INTO Products (name, stock)
            VALUES (?, ?)
        """, products[:num_products])

        self.conn.commit()
        print("✅ Produtos inseridos!")

    def load_product_cache(self):
        cursor = self.conn.cursor()
        cursor.execute("SELECT id, name FROM Products")
        self.product_cache = dict(cursor.fetchall())

    def generate_sales(self, months=7):
        print(f"📊 Gerando vendas para {months} meses...")
        cursor = self.conn.cursor()

        cursor.execute("SELECT id FROM Products")
        product_ids = [r[0] for r in cursor.fetchall()]

        monthly_factors = {
            1: 0.8, 2: 0.9, 3: 1.0, 4: 1.1, 5: 1.0, 6: 1.2,
            7: 1.3, 8: 1.1, 9: 1.0, 10: 1.4, 11: 1.5, 12: 1.8
        }

        weekday_factors = {0: 1.2, 1: 1.3, 2: 1.1, 3: 1.0, 4: 1.4, 5: 1.8, 6: 0.7}

        total_days = months * 30
        today = datetime.now()
        sales_batch = []

        for day_offset in range(total_days, 0, -1):
            sale_date = today - timedelta(days=day_offset)
            date_str = sale_date.strftime("%Y-%m-%d")

            base_sales = 15
            factor = monthly_factors[sale_date.month] * weekday_factors[sale_date.weekday()]
            daily_sales = max(5, min(int(base_sales * factor), 50))

            for _ in range(daily_sales):
                product_id = random.choice(product_ids)
                name = self.product_cache.get(product_id, "")

                if "Caneta" in name or "Caderno" in name:
                    quantity = random.choices([1, 2, 3, 4, 5], [40, 30, 15, 10, 5])[0]
                elif "Cabo" in name or "Pendrive" in name:
                    quantity = random.choices([1, 2], [80, 20])[0]
                else:
                    quantity = random.choices([1, 2, 3], [85, 12, 3])[0]

                sales_batch.append((product_id, date_str, quantity))

                if len(sales_batch) >= 5000:
                    cursor.executemany("""
                        INSERT INTO Sells (id_product, date, quantity)
                        VALUES (?, ?, ?)
                    """, sales_batch)
                    self.conn.commit()
                    sales_batch.clear()

        if sales_batch:
            cursor.executemany("""
                INSERT INTO Sells (id_product, date, quantity)
                VALUES (?, ?, ?)
            """, sales_batch)
            self.conn.commit()

        print(f"✅ Vendas geradas: {self.get_sales_count():,}")

    def get_sales_count(self):
        cursor = self.conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM Sells")
        return cursor.fetchone()[0]

    def update_stocks(self):
        print("📦 Atualizando estoques...")
        cursor = self.conn.cursor()

        cursor.execute("""
            SELECT id_product, SUM(quantity)
            FROM Sells
            GROUP BY id_product
        """)

        for pid, total in cursor.fetchall():
            stock = total + random.randint(10, 50)
            cursor.execute("UPDATE Products SET stock = ? WHERE id = ?", (stock, pid))

        self.conn.commit()
        print("✅ Estoques atualizados!")

    def print_database_summary(self):
        cursor = self.conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM Products")
        products = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM Sells")
        sales = cursor.fetchone()[0]

        print("\n📋 RESUMO")
        print(f"Produtos: {products}")
        print(f"Vendas: {sales}")

    def export_for_ml_training(self):
        cursor = self.conn.cursor()
        cursor.execute("""
            SELECT 
                ((strftime('%w', date) + 6) % 7) AS day_of_week,
                id_product,
                quantity
            FROM Sells
            ORDER BY date
        """)
        rows = cursor.fetchall()
        print(f"🤖 Registros para ML: {len(rows)}")
        return rows

    def close(self):
        if self.conn:
            self.conn.close()
            print("🔒 Conexão encerrada")


def main():
    creator = StoreDatabaseCreator("store.db")

    try:
        creator.create_database()
        creator.generate_products()
        creator.load_product_cache()
        creator.generate_sales()
        creator.update_stocks()
        creator.print_database_summary()
        creator.export_for_ml_training()
        print("\n🎉 BANCO CRIADO COM SUCESSO!")

    finally:
        creator.close()


if __name__ == "__main__":
    main()
