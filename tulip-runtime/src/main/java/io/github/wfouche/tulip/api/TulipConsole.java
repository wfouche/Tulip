package io.github.wfouche.tulip.api;

import io.github.wfouche.tulip.core.Console;

public class TulipConsole {

    /**
     * Outputs a string to the Tulip console.
     * This method serves as a wrapper for the Console.put() method,
     * providing a simplified interface for console output in the Tulip framework.
     *
     * @param text The string to be output to the console.
     */
    public static void put (String text) {
        Console.put(text);
    }
}
