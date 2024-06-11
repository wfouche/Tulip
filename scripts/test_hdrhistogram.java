///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.hdrhistogram:HdrHistogram:2.2.2

import org.HdrHistogram.Histogram;

import java.util.concurrent.ThreadLocalRandom;

public class test_hdrhistogram {
    public static void main(String[] args) {
        //Histogram histogram = new Histogram(3600*1000*1000L, 3);
        Histogram histogram = new Histogram(3);

        // 6 ms delay (average) with 25% of values
        for (int i=0; i != 250000; i++) {
            histogram.recordValue(ThreadLocalRandom.current().nextLong(12 + 1));
        }

        // 14 ms delay (average) with 75% of values
        for (int i=0; i != 750000; i++) {
            histogram.recordValue(ThreadLocalRandom.current().nextLong(28 + 1));
        }
        // histogram.getMean() = 12.0

        System.out.println(histogram.getTotalCount());
        histogram.outputPercentileDistribution(System.out,1.0);
        System.out.println(histogram.getMean());
        System.out.println(histogram.getStdDeviation());
        System.out.println(histogram.getMaxValue());
        System.out.println(histogram.getValueAtPercentile(50.0));
        System.out.println(histogram.getValueAtPercentile(90.0));
        System.out.println(histogram.getValueAtPercentile(95.0));
        System.out.println(histogram.getValueAtPercentile(99.0));
        System.out.println(histogram.getValueAtPercentile(99.9));
    }
}
