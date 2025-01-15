///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.2-dev
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
//SOURCES HttpUser.java
//JAVA 21

import io.github.wfouche.tulip.api.TulipApi;

public class App {
   public static void main(String[] args) {
      TulipApi.runTulip("benchmark_config.jsonc");
   }
}
