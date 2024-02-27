#include <stdio.h>
#include <uecho/uecho.h>
#include <modbus.h>
#include <errno.h>

// Function to initialize and start the uEcho controller
uEchoController* startEchonetController() {
    uEchoController* ctrl = uecho_controller_new();
    if (!ctrl) {
        printf("Failed to create a controller.\n");
        return NULL;
    }

    if (!uecho_controller_start(ctrl)) {
        printf("Failed to start the controller.\n");
        uecho_controller_delete(ctrl);
        return NULL;
    }

    return ctrl;
}

// Function to discover and print ECHONET Lite nodes
void discoverEchonetNodes(uEchoController* ctrl) {
    uEchoNode* node = uecho_controller_getlocalnode(ctrl);
    while (node) {
        printf("Node ID: %s\n", uecho_message_gettid(node));
        // node = uecho_node_getnext(node);
    }
}

// Function to read from a Modbus device using libmodbus
void readFromModbusDevice() {
    modbus_t *ctx;
    uint16_t tab_reg[32];

    ctx = modbus_new_tcp("192.168.1.138",  502);
    if (ctx == NULL) {
        fprintf(stderr, "Unable to create libmodbus context\n");
        return;
    }

    if (modbus_connect(ctx) == -1) {
        fprintf(stderr, "Connection failed: %s\n", modbus_strerror(errno));
        modbus_free(ctx);
        return;
    }

    int rc = modbus_read_registers(ctx,  0,  10, tab_reg);
    if (rc == -1) {
        fprintf(stderr, "Read registers failed: %s\n", modbus_strerror(errno));
        modbus_close(ctx);
        modbus_free(ctx);
        return;
    }

    for (int i =  0; i <  10; i++) {
        printf("Register %d: %d\n", i, tab_reg[i]);
    }

    modbus_close(ctx);
    modbus_free(ctx);
}

int main() {
    uEchoController* ctrl = startEchonetController();
    if (!ctrl) {
        return  1;
    }

    // discoverEchonetNodes(ctrl);

    readFromModbusDevice();

    uecho_controller_stop(ctrl);
    uecho_controller_delete(ctrl);

    return  0;
}
