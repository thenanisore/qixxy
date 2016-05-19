package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.utils.Timer.Task
import com.badlogic.gdx.utils.{Disposable, Timer}
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.oeshiro.qixxy.Gameplay.Objects.GameField
import com.oeshiro.qixxy.Qixxy
import com.oeshiro.qixxy.Screens.MainMenuScreen
import com.oeshiro.qixxy.Utils._

/**
  * Constantly updates the game world parameters
  * and checks for win/lose conditions.
  *
  * @param game - a reference to the game class.
  */
class WorldController(private val game: Qixxy)
  extends InputAdapter with Disposable {

  val LOG = classOf[WorldController].getSimpleName

  val options = Gdx.app.getPreferences("options")
  val dif = options.getString("difficulty", "normal") match {
    case "easy" => EASY
    case "normal" => NORMAL
    case "hard" => HARD
  }
  Gdx.app.log(LOG, options.getString("difficulty", "normal"))

  var field: GameField = _
  var lives: Int = _
  var score: Int = _
  var claimed: Float = _
  var livesVisual: Float = _
  var scoreVisual: Float = _
  val timer = new Timer()

  var isPaused: Boolean = _
  val loseDelay = 3000
  val endDelay = 4000

  def isGameOver: Boolean = lives < 0

  def winPercent = dif match {
    case EASY => 65f
    case NORMAL => 75f
    case HARD => 90f
  }

  def isWin: Boolean = claimed >= winPercent

  init()

  def init() {
    Gdx.input.setInputProcessor(this)
    field = new GameField(this)
    score = 0
    claimed = 0
    lives = livesStart
    isPaused = false

    scoreVisual = score
    livesVisual = lives
  }

  def update(delta: Float) {
    if (!isPaused) {
      handleDebugInput(delta)
      if (!isGameOver && !isWin) {
        handleInput(delta)
        field.update(delta)
      }
    }
  }

  def updateScore(addScore: Int, addPercentage: Float) {
    score += addScore
    claimed += addPercentage
    Gdx.app.log(LOG, s"new score $score")
    Gdx.app.log(LOG, s"claimed $claimed")

    // check win conditions
    if (isWin) win()
  }

  def loseLife() {
    lives -= 1
    isPaused = true
    timer.clear()
    timer.postTask(new Task {
      override def run() {
        // check game over conditions
        if (isGameOver) lose(endDelay)
        field.reset()
        isPaused = false
      }
    })
    timer.delay(loseDelay)
    timer.start()
  }

  def lose(delay: Int) {
    isPaused = true
    Gdx.app.log(LOG, "you lose")
    timer.clear()
    timer.postTask(new Task {
      override def run() {
        backToMenu()
      }
    })
    timer.delay(delay)
    timer.start()
  }

  def win() {
    isPaused = true
    Gdx.app.log(LOG, "you win, congratulations")
    timer.clear()
    timer.postTask(new Task {
      override def run() {
        // check game over conditions
        field.reset()
        backToMenu()
      }
    })
    timer.delay(endDelay)
    timer.start()
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
    if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
      lose(0)
    }

    if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)
      || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)))
    {
      field.player.isReady = true
      field.player.setFastMode()
    } else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
    {
      field.player.isReady = true
      field.player.setSlowMode()
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
