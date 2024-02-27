#include <modbus/modbus.h>
#include <errno.h>
#include <unistd.h>
#include <bitset>

class MODBUS_COM{
    private:
        int response_timeout_sec = 1;
        double ConvertNumberToFloat(unsigned long number, int isDoublePrecision);

    public:  
        MODBUS_COM(){};
        MODBUS_COM(int timeout){    response_timeout_sec = timeout; };   
        int initialize(modbus_t **ctx, const char *ip_address, const int port_no, const int slave_id);
        int creation_tcp_connect(modbus_t **ctx, const char *ip_address, const int port_no);
        int set_slave_id(modbus_t **ctx, const int slave_id);
        int set_timeout(modbus_t **ctx);
        int set_timeout(modbus_t **ctx, const int response_timeout_sec, const int response_timeout_usec);
        int modbus_connection(modbus_t **ctx);
        int read_register(modbus_t **ctx, const int register_no, const int register_length, uint16_t *buf_ui16);
        int set_buf_for_write_register(const unsigned long value, const int register_length, uint16_t *buf_ui16);
        int write_register(modbus_t **ctx, const int register_no, const int register_length, const uint16_t *buf_ui16); 
        long data_conversion(const int register_length, const std::string data_type, const uint16_t *tab_ui16_reg);   
        //float data_conversion_for_float(const int register_length, const std::string data_type, const uint16_t *tab_ui16_reg);
};