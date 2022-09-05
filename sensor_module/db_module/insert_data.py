import psycopg2
from config import config
from datetime import datetime, timezone


params = config()
conn = None
dt = datetime.now(timezone.utc)
conn = psycopg2.connect(**params)
cur = conn.cursor()
insert_script  = 'INSERT INTO meas_ct_power (id, acquisition_time, power, channel_id) VALUES (%s, %s, %s, %s)'
print (dt)
insert_values = (1, dt, 12000,1 )

cur.execute(insert_script,insert_values)
print("done")
conn.commit()
if conn is not None:
    conn.close()
