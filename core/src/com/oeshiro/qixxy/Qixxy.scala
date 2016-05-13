package com.oeshiro.qixxy

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.{Color, FPSLogger}
import com.badlogic.gdx.{Game, Gdx}
import com.oeshiro.qixxy.Screens._

class Qixxy extends Game {

  val LOG = classOf[Qixxy].getSimpleName

  override def create() {

    Gdx.app.log(LOG, "Creating game")
    setScreen(new MainMenuScreen(this))
  }
}