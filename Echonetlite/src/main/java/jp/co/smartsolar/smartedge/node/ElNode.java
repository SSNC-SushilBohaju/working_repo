package src.main.java.jp.co.smartsolar.smartedge.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import src.main.java.jp.co.smartsolar.smartedge.Echonet;
import src.main.java.jp.co.smartsolar.smartedge.ElSocket;
import src.main.java.jp.co.smartsolar.smartedge.ElUtils;
import src.main.java.jp.co.smartsolar.smartedge.eoj.ElObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.DeviceObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.householdpowergeneration.HouseholdSolarPowerGeneration;
import src.main.java.jp.co.smartsolar.smartedge.eoj.profile.NodeProfile;


public final class ElNode {
	private static HashMap<Short, DeviceProxyCreator> mProxyCreators = new HashMap<Short, DeviceProxyCreator>();
	
	private NodeProfile mNodeProfile;
	private List<DeviceObject> mDevices = new ArrayList<DeviceObject>();
	private String mAddress;
	
	public ElNode(NodeProfile nodeProfile, DeviceObject[] devices) {
		// selfNode
		mAddress = ElSocket.SELF_ADDRESS;
		mNodeProfile = nodeProfile;
		for(DeviceObject d : devices) {
			if(isSelfNode()) {
				d.allocateSelfDeviceInstanceCode();
			}
			mDevices.add(d);
		}
		
	}
	
	public ElNode(String address) {
		// otherNode
		mAddress = address;
		mNodeProfile = new NodeProfile.Proxy();
	}

	public void onNew() {
		Echonet.getEventListener().onNewNode(this);
	}
	
	public void onFound() {
		Echonet.getEventListener().onFoundNode(this);
	}
	
	public boolean isSelfNode() {
		return ElSocket.SELF_ADDRESS.equals(mAddress);
	}
	
	public boolean isProxy() {
		return !(ElSocket.SELF_ADDRESS.equals(mAddress));
	}
	
