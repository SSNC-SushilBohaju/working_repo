import struct
from echonetlite.interfaces import monitor
from echonetlite import middleware
from echonetlite.protocol import *
class CTSENSOR(middleware.RemoteDevice):
    def __init__(self, eoj, node_id):
        super(CTSENSOR, self).__init__(eoj=eoj)
        self._node_id = node_id
        monitor.schedule_loopingcall(
            10,
            self._request_power,
            from_device=controller,
            to_eoj=self.eoj,
            to_node_id=self._node_id)
        self.add_listener(EPC_ELECTRIC_UNIT,
                          self._on_did_receive_power)
    def _request_power(self, from_device, to_eoj, to_node_id):
        from_device.send(esv=ESV_CODE['GET'],
                         props=[Property(epc=EPC_ELECTRIC_UNIT),],
                         to_eoj=to_eoj,
                         to_node_id=to_node_id)
    def _on_did_receive_power(self, from_node_id, from_eoj,
                                    to_device, esv, prop):
        if esv not in ESV_RESPONSE_CODES:
            return
        #print(len(bytearray(prop.edt)))
        tmp = struct.unpack('8d',bytearray(prop.edt))
        print(tmp)
        # (val,) = struct.unpack('d', bytearray(prop.edt))
        # print('power is', val)
class MyProfile(middleware.NodeProfile):
    def __init__(self, eoj=None):
        super(MyProfile, self).__init__(eoj=eoj)
        # profile.property[EPC_MANUFACTURE_CODE] = ...
        # profile.property[EPC_IDENTIFICATION_NUMBER] = ...
    def on_did_find_device(self, eoj, from_node_id):
        if (eoj.clsgrp == CLSGRP_CODE['HOUSING_FACILITIES']
            and eoj.cls == CLS_HF_CODE['LV_ELECTRIC_ENERGY_METER']):
            return CTSENSOR(eoj, from_node_id)
        return None
profile = MyProfile()
controller = middleware.Controller(instance_id=1)
monitor.start(node_id='192.168.2.230',                                      # PC ip address
              devices={str(profile.eoj): profile,
                       str(controller.eoj): controller})

