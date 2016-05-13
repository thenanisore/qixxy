package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.{Rectangle, Vector2, MathUtils}

abstract class AbstractObject {

  var position = new Vector2()
  var dimension = new Vector2(1, 1)
  var origin = new Vector2()
  var scale = new Vector2(1, 1)
  var rotation = 0.0f

  var velocity = new Vector2
  var terminalVelocity = new Vector2(1, 1)
  var friction = new Vector2

  var acceleration = new Vector2
  var bounds = new Rectangle

  def update(delta: Float) {
    updateMotionX(delta)
    updateMotionY(delta)

    // move to new position
    position.x += velocity.x * delta
    position.y += velocity.y * delta
  }

  def render(batch: SpriteBatch): Unit

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
