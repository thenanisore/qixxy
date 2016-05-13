package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class Player extends AbstractObject {

  val LOG = classOf[Player].getSimpleName

  // player's states
  sealed abstract class PLAYER_STATE
  case object NOT_DRAWING extends PLAYER_STATE
  sealed abstract class DRAWING extends PLAYER_STATE
  case object DRAWING_SLOW extends DRAWING
  case object DRAWING_FAST extends DRAWING

  // parameters
  val size = 5f

  var state: PLAYER_STATE = _
  var isAlive: Boolean = _
  var isReady: Boolean = _

  var slowVelocity: Vector2 = _
  var currentVelocity: Vector2 = _

  init()

  def init() {
    terminalVelocity.set(100, 100)
    slowVelocity = new Vector2(50, 50)
    currentVelocity = new Vector2(terminalVelocity)

    state = NOT_DRAWING
    isAlive = true
    isReady = false
    position.set(10, 10)
  }

  def moveLeft() {
    velocity.set(-currentVelocity.x, 0)
  }

  def moveRight() {
    velocity.set(currentVelocity.x, 0)
  }

  def moveUp() {
    velocity.set(0, currentVelocity.y)
  }

  def moveDown() {
    velocity.set(0, -currentVelocity.y)
  }

  def setSlowVelocity() {
    if (currentVelocity != slowVelocity)
      currentVelocity.set(slowVelocity)
  }

  def setNormalVelocity() {
    if (currentVelocity != terminalVelocity)
      currentVelocity.set(terminalVelocity)
  }

  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    shaper.setColor(Color.RED)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
  }

  def update(delta: Float, field: GameField) {
    super.updateMotion(delta)
    state match {
      case NOT_DRAWING =>
        val newPos = getNewPosition(delta)
        if (isOnAreaBorder(field.areaVertices, newPos)) {
          super.updatePosition(delta)
          if (isReady) isReady = false
        } else if (isReady && isInArea(field.area, newPos)) {
          state = DRAWING_FAST
          Gdx.app.log(LOG, "state changed to DRAWING")
        }

      case _: DRAWING =>
        if (!isOnAreaBorder(field.areaVertices, getNewPosition(delta)))
          super.updatePosition(delta)
        else {
          state = NOT_DRAWING
          isReady = false
          Gdx.app.log(LOG, "state changed to NOT_DRAWING")
          // TODO draw a new region
        }
    }
  }
}