package src.main.java.jp.co.smartsolar.smartedge.protocol;

import java.util.ArrayList;
import java.util.List;

import src.main.java.jp.co.smartsolar.smartedge.Echonet;
import src.main.java.jp.co.smartsolar.smartedge.ElFrame;
import src.main.java.jp.co.smartsolar.smartedge.ElProperty;
import src.main.java.jp.co.smartsolar.smartedge.ElSocket;
import src.main.java.jp.co.smartsolar.smartedge.eoj.ElObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.device.DeviceObject;
import src.main.java.jp.co.smartsolar.smartedge.eoj.profile.NodeProfile;
import src.main.java.jp.co.smartsolar.smartedge.node.ElNode;


/**
 * 受信したFrameは一度キューに保存し，ElSocketのreceive関数で処理を行う．
 * 
 *
 */
public abstract class ElProtocol {
	//protected Queue<ElFrame> mSelfFrameQueue = new LinkedList<ElFrame>();

	
	
/*
	protected void sendFrameToSelfNode(ElFrame frame) {
		mSelfFrameQueue.offer(frame);
	}
	public void receiveFrameFromSelfNode() {
		ElFrame frame = mSelfFrameQueue.poll();
		if(frame != null) {
			onReceiveFrameFromSelfNode(frame);
		}
	}
	protected void onReceiveFrameFromSelfNode(ElFrame frame) {
		if(frame.isValid()){
			checkObjectInFrame(frame.copy());
			//onReceiveUDPFrame(frame);
			if(isReportFrame(frame)) {
				onReceiveReport(frame);
			}
			if(isRequestFrame(frame)) {
				List<ElFrame> responses = onReceiveRequest(frame);
				
				for(ElFrame res : responses) {
					if(res.getESV() == ElFrame.ESV_INF) {
						res.setDstEchoAddress(ElSocket.MULTICAST_ADDRESS);
					}
					if(res.getESV() == ElFrame.ESV_SET_NO_RES) {
						return;
					}
					send(res);
				}
			}
			
		}
	}*/
	
	//public abstract void sendToSelf(ElFrame frame);
	//public abstract void sendToOther(ElFrame frame);
	//public abstract void sendToGroup(ElFrame frame);
	public abstract void receive();

	public static abstract class Task {
		protected ElFrame mFrame;
		public Task(ElFrame frame) {
			mFrame = frame;
		}
		public void perform() {
			if(mFrame.isValid()){
				checkObjectInFrame(mFrame.copy());
				Echonet.getEventListener().receiveEvent(mFrame);

				if(isReportFrame(mFrame)) {
					onReceiveReport(mFrame);
				}
				if(isRequestFrame(mFrame)) {
					List<ElFrame> responses = onReceiveRequest(mFrame);
					
					for(ElFrame res : responses) {

						if(res.getESV() == ElFrame.ESV_SET_NO_RES) {
							return;
						}
						if(res.getESV() == ElFrame.ESV_INF) {
							res.setDstEchoAddress(ElSocket.MULTICAST_ADDRESS);
							informAll(res);
						} else {
							respond(res);
						}
					}
				}
				
			}
		}
		protected abstract void respond(ElFrame response);
		protected abstract void informAll(ElFrame response);

		protected static boolean isRequestFrame(ElFrame frame) {

			switch(frame.getESV()) {
			case ElFrame.ESV_SETI: case ElFrame.ESV_SETC:
			case ElFrame.ESV_GET:
			case ElFrame.ESV_INF_REQ:
			case ElFrame.ESV_SET_GET:
			case ElFrame.ESV_INFC:
				return true;
			default:
				return false;
			}
		}
		
