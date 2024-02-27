#ifndef HouseholdSolarPowerGeneration_H_
#define HouseholdSolarPowerGeneration_H_
#include <uecho/node.h>

class Household
{
public:
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_OBJECT_CODE;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_ON;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_OFF;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_RESET;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_OPERATION_STATUS;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_SYSTEM_INTERCONNECTION_INFORMATION;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_INSTANTANEOUS_ELECTRICITY_GENERATION;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_CUMULATIVE_AMOUT_OF_ELECTRICITY_SOLD;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_SOLD;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_POWER_GENERATION_OUTPUT_LIMIT_SETTING_1;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_POWER_GENERATION_OUTPUT_LIMIT_SETTING_2;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_LIMIT_SETTING_FOR_AMOUNT_OF_ELECTRICITY_SOLD;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_RATED_POWER_GENERATION_OUTPUT_INTERCONNECTED;
    static const int HOUSEHOLD_SOLAR_POWER_GENERATION_RATED_POWER_GENERATION_OUTPUT_INDEPENDENT;

public:
    uEchoObject *uecho_household_solor_power_generation_new(void);
    bool uecho_household_solor_power_generation_delete(uEchoObject *obj);
    void uecho_household_solor_power_generation_printrequest(uEchoMessage *msg);
    // static bool uecho_household_solor_power_generation_propertyrequesthandler(uEchoObject *obj, uEchoProperty *prop, uEchoEsv esv, size_t pdc, byte *edt);
    void uecho_household_solor_power_generating_object_messagelitener(uEchoObject *obj, uEchoMessage *msg);
    void readFromModbusDevice();

};

#endif
