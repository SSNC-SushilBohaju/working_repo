package src.main.java.jp.co.smartsolar.smartedge;


import java.io.IOException;
import java.net.NetworkInterface;
import java.util.concurrent.LinkedBlockingQueue;
import src.main.java.jp.co.smartsolar.smartedge.protocol.ElProtocol;
import src.main.java.jp.co.smartsolar.smartedge.protocol.ElTCPProtocol;
import src.main.java.jp.co.smartsolar.smartedge.protocol.ElUDPProtocol;
import src.main.java.jp.co.smartsolar.smartedge.protocol.ElProtocol.Task;

public final class ElSocket {
    @SuppressWarnings("unused")
    private static final String TAG = ElSocket.class.getSimpleName();

    public static final String SELF_ADDRESS = "127.0.0.1";
    public static final String MULTICAST_ADDRESS = "224.0.23.0";

    protected static LinkedBlockingQueue<ElProtocol.Task> sTaskQueue = new LinkedBlockingQueue<ElProtocol.Task>();

    public static synchronized void enqueueTask(Task task) {
        sTaskQueue.offer(task);
    }

    private static ElUDPProtocol sUDPProtocol = new ElUDPProtocol();
    private static ElTCPProtocol sTCPProtocol = new ElTCPProtocol();

    private static Thread udpThread;
    private static Thread sTaskPerformerThread;
    private static short sNextTID = 0;
    private static boolean fPerformActive;

    private ElSocket() {
    }

    public static void openSocket() throws IOException {
        sUDPProtocol.openUDP();
        sTCPProtocol.openTCP();
        startReceiverThread();
    }

    public static void openSocket(NetworkInterface nwif) throws IOException {
        sUDPProtocol.openUDP(nwif);
        sTCPProtocol.openTCP();
        startReceiverThread();
    }

    private static void startReceiverThread() {
        if (udpThread == null) {
            udpThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (sUDPProtocol.isOpened()) {
                        // System.out.println("UDP receive");
                        sUDPProtocol.receive();
                    }
                }
            });
            udpThread.start();
        }

        if (sTaskPerformerThread == null) {
            fPerformActive = true;
            sTaskPerformerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (fPerformActive) {
                        try {
                            // System.out.println( "perform" );
                            sTaskQueue.take().perform();
                        } catch (InterruptedException e) {
                            // e.printStackTrace();
                        }
                    }
                }
            });
            sTaskPerformerThread.start();
        }
    }

    public static void closeSocket() throws IOException {
        sTCPProtocol.closeTCP();
        sUDPProtocol.closeUDP();
        stopReceiverThread();
    }

    private static void stopReceiverThread() {
        if (udpThread != null) {
            udpThread.interrupt();
            try {
                udpThread.join();
            } catch (Exception e) {
            }
            udpThread = null;
        }

        if (sTaskPerformerThread != null) {
            fPerformActive = false;
            sTaskPerformerThread.interrupt();
            try {
                sTaskPerformerThread.join();
            } catch (Exception e) {
            }
            sTaskPerformerThread = null;
        }
    }

    public static void resumeReceiverThread() {

    }

    public static void pauseReceiverThread() {

    }

    public static synchronized short nextTID() {
        short ret = sNextTID;
        sNextTID += 1;
        // Echo::getStorage().get()->setNextTID(sNextTID);
        return ret;
    }

    public static short getNextTIDNoIncrement() {
        return sNextTID;
    }

    public static void sendUDPFrame(ElFrame frame) throws IOException {
        sUDPProtocol.sendUDP(frame);
    }

    public static void sendTCPFrame(ElFrame frame) throws IOException {
        sTCPProtocol.sendTCP(frame);
    }
}