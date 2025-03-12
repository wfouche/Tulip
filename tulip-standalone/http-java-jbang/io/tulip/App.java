///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.7-dev
//SOURCES DemoHttpUser.java
//JAVA 21

package io.tulip;

import io.github.wfouche.tulip.api.TulipApi;

public class App {
   public static void main(String[] args) {
      TulipApi.runTulip("benchmark_config.json");
   }
}