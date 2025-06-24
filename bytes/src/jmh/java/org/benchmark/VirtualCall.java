// Copyright The Tuweni Authors
// SPDX-License-Identifier: Apache-2.0
package org.benchmark;

import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
@OutputTimeUnit(value = TimeUnit.MILLISECONDS)
public class VirtualCall {

  abstract static class A {
    int c1, c2, c3;

    public abstract void m();
    //    public abstract void m_aux();
  }

  static class C1 extends A {
    @Override
    public void m() {
      c1++;
    }
    //    @Override
    //    public void m_aux() {
    //      c1++;
    //    }
  }

  static class C2 extends A {
    @Override
    public void m() {
      c2++;
    }
  }

  static class C3 extends C1 {
    @Override
    public void m() {
      c3++;
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
