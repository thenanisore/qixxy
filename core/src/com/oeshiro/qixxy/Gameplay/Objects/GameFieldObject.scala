package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math._
import com.badlogic.gdx.utils.Array
import com.oeshiro.qixxy.Utils

abstract class GameFieldObject(val field: GameField) {

  def size: Float = 6f

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

  val pathMargin = size
  val borderMargin = size

  def updateMotion(delta: Float) {
    updateMotionX(delta)
    updateMotionY(delta)
  }

  def getNewPosition(delta: Float): Vector2 =
    new Vector2(
      position.x + velocity.x * delta,
      position.y + velocity.y * delta
    )

  def updatePosition(delta: Float, needAlign: Boolean) {
    position.x += velocity.x * delta
    position.y += velocity.y * delta
    if (needAlign) align
  }

  def updatePosition(newPos: Vector2, needAlign: Boolean) {
    position.set(newPos)
    if (needAlign) align
  }

  def update(delta: Float) {
    updateMotion(delta)
    updatePosition(delta, false)
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer): Unit

  def isOnAreaBorder(vertices: Array[Vector2], pos: Vector2, e: Float)
                    (implicit last_i: Int = vertices.size - 1): Boolean = {
    val x, y = new Vector2()
    for (i <- 0 until last_i) {
      x.set(vertices.get(i))
      y.set(vertices.get(i + 1))
      val distance = Intersector.distanceSegmentPoint(x, y, pos)
      if (distance <= e) {
        return true
      }
    }
    false
  }

  def isInArea(area: Polygon, pos: Vector2): Boolean =
    area.contains(pos)

  def isCrossPath(path: Array[Vector2], newPos: Vector2)
                 (implicit last_i: Int = path.size - 1): Boolean = {
    val x, y, inter = new Vector2()
    for (i <- 0 until last_i) {
      x.set(path.get(i))
      y.set(path.get(i + 1))
      if (y != position && Intersector.intersectSegments(x, y, position, newPos, inter)) {
        return true
      }
    }
    false
  }

  protected def isNearVertex: Int = {
    var imin = -1
    var min = Utils.viewportWidth
    for (i <- 0 until field.areaVertices.size - 1) {
      val dist = field.areaVertices.get(i).dst2(position)
      if (dist < min && dist < Math.pow(borderMargin, 2)) {
        imin = i
        min = dist
      }
    }
    imin
  }

  protected def align() {
    if (velocity.x != 0 || velocity.y != 0) return
    val i = isNearVertex
    if (i != -1)
      position.set(field.areaVertices.get(i))
  }

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
