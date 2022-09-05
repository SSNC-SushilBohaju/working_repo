from db_module.connect import get_db_connection

connection = get_db_connection()
class DB_module():

    @staticmethod
    def insertData(table_name, data_dict):
        cursor = connection.cursor()
        columns = data_dict.keys()
        values = [data_dict[column] for column in columns]
        insert_query = f" "
        
        join_column = "" 
        for column in columns:
            join_column = join_column + column + ","
        print(insert_query, join_column[:-1], tuple(values))
        insert_query = f"INSERT INTO {table_name} ({join_column[:-1]}) VALUES {tuple(values)}"
        print(insert_query)
        cursor.execute(insert_query)
        return connection.commit()