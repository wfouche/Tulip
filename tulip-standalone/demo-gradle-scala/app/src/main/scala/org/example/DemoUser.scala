package org.example

import io.github.wfouche.tulip.api.TulipUser
import io.github.wfouche.tulip.api.TulipConsole

class DemoUser(val userId: Int, val threadId: Int) extends TulipUser(userId, threadId) {

  override def onStart(): Boolean = {
    TulipConsole.put("ScalaDemoUser " + userId)
    true
  }

  override def action1(): Boolean = {
    Thread.sleep(10)
    true
  }

  override def action2(): Boolean = {
    Thread.sleep(20)
    true
  }

  override def action3(): Boolean = true

  override def onStop(): Boolean = true

}