	public InetAddress getAddress() {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(mAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	
	public String getAddressStr() {
		return mAddress;
	}
	
	public NodeProfile getNodeProfile() {
		return mNodeProfile;
	}
	
	public DeviceObject addOtherDevice(short EchoClassCode, byte EchoInstanceCode) {
		DeviceObject device = newOtherDevice(EchoClassCode, EchoInstanceCode);
		addDevice(device);
		return device;
	}
	
	public void addDevice(DeviceObject device) {
		if(device == null) return;
		if(device.getNode() == this) return;

		mDevices.add(device);
		if(isSelfNode()) {
			device.allocateSelfDeviceInstanceCode();
			device.setNode(this);
			device.onNew();
			device.onFound();
		}
		/*
		short code = device.getEchonetClassCode();
		if(mDeviceGroups.containsKey(code)) {
			List<DeviceObject> deviceList = mDeviceGroups.get(code);
			if(deviceList.size() > 0x7F) return;
			deviceList.add(device);
		} else {
			List<DeviceObject> deviceList = new ArrayList<DeviceObject>();
			deviceList.add(device);
			mDeviceGroups.put(code, deviceList);
		}
		if(mInitialized) {
			device.initialize(this);
		}*/
	}
	
	
	public void removeDevice(DeviceObject device) {
		if(device == null) return;
		if(device.getNode() != this) return;
		device.setNode(null);
		// TODO: Release allocated instance code because new instance code is generated when addDevice is called
		mDevices.remove(device);
	}
	
	public boolean containsDevice(short EchonetClassCode, byte EchonetInstanceCode) {
		for(DeviceObject d : mDevices) {
			if(d.getEchoClassCode() == EchonetClassCode
					&& d.getInstanceCode() == EchonetInstanceCode) {
				return true;
			}
		}
		return false;
	}

	public boolean containsDevice(DeviceObject device) {
		if(device == null) return false;
		if(device.getNode() != this) return false;
		return mDevices.contains(device);
	}

	public ElObject getInstance(byte classGroupCode, byte classCode, byte instanceCode) {
		return getInstance(ElUtils.getEchoClassCode(classGroupCode, classCode), instanceCode);
	}
	
	public ElObject getInstance(short EchonetClassCode, byte EchonetInstanceCode) {
		if(mNodeProfile.getEchoClassCode() == EchonetClassCode
				&& mNodeProfile.getInstanceCode() == EchonetInstanceCode) {
			return mNodeProfile;
		}
		return getDevice(EchonetClassCode, EchonetInstanceCode);
	}
	
	
	public boolean containsInstance(byte classGroupCode, byte classCode, byte instanceCode) {
		short EchonetClassCode = ElUtils.getEchoClassCode(classGroupCode, classCode);
		return containsInstance(EchonetClassCode, instanceCode);
	}
	
	public boolean containsInstance(short EchonetClassCode, byte EchonetInstanceCode) {
		if(mNodeProfile.getEchoClassCode() == EchonetClassCode
				&& mNodeProfile.getInstanceCode() == EchonetInstanceCode) {
			return true;
		}
	
		return containsDevice(EchonetClassCode, EchonetInstanceCode);
	}
	
	public DeviceObject getDevice(byte classGroupCode, byte classCode, byte instanceCode) {
		return getDevice(ElUtils.getEchoClassCode(classGroupCode, classCode), instanceCode);
	}
	
	public DeviceObject getDevice(short EchonetClassCode, byte EchonetInstanceCode) {
		for(DeviceObject d : mDevices) {
			if(d.getEchoClassCode() == EchonetClassCode
					&& d.getInstanceCode() == EchonetInstanceCode) {
				return d;
			}
		}
		return null;
	}
	
	public DeviceObject[] getDevices(byte classGroupCode, byte classCode) {
		return getDevices(ElUtils.getEchoClassCode(classGroupCode, classCode));
	}
	
	public DeviceObject[] getDevices(short EchonetClassCode) {
		List<DeviceObject> ret = new ArrayList<DeviceObject>();
		for(DeviceObject d : mDevices) {
			if(d.getEchoClassCode() == EchonetClassCode) {
				ret.add(d);
			}
		}
		return ret.toArray(new DeviceObject[]{});
	}
	
	public DeviceObject[] getDevices() {
		return (DeviceObject[]) mDevices.toArray(new DeviceObject[]{});
	}
	
	private static DeviceObject newOtherDevice(short EchoClassCode, byte instanceCode) {
		if(mProxyCreators.containsKey(EchoClassCode)) {
			return null;
		}
		switch(EchoClassCode) {
		case HouseholdSolarPowerGeneration.ECHO_CLASS_CODE: return new HouseholdSolarPowerGeneration.Proxy(instanceCode);
		/*
		case SmartElectricEnergyMeter.Echo_CLASS_CODE: return new SmartElectricEnergyMeter.Proxy(instanceCode);
		 
		case ActivityAmountSensor.Echonet_CLASS_CODE: return new ActivityAmountSensor.Proxy(instanceCode);
		case AirPollutionSensor.Echonet_CLASS_CODE: return new AirPollutionSensor.Proxy(instanceCode);
		case AirSpeedSensor.Echonet_CLASS_CODE: return new AirSpeedSensor.Proxy(instanceCode);
		case BathHeatingStatusSensor.Echonet_CLASS_CODE: return new BathHeatingStatusSensor.Proxy(instanceCode);
		case BathWaterLevelSensor.Echonet_CLASS_CODE: return new BathWaterLevelSensor.Proxy(instanceCode);
		case BedPresenceSensor.Echonet_CLASS_CODE: return new BedPresenceSensor.Proxy(instanceCode);
		case CallSensor.Echonet_CLASS_CODE: return new CallSensor.Proxy(instanceCode);
		case CigaretteSmokeSensor.Echonet_CLASS_CODE: return new CigaretteSmokeSensor.Proxy(instanceCode);
		case CO2Sensor.Echonet_CLASS_CODE: return new CO2Sensor.Proxy(instanceCode);
		case CondensationSensor.Echonet_CLASS_CODE: return new CondensationSensor.Proxy(instanceCode);
		case CrimePreventionSensor.Echonet_CLASS_CODE: return new CrimePreventionSensor.Proxy(instanceCode);
		
		case DifferentialPressureSensor.Echonet_CLASS_CODE: return new DifferentialPressureSensor.Proxy(instanceCode);
		case EarthquakeSensor.Echonet_CLASS_CODE: return new EarthquakeSensor.Proxy(instanceCode);
		case ElectricEnergySensor.Echonet_CLASS_CODE: return new ElectricEnergySensor.Proxy(instanceCode);
		case ElectricLeakSensor.Echonet_CLASS_CODE: return new ElectricLeakSensor.Proxy(instanceCode);
		case EmergencyButton.Echonet_CLASS_CODE: return new EmergencyButton.Proxy(instanceCode);
		case FireSensor.Echonet_CLASS_CODE: return new FireSensor.Proxy(instanceCode);
		case FirstAidSensor.Echonet_CLASS_CODE: return new FirstAidSensor.Proxy(instanceCode);
		case FlameSensor.Echonet_CLASS_CODE: return new FlameSensor.Proxy(instanceCode);
		case GasLeakSensor.Echonet_CLASS_CODE: return new GasLeakSensor.Proxy(instanceCode);
		case GasSensor.Echonet_CLASS_CODE: return new GasSensor.Proxy(instanceCode);
		case HumanBodyLocationSensor.Echonet_CLASS_CODE: return new HumanBodyLocationSensor.Proxy(instanceCode);
		case HumanDetectionSensor.Echonet_CLASS_CODE: return new HumanDetectionSensor.Proxy(instanceCode);
		case HumiditySensor.Echonet_CLASS_CODE: return new HumiditySensor.Proxy(instanceCode);
		case IlluminanceSensor.Echonet_CLASS_CODE: return new IlluminanceSensor.Proxy(instanceCode);
		case MailingSensor.Echonet_CLASS_CODE: return new MailingSensor.Proxy(instanceCode);
		case MicromotionSensor.Echonet_CLASS_CODE: return new MicromotionSensor.Proxy(instanceCode);
		case OdorSensor.Echonet_CLASS_CODE: return new OdorSensor.Proxy(instanceCode);
		case OpenCloseSensor.Echonet_CLASS_CODE: return new OpenCloseSensor.Proxy(instanceCode);
		case OxygenSensor.Echonet_CLASS_CODE: return new OxygenSensor.Proxy(instanceCode);
		case PassageSensor.Echonet_CLASS_CODE: return new PassageSensor.Proxy(instanceCode);
		case RainSensor.Echonet_CLASS_CODE: return new RainSensor.Proxy(instanceCode);
		case SnowSensor.Echonet_CLASS_CODE: return new SnowSensor.Proxy(instanceCode);
		case SoundSensor.Echonet_CLASS_CODE: return new SoundSensor.Proxy(instanceCode);
		case TemperatureSensor.Echonet_CLASS_CODE: return new TemperatureSensor.Proxy(instanceCode);
		case VisitorSensor.Echonet_CLASS_CODE: return new VisitorSensor.Proxy(instanceCode);
		case VOCSensor.Echonet_CLASS_CODE: return new VOCSensor.Proxy(instanceCode);
		case WaterFlowRateSensor.Echonet_CLASS_CODE: return new WaterFlowRateSensor.Proxy(instanceCode);
		case WaterLeakSensor.Echonet_CLASS_CODE: return new WaterLeakSensor.Proxy(instanceCode);
		case WaterLevelSensor.Echonet_CLASS_CODE: return new WaterLevelSensor.Proxy(instanceCode);
		case WaterOverflowSensor.Echonet_CLASS_CODE: return new WaterOverflowSensor.Proxy(instanceCode);
		case WeightSensor.Echonet_CLASS_CODE: return new WeightSensor.Proxy(instanceCode);
		case AirCleaner.Echonet_CLASS_CODE: return new AirCleaner.Proxy(instanceCode);
		case AirConditionerVentilationFan.Echonet_CLASS_CODE: return new AirConditionerVentilationFan.Proxy(instanceCode);
		case ElectricHeater.Echonet_CLASS_CODE: return new ElectricHeater.Proxy(instanceCode);
		case FanHeater.Echonet_CLASS_CODE: return new FanHeater.Proxy(instanceCode);
		case HomeAirConditioner.Echonet_CLASS_CODE: return new HomeAirConditioner.Proxy(instanceCode);
		case Humidifier.Echonet_CLASS_CODE: return new Humidifier.Proxy(instanceCode);
		case PackageTypeCommercialAirConditionerIndoorUnit.Echonet_CLASS_CODE: return new PackageTypeCommercialAirConditionerIndoorUnit.Proxy(instanceCode);
		case PackageTypeCommercialAirConditionerOutdoorUnit.Echonet_CLASS_CODE: return new PackageTypeCommercialAirConditionerOutdoorUnit.Proxy(instanceCode);
		case VentilationFan.Echonet_CLASS_CODE: return new VentilationFan.Proxy(instanceCode);
		case BathroomHeaterAndDryer.Echonet_CLASS_CODE: return new BathroomHeaterAndDryer.Proxy(instanceCode);
		case Battery.Echonet_CLASS_CODE: return new Battery.Proxy(instanceCode);
		case Buzzer.Echonet_CLASS_CODE: return new Buzzer.Proxy(instanceCode);
		case ColdOrHotWaterHeatSourceEquipment.Echonet_CLASS_CODE: return new ColdOrHotWaterHeatSourceEquipment.Proxy(instanceCode);
		case ElectricallyOperatedShade.Echonet_CLASS_CODE: return new ElectricallyOperatedShade.Proxy(instanceCode);
		case ElectricLock.Echonet_CLASS_CODE: return new ElectricLock.Proxy(instanceCode);
		case ElectricShutter.Echonet_CLASS_CODE: return new ElectricShutter.Proxy(instanceCode);
		case ElectricStormWindow.Echonet_CLASS_CODE: return new ElectricStormWindow.Proxy(instanceCode);
		case ElectricToiletSeat.Echonet_CLASS_CODE: return new ElectricToiletSeat.Proxy(instanceCode);
		case ElectricVehicle.Echonet_CLASS_CODE: return new ElectricVehicle.Proxy(instanceCode);
		case ElectricWaterHeater.Echonet_CLASS_CODE: return new ElectricWaterHeater.Proxy(instanceCode);
		case EngineCogeneration.Echonet_CLASS_CODE: return new EngineCogeneration.Proxy(instanceCode);
		case FloorHeater.Echonet_CLASS_CODE: return new FloorHeater.Proxy(instanceCode);
		case FuelCell.Echonet_CLASS_CODE: return new FuelCell.Proxy(instanceCode);
		case GasMeter.Echonet_CLASS_CODE: return new GasMeter.Proxy(instanceCode);
		case GeneralLighting.Echonet_CLASS_CODE: return new GeneralLighting.Proxy(instanceCode);
		
		case InstantaneousWaterHeater.Echonet_CLASS_CODE: return new InstantaneousWaterHeater.Proxy(instanceCode);
		case LPGasMeter.Echonet_CLASS_CODE: return new LPGasMeter.Proxy(instanceCode);
		case PowerDistributionBoardMetering.Echonet_CLASS_CODE: return new PowerDistributionBoardMetering.Proxy(instanceCode);
		
		case SmartGasMeter.Echonet_CLASS_CODE: return new SmartGasMeter.Proxy(instanceCode);
		case Sprinkler.Echonet_CLASS_CODE: return new Sprinkler.Proxy(instanceCode);
		case WaterFlowmeter.Echonet_CLASS_CODE: return new WaterFlowmeter.Proxy(instanceCode);
		
		case ClothesDryer.Echonet_CLASS_CODE: return new ClothesDryer.Proxy(instanceCode);
		case CombinationMicrowaveOven.Echonet_CLASS_CODE: return new CombinationMicrowaveOven.Proxy(instanceCode);
		case CookingHeater.Echonet_CLASS_CODE: return new CookingHeater.Proxy(instanceCode);
		case ElectricHotWaterPot.Echonet_CLASS_CODE: return new ElectricHotWaterPot.Proxy(instanceCode);
		case Refrigerator.Echonet_CLASS_CODE: return new Refrigerator.Proxy(instanceCode);
		case RiceCooker.Echonet_CLASS_CODE: return new RiceCooker.Proxy(instanceCode);
		case WasherAndDryer.Echonet_CLASS_CODE: return new WasherAndDryer.Proxy(instanceCode);
		case WashingMachine.Echonet_CLASS_CODE: return new WashingMachine.Proxy(instanceCode);
		case Weighing.Echonet_CLASS_CODE: return new Weighing.Proxy(instanceCode);
		case Controller.Echonet_CLASS_CODE: return new Controller.Proxy(instanceCode);
		case Switch.Echonet_CLASS_CODE: return new Switch.Proxy(instanceCode);
		case Display.Echonet_CLASS_CODE: return new Display.Proxy(instanceCode);
		case Television.Echonet_CLASS_CODE: return new Television.Proxy(instanceCode);
		default: return new DeviceObject.Proxy(EchonetClassCode, instanceCode);

		*/
		// case WattHourMeter.ECHO_CLASS_CODE: return new WattHourMeter.Proxy(instanceCode);
		// case CurrentValueSensorClass.ECHO_CLASS_CODE: return new CurrentValueSensorClass.Proxy(instanceCode);
		default: return new DeviceObject.Proxy(EchoClassCode, instanceCode);
		}
	}
	public static void putDeviceProxyCreator(short EchonetClassCode, DeviceProxyCreator creator) {
		mProxyCreators.put(EchonetClassCode, creator);
	}
	public static void removeDeviceProxyCreator(short EchonetClassCode) {
		mProxyCreators.remove(EchonetClassCode);
	}
	
	public static interface DeviceProxyCreator {
		public DeviceObject create(byte instanceCode);
	}

}