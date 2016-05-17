package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.{ParticleEffect, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Circle, Vector2}
import com.badlogic.gdx.utils.Timer.Task
import com.badlogic.gdx.utils.{Array, Disposable, Timer}

class Fuse(field: GameField, val player: Player)
  extends GameFieldObject(field) with Disposable {

  val LOG = classOf[Fuse].getSimpleName

  sealed abstract class FUSE_STATE
  case object SLEEPING extends FUSE_STATE
  case object WAITING extends FUSE_STATE
  case object CHASING extends FUSE_STATE

  override val size: Float = super.size * 0.5f
  var state: FUSE_STATE = _
  var currentPath: Array[Vector2] = _
  var timer: Timer = _
  var fuseParticle = new ParticleEffect()

  override val startingPosition: Vector2 = player.position
  val delayTime = 250

  val bounds = new Circle(position, size)
  override def getBounds: Circle = {
    bounds.setPosition(position)
    bounds
  }

  init()

  def init() {
    state = SLEEPING
    timer = new Timer
    fuseParticle.load(Gdx.files.internal("particles/spark.pfx"),
      Gdx.files.internal("particles"))
    currentPath = new Array[Vector2]()
    velocity.setZero()
  }

  def start() {
    if (timer.isEmpty) {
      // a fuse waits for a moment before starting
      timer.postTask(new Task {
        override def run() {
          if (player.state.isInstanceOf[player.DRAWING]) {
            velocity.set(player.slowVelocity)
            position.set(player.path.first())
            currentPath.add(player.path.first())
            fuseParticle.reset()
            state = WAITING
            Gdx.app.log(LOG, "started")
          }
        }
      })
      timer.delay(delayTime)
      timer.start()
    }
  }

  def continue() {
    if (state == WAITING) state = CHASING
    fuseParticle.reset()
  }

  def pause() {
    if (state == CHASING) state = WAITING
    fuseParticle.allowCompletion()
  }

  def finish() {
    state = SLEEPING
    fuseParticle.allowCompletion()
    velocity.setZero()
    currentPath.clear()

    Gdx.app.log(LOG, "finished")
  }

  def render(batch: SpriteBatch) {
    if (state == CHASING)
      drawFuse(batch)
  }

  private def drawFuse(batch: SpriteBatch) {
    fuseParticle.draw(batch)
  }

  def renderPath(shaper: ShapeRenderer) {
    shaper.setColor(Color.DARK_GRAY)
    if (currentPath != null) {
      for (i <- 0 until currentPath.size - 1) {
        shaper.rectLine(currentPath.get(i), currentPath.get(i + 1), player.pathWidth)
      }
      shaper.rectLine(currentPath.peek(), position, player.pathWidth)
    }
    shaper.setColor(Color.WHITE)
  }

  override def update(delta: Float) {
    if (state == CHASING) {
      var isNearPlayer = false
      val nextPoint = if (currentPath.size < player.path.size) {
        player.path.get(currentPath.size).cpy()
      } else {
        isNearPlayer = true
        player.position.cpy()
      }

      // get the next velocity vector
      val vel = nextPoint.cpy()
        .sub(position)
        .nor()
        .scl(velocity.len() * delta)
      val newPos = position.cpy().add(vel)
      if (!isNearPlayer && vel.len2() >= nextPoint.cpy().sub(position).len2()) {
        newPos.set(nextPoint.cpy())
        currentPath.add(newPos.cpy())
        Gdx.app.log(LOG, "add new vertex")
      }
      position.set(newPos)
    }
    fuseParticle.setPosition(position.x - 1.2f * size, position.y)
    fuseParticle.update(delta)
  }

  override def dispose() {
    fuseParticle.dispose()
  }
}