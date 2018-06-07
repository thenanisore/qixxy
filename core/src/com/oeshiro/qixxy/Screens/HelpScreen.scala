/*
 * Copyright © 2016 Oleg Ivanov
 * Licensed under the MIT license.
 */

package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, OrthographicCamera, Texture}
import com.badlogic.gdx.{Gdx, Input}
import com.oeshiro.qixxy.{Qixxy, Utils}

/**
  * The HelpScreen class contains the assets and logic of the help screen.
  *
  * @param game - a reference to the game class.
  */
class HelpScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[HelpScreen].getSimpleName

  val camera = new OrthographicCamera()
  camera.setToOrtho(false, Utils.viewportWidth,
    Utils.viewportHeight)

  var batch: SpriteBatch = _
  var help1: Texture = _
  var help2: Texture = _

  // fonts and font settings
  val textFont = Utils.initializeFont(Utils.fontArcade, 30, Color.valueOf("f0e1fe"))
  var selectedPage: Int = _

  init()

  def init() {
    batch = new SpriteBatch()
    help1 = new Texture(Gdx.files.internal("raw/help_1.png"))
    help2 = new Texture(Gdx.files.internal("raw/help_2.png"))
    selectedPage = 1
  }

  override def render(delta: Float) {
    handleInput()
    camera.update()
    batch.setProjectionMatrix(camera.combined)

    super.render(delta)

    batch.begin()
    if (selectedPage == 1) batch.draw(help1, 0, 0)
    else if (selectedPage == 2) batch.draw(help2, 0, 0)
    batch.end()
  }

  private def handleInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      game.setScreen(new MainMenuScreen(game))
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
      selectedPage = 2
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
      selectedPage = 1
    }
  }

  override def dispose() {
    batch.dispose()
    help1.dispose()
    help2.dispose()
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
