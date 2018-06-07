package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.Gdx
import com.oeshiro.qixxy.Gameplay.{WorldController, WorldRenderer}
import com.oeshiro.qixxy.Qixxy

/**
  * The GameScreen class contains the controllers and renderers of a game screen.
  *
  * @param game - a reference to the game class.
  */
class GameScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[GameScreen].getSimpleName

  var wController: WorldController = _
  var wRenderer: WorldRenderer = _
  var paused: Boolean = false

  override def render(delta: Float) {
    if (!paused) {
      wController.update(delta)
    }
    super.render(delta)
    wRenderer.render()
  }

  override def resize(width: Int, height: Int) {
    wRenderer.resize(width, height)
  }

  override def show() {
    wController = new WorldController(game)
    wRenderer = new WorldRenderer(wController)
    Gdx.input.setCatchBackKey(true)
  }

  override def hide() {
    wController.dispose()
    wRenderer.dispose()
    Gdx.input.setCatchBackKey(false)
  }

  override def resume() {
    super.resume()
    paused = false
  }

  override def pause() {
    paused = true
  }
}
