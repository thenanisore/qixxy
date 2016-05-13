package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.utils.Disposable
import com.oeshiro.qixxy.Gameplay.Objects.GameField
import com.oeshiro.qixxy.{Utils, Qixxy}

class WorldController(private val game: Qixxy)
  extends InputAdapter with Disposable {

  val LOG = classOf[WorldController].getSimpleName

  var field: GameField = _
  var lives: Float = _

  init()

  def init() {
    field = new GameField()

    lives = Utils.livesStart
  }

  def update(delta: Float) {
    handleInput(delta)

    field.update(delta)

  }

  private def handleInput(delta: Float) {

  }

  override def dispose() {

  }
}
