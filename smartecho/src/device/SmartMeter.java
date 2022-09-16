package device;

import com.sonycsl.echo.eoj.device.housingfacilities.SmartElectricEnergyMeter;

public class SmartMeter extends SmartElectricEnergyMeter {

    @Override
    protected byte[] getOperationStatus() {
        return new byte[0];
    }

    @Override
    protected boolean setInstallationLocation(byte[] bytes) {
        return false;
    }

    @Override
    protected byte[] getInstallationLocation() {
        return new byte[0];
    }

    @Override
    protected byte[] getFaultStatus() {
        return new byte[0];
    }

    @Override
    protected byte[] getManufacturerCode() {
        return new byte[0];
    }

    @Override
    protected byte[] getNumberOfEffectiveDigitsForCumulativeAmountsOfElectricEnergy() {
        return new byte[0];
    }

    @Override
    protected byte[] getMeasuredCumulativeAmountOfElectricEnergyNormalDirection() {
        return new byte[0];
    }

    @Override
    protected byte[] getUnitForCumulativeAmountsOfElectricEnergyNormalAndReverseDirections() {
        return new byte[0];
    }

    @Override
    protected byte[] getCumulativeAmountsOfElectricEnergyMeasuredAtFixedTimeNormalDirection() {
        return new byte[0];
    }
}
