package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{Gdx, Screen}
import com.oeshiro.qixxy.Qixxy

/**
  * Abstract class for game screens of the game.
  *
  * @param game - a reference to the game class.
  */
abstract class AbstractGameScreen(private val game: Qixxy)
  extends Screen {

  override def render(delta: Float) {
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
  }

  override def resume() {}

  override def dispose() {}
}
