class nanos {
    public static void main(String[] args) {
        long[] duration = new long[1000];

        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 100; i++) {
                long t0 = System.nanoTime();
                long t1 = System.nanoTime();
                while (t1 == t0) {
                    t1 = System.nanoTime();
                }
                duration[i] = t1-t0;
            }
        }
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