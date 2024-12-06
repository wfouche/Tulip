package org.example;

import io.github.wfouche.tulip.api.TulipApi;
import io.github.wfouche.tulip.api.TulipUserFactory;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        TulipApi.runTulip("./benchmark_config.json", new TulipUserFactory());
    }
}
