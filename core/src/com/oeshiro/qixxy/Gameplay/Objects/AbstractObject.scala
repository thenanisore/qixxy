package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math._

import scala.collection.mutable.ArrayBuffer

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

  def update(delta: Float) {
    updateMotion(delta)
    updatePosition(delta)
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer): Unit

  def isOnAreaBorder(vertices: ArrayBuffer[(Float, Float)], pos: Vector2): Boolean = {
    var result = true
    val e = 1
    for (i <- 0 until vertices.length - 1) {
      if (Intersector.distanceSegmentPoint(
        new Vector2(vertices(i)._1, vertices(i)._2),
        new Vector2(vertices(i + 1)._1, vertices(i + 1)._2),
        pos) < e
      )
        return true
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
    // Apply acceleration
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
