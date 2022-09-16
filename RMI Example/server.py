#this code is use in measurement pi 
import Pyro4
import sum_math

@Pyro4.expose
class SmartClient(object):
    def __init__(self):
        self.name = "SmartClient"
        self.version = "0.1"
    
    def request_work(self):
        print("Someone requested work!")
    #control measurement pi other services through controller pi
    def request(self):  
        sum = sum_math.suma(10, 20)         
        return "excute math service"
smart = SmartClient()
daemon = Pyro4.Daemon(host="192.168.2.13", port=5150)
Pyro4.Daemon.serveSimple(
    { smart: "test.SmartClient" },
    ns=False,
    daemon=daemon,
    verbose = True
)
    
