package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.nullchain.Null;

/**
 * Simple performance harness for a normal Null chain.
 */
public class NullChainPerfTest {

    public static void main(String[] args) {
        int iterations = 100_000;
        // Warm-up
        runNormalChain(10_000);
        runBaselineBusiness(10_000);
        runChainOverhead(10_000);

        long elapsedChainNs = runNormalChain(iterations);
        long elapsedBusinessNs = runBaselineBusiness(iterations);
        long elapsedOverheadNs = runChainOverhead(iterations);

        printResult("Null chain", iterations, elapsedChainNs);
        printResult("Baseline business", iterations, elapsedBusinessNs);
        printResult("Chain overhead", iterations, elapsedOverheadNs);
    }

    public static long runNormalChain(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String input = "  value-" + (i & 1023) + "  ";
            String out = Null.of(input)
                    .map(String::trim)
                    .ifGo(s -> s.length() > 0)
                    .map(String::toUpperCase)
                    .orElse("DEFAULT");
            // prevent JIT from eliminating
            if (out == null) {
                throw new IllegalStateException("Unexpected null");
            }
        }
        return System.nanoTime() - start;
    }

    public static long runBaselineBusiness(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String input = "  value-" + (i & 1023) + "  ";
            String trimmed = input.trim();
            String out = trimmed.length() > 0 ? trimmed.toUpperCase() : "DEFAULT";
            if (out == null) {
                throw new IllegalStateException("Unexpected null");
            }
        }
        return System.nanoTime() - start;
    }

    public static long runChainOverhead(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String input = "value-" + (i & 1023);
            String out = Null.of(input)
                    .map(v -> v)
                    .ifGo(v -> true)
                    .orElse("DEFAULT");
            if (out == null) {
                throw new IllegalStateException("Unexpected null");
            }
        }
        return System.nanoTime() - start;
    }

    private static void printResult(String label, int iterations, long elapsedNs) {
        double ms = elapsedNs / 1_000_000.0;
        double avg = elapsedNs / (double) iterations;
        System.out.println(label + " iterations: " + iterations);
        System.out.println("Elapsed: " + ms + " ms");
        System.out.println("Avg: " + avg + " ns/op");
    }
}
