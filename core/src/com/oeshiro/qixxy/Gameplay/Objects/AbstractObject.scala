package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math._
import com.badlogic.gdx.utils.Array

abstract class AbstractObject {

  var position = new Vector2()
  var dimension = new Vector2(1, 1)
  var origin = new Vector2()
  var scale = new Vector2(1, 1)
  var rotation = 0.0f

  var velocity = new Vector2()
  var terminalVelocity = new Vector2(30, 30)
  var friction = new Vector2(500, 500)

  var acceleration = new Vector2()
  var bounds = new Rectangle()

  def updateMotion(delta: Float) {
    updateMotionX(delta)
    updateMotionY(delta)
  }

  def getNewPosition(delta: Float): Vector2 =
    new Vector2(
      position.x + velocity.x * delta,
      position.y + velocity.y * delta
    )

  def updatePosition(delta: Float) {
    position.x += velocity.x * delta
    position.y += velocity.y * delta
  }

  def updatePosition(newPos: Vector2) {
    position.set(newPos)
  }

  def update(delta: Float) {
    updateMotion(delta)
    updatePosition(delta)
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer): Unit

  def isOnAreaBorder(vertices: Array[Vector2], pos: Vector2, e: Float): Boolean = {
    val x, y = new Vector2()
    for (i <- 0 until vertices.size - 1) {
      x.set(vertices.get(i).x, vertices.get(i).y)
      y.set(vertices.get(i + 1).x, vertices.get(i + 1).y)
      val distance = Intersector.distanceSegmentPoint(x, y, pos)
      if (distance <= e) {
        return true
      }
    }
    false
  }

  def isInArea(area: Polygon, pos: Vector2): Boolean =
    area.contains(pos)

  protected def updateMotionX(delta: Float) {
    if (velocity.x != 0) {
      // apply friction
      if (velocity.x > 0) {
        velocity.x = Math.max(velocity.x - friction.x * delta, 0)
      } else {
        velocity.x = Math.min(velocity.x + friction.x * delta, 0)
      }
    }
    // apply acceleration
    velocity.x += acceleration.x * delta
    velocity.x = MathUtils.clamp(velocity.x,
      -terminalVelocity.x, terminalVelocity.x)
  }

  protected def updateMotionY(delta: Float) {
    if (velocity.y != 0) {
      // apply friction
      if (velocity.y > 0) {
        velocity.y = Math.max(velocity.y - friction.y * delta, 0)
      } else {
        velocity.y = Math.min(velocity.y + friction.y * delta, 0)
      }
    }
    // apply acceleration
    velocity.y += acceleration.y * delta
    velocity.y = MathUtils.clamp(velocity.y,
      -terminalVelocity.y, terminalVelocity.y)
  }
}
