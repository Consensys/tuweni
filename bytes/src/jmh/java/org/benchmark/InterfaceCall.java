// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
@OutputTimeUnit(value = TimeUnit.MILLISECONDS)
public class InterfaceCall {
  interface A {
    void m();
  }

  static class C1 implements A {
    int c = 0;

    @Override
    public void m() {
      c++;
    }
  }

  static class C2 implements A {
    int c = 1;

    @Override
    public void m() {
      c++;
    }
  }

  static class C3 implements A {
    int c = 2;

    @Override
    public void m() {
      c++;
    }
  }

  A[] as;

  @Param({"mono", "mega"})
  private String mode;

  @Setup
  public void setup() {
    as = new A[300000000];
    boolean mega = mode.equals("mega");
    for (int c = 0; c < 300000000; c += 3) {
      as[c] = new C1();
      as[c + 1] = mega ? new C2() : new C1();
      as[c + 2] = mega ? new C3() : new C1();
    }
  }

  @Benchmark
  public void test() {
    for (A a : as) {
      a.m();
    }
  }
}
