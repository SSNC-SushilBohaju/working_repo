#include "modbus_manager.h"
#include <errno.h>
#include <stdio.h>
#include <constants.h>
#include <limits>
#include <cmath>

int MODBUS_COM::initialize(modbus_t **ctx, const char *ip_address, const int port_no, const int slave_id){

    int res = 0;

    if((res = creation_tcp_connect(&(*ctx), ip_address, port_no)) != 0){
        return res;
    }    
    if((res = set_slave_id(&(*ctx), slave_id)) != 0){
        return res;
    }
    if((res = modbus_connection(&(*ctx))) != 0){
        return res;
    }
    if((res = set_timeout(&(*ctx))) != 0){
        return res;
    }
    return res;
}

/*****************************************************************
 * Creation tcp connect
*****************************************************************/
int  MODBUS_COM::creation_tcp_connect(modbus_t **ctx, const char *ip_address, const int port_no){
    *ctx = modbus_new_tcp(ip_address, port_no);
    if (*ctx == NULL){         
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        return errno;
    }
    return 0;
}

/*****************************************************************
 * Set slave id
*****************************************************************/
int MODBUS_COM::set_slave_id(modbus_t **ctx, const int slave_id){

    if(modbus_set_slave(*ctx, slave_id) != 0){
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        return errno;
    }
    return 0;
}

/*****************************************************************
 * modbus connection
*****************************************************************/
int MODBUS_COM::modbus_connection(modbus_t **ctx){

    if (modbus_connect(*ctx) == -1){                
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        modbus_free(*ctx);
        return errno;
    }else{
        return 0;
    }
    return 0;
}

/*****************************************************************
 * Set Timeout
*****************************************************************/
int MODBUS_COM::set_timeout(modbus_t **ctx){

    // timeout set
    std::uint32_t old_response_to_sec;
    std::uint32_t old_response_to_usec;
    int res1 = modbus_get_response_timeout(*ctx, &old_response_to_sec, &old_response_to_usec);
    int res2 = modbus_set_response_timeout(*ctx, response_timeout_sec, MODBUS_RESPONSE_TIMEOUT_USEC);

    if(res1 != 0 && res2 != 0){        
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        return errno;
    }
    return 0;
}


/*****************************************************************
 * Set Timeout (Overload)
*****************************************************************/
int MODBUS_COM::set_timeout(modbus_t **ctx, const int response_timeout_sec, const int response_timeout_usec){
    
    // timeout set
    std::uint32_t old_response_to_sec;
    std::uint32_t old_response_to_usec;
    int res1 = modbus_get_response_timeout(*ctx, &old_response_to_sec, &old_response_to_usec);
    int res2 = modbus_set_response_timeout(*ctx, response_timeout_sec, response_timeout_usec);

    if(res1 != 0 && res2 != 0){
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        return errno;
    }
    return 0;
}

/*****************************************************************
 * Read registers
*****************************************************************/
int MODBUS_COM::read_register(modbus_t **ctx, const int register_no, const int register_length, uint16_t *buf_ui16){

    int i_count = 0;

    while(1){
        if (modbus_read_registers(*ctx, register_no, register_length, buf_ui16) == -1){
            i_count++;    
            if(i_count >= MODBUS_NG_COUNT){
                //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
                return errno;
            }
        }else{
            return 0;
        }
    }
    return 0;
}

/*****************************************************************
 * Set buffer for write registers
*****************************************************************/
int MODBUS_COM::set_buf_for_write_register(const unsigned long value, const int register_length, uint16_t *buf_ui16){

    unsigned long data = 0;

    try{
        for(int i = 0; i < register_length ; i++){
            data = value;
            data = data >> (MODBUS_BIN_16_BIT_SHIFT * (register_length - i - 1));
            buf_ui16[i] = data & MODBUS_BIN_BIT_MASK_64bit;
        }
    }catch(...){
        for(int i = 0; i < register_length ; i++){
            buf_ui16[i] = 0;
        }
        return 1;
    }
    return 0;
}

/*****************************************************************
 * Write registers
*****************************************************************/
int MODBUS_COM::write_register(modbus_t **ctx, const int register_no, const int register_length, const uint16_t *buf_ui16){

    if(modbus_write_registers(*ctx, register_no, register_length, buf_ui16) == -1){
        //std::cout << "ERROR:" << modbus_strerror(errno) << std::endl;
        return errno;
    }
    return 0;
}

