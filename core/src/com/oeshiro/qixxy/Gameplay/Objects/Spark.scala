package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Intersector, Vector2}
import com.badlogic.gdx.utils.Array

class Spark(field: GameField, var isClockwise: Boolean)
  extends GameFieldObject(field) {

  val LOG = classOf[Spark].getSimpleName

  sealed abstract class SPARK_STATE
  case object SLEEPING extends SPARK_STATE
  case object MOVING extends SPARK_STATE

  override val size: Float = super.size * 0.8f

  // start at the middle-top of the field
  override val startingPosition = new Vector2(
    (field.areaVertices.get(2).x - field.areaVertices.get(3).x) / 2,
    field.areaVertices.get(2).y)

  var state: SPARK_STATE = _
  var currentPath: Array[Vector2] = _
  var i_next: Int = _
  var needUpdate: Boolean = _

  init()

  def init() {
    state = SLEEPING
    position.set(startingPosition)

    currentPath = new Array[Vector2]()
    updatePath()
    i_next = getNextPoint

    needUpdate = false
    // terminal velocity equals to the player's normal one
    terminalVelocity.set(field.player.terminalVelocity.cpy().scl(0.75f))
    velocity.set(terminalVelocity)
  }

  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    drawSpark(shaper)
  }

  def start() {
    state = MOVING
  }

  private def drawSpark(shaper: ShapeRenderer) {
    shaper.setColor(Color.YELLOW)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
  }

  def changeDirection() {
    isClockwise = !isClockwise
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
  }
}
