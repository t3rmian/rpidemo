package dev.termian.rpidemo;


import com.pi4j.library.pigpio.internal.PIGPIO;
import dev.termian.rpidemo.dht11.DHT11TemperatureListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    protected static final int DHT11_GPIO = Integer.getInteger("DHT11_GPIO", 21);
    private static final int SAMPLE_RATE_US = Integer.getInteger("SAMPLE_RATE_US", 5);
    private static final boolean DISABLE_DEBUG_LOGGING = Boolean.getBoolean("DISABLE_DEBUG_LOGGING");

    static {
        configureLogging();
        checkPrivileges();
    }

    public static void main(final String[] ars) throws Exception {
        PIGPIO.gpioInitialise();
        try {
            DHT11TemperatureListener dht11 = new DHT11TemperatureListener(DHT11_GPIO, SAMPLE_RATE_US);
            System.out.println(dht11.read());
        } finally {
            PIGPIO.gpioTerminate();
        }
    }

    private static void configureLogging() {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (DISABLE_DEBUG_LOGGING) {
            return;
        }
        Logger logger = Logger.getLogger(DHT11TemperatureListener.class.getName());
        logger.setLevel(Level.FINEST);
    }

    private static void checkPrivileges() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"id", "-u"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (!"0".equals(reader.readLine())) {
                throw new SecurityException("GPIO requires program to be run with root privileges");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}