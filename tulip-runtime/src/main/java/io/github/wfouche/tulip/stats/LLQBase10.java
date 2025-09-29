package io.github.wfouche.tulip.stats;

import java.util.Arrays;

public class LLQBase10 {

    // ....
    static long[] idx2llq = new long[137];
    public long[] llqhist = new long[137];

    // Precomputed powers of 10 up to 10^13
    private static final long[] POW10 = {
        1L, // 1 microsecond
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L, // 1 second
        10000000L,
        100000000L,
        1000000000L, // 16.67 minutes
        10000000000L, // 166.67 minutes
        100000000000L, // 27.777 hours  = 1 day + 3.777 hours
        1000000000000L, // 277.77 hours = 11 days + 13.77 hours
        10000000000000L // 2777.7 hours = 115
    };

    public static long llq(long n) {
        if (n < 10) {
            return n;
        }
        long scale = 0;
        for (int i = 1; i < POW10.length; i++) {
            if (n < POW10[i]) {
                scale = POW10[i - 2];
                break;
            }
        }
        // v25 = 25 * scale, v50 = 50 * scale
        long v25 = 25 * scale;
        long v50 = 50 * scale;

        // rounding logic
        return (((n * 10 + v25) / v50) * v50) / 10;
    }

    public void update(long n) {
        long q = llq(n);
        int index = Arrays.binarySearch(idx2llq, q);
        llqhist[index] += 1;
    }

    public double averageValue() {
        double totalSum = 0.0;
        long totalCount = 0;
        for (int i = 0; i != llqhist.length; i++) {
            long qvalue = idx2llq[i];
            long qcount = llqhist[i];
            totalSum += (double) qvalue * (double) qcount;
            totalCount += qcount;
        }
        if (totalCount == 0) {
            return 0.0;
        }
        return totalSum / totalCount;
    }

    public void display() {
        for (int i = 0; i != llqhist.length; i++) {
            System.out.println(idx2llq[i] + " = " + llqhist[i]);
        }
        System.out.println("AVG: " + averageValue());
    }

    static {
        int idx = 0;
        while (idx < 11) {
            idx2llq[idx] = idx;
            idx += 1;
        }

        long num = 10L;
        long step = 5L;
        while (num < 100000000L) {
            num += step;
            idx2llq[idx] = num;
            idx += 1;
            if (Math.log10(num) == (double) ((int) Math.log10(num))) {
                step *= 10L;
            }
        }
    }

    public static void main(String[] args) {
        LLQBase10 hist = new LLQBase10();

        for (long i = 0; i < 100000001L; i++) {
            hist.update(i);
        }

        hist.display();
    }

}
