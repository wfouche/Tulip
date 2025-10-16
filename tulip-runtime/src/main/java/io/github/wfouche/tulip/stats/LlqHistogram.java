package io.github.wfouche.tulip.stats;

// spotless:off
//DEPS com.fasterxml.jackson.core:jackson-databind:2.20.0
//DEPS org.hdrhistogram:HdrHistogram:2.2.2
// spotless:on

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

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
    private static long minNanos;
    private static final long[] qValues = new long[210];
    private final long[] qCounts = new long[210];

    // Precomputed powers of 10 up to 10^13
    //    private static final long[] POW10 = {
    //        1L, // 1 nanosecond
    //        10L,
    //        100L,
    //        1_000L, // 1 microsecond
    //        10_000L,
    //        100_000L,
    //        1_000_000L, // 1 millisecond
    //        10_000_000L,
    //        100_000_000L,
    //        1_000_000_000L, // 1 second
    //        10_000_000_000L, // 10 seconds
    //        100_000_000_000L, // 100 seconds
    //        1_000_000_000_000L, // 1000 seconds
    //        10_000_000_000_000L // 10_1000 seconds
    //    };

    public static long llq(long n) {
        if (n < 10) {
            return n;
        }

        long p = 1;
        while (p * 10 <= n) {
            p *= 10;
        }
        long scale = p / 10;

        // #0 - logarithmic scale calculation
        // CPU time: 03:11.9
        //        scale = (long) Math.pow(10, Math.floor(Math.log10(n)) - 1);

        // #1 - binary search - slower than linear search for this small array
        // CPU time: 03:17.4
        //        int index = Arrays.binarySearch(POW10, n);
        //        //System.out.println("index: " + index);
        //        if (index < 0) {
        //            //System.out.println(POW10[-index-2]);
        //            scale = POW10[-index - 3];
        //        } else {
        //            scale = POW10[index - 1];
        //        }

        // #2 - linear search - faster than binary search for this small array
        // CPU time: 02:26.5
        //        for (int i = 2; i < POW10.length; i++) {
        //            if (n < POW10[i]) {
        //                scale = POW10[i - 2];
        //                break;
        //            }
        //        }

        // #3 - hardcoded linear search - faster than binary search for this small array
        // CPU time: 02:33.7
        //        if (n < 100L) {
        //            scale = 1;
        //        } else if (n < 1_000L) {
        //            scale = 10L;
        //        } else if (n < 10_000L) {
        //            scale = 100L;
        //        } else if (n < 100_000L) {
        //            scale = 1000L;
        //        } else if (n < 1_000_000L) {
        //            scale = 10_000L;
        //        } else if (n < 10_000_000L) {
        //            scale = 100_000L;
        //        } else if (n < 100_000_000L) {
        //            scale = 1_000_000L;
        //        } else if (n < 1_000_000_000L) {
        //            scale = 10_000_000L;
        //        } else if (n < 10_000_000_000L) {
        //            scale = 100_000_000L;
        //        } else if (n < 100_000_000_000L) {
        //            scale = 1_000_000_000L;
        //        } else if (n < 1_000_000_000_000L) {
        //            scale = 10_000_000_000L;
        //        } else if (n < 10_000_000_000_000L) {
        //            scale = 100_000_000_000L;
        //        } else {
        //            scale = 1_000_000_000_000L;
        //        }

        // v25 = 25 * scale, v50 = 50 * scale
        long v25 = 25 * scale;
        long v50 = 50 * scale;

        // rounding logic
        return (((n * 10 + v25) / v50) * v50) / 10;
    }

    public void add(LlqHistogram llqh) {
        for (int i = 0; i < qCounts.length; i++) {
            qCounts[i] += llqh.qCounts[i];
        }
    }

    public void add(org.HdrHistogram.Histogram hdr, long scaleFactor) {
        for (HistogramIterationValue v : hdr.recordedValues()) {
            recordValue(scaleFactor * v.getValueIteratedTo(), v.getCountAddedInThisIterationStep());
        }
    }

    public void reset() {
        Arrays.fill(qCounts, 0L);
    }

    public void recordValue(long n) {
        recordValue(n, 1);
    }

    public void recordValue(long n, long count) {
        long q = llq(n);
        int index = Arrays.binarySearch(qValues, q);
        qCounts[index] += count;
    }

    public long minValue() {
        for (int i = 0; i < qCounts.length; i++) {
            if (qCounts[i] != 0) {
                return qValues[i];
            }
        }
        return 0;
    }

    public long maxValue() {
        for (int i = qCounts.length - 1; i >= 0; i--) {
            if (qCounts[i] != 0) {
                return qValues[i];
            }
        }
        return 0;
    }

    public long numValues() {
        long totalCount = 0;
        for (long count : qCounts) {
            totalCount += count;
        }
        return totalCount;
    }

    public double averageValue() {
        double totalSum = 0.0;
        long totalCount = 0;
        for (int i = 0; i < qCounts.length; i++) {
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

        for (int i = 0; i < qCounts.length; i++) {
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
        // Bins are already sorted, no need to sort again.
        // Collections.sort(bins);

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

        for (int i = 0; i < qCounts.length; i++) {
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

    private String formatTimeValue(long qv, String prefix) {
        if (qv < 1000L) {
            // ns - nanoseconds
            if (qv == 0L) {
                return String.format("    <td>%s &lt; %d ns</td>\n", prefix, minNanos);
            } else {
                return String.format("    <td>%s %d ns</td>\n", prefix, qv);
            }
        } else if (qv < 1_000_000L) {
            // μs - microseconds
            return String.format(Locale.US, "    <td>%s %.1f μs</td>\n", prefix, qv / 1000.0);
        } else if (qv < 1_000_000_000L) {
            // ms - milliseconds
            return String.format(Locale.US, "    <td>%s %.1f ms</td>\n", prefix, qv / 1000000.0);
        } else {
            // s - seconds
            return String.format(Locale.US, "    <td>%s %.1f s</td>\n", prefix, qv / 1000000000.0);
        }
    }

    public String toHtmlString() {
        long nv = numValues();
        long cv = 0;
        long av;
        StringBuilder htmlString = new StringBuilder();
        htmlString.append("  <tr>\n");
        htmlString.append("    <th>Value</th>\n");
        htmlString.append("    <th>Percentile</th>\n");
        htmlString.append("    <th>Total Count</th>\n");
        htmlString.append("    <th>Bucket Size</th>\n");
        htmlString.append("    <th>Percentage</th>\n");
        htmlString.append("    <th>Above Count</th>\n");
        htmlString.append("  </tr>\n");
        for (int i = qCounts.length - 1; i >= 0; i--) {
            if (qCounts[i] != 0) {
                av = cv;
                cv += qCounts[i];
                htmlString.append("  <tr>\n");
                long qv = qValues[i];

                // Value
                htmlString.append(formatTimeValue(qv, ""));

                // Percentile
                htmlString.append(
                        String.format(Locale.US, "    <td>%.6f</td>\n", 1.0 - 1.0 * av / nv));

                // Total Count
                htmlString.append(String.format("    <td>%d</td>\n", nv - av));

                // Bucket Size
                htmlString.append(String.format("    <td>%d</td>\n", qCounts[i]));

                // Percentage
                double percentage = 100.0 * qCounts[i] / nv;
                if (percentage >= 1.0) {
                    htmlString.append(
                            String.format(
                                    Locale.US, "    <td><mark>%.3f</mark></td>\n", percentage));
                } else {
                    htmlString.append(String.format(Locale.US, "    <td>%.3f</td>\n", percentage));
                }

                // Above Count
                htmlString.append(String.format("    <td>%d</td>\n", av));

                htmlString.append("  </tr>\n");
            }
        }

        htmlString.append("  <tr>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("  </tr>\n");

        htmlString.append("  <tr>\n");
        htmlString.append(formatTimeValue((long) averageValue(), "avg: "));
        htmlString.append(formatTimeValue((long) standardDeviationValue(), "sd: "));
        htmlString.append(formatTimeValue(percentileValue(50.0), "p50: "));
        htmlString.append(formatTimeValue(percentileValue(90.0), "p90: "));
        htmlString.append(formatTimeValue(percentileValue(95.0), "p95: "));
        htmlString.append(formatTimeValue(percentileValue(99.0), "p99: "));
        htmlString.append("  </tr>\n");

        return htmlString.toString();
    }

    public void fromJsonString(String jsonString) throws IOException {
        TypeReference<Map<Long, Long>> typeRef = new TypeReference<>() {};
        // 0. Zero values array
        reset();

        // 1. Initialize the ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. Deserialize the JSON string using the TypeReference
        Map<Long, Long> longMap = objectMapper.readValue(jsonString, typeRef);

        // 3. Restore counts
        longMap.forEach(this::recordValue);
    }

    public String toJsonString() {
        StringBuilder jsonString = new StringBuilder("{");
        int count = 0;
        for (int i = 0; i < qCounts.length; i++) {
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

    public void display() {
        System.out.println();
        System.out.println("  AVG: " + averageValue());
        System.out.println("   SD: " + standardDeviationValue());
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
        // initialize qValues
        int idx = 0;
        while (idx < 11) {
            qValues[idx] = idx;
            idx += 1;
        }

        long num = 10L;
        long step = 5L;
        while (num < 1_000_000_000_001L) {
            num += step;
            qValues[idx] = num;
            idx += 1;
            if (Math.log10(num) == (double) ((int) Math.log10(num))) {
                step *= 10L;
            }
        }
        // System.out.println("idx = " + idx);

        // determine minNanos - smallest duration nanoTime can measure
        long[] duration = new long[1_000_000];
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        long t0 = System.nanoTime();
        long t1 = t0;
        for (int i = 0; i < duration.length; i++) {
            while (t1 == t0) {
                t1 = System.nanoTime();
            }
            duration[i] = t1 - t0;
            t0 = t1;
        }
        for (long value : duration) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        minNanos = llq(min);
    }

    public static void main(String[] args) {
        LlqHistogram hist = new LlqHistogram();
        Histogram hdr = new Histogram(4);

        for (long i = 0; i < 1_000_000_001L; i++) {
            hist.recordValue(i);
            hdr.recordValue(i);
        }
        // hist.update(1460139);
        hist.display();
        System.out.println();
        LlqHistogram hist2 = new LlqHistogram();
        hist2.add(hdr, 1);
        hist2.display();
    }
}
