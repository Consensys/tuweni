// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.benchmark;

import java.io.IOException;

import one.profiler.AsyncProfiler;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(value = Scope.Benchmark)
public class ProfiledBenchmark {
  private int counter = getWarmupIterations();

  @Setup(Level.Iteration)
  public void startProfiler() throws IOException {
    if (counter-- == 0) {
      String fileName = "/tmp/flamegraph-" + getUniqueTestId() + ".html";
      AsyncProfiler.getInstance(System.getenv("ASYNC_PROFILER"))
          .execute("start,interval=1ms,collapsed,flamegraph,features=vtable,file=" + fileName);
    }
  }

  /**
   * Warmup iterations from where to start the profiler from.
   *
   * @return warmup iterations
   */
  int getWarmupIterations() {
    return 10;
  }

  /**
   * Benchmark test id to be used in the filename for the profiler benchmark.
   *
   * @return unique test id
   */
  String getUniqueTestId() {
    return this.getClass().getSimpleName() + "-" + System.currentTimeMillis();
  }
}
