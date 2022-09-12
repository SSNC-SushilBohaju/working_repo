package src.main.java.jp.co.smartsolar.smartedge;

public class ElProperty {

    public final byte epc;
	public final byte pdc;
	public final byte[] edt;
	
	public ElProperty(byte epc, byte pdc, byte[] edt) {
		this.epc = epc;
		this.pdc = pdc;
		this.edt = edt;
	}
	
	public ElProperty(byte epc, byte[] edt) {
		this(epc, (edt == null) ? (byte)0 : (byte)edt.length, edt);
	}
	
	public ElProperty(byte epc) {
		this(epc, (byte)0, new byte[0]);
	}
	

	public int size() {
		if(edt != null) return edt.length + 2;
		return 2;
	}
	
	public ElProperty copy() {
		ElProperty ret;
		if(edt == null) {
			ret = new ElProperty(epc, null);
		} else {
			byte[] edt_ = new byte[edt.length];
			for(int i = 0; i < edt.length; i++) {
				edt_[i] = edt[i];
			}
			ret = new ElProperty(epc, edt_);
		}
		return ret;
	}
}

