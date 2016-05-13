package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.{Input, Gdx}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.oeshiro.qixxy.{Utils, Qixxy}

class HelpScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[HelpScreen].getSimpleName

  val camera = new OrthographicCamera()
  camera.setToOrtho(false, Utils.viewportWidth,
    Utils.viewportHeight)

  val batch = new SpriteBatch()

  // fonts and font settings
  val textFont = Utils.initializeFont(Utils.fontArcade, 30, Color.valueOf("f0e1fe"))

  override def render(delta: Float) {
    handleInput()
    camera.update()
    batch.setProjectionMatrix(camera.combined)

    super.render(delta)

    batch.begin()
    writeMessage()
    batch.end()
  }

  private def writeMessage() {
    textFont.draw(batch, "Help", camera.viewportWidth / 2f, camera.viewportHeight / 2f)
  }

  private def handleInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      game.setScreen(new MainMenuScreen(game))
    }
  }

  override def dispose() {
    batch.dispose()
    textFont.dispose()
  }

  override def show() {}
  override def hide() {}

  override def resize(width: Int, height: Int) {
    camera.update()
  }

  override def pause() {}
  override def resume() {}
}
