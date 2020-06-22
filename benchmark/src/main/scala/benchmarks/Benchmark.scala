package benchmarks

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

/**
 * Compare the performance of parsing operations across effectful streams.
 *
 * The following command will run the benchmarks with reasonable settings:
 *
 * > sbt "jmh:run -i 10 -wi 10 -f 2 -t 1 benchmarks.ZStreamBenchmark"
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ZStreamBenchmark extends ExampleData with ZStreamParsingBenchmarks

/**
 * Compare the performance of parsing operations across effectful streams.
 *
 * The following command will run the benchmarks with reasonable settings:
 *
 * > sbt "jmh:run -i 10 -wi 10 -f 2 -t 1 benchmarks.FS2Benchmark"
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class FS2Benchmark extends ExampleData with FS2ParsingBenchmarks
