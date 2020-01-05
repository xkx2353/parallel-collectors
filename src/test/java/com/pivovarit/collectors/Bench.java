package com.pivovarit.collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/*
Benchmark                (parallelism)   Mode  Cnt     Score     Error  Units
Bench.parallel                       1  thrpt    5    63.495 ±   1.101  ops/s
Bench.parallel_batching              1  thrpt    5  8683.955 ± 227.077  ops/s
Bench.parallel                      10  thrpt    5    91.673 ±   1.137  ops/s
Bench.parallel_batching             10  thrpt    5  6274.509 ± 185.763  ops/s
Bench.parallel                     100  thrpt    5   620.897 ±  71.591  ops/s
Bench.parallel_batching            100  thrpt    5  2346.040 ± 137.642  ops/s
Bench.parallel                    1000  thrpt    5   749.441 ±  71.090  ops/s
Bench.parallel_batching           1000  thrpt    5   972.762 ±  78.602  ops/s
 */
public class Bench {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({"1", "10", "100", "1000"})
        public int parallelism;

        private volatile ExecutorService executor;

        @Setup(Level.Trial)
        public void setup() {
            executor = Executors.newFixedThreadPool(1000);
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            executor.shutdown();
        }
    }

    private static final List<Integer> source = IntStream.range(0, 1000)
      .boxed()
      .collect(toList());

    @Benchmark
    public List<Integer> parallel(BenchmarkState state) {
        return source.stream()
          .collect(ParallelCollectors.parallel(i -> i, toList(), state.executor, state.parallelism))
          .join();
    }

    @Benchmark
    public List<Integer> parallel_batching(BenchmarkState state) {
        return source.stream()
          .collect(ParallelCollectors.Batching.parallel(i -> i, toList(), state.executor, state.parallelism))
          .join();
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(
          new OptionsBuilder()
            .include(Bench.class.getSimpleName())
            .warmupIterations(5)
            .measurementIterations(5)
            .forks(1)
            .build()).run();
    }
}