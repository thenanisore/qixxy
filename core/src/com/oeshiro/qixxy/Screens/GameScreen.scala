package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.oeshiro.qixxy.Gameplay.{WorldController, WorldRenderer}
import com.oeshiro.qixxy.Qixxy

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

    Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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
