import psycopg2
from config import config
from connect import connect

connect()


def create_tables():
	# Connect to the PostgreSQL database server
	params = config()
	conn = None
	conn = psycopg2.connect(**params)
	# Get cursor object from the database connection
	cursor                = conn.cursor()
	name_Table            = "meas_ct_power"

	# Create table statement
	sqlCreateTable = "create table "+name_Table+" (id SERIAL PRIMARY KEY,acquisition_time varchar(255), power double precision, channel_id integer);"
	# Create a table in PostgreSQL database
	cursor.execute(sqlCreateTable)
	conn.commit()
	# Get the updated list of tables
	sqlGetTableList = "SELECT table_schema,table_name FROM information_schema.tables where table_schema='sedb' ORDER BY table_schema,table_name ;"
	#sqlGetTableList = "\dt"
	# Retrieve all the rows from the cursor
	cursor.execute(sqlGetTableList)
	tables = cursor.fetchall()
	# Print the names of the tables
	for table in tables:
    		print(table)

if __name__ == '__main__':
	create_tables()
