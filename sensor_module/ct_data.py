from datetime import datetime, timezone
from config.ct_mapping import CtAddress ,CtVariable
import math
from i2c_rpi.i2c import Bus
import sys
import time
from db_module.db_service import DB_module
class Current():
    def __init__(self,bus_num=1,addr=CtAddress.ADC_DEFAULT_IIC_ADDR.value):
        self.bus = Bus(bus_num)
        self.addr = addr
    def getdata(self,n,ring_buffer):
        summation = 0
        intdata = 0
        num_of_summed_data = 0
        for i in range (int(ring_buffer)):
            data = self.bus.read_i2c_block_data(CtAddress.ADC_DEFAULT_IIC_ADDR.value, CtAddress.REG_VOL_START.value+n, 2)       # sensor address start from 0X20
            if (data[1]<<8|data[0]) > 0:
                intdata += data[1]<<8|data[0]                #convert output in the form of list to interger by using bitwise OR
                num_of_summed_data += 1
        try:
            Vo = intdata / num_of_summed_data
            mV = ((Vo * 3300 ) / 4096 )
            #mV= mV - 1.65      # output voltage when no load =1.65 so offset it
            ipri = ((mV / 80 ) * 3)  #I = V/rBurden * number of turns (3000) 3000/1000 
            #print (ipri)                            #current in ampere
            summation = ( ipri ** 2 )
            irms = (math.sqrt (summation )) * 0.707 #RMS value of current in Ampere
            apperentpower = 210 * irms    # power in watt Vrms * Irms      
            return irms ,apperentpower
        except ZeroDivisionError:
            return None
ADC =Current()
def main():
    # adding Folder_2/subfolder to the system path
    #print(apperentpower)
    while True:
        # run in loop to get all attach sensor data from raspberry pi
        n = range(0, 8, 1)
        #get current data
        for i in n:
            pin_current = ADC.getdata(i,CtVariable.ring_buffer.value)
            if pin_current is not None:
                ampere1 = pin_current[0]                                                #irms value is on 0 position of func curre$
                #print("CT0"+ str(i) + " pin_current(A):" + str(ampere1) + " A")
            else:
                print ("sensor error")
        #get power data
        for i in n:
            power = get_power_data(i)   
            if power:
                #power value is on 1 position of func curr$
                #print ("CT0"+ str(i) + " Power(VA):"  + str(power) +" W")
                insertResponse = DB_module.insertData(table_name="meas_ct_power", data_dict={
                "acquisition_time": str(datetime.now().timestamp()),
                "power":power,
                "channel_id":i
                })
                print(insertResponse)
            else:
                print ("sensor error")
        #led.led_blinking()
        time.sleep(10)
def get_power_data(i):
    pin_current = ADC.getdata(i,CtVariable.ring_buffer.value)
    if pin_current is not None:
        power1 = pin_current[1]
        return power1

    else: 
        return 0
def send_data():
    data = []
    n = range(0, 8, 1)
    #get current data
    for i in n:
        power = get_power_data(i)          
        if power:
            data.append(power) 
        else:
            data.append(0)
    return data
