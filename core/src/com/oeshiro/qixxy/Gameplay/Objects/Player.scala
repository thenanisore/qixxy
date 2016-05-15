package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class Player(val field: GameField) extends AbstractObject {

  val LOG = classOf[Player].getSimpleName

  // player's states
  sealed abstract class PLAYER_STATE
  case object NOT_DRAWING extends PLAYER_STATE
  sealed abstract class DRAWING extends PLAYER_STATE
  case object DRAWING_SLOW extends DRAWING
  case object DRAWING_FAST extends DRAWING

  // movement's states
  sealed abstract class DIRECTION_STATE
  case object UP extends DIRECTION_STATE
  case object DOWN extends DIRECTION_STATE
  case object RIGHT extends DIRECTION_STATE
  case object LEFT extends DIRECTION_STATE

  // parameters
  val size = 5f
  var state: PLAYER_STATE = _
  var direction: DIRECTION_STATE = _
  var isAlive: Boolean = _
  var isReady: Boolean = _
  var isTurned: Boolean = _
  var isBack: Boolean = _

  private var newPos: Vector2 = _
  private var onBorder: Boolean = _
  private var isNearBorder: Boolean = _
  private var inArea: Boolean = _

  var slowVelocity: Vector2 = _
  var currentVelocity: Vector2 = _

  var path: Array[Vector2] = _

  init()

  def init() {
    terminalVelocity.set(200, 200)
    slowVelocity = new Vector2(100, 100)
    currentVelocity = new Vector2(terminalVelocity)

    state = NOT_DRAWING
    direction = RIGHT
    isAlive = true
    isReady = false
    isTurned = false
    isBack = false
    position.set(field.areaVertices.get(0).x,
      (field.areaVertices.get(0).y + field.areaVertices.get(1).y)/ 2f )

    path = new Array[Vector2]()
  }

  private def changeDirection(dir: DIRECTION_STATE) {
    if (dir != direction) {
      if (state.isInstanceOf[DRAWING]) {
        direction match {
          case UP =>
            isTurned = dir == LEFT || dir == RIGHT
            isBack = dir == DOWN
          case DOWN =>
            isTurned = dir == LEFT || dir == RIGHT
            isBack = dir == UP
          case LEFT =>
            isTurned = dir == UP || dir == DOWN
            isBack = dir == RIGHT
          case RIGHT =>
            isTurned = dir == UP || dir == DOWN
            isBack = dir == LEFT
        }
        if (isBack) {
          stop()
          isBack = false
          return
        }
        if (isTurned) Gdx.app.log(LOG, s"turned from $direction to $dir")
      }
      direction = dir
    }
  }

  def moveLeft() {
    velocity.set(-currentVelocity.x, 0)
    changeDirection(LEFT)
  }

  def moveRight() {
    velocity.set(currentVelocity.x, 0)
    changeDirection(RIGHT)
  }

  def moveUp() {
    velocity.set(0, currentVelocity.y)
    changeDirection(UP)
  }

  def moveDown() {
    velocity.set(0, -currentVelocity.y)
    changeDirection(DOWN)
  }

  def stop() {
    velocity.set(0, 0)
  }

  def setSlowVelocity() {
    if (currentVelocity != slowVelocity)
      currentVelocity.set(slowVelocity)
  }

  def setNormalVelocity() {
    if (currentVelocity != terminalVelocity)
      currentVelocity.set(terminalVelocity)
  }

  private def resetReady() {
    if (isReady)
      isReady = false
  }

  private def startDrawing(delta: Float) {
    state = DRAWING_FAST
    path.add(field.getExactPoint(position))
    Gdx.app.log(LOG, "state changed to DRAWING")
    Gdx.app.log(LOG, s"started at ${path.first()}")
  }

  private def continueDrawing(delta: Float) {
    if (isTurned) {
      path.add(position.cpy())
      isTurned = false
    }
  }

  private def finishDrawing(delta: Float) {
    state = NOT_DRAWING
    path.add(field.getExactPoint(position))
    Gdx.app.log(LOG, s"finished at ${path.peek()}")
    isReady = false
    updatePosition(path.peek())
    field.processPath(path)
    path.clear()
    Gdx.app.log(LOG, "state changed to NOT_DRAWING")
    Gdx.app.log(LOG, s"now areas: ${field.claimedAreas.size}")
  }

  private def drawPlayer(shaper: ShapeRenderer) {
    shaper.setColor(Color.RED)
    shaper.circle(position.x, position.y, size)
    shaper.setColor(Color.WHITE)
  }

  private def drawPath(shaper: ShapeRenderer) {
    if (path != null) {
      for (i <- 0 until path.size - 1) {
        shaper.line(path.get(i), path.get(i + 1))
      }
      shaper.line(path.peek(), position)
    }
  }

  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    drawPlayer(shaper)
    if (state.isInstanceOf[DRAWING]) {
      drawPath(shaper)
    }
  }

  override def update(delta: Float) {
    // calculate new parameters and position
    super.updateMotion(delta)
    newPos = getNewPosition(delta)
    onBorder = isOnAreaBorder(field.areaVertices, newPos, 0.01f)
    isNearBorder = isOnAreaBorder(field.areaVertices, newPos, size)
    inArea = isInArea(field.area, newPos)

    state match {
      case NOT_DRAWING =>
        if (onBorder) {
          updatePosition(delta)
          resetReady()
        } else {
          if (isReady && inArea) {
            updatePosition(delta)
            startDrawing(delta)
          } else {
            updatePosition(field.getExactPoint(newPos))
            resetReady()
          }
        }

      case _: DRAWING =>
        // check if try to close a new area
        if (!inArea || isNearBorder) {
          finishDrawing(delta)
        } else {
          continueDrawing(delta)
        }

        // check if cross the current path or is too close
        if (!isOnAreaBorder(path, newPos, size * 0.75f)(path.size - 2)
            && !isCrossPath(path, newPos)(path.size - 2)) {
          updatePosition(delta)
        }
    }
  }
}