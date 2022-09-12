package src.main.java.jp.co.smartsolar.smartedge.eoj;

import java.io.IOException;
//import java.net.InetAddress;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.List;

import src.main.java.jp.co.smartsolar.smartedge.Echonet;
import src.main.java.jp.co.smartsolar.smartedge.ElFrame;
import src.main.java.jp.co.smartsolar.smartedge.ElProperty;
import src.main.java.jp.co.smartsolar.smartedge.ElSocket;
import src.main.java.jp.co.smartsolar.smartedge.ElUtils;
import src.main.java.jp.co.smartsolar.smartedge.eoj.profile.NodeProfile;
import src.main.java.jp.co.smartsolar.smartedge.node.ElNode;


public abstract class ElObject {

	private ElNode mNode = null;
	
	
	private Receiver mReceiver = null;
	
	

	private HashSet<Byte> mStatusChangeAnnouncementProperties;
	private HashSet<Byte> mSetProperties;
	private HashSet<Byte> mGetProperties;
	
	public ElObject() {
		super();

		mStatusChangeAnnouncementProperties = new HashSet<Byte>();
		mSetProperties = new HashSet<Byte>();
		mGetProperties = new HashSet<Byte>();
		
		setupPropertyMaps();
	}
	
	public void onNew() {
		Echonet.getEventListener().onNewElObject(this);
	}
	
	public void onFound() {
		Echonet.getEventListener().onFoundElObject(this);
	}
	
	protected void setupPropertyMaps() {}

	protected final void addStatusChangeAnnouncementProperty(byte epc) {
		mStatusChangeAnnouncementProperties.add(epc);
	}

	protected final void removeStatusChangeAnnouncementProperty(byte epc) {
		if(mStatusChangeAnnouncementProperties.contains(epc))
			mStatusChangeAnnouncementProperties.remove(epc);
	}
	
	protected final void clearStatusChangeAnnouncementProperties() {
		mStatusChangeAnnouncementProperties.clear();
	}
	
	public final byte[] getStatusChangeAnnouncementProperties() {
		byte[] ret = new byte[mStatusChangeAnnouncementProperties.size()];
		Iterator<Byte> it = mStatusChangeAnnouncementProperties.iterator();
		for(int i = 0; i < ret.length; i++) {
			ret[i] = it.next();
		}
		return ret;
	}
	
	protected final void addSetProperty(byte epc) {
		mSetProperties.add(epc);
	}

	
	protected final void removeSetProperty(byte epc) {
		if(mSetProperties.contains(epc))
			mSetProperties.remove(epc);
	}
	
	protected final void clearSetProperties() {
		mSetProperties.clear();
	}
	
	public final byte[] getSetProperties() {
		byte[] ret = new byte[mSetProperties.size()];
		Iterator<Byte> it = mSetProperties.iterator();
		for(int i = 0; i < ret.length; i++) {
			ret[i] = it.next();
		}
		return ret;
	}
	
	protected final void addGetProperty(byte epc) {
		mGetProperties.add(epc);
	}
	
	protected final void removeGetProperty(byte epc) {
		if(mGetProperties.contains(epc))
			mGetProperties.remove(epc);
	}
	
	protected final void clearGetProperties() {
		mGetProperties.clear();
	}
	
	public final byte[] getGetProperties() {
		byte[] ret = new byte[mGetProperties.size()];
		Iterator<Byte> it = mGetProperties.iterator();
		for(int i = 0; i < ret.length; i++) {
			ret[i] = it.next();
		}
		return ret;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("groupCode:");
		sb.append(String.format("%02x", getClassGroupCode()));
		sb.append(",classCode:");
		sb.append(String.format("%02x", getClassCode()));
		sb.append(",instanceCode:");
		sb.append(String.format("%02x", getInstanceCode()));
		sb.append(",address:");
		if(getNode() != null) {
			sb.append(getNode().getAddressStr());
		}
		return new String(sb);
	}
	
