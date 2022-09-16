import com.sonycsl.echo.Echo;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.SmartElectricEnergyMeter;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;
import com.sonycsl.echo.processing.defaults.DefaultController;
import com.sonycsl.echo.processing.defaults.DefaultNodeProfile;
import device.SmartMeter;
import service.SmartEchoServices;

import java.io.IOException;

public class SmartEchoMain {

    public static void main(String[] args) {
        SmartEchoServices services = new SmartEchoServices();
        services.startSmartElectricEnergyMeter();
        services.createDataFrame("hel");
        services.createDataFrame("Test");
        try {
            Echo.start( new DefaultNodeProfile(), new DeviceObject[] {
                    new DefaultController()
            });

            EchoNode[] nodes = Echo.getNodes() ;
            // 自分のノード
            EchoNode local = Echo.getSelfNode() ;

            for(EchoNode en : nodes){
                if(en == local){
                    System.out.println("Node id = " + en.getAddress().getHostAddress() + "(local)");
                }else{
                    System.out.println("Node id = " + en.getAddressStr());
                }
            }

            for ( EchoNode en : nodes ) {
                if ( en == local ) {
                    System.out.println("Node id = " + en.getAddress().getHostAddress() + "(local)");
                }
                else {
                    System.out.println("Node id = " + en.getAddress().getHostAddress());
                }

                System.out.println(" Node Profile = " + en.getNodeProfile());

                System.out.println(" Devices:");
                DeviceObject[] dos = en.getDevices();

                for ( DeviceObject d : dos ) {
                    System.out.println("  " + d.getClass().getSuperclass().getSimpleName());
                    services.createDataFrame("hel");
                }
                System.out.println("----");
            }

            while (true) {
                // ノード一覧の取得をリクエストしています。
                NodeProfile.getG().reqGetSelfNodeInstanceListS().send();
                System.out.println(NodeProfile.getG().reqGetSelfNodeInstanceListS().send());


                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
        catch( IOException e) {
            e.printStackTrace();
        }


    }
}
