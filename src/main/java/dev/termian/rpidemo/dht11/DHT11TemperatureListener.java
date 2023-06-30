package dev.termian.rpidemo.dht11;

import com.pi4j.library.pigpio.PiGpioConst;
import com.pi4j.library.pigpio.internal.PIGPIO;
import com.pi4j.library.pigpio.internal.PiGpioAlertCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class DHT11TemperatureListener implements PiGpioAlertCallback {

    private static final Logger logger = LoggerFactory.getLogger(DHT11TemperatureListener.class);
    private static final long MAX_UNSIGNED_INT = 4294967295L;
    private static final long DATA_BIT_DURATION_THRESHOLD = 49;
    private static final int MCU_START_BITS = 3;
    private static final int DHT_START_BITS = 3;
    private static final int DHT_RESPONSE_BITS = 40 * 2;

    private final long[] signalTimes = new long[MCU_START_BITS + DHT_START_BITS + DHT_RESPONSE_BITS];
    private final int gpio;
    private int signalIndex;

    public DHT11TemperatureListener(int gpio, int sampleRate) {
        this.gpio = gpio;
        Arrays.fill(signalTimes, -1);
        initPGPIO(gpio, sampleRate);
    }

    protected void initPGPIO(int gpio, int sampleRate) {
        PIGPIO.gpioCfgClock(sampleRate, 1, 0);
        PIGPIO.gpioSetPullUpDown(gpio, PiGpioConst.PI_PUD_OFF);
        PIGPIO.gpioGlitchFilter(gpio, 0);
        PIGPIO.gpioNoiseFilter(gpio, 0, 0);
    }

    public HumidityTemperature read() throws InterruptedException {
        sendStartSignal();
        waitForResponse();
        try {
            return parseTransmission(signalTimes);
        } finally {
            clearState();
        }
    }

    private void sendStartSignal() throws InterruptedException {
        PIGPIO.gpioSetAlertFunc(gpio, this);
        PIGPIO.gpioSetMode(gpio, PiGpioConst.PI_OUTPUT);
        PIGPIO.gpioWrite(gpio, PiGpioConst.PI_LOW);
        TimeUnit.MILLISECONDS.sleep(20);
        PIGPIO.gpioWrite(gpio, PiGpioConst.PI_HIGH);
    }

    private void waitForResponse() throws InterruptedException {
        PIGPIO.gpioSetMode(gpio, PiGpioConst.PI_INPUT);
        synchronized (this) {
            wait(1000);
        }
    }

    @Override
    public void call(int pin, int state, long tick) {
        signalTimes[signalIndex++] = tick;
        if (signalIndex == signalTimes.length) {
            logger.debug("Last signal state: {}", state);
            synchronized (this) {
                notify();
            }
        }
    }

    HumidityTemperature parseTransmission(long[] signalTimes) {
        String bits = get40RecentBits(signalTimes);
        int integralRH = parseIntBits(bits, 0, 8);
        int decimalRH = parseIntBits(bits, 8, 16);
        int integralT = parseIntBits(bits, 16, 24);
        int decimalT = parseIntBits(bits, 24, 32);
        int checksum = parseIntBits(bits, 32, 40);
        int expectedChecksum = (integralRH + decimalRH + integralT + decimalT) & 0xFF;
        if (checksum != expectedChecksum) {
            throw new IllegalStateException("Checksum verification failed. Data transmission may be corrupted.");
        }
        return new HumidityTemperature(integralRH, decimalRH, integralT, decimalT);
    }

    private String get40RecentBits(long[] signalTimes) {
        long[] dataDurations = calculateDataDurations(signalTimes);
        String bits = LongStream.of(dataDurations)
                .mapToObj(duration -> duration > DATA_BIT_DURATION_THRESHOLD ? "1" : "0")
                .collect(Collectors.joining(""));
        if (bits.length() != 40) {
            throw new IllegalStateException("Data transmission incomplete. Expected 40 bits but received " + bits.length());
        }
        logger.debug("Bits received: {}", bits);
        return bits;
    }

    private long[] calculateDataDurations(long[] signalTimes) {
        logger.debug("Signal times (ticks): {}", signalTimes);
        long[] durations = new long[signalTimes.length - 1];
        for (int j = 1; j < signalTimes.length; j++) {
            if (signalTimes[j] >= 0) {
                durations[j - 1] = calculateDuration(signalTimes[j - 1], signalTimes[j]);
            }
        }
        logger.debug("Signal durations: {}", durations);
        long[] dataDurations = new long[40];
        for (int j = durations.length - 1, k = 0, l = dataDurations.length - 1; j >= 0 && l >= 0; j--) {
            if (durations[j] <= 0) {
                continue;
            }
            if (k++ % 2 == 1) {
                dataDurations[l--] = durations[j];
            }
        }
        logger.debug("Data only durations: {}", dataDurations);
        return dataDurations;
    }

    long calculateDuration(long predecessorTick, long successorTick) {
        long duration = successorTick - predecessorTick;
        if (duration < 0) {
            duration += MAX_UNSIGNED_INT + 1;
        }
        return duration;
    }

    private static int parseIntBits(String bits, int beginIndex, int endIndex) {
        return Integer.parseInt(bits.substring(beginIndex, endIndex), 2);
    }

    public void clearState() {
        PIGPIO.gpioDisableAlertFunc(gpio);
        Arrays.fill(signalTimes, -1);
        signalIndex = 0;
    }
}
