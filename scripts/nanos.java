class nanos {
    public static void main(String[] args) {
        long[] duration = new long[1_000_000];

        // ....
        for (int j = 0; j < 10; j++) {
            long t0 = System.nanoTime();
            long t1 = t0;
            for (int i = 0; i < duration.length; i++) {
                while (t1 == t0) {
                    t1 = System.nanoTime();
                }
                duration[i] = t1-t0;
                t0 = t1;
            }
        }

        // ....
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long value : duration) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        System.out.println();
        System.out.println("min: " + min + " max: " + max);
    }
}