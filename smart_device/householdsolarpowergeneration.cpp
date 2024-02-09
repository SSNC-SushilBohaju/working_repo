

#include <stdlib.h>

#include <uecho/device.h>
#include <uecho/node.h>
#include <cstdint>
#include <uecho/misc.h>

#define HOUSEHOLD_SOLAR_POWER_GENERATION_OBJECT_CODE 0x027901
#define HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_ON 0x30
#define HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_OFF 0x31
#define HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_RESET 0x00

#define HOUSEHOLD_SOLAR_POWER_GENERATION_OPERATION_STATUS 0x80
#define HOUSEHOLD_SOLAR_POWER_GENERATION_SYSTEM_INTERCONNECTION_INFORMATION 0xD0
#define HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_INSTANTANEOUS_ELECTRICITY_GENERATION 0xE0
#define HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION 0xE1
#define HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION 0xE2
#define HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_CUMULATIVE_AMOUT_OF_ELECTRICITY_SOLD 0xE3
#define HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_SOLD 0xE4
#define HOUSEHOLD_SOLAR_POWER_GENERATION_POWER_GENERATION_OUTPUT_LIMIT_SETTING_1 0xE5
#define HOUSEHOLD_SOLAR_POWER_GENERATION_POWER_GENERATION_OUTPUT_LIMIT_SETTING_2 0xE6
#define HOUSEHOLD_SOLAR_POWER_GENERATION_LIMIT_SETTING_FOR_AMOUNT_OF_ELECTRICITY_SOLD 0xE7
#define HOUSEHOLD_SOLAR_POWER_GENERATION_RATED_POWER_GENERATION_OUTPUT_INTERCONNECTED 0xE8
#define HOUSEHOLD_SOLAR_POWER_GENERATION_RATED_POWER_GENERATION_OUTPUT_INDEPENDENT 0xE9

typedef uint8_t byte;
void uecho_household_solor_power_generation_printrequest(uEchoMessage *msg)
{
  uEchoProperty *prop;
  size_t opc, n;
/home/pi/smart_ai_c/uecho
  opc = uecho_message_getopc(msg);
  printf("%s %1X %1X %02X %03X %03X %02X %ld ",
         uecho_message_getsourceaddress(msg),
         uecho_message_getehd1(msg),
         uecho_message_getehd2(msg),
         uecho_message_gettid(msg),
         uecho_message_getsourceobjectcode(msg),
         uecho_message_getdestinationobjectcode(msg),
         uecho_message_getesv(msg),
         opc);

  for (n = 0; n < opc; n++)
  {
    prop = uecho_message_getproperty(msg, n);
    printf("%02X", uecho_property_getcode(prop));
  }

  printf("\n");
}

void uecho_household_solor_power_generating_object_messagelitener(uEchoObject *obj, uEchoMessage *msg)
{
  uecho_household_solor_power_generation_printrequest(msg);
}

/*::::::::::::::::::::::::::Propetry handler for set command::::::::::::::::::::::::::*/

bool uecho_household_solor_power_generation_propertyrequesthandler(uEchoObject *obj, uEchoProperty *prop, uEchoEsv esv, size_t pdc, byte *edt)
{
  byte status;
  printf("ESV = %02X : %02X (%ld), ", esv, uecho_property_getcode(prop), pdc);

  if ((pdc != 1) || !edt)
  {
    printf("Bad Request\n");
    return false;
  }
  status = edt[0];
  switch (status)
  {
  case HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_ON:
    printf("POWER = ON\n");
    break;
  case HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_OFF:
    printf("POWER = OFF\n");
    break;
  case HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_RESET:
    printf("resetting cumulative amount of electric energy\n");
    break;
  default:
    printf("POWER = %02X\n", status);
    break;
  }

  return true;
}

/*::::::::::::::::::::::::::Change Integer value to Bytes::::::::::::::::::::::::::*/

void setIntValueTo4Bytes(uint32_t value, uint8_t *array)
{
  // Convert the integer value to a  4-byte array representation
  array[0] = (value >> 24) & 0xFF;
  array[1] = (value >> 16) & 0xFF;
  array[2] = (value >> 8) & 0xFF;
  array[3] = value & 0xFF;
}

void setShortValueTo2Bytes(uint16_t value, uint8_t *array)
{
  // Convert the  16-bit integer value to a  2-byte array representation
  array[0] = (value >> 8) & 0xFF; // Higher byte
  array[1] = value & 0xFF;        // Lower byte
}

/*::::::::::::::::::::::::::Calculation for Data::::::::::::::::::::::::::*/

float getInstantaneousEnergy(void)
{
  return 655.22;
}

float cummulativeamountofelectricitygenerate(void)
{
  return 666666;
}

uEchoObject *uecho_household_solor_power_generation_new(void)
{
  uEchoObject *obj;
  byte prop[32];
  byte Watt[2];
  byte Cummulative_Energy[4];
  float energy = getInstantaneousEnergy();
  float cummulativelectricitygenerate = cummulativeamountofelectricitygenerate();

  // setShortValueTo2Bytes(energy, Watt);
  uecho_integer2byte(energy, Watt, sizeof(Watt));
  uecho_integer2byte(cummulativelectricitygenerate, Cummulative_Energy, sizeof(Cummulative_Energy));

  /*::::::::::::::::::::::::::creating echo object and set manufacture code::::::::::::::::::::::::::*/

  obj = uecho_device_new();
  uecho_object_setmanufacturercode(obj, 0xFFFFF0);
  uecho_object_setcode(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_OBJECT_CODE);

  /*::::::::::::::::::::::::::set property of echo object::::::::::::::::::::::::::*/

  uecho_object_setproperty(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_OPERATION_STATUS, uEchoPropertyAttrReadWrite);
  prop[0] = HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_POWER_ON;
  uecho_object_setpropertydata(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_OPERATION_STATUS, prop, 1);
  uecho_object_setpropertywriterequesthandler(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_OPERATION_STATUS, uecho_household_solor_power_generation_propertyrequesthandler);

  uecho_object_setproperty(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION, uEchoPropertyAttrReadWrite);
  prop[0] = HOUSEHOLD_SOLAR_POWER_GENERATION_PROPERTY_RESET;
  uecho_object_setpropertydata(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION, prop, 1);
  uecho_object_setpropertywriterequesthandler(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_RESETTING_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION, uecho_household_solor_power_generation_propertyrequesthandler);

  /*::::::::::::::::::::::::::set property of echo object for get request::::::::::::::::::::::::::*/

  uecho_object_setpropertydata(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_INSTANTANEOUS_ELECTRICITY_GENERATION, Watt, 2);
  uecho_object_setpropertydata(obj, HOUSEHOLD_SOLAR_POWER_GENERATION_MEASURED_CUMULATIVE_AMOUT_OF_ELECTRICITY_GENERATION, Cummulative_Energy, 4);

  return obj;
}

bool uecho_household_solor_power_generation_delete(uEchoObject *obj)
{

  return uecho_object_delete(obj);
}