	public final byte getClassGroupCode() {
		short code = getEchoClassCode();
		return (byte)((code >> 8) & 0xFF);
	}
	
	public final byte getClassCode() {
		short code = getEchoClassCode();
		return (byte)(code & 0xFF);
	}
	
	public abstract byte getInstanceCode();
	
	public abstract short getEchoClassCode();
	
	public final int getElObjectCode() {
		return ElUtils.getElObjectCode(getEchoClassCode(), getInstanceCode());
	}
	
	public final void setNode(ElNode node) {
		mNode = node;
	}
	
	public final ElNode getNode() {
		return mNode;
	}
	
	public final void removeNode() {
		mNode = null;
	}

	public final boolean isSelfObject() {
		ElNode node = getNode();
		if(node==null) return false;
		else return node.isSelfNode();
		
	}
	
	public final boolean isProxy() {
		ElNode node = getNode();
		if(node==null) return true;
		else return node.isProxy();
	}
	
	
	protected synchronized boolean setProperty(ElProperty property) {
		return false;
	}
	
	protected synchronized byte[] getProperty(byte epc) {
		return null;
	}
	
	protected synchronized boolean isValidProperty(ElProperty property) {
		return false;
	}
	
	public final void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}
	
	public final Receiver getReceiver() {
		return mReceiver;
	}
		
	public final ElFrame onReceiveRequest(ElFrame frame) {
		/*
		ElProperty[] properties = frame.getProperties();
		Echo.EventListener listener = Echo.getEventListener();
		
		// receive response
		for(ElProperty p : properties) {
			try {
				switch(frame.getESV()) {
				case ElFrame.ESV_SET_RES: case ElFrame.ESV_SETI_SNA: case ElFrame.ESV_SETC_SNA:
					if(mReceiver != null) mReceiver.onSetProperty(this, frame.getTid(), frame.getESV(), p, (p.pdc == 0));
					if(listener != null) listener.onSetProperty(this, frame.getTid(), frame.getESV(), p, (p.pdc == 0));
					break;
				case ElFrame.ESV_GET_RES: case ElFrame.ESV_GET_SNA:
				case ElFrame.ESV_INF: case ElFrame.ESV_INF_SNA:
				case ElFrame.ESV_INFC:
					onReceiveProperty(p);
					if(mReceiver != null) mReceiver.onGetProperty(this, frame.getTid(), frame.getESV(), p, (p.pdc != 0));
					if(listener != null) listener.onGetProperty(this, frame.getTid(), frame.getESV(), p, (p.pdc != 0));
					break;
				case ElFrame.ESV_INFC_RES:
					if(mReceiver != null) mReceiver.onInformProperty(this, frame.getTid(), frame.getESV(), p);
					if(listener != null) listener.onInformProperty(this, frame.getTid(), frame.getESV(), p);
					break;
				}
			} catch(Exception e) {
				try{if(listener != null) listener.onCatchException(e);}catch(Exception ex){}
			}
		}*/

		// receive request
		byte esv = 0;
		switch(frame.getESV()) {
		case ElFrame.ESV_SETI:
			esv = ElFrame.ESV_SET_NO_RES;
			break;
		case ElFrame.ESV_SETC:
			esv = ElFrame.ESV_SET_RES;
			break;
		case ElFrame.ESV_GET:
			esv = ElFrame.ESV_GET_RES;
			break;
		case ElFrame.ESV_INF_REQ:
			esv = ElFrame.ESV_INF;
			break;
		case ElFrame.ESV_INFC:
			esv =  ElFrame.ESV_INFC_RES;
			break;
		case ElFrame.ESV_SET_GET:
			esv = ElFrame.ESV_SET_GET_SNA;
			break;
		}
		ElFrame response = new ElFrame(frame.getDstEchoClassCode(), frame.getDstEchoInstanceCode()
				, frame.getSrcEchoClassCode(), frame.getSrcEchoInstanceCode()
				, frame.getSrcEchoAddress(), esv);
		response.setTID(frame.getTID());
		switch(frame.getESV()) {
		case ElFrame.ESV_SETI:
		case ElFrame.ESV_SETC:
			for(ElProperty p : frame.getPropertyList()) {
				onReceiveSetRequest(p, response);
			}
			break;
		case ElFrame.ESV_GET:
		case ElFrame.ESV_INF_REQ:
			for(ElProperty p : frame.getPropertyList()) {
				onReceiveGetRequest(p.epc, response);
			}
			break;
		case ElFrame.ESV_INFC:
			for(ElProperty p : frame.getPropertyList()) {
				response.addPropertyForResponse(p.epc);
			}
			break;
		}
		return response;

		
	}

	protected final void onReceiveSetRequest(ElProperty property, ElFrame response) {
		boolean success = false;
		Echonet.EventListener listener = Echonet.getEventListener();
		try {
			if(mSetProperties.contains(property.epc)) {
				boolean valid = isValidProperty(property);
				if(listener != null) listener.isValidProperty(this, property, valid);
				if(valid) {
					// valid property
					success = setProperty(property);
					if(listener != null) listener.setProperty(this, property, success);
				} else {
					// invalid property
					success = false;
				}
			} else {
				success = false;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			success = false;
			try{if(listener != null) listener.onCatchException(e);}catch(Exception ex){}
		}

		if(success) {
			response.addPropertyForResponse(property.epc);
		} else {
			response.addPropertyForResponse(property.epc, property.edt);
		}
	}
	
	protected final void onReceiveGetRequest(byte epc, ElFrame response) {
		byte[] edt = null;
		Echonet.EventListener listener = Echonet.getEventListener();
		try {
			if(response.getESV() == ElFrame.ESV_GET_RES || response.getESV() == ElFrame.ESV_GET_SNA) {
				edt = mGetProperties.contains(epc) ? getProperty(epc) : null;
			} else {
				edt = getProperty(epc);
			}
			ElProperty property = new ElProperty(epc, edt);
			listener.getProperty(this, property);
			boolean valid = isValidProperty(property);
			if(listener != null) listener.isValidProperty(this, property, valid);
			if(valid) {
				// valid property
			} else {
				// invalid property
				edt = null;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			edt = null;
			try{if(listener != null) listener.onCatchException(e);}catch(Exception ex){}
		}
		response.addPropertyForResponse(epc, edt);
	}

	public Setter set() {
		return set(true);
	}
	
	public Setter set(boolean responseRequired) {
		return new Setter(getEchoClassCode(), getInstanceCode()
				, getNode().getAddressStr(), responseRequired);
	}
	
	public Getter get() {
		return new Getter(getEchoClassCode(), getInstanceCode()
				, getNode().getAddressStr());
	}
	
	public Informer inform() {
		return inform(isSelfObject());
	}
	
	protected Informer inform(boolean multicast) {

		String address;
		if(multicast) {
			address = ElSocket.MULTICAST_ADDRESS;
		} else {
			address = getNode().getAddressStr();
		}
		return new Informer(getEchoClassCode(), getInstanceCode()
				, address, isSelfObject());
	}
	
	protected InformerC informC(String address) {
		return new InformerC(getEchoClassCode(), getInstanceCode()
				, address);
	}
	
	
	public static class Receiver {
		
		public void onReceive(ElObject eoj, ElFrame frame){
			onReceiveFrame(eoj, frame);

			switch(frame.getESV()) {
			case ElFrame.ESV_SET_RES: case ElFrame.ESV_SETI_SNA: case ElFrame.ESV_SETC_SNA:
				for(ElProperty p : frame.getPropertyList()) {
					onSetProperty(eoj, frame.getTID(), frame.getESV(), p, (p.pdc == 0));
				}
				break;
			case ElFrame.ESV_GET_RES: case ElFrame.ESV_GET_SNA:
			case ElFrame.ESV_INF: case ElFrame.ESV_INF_SNA:
			case ElFrame.ESV_INFC:
				for(ElProperty p : frame.getPropertyList()) {
					onGetProperty(eoj, frame.getTID(), frame.getESV(), p, (p.pdc != 0));
				}
				break;
			case ElFrame.ESV_INFC_RES:
				for(ElProperty p : frame.getPropertyList()) {
					onInformProperty(eoj, frame.getTID(), frame.getESV(), p);
				}
				break;
			}
		}
		
		public void onReceiveFrame(ElObject eoj, ElFrame frame) {
			
		}
		
		protected boolean onSetProperty(ElObject eoj, short tid, byte esv, ElProperty property, boolean success) {
			return false;
		}
		
		protected boolean onGetProperty(ElObject eoj, short tid, byte esv, ElProperty property, boolean success) {
			return false;
		}
		
		protected boolean onInformProperty(ElObject eoj, short tid, byte esv, ElProperty property) {
			return false;
		}
		
	}
	
	protected static abstract class Sender {
		protected short mSrcEchoClassCode;
		protected byte mSrcEchoInstanceCode;
		protected short mDstEchoClassCode;
		protected byte mDstEchoInstanceCode;
		protected String mDstEchoAddress;
		protected byte mESV;
		
		
		public Sender(short srcEchoClassCode, byte srcEchoInstanceCode
				, short dstEchoClassCode, byte dstEchoInstanceCode
				, String dstEchoAddress, byte esv) {
			mSrcEchoClassCode = srcEchoClassCode;
			mSrcEchoInstanceCode = srcEchoInstanceCode;
			mDstEchoClassCode = dstEchoClassCode;
			mDstEchoInstanceCode = dstEchoInstanceCode;
			mDstEchoAddress = dstEchoAddress;
			mESV = esv;
		}
		/*
		protected ElFrame send() throws IOException {
			short tid = ElSocket.getNextTID();
			ElFrame frame = new ElFrame(tid, mSeoj, mDeoj, mEsv);
			switch(mEsv) {
			case ElFrame.ESV_SETI : case ElFrame.ESV_SETC :
				for(ElProperty p : mPropertyList) {
					if(isValidProperty(p)) {
						frame.addProperty(p);
					}
				}
				break;
			case ElFrame.ESV_GET : case ElFrame.ESV_INF_REQ :
				for(ElProperty p : mPropertyList) {
					frame.addProperty(p);
				}
				break;
			case ElFrame.ESV_INF : case ElFrame.ESV_INFC :
				
				for(ElProperty p : mPropertyList) {
					ElProperty pp = new ElProperty(p.epc, mEoj.getProperty(p.epc));
					if(isValidProperty(pp)) {
						frame.addProperty(pp);
					}
				}
				break;
			}
			byte[] data = frame.getFrameByteArray();
			
			if (mMulticast) {
				ElSocket.sendGroup(data);
			} else {
				//ElSocket.send(mEoj.getNode().getAddress(), data);
				if(getDeoj().getNode() == null) {
					throw new IOException("Not found target node.");
				}
				ElSocket.send(getDeoj().getNode().getAddress(), data);
			}
			//if(Echo.getMethodInvokedListener() == null) return;
			//Echo.getMethodInvokedListener().onInvokedSendMethod(frame);
			
			
			Echo.EventListener listener = Echo.getEventListener();
			try {
				if(listener != null) listener.sendEvent(frame);
			} catch(Exception e) {
				try{if(listener != null) listener.onCatchException(e);}catch(Exception ex){}
			}
			return tid;
		}*/

		abstract ElFrame send() throws IOException ;
		public void send(ElFrame frame) throws IOException {
			short tid = ElSocket.nextTID();
			frame.setTID(tid);
			ElSocket.sendUDPFrame(frame);
		}
		abstract ElFrame sendTCP() throws IOException ;
		public void sendTCP(ElFrame frame) throws IOException {
			short tid = ElSocket.nextTID();
			frame.setTID(tid);
			ElSocket.sendTCPFrame(frame);
		}
		
		public void setSeoj(short srcEchoClassCode, byte srcEchoInstanceCode) {
			mSrcEchoClassCode = srcEchoClassCode;
			mSrcEchoInstanceCode = srcEchoInstanceCode;
		}
		
		
		public void setDeoj(short dstEchoClassCode, byte dstEchoInstanceCode
				, String dstEchoAddress) {
			mDstEchoClassCode = dstEchoClassCode;
			mDstEchoInstanceCode = dstEchoInstanceCode;
			mDstEchoAddress = dstEchoAddress;
		}

	}
	
	public static class Setter extends Sender {

		
		protected ArrayList<ElProperty> mPropertyList;
		public Setter(short dstEchoClassCode, byte dstEchoInstanceCode
				, String dstEchoAddress, boolean responseRequired) {
			super(NodeProfile.ECHO_CLASS_CODE
				, Echonet.getSelfNode().getNodeProfile().getInstanceCode()
				, dstEchoClassCode, dstEchoInstanceCode, dstEchoAddress
					, responseRequired ? ElFrame.ESV_SETC : ElFrame.ESV_SETI);
			mPropertyList = new ArrayList<ElProperty>();
		}

		@Override
		public ElFrame send() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			for(ElProperty p : mPropertyList) {
				frame.addProperty(p);
			}
			send(frame);
			return frame;
		}

		@Override
		public ElFrame sendTCP() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			for(ElProperty p : mPropertyList) {
				frame.addProperty(p);
			}
			sendTCP(frame);
			return frame;
		}
		
		public Setter reqSetProperty(byte epc, byte[] edt) {
			mPropertyList.add(new ElProperty(epc, edt));
			return this;
		}


	}
	
	public static class Getter extends Sender {
		protected ArrayList<Byte> mEPCList;

		
		
		public Getter(short dstEchoClassCode, byte dstEchoInstanceCode
				, String dstEchoAddress) {
			super(NodeProfile.ECHO_CLASS_CODE
					, Echonet.getSelfNode().getNodeProfile().getInstanceCode()
					, dstEchoClassCode, dstEchoInstanceCode, dstEchoAddress
					, ElFrame.ESV_GET);
			mEPCList = new ArrayList<Byte>();
		}
		
		@Override
		public ElFrame send() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);


			for(Byte epc : mEPCList) {
				frame.addProperty(new ElProperty(epc));
			}
			send(frame);
			return frame;
		}

		@Override
		public ElFrame sendTCP() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);


			for(Byte epc : mEPCList) {
				frame.addProperty(new ElProperty(epc));
			}
			sendTCP(frame);
			return frame;
		}

		public Getter reqGetProperty(byte epc) {
			mEPCList.add(epc);
			return this;
		}

	}
	
	
	public static class Informer extends Sender {
		protected ArrayList<Byte> mEPCList;

		public Informer(short echoClassCode, byte echoInstanceCode
				, String dstEchoAddress, boolean isSelfObject) {
			super(
					isSelfObject ? echoClassCode : NodeProfile.ECHO_CLASS_CODE
							, isSelfObject ? echoInstanceCode : Echonet.getSelfNode().getNodeProfile().getInstanceCode()
							, isSelfObject ? NodeProfile.ECHO_CLASS_CODE : echoClassCode
							, isSelfObject ? NodeProfile.INSTANCE_CODE : echoInstanceCode
							, dstEchoAddress
							, isSelfObject ? ElFrame.ESV_INF : ElFrame.ESV_INF_REQ);
			mEPCList = new ArrayList<Byte>();
		}

		@Override
		public ElFrame send() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			if(mESV == ElFrame.ESV_INF_REQ) {
				for(Byte epc : mEPCList) {
					frame.addProperty(new ElProperty(epc));
				}
			} else {
				ElNode node = Echonet.getSelfNode();
				ElObject seoj = node.getInstance(mSrcEchoClassCode, mSrcEchoInstanceCode);
				if(seoj.get() == null) {
					return frame;
				}
				for(Byte epc : mEPCList) {
					byte[] edt = seoj.getProperty(epc);

					if(edt != null) {
						ElProperty property = new ElProperty(epc, edt);

						if(seoj.isValidProperty(property)) {
							frame.addProperty(property);
						}
					}
				}
			}

			send(frame);
			return frame;
		}
		
		public Informer reqInformProperty(byte epc) {
			mEPCList.add(epc);
			return this;
		}

		@Override
		public ElFrame sendTCP() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			if(mESV == ElFrame.ESV_INF_REQ) {
				for(Byte epc : mEPCList) {
					frame.addProperty(new ElProperty(epc));
				}
			} else {
				ElNode node = Echonet.getSelfNode();
				if(node == null) {
					return frame;
				}
				ElObject seoj = node.getInstance(mSrcEchoClassCode, mSrcEchoInstanceCode);
				if(seoj.get() == null) {
					return frame;
				}
				for(Byte epc : mEPCList) {
					byte[] edt = seoj.getProperty(epc);

					if(edt != null) {
						ElProperty property = new ElProperty(epc, edt);

						if(seoj.isValidProperty(property)) {
							frame.addProperty(property);
						}
					}
				}
			}

			sendTCP(frame);
			return frame;
		}
	}

	public static class InformerC extends Sender {
		protected ArrayList<Byte> mEPCList;
		
		public InformerC(short srcEchoClassCode, byte srcEchoInstanceCode
				, String dstEchoAddress) {
			super(NodeProfile.ECHO_CLASS_CODE
					, Echonet.getSelfNode().getNodeProfile().getInstanceCode()
					, NodeProfile.ECHO_CLASS_CODE, NodeProfile.INSTANCE_CODE, dstEchoAddress
					, ElFrame.ESV_INFC);
			mEPCList = new ArrayList<Byte>();
		}

		@Override
		public ElFrame send() throws IOException {

			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			ElNode node = Echonet.getSelfNode();
			if(node == null) {
				return frame;
			}
			ElObject seoj = node.getInstance(mSrcEchoClassCode, mSrcEchoInstanceCode);
			if(seoj.get() == null) {
				return frame;
			}
			for(Byte epc : mEPCList) {
				byte[] edt = seoj.getProperty(epc);

				if(edt != null) {
					ElProperty property = new ElProperty(epc, edt);

					if(seoj.isValidProperty(property)) {
						frame.addProperty(property);
					}
				}
			}

			send(frame);
			return frame;
		}
		
		public InformerC reqInformProperty(byte epc) {
			mEPCList.add(epc);
			return this;
		}

		@Override
		public ElFrame sendTCP() throws IOException {
			ElFrame frame = new ElFrame(mSrcEchoClassCode, mSrcEchoInstanceCode
					, mDstEchoClassCode, mDstEchoInstanceCode
					, mDstEchoAddress, mESV);

			ElNode node = Echonet.getSelfNode();
			if(node == null) {
				return frame;
			}
			ElObject seoj = node.getInstance(mSrcEchoClassCode, mSrcEchoInstanceCode);
			if(seoj.get() == null) {
				return frame;
			}
			for(Byte epc : mEPCList) {
				byte[] edt = seoj.getProperty(epc);

				if(edt != null) {
					ElProperty property = new ElProperty(epc, edt);

					if(seoj.isValidProperty(property)) {
						frame.addProperty(property);
					}
				}
			}

			sendTCP(frame);
			return frame;
		}
	}

}