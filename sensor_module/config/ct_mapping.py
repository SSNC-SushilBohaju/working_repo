
from enum import Enum

#address
class CtAddress(Enum):
    ADC_DEFAULT_IIC_ADDR = 0X04
    ADC_CHAN_NUM = 8
    REG_RAW_DATA_START = 0X10
    REG_VOL_START = 0X20
    REG_RTO_START = 0X30
    REG_SET_ADDR = 0XC0

class CtVariable(Enum):
    sampling = 700
    ring_buffer = sampling * 1.5
    
