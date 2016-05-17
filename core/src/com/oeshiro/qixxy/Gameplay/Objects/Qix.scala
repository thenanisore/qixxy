package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Intersector, Vector2}

class Qix(field: GameField) extends GameFieldObject(field) {

  val LOG = classOf[Qix].getSimpleName

  sealed abstract class QIX_STATE
  case object SLEEPING extends QIX_STATE
  case object MOVING extends QIX_STATE


  var state: QIX_STATE = _
  var nextPoint: Vector2 = _
  override val size: Float = super.size * 4f
  val speed = 120

  // start near the middle of the field
  val startingPosition = new Vector2(
    (field.areaVertices.get(2).x + field.areaVertices.get(3).x) * 0.5f,
    (field.areaVertices.get(2).y + field.areaVertices.get(1).y) * 0.5f)

  init()

  def init() {
    terminalVelocity.set(speed, speed)
    state = SLEEPING
    nextPoint = new Vector2()
    position.set(startingPosition)
    velocity.set(terminalVelocity)
  }

  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    drawQix(shaper)
    shaper.circle(nextPoint.x, nextPoint.y, 1)
  }

  private def drawQix(shaper: ShapeRenderer) {
    shaper.setColor(Color.BLUE)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
  }

  private def checkCollisionWithBorders(pos: Vector2): Boolean =
    isOnAreaBorder(field.areaVertices, pos, size) || !isInArea(field.area, pos)

  override def update(delta: Float) {
    state match {
      case SLEEPING =>
        while (!isInArea(field.area, nextPoint)
               || isOnAreaBorder(field.areaVertices, nextPoint, size)
               || position.epsilonEquals(nextPoint, size * 0.5f))
          nextPoint.set(field.getRandomPoint())
        state = MOVING

      case MOVING =>
        val vel = nextPoint.cpy()
          .sub(position)
          .nor()
          .scl(velocity.len() * delta)
        val newPos = position.cpy().add(vel)
        if (checkCollisionWithBorders(newPos)) {
          state = SLEEPING
          nextPoint.set(position)
          return
        }
        if (vel.len2() >= nextPoint.cpy().sub(position).len2()) {
          newPos.set(nextPoint.cpy())
          state = SLEEPING
        }
        position.set(newPos)
    }
  }
}