package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer.Task
import com.badlogic.gdx.utils.{Timer, Array}

class Fuse(field: GameField, val player: Player)
  extends GameFieldObject(field) {

  val LOG = classOf[Fuse].getSimpleName

  sealed abstract class FUSE_STATE
  case object SLEEPING extends FUSE_STATE
  case object WAITING extends FUSE_STATE
  case object CHASING extends FUSE_STATE

  override val size: Float = super.size * 0.5f
  var state: FUSE_STATE = _
  var currentPath: Array[Vector2] = _
  var timer: Timer = _

  val delayTime = 250

  init()

  def init() {
    state = SLEEPING
    timer = new Timer
    currentPath = new Array[Vector2]()
    velocity.setZero()
  }

  def start() {
    state = WAITING
    velocity.set(player.slowVelocity)
    position.set(player.path.first())
    currentPath.add(player.path.first())

    Gdx.app.log(LOG, "started")
  }

  def continue() {
    if (timer.isEmpty) {
      // a fuse waits for a moment before continuing
      timer.postTask(new Task {
        override def run() { state = CHASING }
      })
      timer.delay(delayTime)
      timer.start()
    }
  }

  def pause() {
    state = WAITING
  }

  def finish() {
    state = SLEEPING
    velocity.setZero()
    currentPath.clear()

    Gdx.app.log(LOG, "finished")
  }

  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    // always draw path, fuse is visible while moving only
    if (state == CHASING || state == WAITING)
      drawFusePath(shaper)
    if (state == CHASING)
      drawFuse(shaper)
  }

  private def drawFuse(shaper: ShapeRenderer) {
    shaper.setColor(Color.YELLOW)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
  }

  private def drawFusePath(shaper: ShapeRenderer) {
    shaper.setColor(Color.GRAY)
    if (currentPath != null) {
      for (i <- 0 until currentPath.size - 1) {
        shaper.line(currentPath.get(i), currentPath.get(i + 1))
      }
      shaper.line(currentPath.peek(), position)
    }
    shaper.setColor(Color.WHITE)
  }

  override def update(delta: Float) {
    if (state == CHASING) {
      val nextPoint = if (currentPath.size < player.path.size) {
        player.path.get(currentPath.size).cpy()
      } else {
        player.position.cpy()
      }

      // get the next velocity vector
      val vel = nextPoint.cpy()
        .sub(position)
        .nor()
        .scl(velocity.len() * delta)
      val newPos = position.add(vel)
      if (vel.len2() > nextPoint.cpy().sub(position).len2()) {
        newPos.set(nextPoint.cpy())
        currentPath.add(newPos.cpy())
        Gdx.app.log(LOG, "add new vertex")
      }
      position.set(newPos)
    }
  }
}