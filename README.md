### rpidemo

This repository currently contains a demo for reading relative humidity and temperature from DHT11 using `pi4j-library-pigpio`.
The solution is based on the *pigpio* alert function and is fairly robust. It does not suffer from missed reads. Root privileges are required for GPIO access.
Read more on https://blog.termian.dev/posts/dht11-java-pi4j-pigpio

```shell
./mvnw clean install
# run on RPI
sudo java -jar -DDHT11_GPIO=21 target/rpidemo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Besides the program, there are also some JIT JMH tests that verify whether polling times for `pi4j-core` and `pi4j-library-pigpio` are acceptable as a potential alternative.
There is also a GraalVM configuration that builds a crude native executable to get the feeling of timings for AOT compilation (can be build on OCI).

```shell
# run on OCI aarch64
./mvnw clean install -Pnative
# run on RPI
sudo target/rpidemo -Dpi4j.library.path=/path/to/unpacked/pi4j-library-pigpio-2.3.0.jar!lib/aarch64/
```