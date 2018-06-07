package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.{Gdx, Input}
import com.oeshiro.qixxy.{Qixxy, Utils}

/**
  * The RecordsScreen class contains the assets and logic of the leaderboard screen.
  *
  * @param game - a reference to the game class.
  * @todo implement
  */
class RecordsScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[RecordsScreen].getSimpleName

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
    textFont.draw(batch, "Leaders here", camera.viewportWidth / 2f, camera.viewportHeight / 2f)
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
