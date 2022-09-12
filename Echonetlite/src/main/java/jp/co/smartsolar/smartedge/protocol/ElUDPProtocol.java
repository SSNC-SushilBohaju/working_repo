package src.main.java.jp.co.smartsolar.smartedge.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import src.main.java.jp.co.smartsolar.smartedge.Echonet;
import src.main.java.jp.co.smartsolar.smartedge.ElFrame;
import src.main.java.jp.co.smartsolar.smartedge.ElSocket;
import src.main.java.jp.co.smartsolar.smartedge.ElUtils;

public class ElUDPProtocol extends ElProtocol {
    public static int UDP_MAX_PACKET_SIZE = 65507;

    private static final int PORT = 3610;

    private MulticastSocket mMulticastSocket;
    private InetAddress mMulticastAddress;

    public ElUDPProtocol() {

    }

    public void openUDP() throws IOException {
        mMulticastAddress = InetAddress.getByName(ElSocket.MULTICAST_ADDRESS);
        mMulticastSocket = new MulticastSocket(PORT);
        mMulticastSocket.setNetworkInterface(ElUtils.getNetworkInterface());
        mMulticastSocket.joinGroup(mMulticastAddress);
        mMulticastSocket.setLoopbackMode(true);
        mMulticastSocket.setSoTimeout(0);
    }

    public void openUDP(NetworkInterface nwif) throws IOException {
        mMulticastAddress = InetAddress.getByName(ElSocket.MULTICAST_ADDRESS);
        mMulticastSocket = new MulticastSocket(PORT);
        mMulticastSocket.setNetworkInterface(nwif);
        mMulticastSocket.joinGroup(mMulticastAddress);
        mMulticastSocket.setLoopbackMode(true);
        mMulticastSocket.setSoTimeout(0);
    }

    public void closeUDP() {

        if (mMulticastSocket != null) {
            try {
                mMulticastSocket.leaveGroup(mMulticastAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMulticastSocket.close();
            mMulticastSocket = null;
        }
    }

    public void sendUDP(ElFrame frame) throws IOException {

        Echonet.getEventListener().sendEvent(frame);

        if (frame.getDstEchoAddress().equals(ElSocket.SELF_ADDRESS)) {
            sendToSelf(frame.copy());
            return;
        }

        if (mMulticastSocket != null) {
            byte[] data = frame.getFrameByteArray();

            InetAddress address = InetAddress.getByName(frame.getDstEchoAddress());
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    address, PORT);

            mMulticastSocket.send(packet);
            if (frame.getDstEchoAddress().equals(ElSocket.MULTICAST_ADDRESS)) {
                ElFrame f = frame.copy();
                f.setDstEchoAddress(ElSocket.SELF_ADDRESS);
                sendToSelf(f);
            }
        }
    }

    public void sendToSelf(ElFrame frame) {
        UDPProtocolTask task = new UDPProtocolTask(frame, this);
        ElSocket.enqueueTask(task);
    }

    public boolean isOpened() {
        return (mMulticastSocket != null && !mMulticastSocket.isClosed());
    }

    private DatagramPacket rxPacket = new DatagramPacket(
            new byte[UDP_MAX_PACKET_SIZE], UDP_MAX_PACKET_SIZE);

    public void receive() {
        // closed?
        if (mMulticastSocket == null) {
            // System.err.println("sMulticastSocket has been closed.");
            // try {
            // openUDP();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            return;
        }
        try {
            rxPacket.setLength(rxPacket.getData().length);
            mMulticastSocket.receive(rxPacket);
        } catch (IOException e) {
            // e.printStackTrace();
            // try {
            // openUDP();
            // } catch (IOException e1) {
            // e1.printStackTrace();
            // }
            return;
        }
        if (mMulticastSocket == null) {
            return;
        }
        Enumeration<InetAddress> enumIpAddr;
        try {
            enumIpAddr = mMulticastSocket.getNetworkInterface().getInetAddresses();
        } catch (SocketException e) {
            // e.printStackTrace();
            // try {
            // openUDP();
            // } catch (IOException e1) {
            // e1.printStackTrace();
            // }
            return;
        }
        while (enumIpAddr.hasMoreElements()) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            if (inetAddress.equals(rxPacket.getAddress())) {
                // from self node
                return;
            }
        }
        byte[] data = new byte[rxPacket.getLength()];
        System.arraycopy(rxPacket.getData(), 0, data, 0, rxPacket.getLength());

        if (data.length < ElFrame.MIN_FRAME_SIZE) {
            return;
        }
        InetAddress address = rxPacket.getAddress();
        String srcEchoAddress = address.getHostAddress();
        ElFrame frame = new ElFrame(srcEchoAddress, data);

        UDPProtocolTask task = new UDPProtocolTask(frame, this);
        ElSocket.enqueueTask(task);
    }

    public static class UDPProtocolTask extends ElProtocol.Task {

        ElUDPProtocol mProtocol;

        public UDPProtocolTask(ElFrame frame, ElUDPProtocol protocol) {
            super(frame);
            mProtocol = protocol;
        }

        @Override
        protected void respond(ElFrame response) {
            try {
                mProtocol.sendUDP(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void informAll(ElFrame response) {
            try {
                mProtocol.sendUDP(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}