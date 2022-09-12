package src.main.java.jp.co.smartsolar.smartedge;

import java.io.IOException;
import java.io.PrintStream;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.main.java.jp.co.smartsolar.smartedge.eoj.ElObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.DeviceObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.CurrentValueSensor.CurrentValueSensorClass;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.CurrentValueSensor.WattHourMeter;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.householdpowergeneration.HouseholdSolarPowerGeneration;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.householdpowergeneration.SmartElectricEnergyMeter;
import src.main.java.jp.co.smartsolar.smartedge.eoj.profile.NodeProfile;
import src.main.java.jp.co.smartsolar.smartedge.eoj.profile.ProfileObject;
import src.main.java.jp.co.smartsolar.smartedge.node.ElNode;

public final class Echonet {

    private static volatile ElNode sSelfNode;
    private static Map<String, ElNode> sOtherNodes;

    private static Events sEvents = null;
    private static ArrayList<EventListener> sListeners;

    private volatile static boolean sStarted = false;
    private volatile static boolean sCleared = true;

    static {
        sOtherNodes = new ConcurrentHashMap<String, ElNode>();
        sListeners = new ArrayList<EventListener>();
        sEvents = new Events();
    }

    private Echonet() {
    }

    public synchronized static ElNode start(NodeProfile profile, DeviceObject[] devices,
            NetworkInterface nwif) throws IOException {
        if (sStarted)
            return null;
        if (!sCleared)
            return null;

        sStarted = true;
        sCleared = false;

        sSelfNode = new ElNode(profile, devices);
        profile.setNode(sSelfNode);
        for (DeviceObject dev : devices) {
            dev.setNode(sSelfNode);
        }
        ElSocket.openSocket(nwif);

        return postOpenSocket(devices);
    }

    public synchronized static ElNode start(NodeProfile profile, DeviceObject[] devices)
            throws IOException {
        if (sStarted)
            return null;
        if (!sCleared)
            return null;

        sStarted = true;
        sCleared = false;

        sSelfNode = new ElNode(profile, devices);
        profile.setNode(sSelfNode);
        for (DeviceObject dev : devices) {
            dev.setNode(sSelfNode);
        }
        ElSocket.openSocket();

        return postOpenSocket(devices);
    }

    private static ElNode postOpenSocket(DeviceObject[] devices) throws IOException {
        // Echo.getEventListener().onNewNode(sSelfNode);
        sSelfNode.onNew();
        // Echo.getEventListener().onFoundNode(sSelfNode);
        sSelfNode.onFound();
        sSelfNode.getNodeProfile().onNew();
        sSelfNode.getNodeProfile().onFound();

        for (DeviceObject dev : devices) {
            dev.onNew();
            dev.onFound();
        }

        sSelfNode.getNodeProfile().inform().reqInformInstanceListNotification().send();
        return sSelfNode;
    }

    public synchronized static void restart() throws IOException {
        if (sCleared)
            return;
        sStarted = true;
        ElSocket.openSocket();
        sSelfNode.getNodeProfile().inform().reqInformInstanceListNotification().send();
    }

    public synchronized static void stop() throws IOException {
        System.err.println("Echo stop");
        ElSocket.closeSocket();
        sStarted = false;
        System.err.println("Echo closed");
        // sNodes.clear();
    }

    public synchronized static void clear() throws IOException {
        stop();
        sCleared = true;

        sSelfNode = null;

        sOtherNodes.clear();
        sListeners.clear();
    }

    public static boolean isStarted() {
        return sStarted;
    }

    // remain for back compatibility.
    @Deprecated
    public static ElNode getNode() {
        return getSelfNode();
    }

    public static ElNode getSelfNode() {
        return sSelfNode;
    }

    public static ElNode[] getNodes() {
        Collection<ElNode> nodes = sOtherNodes.values();
        List<ElNode> ret = new ArrayList<ElNode>();
        if (sSelfNode != null) {
            ret.add(sSelfNode);
        }
        for (ElNode n : nodes) {
            ret.add(n);
        }
        return ret.toArray(new ElNode[] {});
    }