		protected static boolean isReportFrame(ElFrame frame) {
			switch(frame.getESV()) {
			case ElFrame.ESV_SETI_SNA:
			case ElFrame.ESV_SET_RES: case ElFrame.ESV_SETC_SNA:
			case ElFrame.ESV_GET_RES: case ElFrame.ESV_GET_SNA:
			case ElFrame.ESV_INF: case ElFrame.ESV_INF_SNA:
			case ElFrame.ESV_INFC_RES:
			case ElFrame.ESV_INFC:
				return true;
			default:
				return false;
			}
		}
		
		
		protected static List<ElFrame> onReceiveRequest(ElFrame frame) {

			ArrayList<ElFrame> responses = new ArrayList<ElFrame>();
			ElNode selfNode = Echonet.getSelfNode();
			if(selfNode == null) {
				return responses;
			}
			if(frame.getDstEchoInstanceCode() == 0) {
				if(frame.getDstEchoClassCode() == NodeProfile.ECHO_CLASS_CODE) {
					ElObject deoj = selfNode.getNodeProfile();
					ElFrame res = onReceiveRequest(deoj, frame);
					if(res != null) {responses.add(res);}
				} else {
					DeviceObject[] deojList = selfNode.getDevices(frame.getDstEchoClassCode());
					for(DeviceObject deoj : deojList) {
						ElFrame res = onReceiveRequest(deoj, frame);
						if(res != null) {responses.add(res);}
					}
				}
			} else {
				ElObject deoj = selfNode.getInstance(frame.getDstEchoClassCode(), frame.getDstEchoInstanceCode());
				if(deoj == null) {return responses;}
				ElFrame res = onReceiveRequest(deoj, frame);
				if(res != null) {responses.add(res);}
			}
			return responses;
		}
		
		protected static ElFrame onReceiveRequest(ElObject deoj, ElFrame frame) {

			ElFrame request = frame.copy();
			request.setDstEchoInstanceCode(deoj.getInstanceCode());
			ElFrame response = deoj.onReceiveRequest(request);
			
			return response;
		}

