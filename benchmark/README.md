circe-zstream-benchmark
-----------------------

modelled after - and borrowed from - [circe-benchmarks](https://github.com/circe/circe-benchmarks), this is a partial benchmarking suite comparing the performance of streaming parsing implementations.

# OBSERVED RESULTS 

performance differences between streaming parser implementations written using both zio/zstream and cats-effect/fs2.

Benchmark                      Mode  Cnt     Score      Error  Units
FS2Benchmark.decodeFoos       thrpt   20    14.147 ±    0.910  ops/s
ZStreamBenchmark.decodeFoos   thrpt   20    11.588 ±    0.267  ops/s

FS2Benchmark.decodeInts       thrpt   20  1066.389 ±  193.975  ops/s
ZStreamBenchmark.decodeInts   thrpt   20   590.725 ±   73.551  ops/s

FS2Benchmark.decodeInts2      thrpt   20  3305.753 ±  231.636  ops/s
ZStreamBenchmark.decodeInts2  thrpt   20  2526.520 ± 1072.821  ops/s

FS2Benchmark.parseFoos        thrpt   20    17.221 ±    0.106  ops/s
ZStreamBenchmark.parseFoos    thrpt   20    13.653 ±    1.008  ops/s

FS2Benchmark.parseInts        thrpt   20  3237.710 ±  207.122  ops/s
ZStreamBenchmark.parseInts    thrpt   20  1566.975 ±  907.996  ops/s

FS2Benchmark.parseInts2       thrpt   20  3393.618 ±  540.888  ops/s
ZStreamBenchmark.parseInts2   thrpt   20  3993.057 ± 1167.822  ops/s