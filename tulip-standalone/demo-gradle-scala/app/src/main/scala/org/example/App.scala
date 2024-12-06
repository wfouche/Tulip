package org.example

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

object App {

  def main(args: Array[String]): Unit = {
    TulipApi.runTulip("benchmark_config.json", new TulipUserFactory())
  }

}
