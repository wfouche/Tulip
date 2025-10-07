package io.github.wfouche.tulip.stats;

// spotless:off
//DEPS com.fasterxml.jackson.core:jackson-databind:2.20.0
// spotless:on

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LlqHistogram {

    // Helper class to group value and count for sorting
    private static class Bin implements Comparable<Bin> {
        final long value;
        final long count;

        public Bin(long value, long count) {
            this.value = value;
            this.count = count;
        }

        // Sorts based on the quantized value (ascending order)
        @Override
        public int compareTo(Bin other) {
            return Long.compare(this.value, other.value);
        }
    }

    // ....
    static long[] qValues = new long[156];
    public long[] qCounts = new long[156];
    public int maxIndex = -1;
    public long minValue = Long.MAX_VALUE;
    public long maxValue = Long.MIN_VALUE;
    public long numValues = 0;

    // Precomputed powers of 10 up to 10^13
    private static final long[] POW10 = {
        1L, // 1 microsecond
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L, // 1 second
        10000000L, // 10 seconds
        100000000L, // 100 seconds
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

    public void reset() {
        Arrays.fill(qCounts, 0L);
        maxIndex = -1;
        minValue = Long.MAX_VALUE;
        maxValue = Long.MIN_VALUE;
        numValues = 0;
    }

    public void update(long n) {
        update(n, 1);
    }

    public void update(long n, long count) {
        long q = llq(n);
        int index = Arrays.binarySearch(qValues, q);
        if (index > maxIndex) {
            maxIndex = index;
        }
        qCounts[index] += count;
        if (n < minValue) {
            minValue = n;
        }
        if (n > maxValue) {
            maxValue = n;
        }
        numValues += 1;
    }

    public long minValue() {
        if (minValue == Long.MAX_VALUE) {
            return 0;
        }
        return minValue;
    }

    public long maxValue() {
        if (maxValue == Long.MIN_VALUE) {
            return 0;
        }
        return maxValue;
    }

    public int maxIndex() {
        return maxIndex;
    }

    public long numValues() {
        return numValues;
    }

    public double averageValue() {
        double totalSum = 0.0;
        long totalCount = 0;
        for (int i = 0; i < maxIndex + 1; i++) {
            long qvalue = qValues[i];
            long qcount = qCounts[i];
            totalSum += (double) qvalue * (double) qcount;
            totalCount += qcount;
        }
        if (totalCount == 0) {
            return 0.0;
        }
        return totalSum / totalCount;
    }

    public long percentileValue(double percentile) {
        if (percentile < 0.0 || percentile > 100.0) {
            throw new IllegalArgumentException(
                    "Percentile must be between 0.0 and 100.0 (inclusive).");
        }

        // Step 1: Combine and Sort Bins
        List<Bin> bins = new ArrayList<>(qValues.length);
        long totalCount = 0L;

        for (int i = 0; i < maxIndex + 1; i++) {
            long count = qCounts[i];
            if (count > 0) {
                bins.add(new Bin(qValues[i], count));
                totalCount += count;
            }
        }

        // Handle empty dataset
        if (totalCount == 0) {
            return 0L;
        }

        // Sort the bins by value. This is crucial for percentile calculation.
        Collections.sort(bins);

        // Handle 0th and 100th percentile edge cases for quick return
        if (percentile == 0.0) {
            return bins.get(0).value; // Minimum value
        }
        if (percentile == 100.0) {
            return bins.get(bins.size() - 1).value; // Maximum value
        }

        // Step 2: Determine Target Rank (R)
        // Standard method (R=ceil(P/100 * N)): finds the rank (1-based index) we are looking for.
        // The value is the smallest value whose cumulative frequency distribution is greater than
        // or equal to R.
        long targetRank = (long) Math.ceil((percentile / 100.0) * totalCount);
        long cumulativeCount = 0L;

        // Step 3: Find the Quantized Value at the Target Rank
        for (Bin bin : bins) {
            cumulativeCount += bin.count;
            if (cumulativeCount >= targetRank) {
                return bin.value;
            }
        }

        // Fallback (should only be reached due to precision issues near 100%)
        return bins.get(bins.size() - 1).value;
    }

    public double standardDeviationValue() {
        // 1. Calculate the mean (mu). This also validates array length.
        double mean = averageValue();

        long totalCount = 0L;
        // Sum of (Value - Mean)^2 * Count
        double sumOfSquaredDifferences = 0.0;

        for (int i = 0; i < maxIndex + 1; i++) {
            long value = qValues[i];
            long count = qCounts[i];

            if (count > 0) {
                totalCount += count;

                // Difference (Value_i - Mean)
                double difference = (double) value - mean;

                // Squared Difference * Count
                // We use (double)count here to ensure the calculation is done in double precision.
                sumOfSquaredDifferences += Math.pow(difference, 2) * (double) count;
            }
        }

        // Standard deviation is 0 if N=0 or N=1 (no variance)
        if (totalCount <= 1) {
            return 0.0;
        }

        // 3. Calculate Variance (sum of squared diff / N)
        // Using totalCount for population standard deviation (assuming the histogram represents the
        // whole population)
        double variance = sumOfSquaredDifferences / totalCount;

        // 4. Calculate Standard Deviation (sqrt(Variance))
        return Math.sqrt(variance);
    }

    public String toJsonString() {
        StringBuilder jsonString = new StringBuilder("{");
        int count = 0;
        for (int i = 0; i < maxIndex + 1; i++) {
            if (qCounts[i] != 0) {
                if (count > 0) {
                    jsonString.append(", ");
                }
                jsonString.append("\"");
                jsonString.append(qValues[i]);
                jsonString.append("\"");
                jsonString.append(": ");
                jsonString.append(qCounts[i]);
                count += 1;
            }
        }
        jsonString.append("}");
        return jsonString.toString();
    }

    public void fromJsonString(String jsonString) {
        TypeReference<Map<Long, Long>> typeRef = new TypeReference<Map<Long, Long>>() {};
        try {
            // 0. Zero values array
            reset();

            // 1. Initialize the ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // 2. Deserialize the JSON string using the TypeReference
            Map<Long, Long> longMap = objectMapper.readValue(jsonString, typeRef);

            // 3. Restore counts
            longMap.forEach(this::update);

        } catch (IOException e) {
            System.err.println("Error reading JSON string: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void display() {
        System.out.println();
        System.out.println("  IDX: " + maxIndex());
        System.out.println("  AVG: " + averageValue());
        System.out.println("  STD: " + standardDeviationValue());
        System.out.println("  P50: " + percentileValue(50.0));
        System.out.println("  P90: " + percentileValue(90.0));
        System.out.println("  P95: " + percentileValue(95.0));
        System.out.println("  P99: " + percentileValue(99.0));
        System.out.println("  MIN: " + minValue());
        System.out.println("  MAX: " + maxValue());
        System.out.println("  NUM: " + numValues());
        String json = toJsonString();
        System.out.println("  JSN: " + json);
    }

    static {
        int idx = 0;
        while (idx < 11) {
            qValues[idx] = idx;
            idx += 1;
        }

        long num = 10L;
        long step = 5L;
        while (num < 1000000001L) {
            num += step;
            qValues[idx] = num;
            idx += 1;
            if (Math.log10(num) == (double) ((int) Math.log10(num))) {
                step *= 10L;
            }
        }
    }

    public static void main(String[] args) {
        LlqHistogram hist = new LlqHistogram();

        // for (long i = 0; i < 1000000001L; i++) {
        //    hist.update(i);
        // }
        System.out.println("LLQ(1460139) = " + llq(1460139));
        hist.update(1460139);
        hist.display();
    }
}
