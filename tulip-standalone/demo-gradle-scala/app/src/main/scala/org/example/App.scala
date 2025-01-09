package org.example

import io.github.wfouche.tulip.api._

object App {
  def main(args: Array[String]): Unit = {
    TulipApi.runTulip("benchmark_config.jsonc")
  }
}
