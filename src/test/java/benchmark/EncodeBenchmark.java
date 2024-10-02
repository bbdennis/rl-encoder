package benchmark;

import com.inj.rle.RLEncoder;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Threads(1)
public class EncodeBenchmark {

    private static final RLEncoder encoder = new RLEncoder();

    private byte[] inputBytes;

    @Setup(Level.Trial)
    public void setup() throws URISyntaxException, IOException {
        Path input = Paths.get(getClass().getClassLoader().getResource("sample.b64").toURI());
        System.out.println("Encoding file: " + input);

        StringBuilder sb = new StringBuilder();
        Files.readAllLines(input).forEach(sb::append);
        inputBytes = sb.toString().getBytes();

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(warmups = 1, value = 1)
    @Warmup(batchSize = -1, iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(batchSize = -1, iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public Object encode() throws IOException {
        return encoder.encode(inputBytes, 0, inputBytes.length);
    }

    @TearDown
    public void tearDown() {
        // clean up
    }
}
