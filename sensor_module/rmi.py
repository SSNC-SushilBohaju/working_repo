#this code is use in measurement pi 
import Pyro4
import ct_data


@Pyro4.expose
class SmartClient(object):
    def __init__(self):
        self.name = "SmartClient"
        self.version = "0.1"
    
    def request_work(self):
        print("Someone requested work!")
    #control measurement pi other services through controller pi
    def request(self):  
        ct_data.main()       
        return "excute CT senosr  service"
smart = SmartClient()
daemon = Pyro4.Daemon(host="192.168.2.97", port=5150)
Pyro4.Daemon.serveSimple(
    { smart: "test.SmartClient" },
    ns=False,
    daemon=daemon,
    verbose = True
)
