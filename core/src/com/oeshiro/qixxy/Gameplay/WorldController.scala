package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.badlogic.gdx.utils.Disposable
import com.oeshiro.qixxy.Gameplay.Objects.GameField
import com.oeshiro.qixxy.Screens.MainMenuScreen
import com.oeshiro.qixxy.{Utils, Qixxy}

class WorldController(private val game: Qixxy)
  extends InputAdapter with Disposable {

  val LOG = classOf[WorldController].getSimpleName

  var field: GameField = _
  var lives: Float = _
  var score: Int = _
  var livesVisual: Float = _
  var scoreVisual: Float = _

  def isGameOver: Boolean = lives < 0

  init()

  def init() {
    Gdx.input.setInputProcessor(this)
    field = new GameField()
    score = 0
    scoreVisual = score
    lives = Utils.livesStart
    livesVisual = lives
  }

  def update(delta: Float) {
    handleDebugInput(delta)
    if (!isGameOver) {
      handleInput(delta)
    }

    field.update(delta)
    if (livesVisual > lives)
      livesVisual = Math.max(lives, livesVisual - 1 * delta)
    if (scoreVisual < score)
      scoreVisual = Math.min(score, scoreVisual + 250 * delta)

  }

  private def backToMenu() {
    // switch to menu screen
    game.setScreen(new MainMenuScreen(game))
  }

  private def handleDebugInput(delta: Float) {
    if (Gdx.input.isKeyPressed(Keys.R)) {
      init()
    }
  }

  private def handleInput(delta: Float) {
    if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
          || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)))
    {
      field.player.isReady = true
    }

    // movement keys
    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      field.player.moveLeft()
    } else if (Gdx.input.isKeyPressed(Keys.RIGHT) ) {
      field.player.moveRight()
    } else if (Gdx.input.isKeyPressed(Keys.UP)) {
      field.player.moveUp()
    } else if (Gdx.input.isKeyPressed(Keys.DOWN) ) {
      field.player.moveDown()
    } else {
      field.player.stop()
    }
}

  override def dispose() {
    field.dispose()
  }
}
