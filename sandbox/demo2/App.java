///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.2-dev
//DEPS io.rest-assured:rest-assured:5.5.0
//SOURCES HttpUser.java
//JAVA 21

import io.github.wfouche.tulip.api.TulipApi;

public class App {
   public static void main(String[] args) {
      TulipApi.runTulip("benchmark_config.jsonc");
   }
}