/****************************************************
 * Convert data acquired by modbus communication
 * U64,S64,U32,S32,U16,S16,FP32
****************************************************/
long MODBUS_COM::data_conversion(const int register_length, const std::string data_type, const uint16_t *tab_ui16_reg){

    long l_ret = 0;

    if(data_type == "U64"){
        unsigned long ul_data = 0;  
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                ul_data = tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK;
            }else{
                ul_data = (ul_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        if(ul_data == 0xFFFFFFFFFFFFFFFF){
            ul_data = 0;
        }
        l_ret = ul_data;

    }else if(data_type == "S64"){
        long l_data = 0;
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                l_data = tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK;
            }else{
                l_data = (l_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        if(l_data == (long)0x8000000000000000){
            l_data = 0;
        }
        l_ret = l_data;

    }else if(data_type == "U32"){
        unsigned int ui_data = 0;
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                ui_data = tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK;
            }else{
                ui_data = (ui_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        if(ui_data == 0xFFFFFFFF){
            ui_data = 0;
        }
        l_ret = ui_data;

    }else if(data_type == "S32"){
        int i_data = 0;
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                i_data = tab_ui16_reg[i]  & MODBUS_BIN_BIT_MASK;
            }else{
                i_data = (i_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        if(i_data == (int)0x80000000){
            i_data = 0;
        }
        l_ret = i_data;

    }else if(data_type == "FP32"){
        unsigned int ui_data = 0;
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                ui_data = tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK;
            }else{
                ui_data = (ui_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        
        float f_ret = (float)ConvertNumberToFloat(ui_data, 0);
        if(f_ret == std::numeric_limits<double>::infinity()){
            return 0;
        }
        l_ret = (long)(f_ret * 1000);
    
    }else if(data_type == "U16"){
        unsigned short us_data = tab_ui16_reg[0];
        l_ret = us_data;

    }else if(data_type == "S16"){
        short s_data = tab_ui16_reg[0];
        l_ret = s_data;
    }
    // #7 ---s
    else if(data_type == "B16"){
        unsigned short us_data = tab_ui16_reg[0];
        l_ret = us_data;
    }
    // #7 ---e
    return l_ret;
}

/****************************************************
 * Convert data to IEEE754 from uint
 * 
****************************************************/
double MODBUS_COM::ConvertNumberToFloat(unsigned long number, int isDoublePrecision)
{
    int mantissaShift = isDoublePrecision ? 52 : 23;
    unsigned long exponentMask = isDoublePrecision ? 0x7FF0000000000000 : 0x7f800000;
    int bias = isDoublePrecision ? 1023 : 127;
    int signShift = isDoublePrecision ? 63 : 31;

    int sign = (number >> signShift) & 0x01;
    int exponent = ((number & exponentMask) >> mantissaShift) - bias;

    int power = -1;
    double total = 0.0;
    for ( int i = 0; i < mantissaShift; i++ )
    {
        int calc = (number >> (mantissaShift-i-1)) & 0x01;
        total += calc * pow(2.0, power);
        power--;
    }
    double value = (sign ? -1 : 1) * pow(2.0, exponent) * (total + 1.0);

    return value;
}

/****************************************************
 * Convert data acquired by modbus communication
 * FP32
****************************************************/
/*
float MODBUS_COM::data_conversion_for_float(const int register_length, const std::string data_type, const uint16_t *tab_ui16_reg){

    float f_ret = 0.0f;
    
    if(data_type == "FP32"){

        unsigned int ui_data = 0;
        for(int i = 0 ; i < register_length ; i++){
            if(i == 0){
                ui_data = tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK;
            }else{
                ui_data = (ui_data << MODBUS_BIN_16_BIT_SHIFT) | (tab_ui16_reg[i] & MODBUS_BIN_BIT_MASK);
            }
        }
        f_ret = (float)ConvertNumberToFloat(ui_data, 0);
        if(f_ret == std::numeric_limits<double>::infinity()){
            f_ret = 0.0f;
        }
    }
    return f_ret;
}
*/