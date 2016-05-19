package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{ParticleEffect, SpriteBatch}
import com.badlogic.gdx.math.{Circle, Intersector, Vector2}
import com.badlogic.gdx.utils.{Array, Disposable}
import com.oeshiro.qixxy.Utils._

/**
  * A class representing a spark, containing all spark
  * rendering and updating algorithms.
  *
  * @param field - a reference to the game field.
  */
class Spark(field: GameField, var isClockwise: Boolean)
  extends GameFieldObject(field) with Disposable {

  val LOG = classOf[Spark].getSimpleName

  sealed abstract class SPARK_STATE
  case object SLEEPING extends SPARK_STATE
  case object MOVING extends SPARK_STATE

  var sparkParticle = new ParticleEffect()
  override val size: Float = super.size * 0.8f

  // start at the middle-top of the field
  override val startingPosition = new Vector2(
    (field.areaVertices.get(2).x - field.areaVertices.get(3).x) / 2,
    field.areaVertices.get(2).y)

  var state: SPARK_STATE = _
  var currentPath: Array[Vector2] = _
  var i_next: Int = _
  var needUpdate: Boolean = _

  val bounds = new Circle(position, size)
  override def getBounds: Circle = {
    bounds.setPosition(position)
    bounds
  }

  init()

  def init() {
    state = SLEEPING
    position.set(startingPosition)
    sparkParticle.load(Gdx.files.internal("particles/spark.pfx"),
      Gdx.files.internal("particles"))

    currentPath = new Array[Vector2]()
    updatePath()
    i_next = getNextPoint

    needUpdate = false
    // terminal velocity equals to the player's normal one
    val speed = field.player.terminalVelocity.cpy()
    field.dif match {
      case EASY => speed.scl(0.8f)
      case NORMAL => speed.scl(1.2f)
      case HARD => speed.scl(1.5f)
    }
    terminalVelocity.set(speed)
    velocity.set(terminalVelocity)
  }

  def render(batch: SpriteBatch) {
    sparkParticle.draw(batch)
  }

  def start() {
    state = MOVING
  }

  def changeDirection() {
    isClockwise = !isClockwise
    i_next = getNextPoint
  }

  def isTrapped: Boolean = !isInArea(field.area, position) &&
    !isOnAreaBorder(field.areaVertices, position, 0.01f)

  def updatePath() {
    if (!isTrapped) {
      currentPath.clear()
      currentPath.addAll(field.areaVertices)
      i_next = getNextPoint
      needUpdate = false
    }
  }

  def reset(newPos: Vector2) {
    position.set(newPos)
    i_next = getNextPoint
  }

  private def getNextPoint: Int = {
    var index = 0
    (0 until currentPath.size - 1) foreach { i =>
      if (Intersector.distanceSegmentPoint(currentPath.get(i), currentPath.get(i + 1), position) < 0.01)
        index = if (isClockwise) i else i + 1
    }
    index
  }

  private def checkPath() {
    if (!needUpdate && isTrapped)
      needUpdate = true
    else if (needUpdate && !isTrapped)
      updatePath()
  }

  override def update(delta: Float) {
    if (state == MOVING) {
      checkPath()
      val nextPoint = {
        i_next = if (i_next == currentPath.size) 1
        else if (i_next == 0) currentPath.size - 1
        else i_next
        currentPath.get(i_next)
      }

      // get the next velocity vector
      val vel = nextPoint.cpy()
        .sub(position)
        .nor()
        .scl(velocity.len() * delta)
      val newPos = position.cpy().add(vel)
      if (vel.len2() >= nextPoint.cpy().sub(position).len2()) {
        newPos.set(nextPoint.cpy())
        i_next += (if (isClockwise) -1 else 1)
      }
      position.set(newPos)
    }
    sparkParticle.setPosition(position.x - size, position.y)
    sparkParticle.update(delta)
  }

  override def dispose() {
    sparkParticle.dispose()
  }
}