    public static ElNode getNode(String address) {
        if (ElSocket.SELF_ADDRESS.equals(address)) {
            return sSelfNode;
        }
        return sOtherNodes.get(address);
    }

    public synchronized static ElNode addOtherNode(String address) {
        ElNode node = new ElNode(address);
        node.getNodeProfile().setNode(node);
        sOtherNodes.put(address, node);

        return node;
    }

    public static void removeOtherNode(String address) {
        sOtherNodes.remove(address);
    }

    // public static void removeAllNode() {
    // sNodes.clear();
    // }

    /*
     * public synchronized static ElNode[] getActiveNodes() {
     * Collection<ElNode> nodes = sOtherNodes.values(); List<ElNode> ret =
     * new ArrayList<ElNode>(); if(sSelfNode != null && sSelfNode.isActive()){
     * ret.add(sSelfNode); } for(ElNode n : nodes) { if(n.isActive())
     * ret.add(n); } return ret.toArray(new ElNode[]{}); }
     */

    // @Deprecated
    // public static ElObject getInstance(InetAddress address, byte
    // classGroupCode, byte classCode, byte instanceCode) {
    // return getInstance(address, ElUtils.getEchoClassCode(classGroupCode,
    // classCode), instanceCode);
    // }
    //
    // @Deprecated
    // public static ElObject getInstance(InetAddress address, int
    // objectCode){
    // return getInstance(address,
    // ElUtils.getEchoClassCodeFromObjectCode(objectCode),
    // ElUtils.getInstanceCodeFromObjectCode(objectCode));
    // }
    //
    // @Deprecated
    // public static ElObject getInstance(InetAddress address, short
    // echoClassCode, byte instanceCode) {
    //
    // if(sCleared) {
    // return null;
    // }
    // if(address == null) {
    // return null;
    // }
    // if(address.equals(sSelfNode.getAddressStr())) {
    // if(!sSelfNode.containsInstance(echoClassCode, instanceCode)) return null;
    // return sSelfNode.getInstance(echoClassCode, instanceCode);
    // } else if(sOtherNodes.containsKey(address)) {
    // ElNode node = sOtherNodes.get(address);
    // if(!node.containsInstance(echoClassCode, instanceCode)) return null;
    // return node.getInstance(echoClassCode, instanceCode);
    // } else {
    // return null;
    // }
    // }
    /*
     * public synchronized static void updateNodeInstance(InetAddress address,
     * byte classGroupCode, byte classCode, byte instanceCode) { if(sCleared) {
     * return; } if(address == null) { return; }
     * if(address.equals(sSelfNode.getAddress())) {
     * //if(sLocalNode.containsInstance(classGroupCode, classCode,
     * instanceCode)) return;
     * //sLocalNode.addDevice(ElUtils.getEchoClassCode(classGroupCode,
     * classCode), instanceCode); if(sSelfNode.containsInstance(classGroupCode,
     * classCode, instanceCode)) { sSelfNode.getInstance(classGroupCode,
     * classCode, instanceCode).setActive(true); return; } } else
     * if(sOtherNodes.containsKey(address)) { ElNode node =
     * sOtherNodes.get(address); if(node.containsInstance(classGroupCode,
     * classCode, instanceCode)){ node.getInstance(classGroupCode, classCode,
     * instanceCode).setActive(true); return; } else {
     * node.addDevice(ElUtils.getEchoClassCode(classGroupCode, classCode),
     * instanceCode); } } else { if(NodeProfile.ECHO_CLASS_CODE ==
     * ElUtils.getEchoClassCode(classGroupCode, classCode) &&
     * NodeProfile.INSTANCE_CODE == instanceCode) { new ElNode(address, new
     * ArrayList<Integer>()); } else { ArrayList<Integer> list = new
     * ArrayList<Integer>();
     * list.add(ElUtils.getElObjectCode(classGroupCode, classCode,
     * instanceCode)); new ElNode(address, list); } } }
     */
    // public synchronized static void updateNodeDevices(InetAddress address,
    // List<Integer> ElObjectCodeList) {
    // if(ElObjectCodeList == null) return;
    /*
     * if(sLocalNode.getAddress().equals(address)) {
     * //sLocalNode.updateDevices(ElObjectCodeList); return; }else
     * if(sNodes.containsKey(address)) { ElNode node = sNodes.get(address);
     * node.updateDevices(ElObjectCodeList); } else { new ElNode(address,
     * ElObjectCodeList); }
     */
    /*
     * if(sCleared) { return; } if(address == null) { return; }
     * if(!address.equals(sSelfNode.getAddress()) &&
     * !sOtherNodes.containsKey(address)) { new ElNode(address,
     * ElObjectCodeList); return; } for(int objCode: ElObjectCodeList) {
     * byte[] a = ElUtils.toByteArray(objCode, 4); updateNodeInstance(address,
     * a[1],a[2],a[3]); } if(!sOtherNodes.containsKey(address)) return;
     * for(DeviceObject dev : sOtherNodes.get(address).getDevices()) { boolean
     * active = false; for(int code : ElObjectCodeList) { if(code ==
     * dev.getElObjectCode()) { active = true; break; } }
     * dev.setActive(active); } }
     */

