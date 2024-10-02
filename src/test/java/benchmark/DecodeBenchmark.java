package benchmark;

import com.inj.rle.RLDecoder;
import com.inj.rle.RLEncoder;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Threads(1)
public class DecodeBenchmark {

    private static final RLDecoder decoder = new RLDecoder();

    private List<String> inputForDecode;

    @Setup(Level.Trial)
    public void setup() throws URISyntaxException, IOException {

        Path encodedFile = Paths.get(getClass().getClassLoader().getResource("sample.b64_encoded.csv").toURI());
        inputForDecode = Files.readAllLines(encodedFile);

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Fork(warmups = 1, value = 1)
    @Warmup(batchSize = -1, iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(batchSize = -1, iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public Object decode() throws IOException {
        return decoder.decode(inputForDecode);
    }

    @TearDown
    public void tearDown() {
        // clean up
    }
}
