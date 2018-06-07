/*
 * Copyright © 2016 Oleg Ivanov
 * Licensed under the MIT license.
 */

package com.oeshiro.qixxy

import com.badlogic.gdx.{Game, Gdx}
import com.oeshiro.qixxy.Screens._

class Qixxy extends Game {
  val LOG = classOf[Qixxy].getSimpleName

  override def create() {
    setScreen(new MainMenuScreen(this))
  }
}
