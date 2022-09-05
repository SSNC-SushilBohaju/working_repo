import smbus2 as smbus
from smbus2 import i2c_msg
class Bus:
    instance = None
    MRAA_I2C = 0

    def __init__(self, bus=None):
        if bus is None:
            try:
                import RPi.GPIO as GPIO
                # use the bus that matches your raspi version
                rev = GPIO.RPI_REVISION
            except:
                rev = 3
            if rev == 2 or rev == 3:
                bus = 1  # for Pi 2+
            else:
                bus = 0
        if not Bus.instance:
            Bus.instance = smbus.SMBus(bus)
        self.bus = bus
        self.msg = i2c_msg
    def __getattr__(self, name):
        return getattr(self.instance, name)
