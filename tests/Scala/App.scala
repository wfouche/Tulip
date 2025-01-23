//> using jvm 21
//> using dep io.github.wfouche.tulip:tulip-runtime:2.1.4
//> using dep org.springframework.boot:spring-boot-starter-web:3.4.1
//> using dep org.slf4j:slf4j-api:2.0.16
//> using dep ch.qos.logback:logback-core:1.5.16
//> using dep ch.qos.logback:logback-classic:1.5.16
//> using javaOpt -server, -Xms2g, -Xmx2g, -XX:+UseZGC, -XX:+ZGenerational
//> using repositories m2local

// https://yadukrishnan.live/developing-java-applications-with-scala-cli
// https://www.baeldung.com/scala/scala-cli-intro

import io.github.wfouche.tulip.api.TulipApi

object App {
  def main(args: Array[String]): Unit = {
    TulipApi.runTulip("benchmark_config.jsonc")
  }
}
