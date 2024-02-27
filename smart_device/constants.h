#pragma once
#include <iostream>
// Modobus Communication Parameters
const int MODBUS_NG_COUNT = 3;
const int MODBUS_RESPONSE_TIMEOUT_SEC = 3;
const int MODBUS_RESPONSE_TIMEOUT_USEC = 0;
#define MODBUS_BIN_BIT_MASK 0xFFFF              // シフト演算
#define MODBUS_BIN_BIT_MASK_64bit 0x000000000000FFFF              // シフト演算
const int MODBUS_BIN_16_BIT_SHIFT = 16;  