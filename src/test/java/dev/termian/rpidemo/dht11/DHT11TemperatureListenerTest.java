package dev.termian.rpidemo.dht11;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DHT11TemperatureListenerTest {

    private DHT11TemperatureListener dht11;

    @Before
    public void setUp() {
        dht11 = new DHT11TemperatureListener(-1, -1) {
            @Override
            protected void initPGPIO(int gpio, int sampleRate) {
                // noop
            }
        };
    }

    @Test
    public void givenNoTickWrap_When_calculateDuration_Then_returnProperPositiveDuration() {
        long duration = dht11.calculateDuration(1, 2);
        assertEquals(1, duration);
    }

    @Test
    public void givenTickWrap_When_calculateDuration_Then_returnProperPositiveDuration() {
        long duration = dht11.calculateDuration(4294967295L, 0);
        assertEquals(1, duration);
    }

    @Test
    public void givenTicksFromRealCase_parseTransmission_Then_returnCorrectHumidityTemperature() {
        HumidityTemperature humidityTemperature = dht11.parseTransmission(new long[]{
                3932794588L, 3932815698L, 3932815718L, 3932815798L, 3932815878L, 3932815928L,
                3932815958L, 3932816008L, 3932816033L, 3932816083L, 3932816158L, 3932816208L,
                3932816278L, 3932816333L, 3932816358L, 3932816408L, 3932816478L, 3932816533L,
                3932816558L, 3932816608L, 3932816638L, 3932816688L, 3932816713L, 3932816768L,
                3932816793L, 3932816843L, 3932816868L, 3932816923L, 3932816948L, 3932816998L,
                3932817068L, 3932817123L, 3932817148L, 3932817198L, 3932817223L, 3932817278L,
                3932817353L, 3932817403L, 3932817428L, 3932817478L, 3932817503L, 3932817558L,
                3932817583L, 3932817633L, 3932817708L, 3932817758L, 3932817783L, 3932817838L,
                3932817908L, 3932817958L, 3932818028L, 3932818083L, 3932818108L, 3932818163L,
                3932818188L, 3932818238L, 3932818263L, 3932818318L, 3932818343L, 3932818393L,
                3932818418L, 3932818473L, 3932818498L, 3932818548L, 3932818573L, 3932818628L,
                3932818653L, 3932818703L, 3932818778L, 3932818828L, 3932818853L, 3932818908L,
                3932818978L, 3932819028L, 3932819053L, 3932819108L, 3932819178L, 3932819233L,
                3932819258L, 3932819308L, 3932819378L, 3932819433L, 3932819458L, 3932819508L,
                3932819533L, 3932819588L
        });
        assertEquals("| Humidity = 52.9% | Temperature = 22.1°C (71.78°f)", humidityTemperature.toString().substring(13));
    }
}