import time
from rpi_ws281x import *
import rpi_ws281x as ws
def led_blinking():
        # LED configuration.
        LED_CHANNEL = 0
        LED_COUNT = 3              # How many LEDs to light.
        LED_FREQ_HZ = 800000        # Frequency of the LED signal.  Should be 800khz or 400khz.
        LED_DMA_NUM = 10            # DMA channel to use, can be 0-14.
        LED_GPIO = 12               # GPIO connected to the LED signal line.  Must support PWM!
        LED_BRIGHTNESS = 255        # Set to 0 for darkest and 255 for brightest
        LED_INVERT = 0              # Set to 1 to invert the LED signal, good if using NPN transistor as a 3.3V->5V level converter.  Keep at 0
        #                             for a normal/non-inverted signal.
        DOT_COLORS = [0x200000,   # red
                                0x201000,   # orange
                                0x202000,   # yellow
                                0x002000,   # green
                                0x002020,   # lightblue
                                0x000020,   # blue
                                0x100010,   # purple
                                0x200010]  # pink
        leds = ws.new_ws2811_t()
        # Initialize all channels to off
        for channum in range(2):
                channel = ws.ws2811_channel_get(leds, channum)
                ws.ws2811_channel_t_count_set(channel, 0)
                ws.ws2811_channel_t_gpionum_set(channel, 0)
                ws.ws2811_channel_t_invert_set(channel, 0)
                ws.ws2811_channel_t_brightness_set(channel, 0)
        channel = ws.ws2811_channel_get(leds, LED_CHANNEL)
        ws.ws2811_channel_t_count_set(channel, LED_COUNT)
        ws.ws2811_channel_t_gpionum_set(channel, LED_GPIO)
        ws.ws2811_channel_t_invert_set(channel, LED_INVERT)
        ws.ws2811_channel_t_brightness_set(channel, LED_BRIGHTNESS)
        ws.ws2811_t_freq_set(leds, LED_FREQ_HZ)
        ws.ws2811_t_dmanum_set(leds, LED_DMA_NUM)
        # Initialize library with LED configuration.
        resp = ws.ws2811_init(leds)
        if resp != ws.WS2811_SUCCESS:
                message = ws.ws2811_get_return_t_str(resp)
                raise RuntimeError('ws2811_init failed with code {0} ({1})'.format(resp, message))
        # Wrap following code in a try/finally to ensure cleanup functions are called
        # after library is initialized.
        try:
                offset = 0
                for offset in range (7):
                        # Update each LED color in the buffer.
                        for i in range(LED_COUNT):
                                # Pick a color based on LED position and an offset for animation.
                                color = DOT_COLORS[(i + offset) % len(DOT_COLORS)]

                                # Set the LED color buffer value.
                                ws.ws2811_led_set(channel, i, color)

                        # Send the LED color data to the hardware.
                        resp = ws.ws2811_render(leds)
                        if resp != 0:
                                raise RuntimeError('ws2811_render failed with code {0}'.format(resp))

                        time.sleep(0.25)
                        offset += 1
        finally:
                ws.ws2811_fini(leds)
                ws.delete_ws2811_t(leds)

if __name__ == '__main__':
    led_blinking()