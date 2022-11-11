
import math
import Pyro4
di = Pyro4.Proxy("PYRO:test.SmartClient@192.168.2.54:5150")
print(di.request_work())
print(di.request())