    public static void addEventListener(EventListener listener) {
        sListeners.add(listener);
    }

    public static EventListener getEventListener() {
        return sEvents;
    }

    public static class EventListener {
        public void setProperty(ElObject eoj, ElProperty property, boolean success) {
        }

        public void getProperty(ElObject eoj, ElProperty property) {
        }

        public void isValidProperty(ElObject eoj, ElProperty property, boolean valid) {
        }

        public void onSetProperty(ElObject eoj, short tid, byte esv, ElProperty property,
                boolean success) {
        }

        public void onGetProperty(ElObject eoj, short tid, byte esv, ElProperty property,
                boolean success) {
        }

        public void onInformProperty(ElObject eoj, short tid, byte esv, ElProperty property) {
        }

        public void reqSetPropertyEvent(ElObject eoj, ElProperty property) {
        }

        public void reqGetPropertyEvent(ElObject eoj, ElProperty property) {
        }

        public void reqInformPropertyEvent(ElObject eoj, ElProperty property) {
        }

        public void reqInformCPropertyEvent(ElObject eoj, ElProperty property) {
        }

        public void sendEvent(ElFrame frame) {
        }

        public void receiveEvent(ElFrame frame) {
        }

        public void onCatchException(Exception e) {
        }

        public void onFoundNode(ElNode node) {
        }

        public void onFoundElObject(ElObject eoj) {
        }

        public void onNewNode(ElNode node) {
        }

        public void onNewElObject(ElObject eoj) {
        }

        public void onNewProfileObject(ProfileObject profile) {
        }

        public void onNewNodeProfile(NodeProfile profile) {
        }

        public void onNewDeviceObject(DeviceObject device) {
        }
        /* 
        public void onNewActivityAmountSensor(ActivityAmountSensor device) {
        }

        public void onNewAirPollutionSensor(AirPollutionSensor device) {
        }

        public void onNewAirSpeedSensor(AirSpeedSensor device) {
        }

        public void onNewBathHeatingStatusSensor(BathHeatingStatusSensor device) {
        }

        public void onNewBathWaterLevelSensor(BathWaterLevelSensor device) {
        }

        public void onNewBedPresenceSensor(BedPresenceSensor device) {
        }

        public void onNewCallSensor(CallSensor device) {
        }

        public void onNewCigaretteSmokeSensor(CigaretteSmokeSensor device) {
        }

        public void onNewCO2Sensor(CO2Sensor device) {
        }

        public void onNewCondensationSensor(CondensationSensor device) {
        }

        public void onNewCrimePreventionSensor(CrimePreventionSensor device) {
        }
        */
        public void onNewCurrentValueSensor(CurrentValueSensorClass device) {
        }
        
        public void onNewHouseholdSolarPowerGeneration(HouseholdSolarPowerGeneration device) {
        }

        public void onNewSmartElectricEnergyMeter(SmartElectricEnergyMeter device) {
        }

        public void onNewWattHourMeter(WattHourMeter device) {
        }
    }

    public static class Logger extends EventListener {
        PrintStream mOut;

        public Logger(PrintStream out) {
            mOut = out;
        }

