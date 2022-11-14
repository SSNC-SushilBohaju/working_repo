import struct
from ct_data import send_data
from echonetlite.interfaces import monitor
from echonetlite import middleware
from echonetlite.protocol import *
class CTSENSOR(middleware.NodeSuperObject):
    def __init__(self, eoj):
        super(CTSENSOR, self).__init__(eoj=eoj)
        # self.property[EPC_MANUFACTURE_CODE] = ...
        self._add_property(EPC_ELECTRIC_UNIT, [0,0])
        self.get_property_map += [
            EPC_ELECTRIC_UNIT]
        monitor.schedule_loopingcall(
            1,
            self._update_power)
    def _update_power(self):
        # update power value here
        val =send_data()  #call function of another program for ct senosr data
        #self._properties[EPC_ELECTRIC_UNIT] = struct.pack('%sf' % len(val), 	*val)"
        print (val)
        # for v in val:
        #     print(v)
        self._properties[EPC_ELECTRIC_UNIT] = struct.pack('d'*len(val), *val) 
# Create local devices
profile = middleware.NodeProfile()
# profile.property[EPC_MANUFACTURE_CODE] = ...
# profile.property[EPC_IDENTIFICATION_NUMBER] = ...
Power = CTSENSOR(eoj=EOJ(clsgrp=CLSGRP_CODE['HOUSING_FACILITIES'],
                         cls=CLS_HF_CODE['LV_ELECTRIC_ENERGY_METER'], instance_id=1))
# Start the Echonet Lite message loop
monitor.start(node_id='192.168.2.97',                                                       #pi ip address
              devices={str(profile.eoj): profile,
                       str(Power.eoj): Power})
