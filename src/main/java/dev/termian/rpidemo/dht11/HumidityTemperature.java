package dev.termian.rpidemo.dht11;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HumidityTemperature {
    private static final DateTimeFormatter ISO_TIME_MS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final BigDecimal relativeHumidity;
    private final BigDecimal temperature;
    private final LocalDateTime dateTime;
    public HumidityTemperature(int integralRH, int decimalRH, int integralT, int decimalT) {
        this(integralRH, decimalRH, integralT, decimalT, LocalDateTime.now());
    }

    public HumidityTemperature(int integralRH, int decimalRH, int integralT, int decimalT, LocalDateTime dateTime) {
        this.relativeHumidity = new BigDecimal(integralRH + "." + decimalRH);
        this.temperature = new BigDecimal(integralT + "." + decimalT);
        this.dateTime = dateTime;
    }

    public BigDecimal getRelativeHumidity() {
        return relativeHumidity;
    }

    public BigDecimal getTemperature(TemperatureScale scale) {
        if (scale == TemperatureScale.FAHRENHEIT) {
            return temperature.multiply(new BigDecimal("1.8")).add(BigDecimal.valueOf(32L));
        }
        return temperature;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return String.format("%s | Humidity = %s%% | Temperature = %s°C (%s°f)",
                ISO_TIME_MS.format(getDateTime()),
                getRelativeHumidity(),
                getTemperature(TemperatureScale.CELSIUS),
                getTemperature(TemperatureScale.FAHRENHEIT)
        );
    }

    public enum TemperatureScale {
        CELSIUS, FAHRENHEIT
    }
}