        @Override
        public void setProperty(ElObject eoj, ElProperty property,
                boolean success) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:set," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt)
                    + ",success:" + success);
        }

        @Override
        public void getProperty(ElObject eoj, ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:get," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));
        }

        @Override
        public void isValidProperty(ElObject eoj, ElProperty property,
                boolean valid) {
            // TODO Auto-generated method stub
            super.isValidProperty(eoj, property, valid);
        }

        @Override
        public void onSetProperty(ElObject eoj, short tid, byte esv,
                ElProperty property, boolean success) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:onSet," + eoj.toString()
                    + ",tid:" + ElUtils.toHexString(tid)
                    + ",esv:" + ElUtils.toHexString(esv)
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt)
                    + ",success:" + success);
        }

        @Override
        public void onGetProperty(ElObject eoj, short tid, byte esv,
                ElProperty property, boolean success) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:onGet," + eoj.toString()
                    + ",tid:" + ElUtils.toHexString(tid)
                    + ",esv:" + ElUtils.toHexString(esv)
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));

        }

        @Override
        public void onInformProperty(ElObject eoj, short tid, byte esv,
                ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:onInform," + eoj.toString()
                    + ",tid:" + ElUtils.toHexString(tid)
                    + ",esv:" + ElUtils.toHexString(esv)
                    + ",epc:" + ElUtils.toHexString(property.epc));
        }

        @Override
        public void reqSetPropertyEvent(ElObject eoj, ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:reqSet," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));
        }

        @Override
        public void reqGetPropertyEvent(ElObject eoj, ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:reqGet," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));
        }

        @Override
        public void reqInformPropertyEvent(ElObject eoj, ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:reqInform," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));
        }

        @Override
        public void reqInformCPropertyEvent(ElObject eoj,
                ElProperty property) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:reqInformC," + eoj.toString()
                    + ",epc:" + ElUtils.toHexString(property.epc)
                    + ",pdc:" + ElUtils.toHexString(property.pdc)
                    + ",edt:" + ElUtils.toHexString(property.edt));
        }

        @Override
        public void sendEvent(ElFrame frame) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:send,tid:" + ElUtils.toHexString(frame.getTID())
                    + ",esv:" + ElUtils.toHexString(frame.getESV())
                    + ",seoj:[class:" + String.format("%04x", frame.getSrcEchoClassCode())
                    + ",instance:" + String.format("%02x", frame.getSrcEchoInstanceCode())
                    + "],deoj:[class:" + String.format("%04x", frame.getDstEchoClassCode())
                    + ",instance:" + String.format("%02x", frame.getDstEchoInstanceCode())
                    + "],data:" + ElUtils.toHexString(frame.getFrameByteArray()));
        }

        @Override
        public void receiveEvent(ElFrame frame) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:receive,tid:" + ElUtils.toHexString(frame.getTID())
                    + ",esv:" + ElUtils.toHexString(frame.getESV())
                    + ",seoj:[class:" + String.format("%04x", frame.getSrcEchoClassCode())
                    + ",instance:" + String.format("%02x", frame.getSrcEchoInstanceCode())
                    + "],deoj:[class:" + String.format("%04x", frame.getDstEchoClassCode())
                    + ",instance:" + String.format("%02x", frame.getDstEchoInstanceCode())
                    + "],data:" + ElUtils.toHexString(frame.getFrameByteArray()));
        }

        @Override
        public void onCatchException(Exception e) {
            // TODO Auto-generated method stub
            super.onCatchException(e);
        }

        @Override
        public void onNewNode(ElNode node) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:new,type:node,address:"
                    + node.getAddressStr());
        }

        @Override
        public void onNewElObject(ElObject eoj) {
            long millis = System.currentTimeMillis();
            mOut.println("millis:" + millis
                    + ",method:new,type:eoj,"
                    + eoj.toString());
        }

    }

    private static class Events extends EventListener {

        @Override
        public void setProperty(ElObject eoj, ElProperty property,
                boolean success) {
            super.setProperty(eoj, property, success);
            for (EventListener listener : sListeners) {
                listener.setProperty(eoj, property, success);
            }
        }

        @Override
        public void getProperty(ElObject eoj, ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.getProperty(eoj, property);
            }
        }

        @Override
        public void isValidProperty(ElObject eoj, ElProperty property,
                boolean valid) {
            for (EventListener listener : sListeners) {
                listener.isValidProperty(eoj, property, valid);
            }
        }

        @Override
        public void onSetProperty(ElObject eoj, short tid, byte esv,
                ElProperty property, boolean success) {
            for (EventListener listener : sListeners) {
                listener.onSetProperty(eoj, tid, esv, property, success);
            }
        }

        @Override
        public void onGetProperty(ElObject eoj, short tid, byte esv,
                ElProperty property, boolean success) {
            for (EventListener listener : sListeners) {
                listener.onGetProperty(eoj, tid, esv, property, success);
            }
        }

        @Override
        public void onInformProperty(ElObject eoj, short tid, byte esv,
                ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.onInformProperty(eoj, tid, esv, property);
            }
        }

        @Override
        public void reqSetPropertyEvent(ElObject eoj, ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.reqSetPropertyEvent(eoj, property);
            }
        }

        @Override
        public void reqGetPropertyEvent(ElObject eoj, ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.reqGetPropertyEvent(eoj, property);
            }
        }

        @Override
        public void reqInformPropertyEvent(ElObject eoj, ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.reqInformPropertyEvent(eoj, property);
            }
        }

        @Override
        public void reqInformCPropertyEvent(ElObject eoj,
                ElProperty property) {
            for (EventListener listener : sListeners) {
                listener.reqInformCPropertyEvent(eoj, property);
            }
        }

        @Override
        public void sendEvent(ElFrame frame) {
            for (EventListener listener : sListeners) {
                listener.sendEvent(frame);
            }
        }

        @Override
        public void receiveEvent(ElFrame frame) {
            for (EventListener listener : sListeners) {
                listener.receiveEvent(frame);
            }
        }

        @Override
        public void onCatchException(Exception e) {
            for (EventListener listener : sListeners) {
                listener.onCatchException(e);
            }
        }

        @Override
        public void onFoundNode(ElNode node) {
            for (EventListener listener : sListeners) {
                listener.onFoundNode(node);
            }
        }

        @Override
        public void onFoundElObject(ElObject eoj) {
            for (EventListener listener : sListeners) {
                listener.onFoundElObject(eoj);
            }
        }

        @Override
        public void onNewNode(ElNode node) {
            for (EventListener listener : sListeners) {
                listener.onNewNode(node);
            }
        }

        @Override
        public void onNewElObject(ElObject eoj) {
            for (EventListener listener : sListeners) {
                listener.onNewElObject(eoj);
            }
        }

        @Override
        public void onNewProfileObject(ProfileObject profile) {
            for (EventListener listener : sListeners) {
                listener.onNewProfileObject(profile);
            }
        }

        @Override
        public void onNewNodeProfile(NodeProfile profile) {
            for (EventListener listener : sListeners) {
                listener.onNewNodeProfile(profile);
            }
        }

        @Override
        public void onNewDeviceObject(DeviceObject device) {
            for (EventListener listener : sListeners) {
                listener.onNewDeviceObject(device);
            }
        }
        /* 
        @Override
        public void onNewActivityAmountSensor(ActivityAmountSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewActivityAmountSensor(device);
            }
        }

        @Override
        public void onNewAirPollutionSensor(AirPollutionSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewAirPollutionSensor(device);
            }
        }

        @Override
        public void onNewAirSpeedSensor(AirSpeedSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewAirSpeedSensor(device);
            }
        }

        @Override
        public void onNewBathHeatingStatusSensor(BathHeatingStatusSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewBathHeatingStatusSensor(device);
            }
        }

        @Override
        public void onNewBathWaterLevelSensor(BathWaterLevelSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewBathWaterLevelSensor(device);
            }
        }

        @Override
        public void onNewBedPresenceSensor(BedPresenceSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewBedPresenceSensor(device);
            }
        }

        @Override
        public void onNewCallSensor(CallSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewCallSensor(device);
            }
        }

        @Override
        public void onNewCigaretteSmokeSensor(CigaretteSmokeSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewCigaretteSmokeSensor(device);
            }
        }

        @Override
        public void onNewCO2Sensor(CO2Sensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewCO2Sensor(device);
            }
        }

        @Override
        public void onNewCondensationSensor(CondensationSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewCondensationSensor(device);
            }
        }

        @Override
        public void onNewCrimePreventionSensor(CrimePreventionSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewCrimePreventionSensor(device);
            }
        }
        */
        @Override
        public void onNewCurrentValueSensor(CurrentValueSensorClass device) {
            for (EventListener listener : sListeners) {
                listener.onNewCurrentValueSensor(device);
            }
        }
        /* 
        @Override
        public void onNewDifferentialPressureSensor(
                DifferentialPressureSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewDifferentialPressureSensor(device);
            }
        }

        @Override
        public void onNewEarthquakeSensor(EarthquakeSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewEarthquakeSensor(device);
            }
        }

        @Override
        public void onNewElectricEnergySensor(ElectricEnergySensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricEnergySensor(device);
            }
        }

        @Override
        public void onNewElectricLeakSensor(ElectricLeakSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricLeakSensor(device);
            }
        }

        @Override
        public void onNewEmergencyButton(EmergencyButton device) {
            for (EventListener listener : sListeners) {
                listener.onNewEmergencyButton(device);
            }
        }

        @Override
        public void onNewFireSensor(FireSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewFireSensor(device);
            }
        }

        @Override
        public void onNewFirstAidSensor(FirstAidSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewFirstAidSensor(device);
            }
        }

        @Override
        public void onNewFlameSensor(FlameSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewFlameSensor(device);
            }
        }

        @Override
        public void onNewGasLeakSensor(GasLeakSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewGasLeakSensor(device);
            }
        }

        @Override
        public void onNewGasSensor(GasSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewGasSensor(device);
            }
        }

        @Override
        public void onNewHumanBodyLocationSensor(HumanBodyLocationSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewHumanBodyLocationSensor(device);
            }
        }

        @Override
        public void onNewHumanDetectionSensor(HumanDetectionSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewHumanDetectionSensor(device);
            }
        }

        @Override
        public void onNewHumiditySensor(HumiditySensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewHumiditySensor(device);
            }
        }

        @Override
        public void onNewIlluminanceSensor(IlluminanceSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewIlluminanceSensor(device);
            }
        }

        @Override
        public void onNewMailingSensor(MailingSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewMailingSensor(device);
            }
        }

        @Override
        public void onNewMicromotionSensor(MicromotionSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewMicromotionSensor(device);
            }
        }

        @Override
        public void onNewOdorSensor(OdorSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewOdorSensor(device);
            }
        }

        @Override
        public void onNewOpenCloseSensor(OpenCloseSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewOpenCloseSensor(device);
            }
        }

        @Override
        public void onNewOxygenSensor(OxygenSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewOxygenSensor(device);
            }
        }

        @Override
        public void onNewPassageSensor(PassageSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewPassageSensor(device);
            }
        }

        @Override
        public void onNewRainSensor(RainSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewRainSensor(device);
            }
        }

        @Override
        public void onNewSnowSensor(SnowSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewSnowSensor(device);
            }
        }

        @Override
        public void onNewSoundSensor(SoundSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewSoundSensor(device);
            }
        }

        @Override
        public void onNewTemperatureSensor(TemperatureSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewTemperatureSensor(device);
            }
        }

        @Override
        public void onNewVisitorSensor(VisitorSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewVisitorSensor(device);
            }
        }

        @Override
        public void onNewVOCSensor(VOCSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewVOCSensor(device);
            }
        }

        @Override
        public void onNewWaterFlowRateSensor(WaterFlowRateSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewWaterFlowRateSensor(device);
            }
        }

        @Override
        public void onNewWaterLeakSensor(WaterLeakSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewWaterLeakSensor(device);
            }
        }

        @Override
        public void onNewWaterLevelSensor(WaterLevelSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewWaterLevelSensor(device);
            }
        }

        @Override
        public void onNewWaterOverflowSensor(WaterOverflowSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewWaterOverflowSensor(device);
            }
        }

        @Override
        public void onNewWeightSensor(WeightSensor device) {
            for (EventListener listener : sListeners) {
                listener.onNewWeightSensor(device);
            }
        }

        @Override
        public void onNewAirCleaner(AirCleaner device) {
            for (EventListener listener : sListeners) {
                listener.onNewAirCleaner(device);
            }
        }

        @Override
        public void onNewAirConditionerVentilationFan(
                AirConditionerVentilationFan device) {
            for (EventListener listener : sListeners) {
                listener.onNewAirConditionerVentilationFan(device);
            }
        }

        @Override
        public void onNewElectricHeater(ElectricHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricHeater(device);
            }
        }

        @Override
        public void onNewFanHeater(FanHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewFanHeater(device);
            }
        }

        @Override
        public void onNewHomeAirConditioner(HomeAirConditioner device) {
            for (EventListener listener : sListeners) {
                listener.onNewHomeAirConditioner(device);
            }
        }

        @Override
        public void onNewHumidifier(Humidifier device) {
            for (EventListener listener : sListeners) {
                listener.onNewHumidifier(device);
            }
        }

        @Override
        public void onNewPackageTypeCommercialAirConditionerIndoorUnit(
                PackageTypeCommercialAirConditionerIndoorUnit device) {
            for (EventListener listener : sListeners) {
                listener.onNewPackageTypeCommercialAirConditionerIndoorUnit(device);
            }
        }

        @Override
        public void onNewPackageTypeCommercialAirConditionerOutdoorUnit(
                PackageTypeCommercialAirConditionerOutdoorUnit device) {
            for (EventListener listener : sListeners) {
                listener.onNewPackageTypeCommercialAirConditionerOutdoorUnit(device);
            }
        }

        @Override
        public void onNewVentilationFan(VentilationFan device) {
            for (EventListener listener : sListeners) {
                listener.onNewVentilationFan(device);
            }
        }

        @Override
        public void onNewBathroomHeaterAndDryer(BathroomHeaterAndDryer device) {
            for (EventListener listener : sListeners) {
                listener.onNewBathroomHeaterAndDryer(device);
            }
        }

        @Override
        public void onNewBattery(Battery device) {
            for (EventListener listener : sListeners) {
                listener.onNewBattery(device);
            }
        }

        @Override
        public void onNewBuzzer(Buzzer device) {
            for (EventListener listener : sListeners) {
                listener.onNewBuzzer(device);
            }
        }

        @Override
        public void onNewColdOrHotWaterHeatSourceEquipment(
                ColdOrHotWaterHeatSourceEquipment device) {
            for (EventListener listener : sListeners) {
                listener.onNewColdOrHotWaterHeatSourceEquipment(device);
            }
        }

        @Override
        public void onNewElectricallyOperatedShade(
                ElectricallyOperatedShade device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricallyOperatedShade(device);
            }
        }

        @Override
        public void onNewElectricLock(ElectricLock device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricLock(device);
            }
        }

        @Override
        public void onNewElectricShutter(ElectricShutter device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricShutter(device);
            }
        }

        @Override
        public void onNewElectricStormWindow(ElectricStormWindow device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricStormWindow(device);
            }
        }

        @Override
        public void onNewElectricToiletSeat(ElectricToiletSeat device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricToiletSeat(device);
            }
        }

        @Override
        public void onNewElectricVehicle(ElectricVehicle device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricVehicle(device);
            }
        }

        @Override
        public void onNewElectricWaterHeater(ElectricWaterHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricWaterHeater(device);
            }
        }

        @Override
        public void onNewEngineCogeneration(EngineCogeneration device) {
            for (EventListener listener : sListeners) {
                listener.onNewEngineCogeneration(device);
            }
        }

        @Override
        public void onNewFloorHeater(FloorHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewFloorHeater(device);
            }
        }

        @Override
        public void onNewFuelCell(FuelCell device) {
            for (EventListener listener : sListeners) {
                listener.onNewFuelCell(device);
            }
        }

        @Override
        public void onNewGasMeter(GasMeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewGasMeter(device);
            }
        }

        @Override
        public void onNewGeneralLighting(GeneralLighting device) {
            for (EventListener listener : sListeners) {
                listener.onNewGeneralLighting(device);
            }
        }
        */
        @Override
        public void onNewHouseholdSolarPowerGeneration(
                HouseholdSolarPowerGeneration device) {
            for (EventListener listener : sListeners) {
                listener.onNewHouseholdSolarPowerGeneration(device);
            }
        }
        /* 
        @Override
        public void onNewInstantaneousWaterHeater(
                InstantaneousWaterHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewInstantaneousWaterHeater(device);
            }
        }

        @Override
        public void onNewLPGasMeter(LPGasMeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewLPGasMeter(device);
            }
        }

        @Override
        public void onNewPowerDistributionBoardMetering(
                PowerDistributionBoardMetering device) {
            for (EventListener listener : sListeners) {
                listener.onNewPowerDistributionBoardMetering(device);
            }
        }
        */ 
        @Override
        public void onNewSmartElectricEnergyMeter(
                SmartElectricEnergyMeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewSmartElectricEnergyMeter(device);
            }
        }
        /* 
        @Override
        public void onNewSmartGasMeter(SmartGasMeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewSmartGasMeter(device);
            }
        }

        @Override
        public void onNewSprinkler(Sprinkler device) {
            for (EventListener listener : sListeners) {
                listener.onNewSprinkler(device);
            }
        }

        @Override
        public void onNewWaterFlowmeter(WaterFlowmeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewWaterFlowmeter(device);
            }
        }

        @Override
        public void onNewWattHourMeter(WattHourMeter device) {
            for (EventListener listener : sListeners) {
                listener.onNewWattHourMeter(device);
            }
        }

        @Override
        public void onNewClothesDryer(ClothesDryer device) {
            for (EventListener listener : sListeners) {
                listener.onNewClothesDryer(device);
            }
        }

        @Override
        public void onNewCombinationMicrowaveOven(
                CombinationMicrowaveOven device) {
            for (EventListener listener : sListeners) {
                listener.onNewCombinationMicrowaveOven(device);
            }
        }

        @Override
        public void onNewCookingHeater(CookingHeater device) {
            for (EventListener listener : sListeners) {
                listener.onNewCookingHeater(device);
            }
        }

        @Override
        public void onNewElectricHotWaterPot(ElectricHotWaterPot device) {
            for (EventListener listener : sListeners) {
                listener.onNewElectricHotWaterPot(device);
            }
        }

        @Override
        public void onNewRefrigerator(Refrigerator device) {
            for (EventListener listener : sListeners) {
                listener.onNewRefrigerator(device);
            }
        }

        @Override
        public void onNewRiceCooker(RiceCooker device) {
            for (EventListener listener : sListeners) {
                listener.onNewRiceCooker(device);
            }
        }

        @Override
        public void onNewWasherAndDryer(WasherAndDryer device) {
            for (EventListener listener : sListeners) {
                listener.onNewWasherAndDryer(device);
            }
        }

        @Override
        public void onNewWashingMachine(WashingMachine device) {
            for (EventListener listener : sListeners) {
                listener.onNewWashingMachine(device);
            }
        }

        @Override
        public void onNewWeighing(Weighing device) {
            for (EventListener listener : sListeners) {
                listener.onNewWeighing(device);
            }
        }

        @Override
        public void onNewController(Controller device) {
            for (EventListener listener : sListeners) {
                listener.onNewController(device);
            }
        }

        @Override
        public void onNewSwitch(Switch device) {
            for (EventListener listener : sListeners) {
                listener.onNewSwitch(device);
            }
        }

        @Override
        public void onNewDisplay(Display device) {
            for (EventListener listener : sListeners) {
                listener.onNewDisplay(device);
            }
        }

        @Override
        public void onNewTelevision(Television device) {
            for (EventListener listener : sListeners) {
                listener.onNewTelevision(device);
            }
        }
        */

    }
}