		protected static void onReceiveReport(ElFrame frame) {
			ElNode node = Echonet.getNode(frame.getSrcEchoAddress());
			ElObject seoj = node.getInstance(frame.getSrcEchoClassCode(),
												frame.getSrcEchoInstanceCode());

			if(seoj == null) {return;}
			seoj.setNode(node);

			// receiver
			ElObject.Receiver receiver = seoj.getReceiver();
			if(receiver != null) {
				receiver.onReceive(seoj, frame);
			}
		}

		
		protected static void checkObjectInFrame(ElFrame frame) {
			if(ElSocket.SELF_ADDRESS.equals(frame.getSrcEchoAddress())) {
				// self node
				return;
			}
			
			// other node
			ElNode node = Echonet.getNode(frame.getSrcEchoAddress());
			boolean flagNewNode = false;
			if(node == null) {
				node = Echonet.addOtherNode(frame.getSrcEchoAddress());
				flagNewNode = true;
				if(node == null) {return;}

				node.getNodeProfile().setNode(node);
			}

			if(frame.getSrcEchoClassCode() == NodeProfile.ECHO_CLASS_CODE
					&& frame.getSrcEchoInstanceCode() == NodeProfile.INSTANCE_CODE_TRANSMISSION_ONLY) {
				//node.get()->getNodeProfile().get()->setInstanceCode(NodeProfile::INSTANCE_CODE_TRANSMISSION_ONLY);
				NodeProfile profile = node.getNodeProfile();
				NodeProfile.Proxy proxy = (NodeProfile.Proxy)profile;
				proxy.setInstanceCode(NodeProfile.INSTANCE_CODE_TRANSMISSION_ONLY);
			}

			boolean flagNewDevice = false;
			ElObject seoj = node.getInstance(frame.getSrcEchoClassCode(), frame.getSrcEchoInstanceCode());
			if(seoj == null) {
				// generate
				// device

				seoj = node.addOtherDevice(frame.getSrcEchoClassCode(), frame.getSrcEchoInstanceCode());
				flagNewDevice = true;

				if(seoj != null) {seoj.setNode(node);}

				//seoj = node.get()->getInstnace(frame.getSrcEchoClassCode(), frame.getSrcEchoInstanceCode());
			}
			if(seoj == null) {
				if(flagNewNode) {
					//Echo.getEventListener().onNewNode(node);
					node.onNew();
				}
				//Echo.getEventListener().onFoundNode(node);
				node.onFound();
				return;
			}
			if(seoj.getEchoClassCode() == NodeProfile.ECHO_CLASS_CODE
					&& (seoj.getInstanceCode() == NodeProfile.INSTANCE_CODE
						|| seoj.getInstanceCode() == NodeProfile.INSTANCE_CODE_TRANSMISSION_ONLY)
					&& (frame.getESV() == ElFrame.ESV_GET_RES
						|| frame.getESV() == ElFrame.ESV_GET_SNA
						|| frame.getESV() == ElFrame.ESV_INF
						|| frame.getESV() == ElFrame.ESV_INF_SNA
						|| frame.getESV() == ElFrame.ESV_INFC)) {
				// seoj is NodeProfile
				List<ElObject> foundDevices = new ArrayList<ElObject>();
				List<Boolean> flagNewDevices = new ArrayList<Boolean>();

				for(ElProperty p : frame.getPropertyList()) {
					if(p.epc != NodeProfile.EPC_INSTANCE_LIST_NOTIFICATION
						&& p.epc != NodeProfile.EPC_SELF_NODE_INSTANCE_LIST_S) {continue;}
					if(p.pdc == 0) {continue;}
					int deviceListSize = (int)p.edt[0];
					if(deviceListSize > 84) {
						deviceListSize = 84;
					}
					for(int d = 0, i = 1; d < deviceListSize; d++) {
						if(i == p.pdc) break;
						short echoClassCode = (short)(((p.edt[i]) & 0xFF) << 8);
						i += 1;
						if(i == p.pdc) break;
						echoClassCode += p.edt[i] & 0xFF;
						i += 1;
						if(i == p.pdc) break;
						byte echoInstanceCode = p.edt[i];
						i += 1;
						if(node.containsDevice(echoClassCode, echoInstanceCode)) {
							flagNewDevices.add(false);
							foundDevices.add(node.getInstance(echoClassCode, echoInstanceCode));
						} else {
							// new
							flagNewDevices.add(true);
							ElObject eoj = node.addOtherDevice(echoClassCode, echoInstanceCode);
							foundDevices.add(eoj);
							if(eoj != null) {eoj.setNode(node);}
						}
					}
				}

				if(flagNewNode) {
					//Echo.getEventListener().onNewNode(node);
					node.onNew();
				}
				//Echo.getEventListener().onFoundNode(node);
				node.onFound();
				if(flagNewDevice) {
					//Echo.getEventListener().onNewElObject(seoj);
					seoj.onNew();
				}
				//Echo.getEventListener().onFoundElObject(seoj);
				seoj.onFound();
				int foundDeviceListSize = foundDevices.size();
				for(int i = 0; i < foundDeviceListSize; i++) {
					if(flagNewDevices.get(i)) {
						//Echo.getEventListener().onNewElObject(foundDevices.get(i));
						foundDevices.get(i).onNew();
					}
					//Echo.getEventListener().onFoundElObject(foundDevices.get(i));
					foundDevices.get(i).onFound();
				}
			} else {
				// seoj is DeviceObject
				if(flagNewNode) {
					//Echo.getEventListener().onNewNode(node);
					node.onNew();
				}
				//Echo.getEventListener().onFoundNode(node);
				node.onFound();
				if(flagNewDevice) {
					//Echo.getEventListener().onNewElObject(seoj);
					seoj.onNew();
				}
				//Echo.getEventListener().onFoundElObject(seoj);
				seoj.onFound();
				return;
			}
		}
	}

}