package service;

import com.sonycsl.echo.Echo;
import com.sonycsl.echo.EchoFrame;
import com.sonycsl.echo.EchoProperty;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.SmartElectricEnergyMeter;
import com.sonycsl.echo.processing.defaults.DefaultNodeProfile;
import device.SmartMeter;

import java.io.IOException;

public class SmartEchoServices {


    public void createDataFrame(String data){
        Echo.addEventListener(new Echo.EventListener() {
            public void onGetPower(SmartElectricEnergyMeter smartElectricEnergyMeter){
                smartElectricEnergyMeter.setReceiver(new SmartElectricEnergyMeter.Receiver(){
                    @Override
                    protected void onGetMeasuredInstantaneousCurrents(EchoObject eoj, short tid, byte esv, EchoProperty property, boolean success) {
                        super.onGetMeasuredInstantaneousCurrents(eoj, tid, esv, property, success);
                        System.out.println(property.edt[0]);

                    }

                    @Override
                    protected void onGetDayForWhichTheHistoricalDataOfMeasuredCumulativeAmountsOfElectricEnergyIsToBeRetrieved(EchoObject eoj, short tid, byte esv, EchoProperty property, boolean success) {
                        super.onGetDayForWhichTheHistoricalDataOfMeasuredCumulativeAmountsOfElectricEnergyIsToBeRetrieved(eoj, tid, esv, property, success);
                        System.out.println(property.edt[0]);
                    }

                    @Override
                    protected void onSetDayForWhichTheHistoricalDataOfMeasuredCumulativeAmountsOfElectricEnergyIsToBeRetrieved(EchoObject eoj, short tid, byte esv, EchoProperty property, boolean success) {
                        super.onSetDayForWhichTheHistoricalDataOfMeasuredCumulativeAmountsOfElectricEnergyIsToBeRetrieved(eoj, tid, esv, property, success);
                    }
                });
                System.out.println("Test");
            }
        });
    }

    public void startSmartElectricEnergyMeter(){
        try{
            SmartMeter smartMeter = new SmartMeter();
            Echo.start(new DefaultNodeProfile(), new DeviceObject[]{smartMeter});

        }catch(IOException e){
            e.printStackTrace();
        }

    }


}
