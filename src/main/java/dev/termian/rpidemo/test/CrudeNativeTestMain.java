package dev.termian.rpidemo.test;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.library.pigpio.PiGpioConst;
import com.pi4j.library.pigpio.internal.PIGPIO;
import dev.termian.rpidemo.Main;

public class CrudeNativeTestMain extends Main {
    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            System.out.println("Iteration " + i);
            timePi4Jv2();
            timePIGPIO();
        }
    }

    private static void timePi4Jv2() {
        Context pi4j = Pi4J.newAutoContext();
        DigitalInputConfig inCfg = DigitalInput.newConfigBuilder(pi4j)
                .address(DHT11_GPIO)
                .pull(PullResistance.OFF)
                .debounce(0L)
                .provider("pigpio-digital-input")
                .build();
        DigitalInput input = pi4j.create(inCfg);

        long start = System.nanoTime();
        input.initialize(pi4j);
        System.out.printf("Pi4Jv2 Initialization duration: %dns%n", System.nanoTime() - start);

        start = System.nanoTime();
        DigitalState state = input.state();
        System.out.printf("Pi4Jv2 Read duration: %dns, state %s%n", System.nanoTime() - start, state);

        pi4j.shutdown();
    }

    private static void timePIGPIO() {
        PIGPIO.gpioInitialise();
        PIGPIO.gpioSetMode(Main.DHT11_GPIO, PiGpioConst.PI_OUTPUT);
        PIGPIO.gpioSetPullUpDown(Main.DHT11_GPIO, PiGpioConst.PI_PUD_OFF);
        PIGPIO.gpioGlitchFilter(Main.DHT11_GPIO, 0);
        PIGPIO.gpioNoiseFilter(Main.DHT11_GPIO, 0, 0);

        long start = System.nanoTime();
        PIGPIO.gpioSetMode(Main.DHT11_GPIO, PiGpioConst.PI_INPUT);
        System.out.printf("PIGPIO Initialization duration: %dns%n", System.nanoTime() - start);

        start = System.nanoTime();
        int state = PIGPIO.gpioRead(Main.DHT11_GPIO);
        System.out.printf("PIGPIO Read duration: %dns, state %s%n", System.nanoTime() - start, state);

        PIGPIO.gpioTerminate();
    }
}
