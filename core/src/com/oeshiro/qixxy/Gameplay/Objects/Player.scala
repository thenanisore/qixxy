package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.{Sprite, SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Circle, Intersector, Vector2}
import com.badlogic.gdx.utils.{Array, Disposable}
import com.oeshiro.qixxy.Utils._

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

/**
  * A class representing a player, containing all player
  * rendering and updating algorithms.
  *
  * @param field - a reference to the game field.
  */
class Player(field: GameField)
  extends GameFieldObject(field) with Disposable {

  val LOG = classOf[Player].getSimpleName

  var texture: Texture = _
  var textureSlow: Texture = _
  var textureFast: Texture = _
  var s_texture: Sprite = _
  var s_textureSlow: Sprite = _
  var s_textureFast: Sprite = _
  val pixelSize = 50

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
  override val startingPosition = new Vector2((
    field.areaVertices.get(0).x + field.areaVertices.get(1).x) / 2f,
    field.areaVertices.get(0).y)
  val speed = field.dif match {
    case EASY => 150
    case NORMAL => 125
    case HARD => 100
  }

  var state: PLAYER_STATE = _
  var direction: DIRECTION_STATE = _
  var isReady: Boolean = _
  var isSlow: Boolean = _
  var isFast: Boolean = _
  var isTurned: Boolean = _
  var isBack: Boolean = _

  private var newPos: Vector2 = _
  private var onBorder: Boolean = _
  private var isNearBorder: Boolean = _
  private var inArea: Boolean = _

  var slowVelocity: Vector2 = _
  var currentVelocity: Vector2 = _

  var path: Array[Vector2] = _
  var fuse: Fuse = _

  val bounds = new Circle(position, size)
  override def getBounds: Circle = {
    bounds.setPosition(position)
    bounds
  }

  init()

  def init() {
    terminalVelocity.set(speed, speed)
    slowVelocity = new Vector2(terminalVelocity).scl(0.3f)
    currentVelocity = new Vector2(terminalVelocity)
    position.set(startingPosition)
    origin.set(pixelSize * 0.5f, pixelSize * 0.5f)
    rotation = 0f

    initSprites()

    state = NOT_DRAWING
    direction = RIGHT
    isReady = false
    isTurned = false
    isBack = false
    isSlow = false
    isFast = true

    path = new Array[Vector2]()
    fuse = new Fuse(field, this)
  }

  def initSprites() {
    texture = new Texture(Gdx.files.internal("raw/ship.png"))
    textureFast = new Texture(Gdx.files.internal("raw/ship_moving.png"))
    textureSlow = new Texture(Gdx.files.internal("raw/ship_moving_slow.png"))
    s_texture = new Sprite(new TextureRegion(texture))
    s_texture.setOriginCenter()
    s_texture.setScale(0.5f)
    s_textureSlow = new Sprite(new TextureRegion(textureSlow))
    s_textureSlow.setOriginCenter()
    s_textureSlow.setScale(0.5f)
    s_textureFast = new Sprite(new TextureRegion(textureFast))
    s_textureFast.setOriginCenter()
    s_textureFast.setScale(0.5f)
  }

  def setSlowMode() {
    if (state != DRAWING_FAST) {
      isSlow = true
      if (state == DRAWING_SLOW)
        setSlowVelocity()
    }
  }

  def setFastMode() {
    isFast = true
    if (state == DRAWING_SLOW) state = DRAWING_FAST
    setNormalVelocity()
  }

  def isStoppedDrawing: Boolean =
    state.isInstanceOf[DRAWING] && velocity.x == 0 && velocity.y == 0

  def isDrawing: Boolean = state.isInstanceOf[DRAWING]

  private def changeDirection(dir: DIRECTION_STATE) {
    if (dir != direction) {
      if (direction == LEFT) {
        s_texture.flip(true, false)
        s_textureSlow.flip(true, false)
        s_textureFast.flip(true, false)
      }
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
        if (isBack || !isReady) {
          stop()
          isBack = false
          return
        }
        if (isTurned) Gdx.app.log(LOG, s"turned from $direction to $dir")
      }
      direction = dir
      rotate()
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

  private val rotations = HashMap("UP" -> 90, "LEFT" -> 0, "DOWN" -> 270, "RIGHT" -> 0)

  private def rotate() {
    s_texture.setRotation(rotations(direction.toString))
    s_textureSlow.setRotation(rotations(direction.toString))
    s_textureFast.setRotation(rotations(direction.toString))

    if (direction == LEFT) {
      s_texture.flip(true, false)
      s_textureSlow.flip(true, false)
      s_textureFast.flip(true, false)
    }
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

  private def resetSlow() {
    if (isSlow) {
      isSlow = false
      setNormalVelocity()
    }
  }

  private def startDrawing(delta: Float) {
    state = if (isSlow) {
      setSlowVelocity()
      DRAWING_SLOW
    } else {
      setNormalVelocity()
      DRAWING_FAST
    }
    path.add(field.getExactPoint(position))
    fuse.start()

    Gdx.app.log(LOG, "state changed to DRAWING")
    Gdx.app.log(LOG, s"started at ${path.first()}")
  }

  private def continueDrawing(delta: Float) {
    if (isTurned && isReady) {
      path.add(position.cpy())
      Gdx.app.log(LOG, s"add $position")
      isTurned = false
    }
  }

  private def finishDrawing(delta: Float, newPos: Vector2) {
    field.alignPath(path, newPos)
    Gdx.app.log(LOG, s"finished at ${path.peek()}")
    updatePosition(path.peek(), needAlign = true)
    field.processPath(path, state == DRAWING_SLOW)
    state = NOT_DRAWING
    path.clear()
    isReady = false

    fuse.finish()
    resetSlow()
    stop()

    Gdx.app.log(LOG, "state changed to NOT_DRAWING")
    Gdx.app.log(LOG, s"now areas: ${field.claimedAreas.size}")
  }

  private def drawPlayer(batch: SpriteBatch) {
    if (velocity.x == 0 && velocity.y == 0) {
      s_texture.draw(batch)
    } else {
      if (state == DRAWING_SLOW) s_textureSlow.draw(batch)
      else s_textureFast.draw(batch)
    }
  }

  val pathWidth = 2

  def renderPaths(shaper: ShapeRenderer) {
    if (state.isInstanceOf[DRAWING] && path != null) {
      for (i <- 0 until path.size - 1) {
        shaper.rectLine(path.get(i), path.get(i + 1), pathWidth)
      }
      shaper.rectLine(path.peek(), position, pathWidth)
      // always draw path, fuse is visible while moving only
      if (fuse.state != fuse.SLEEPING)
        fuse.renderPath(shaper)
    }
  }

  def render(batch: SpriteBatch) {
    if (state.isInstanceOf[DRAWING]) {
      fuse.render(batch)
    }
    drawPlayer(batch)
  }

  def checkCollision(enemy: GameFieldObject): Boolean =
    getBounds.overlaps(enemy.getBounds)

  def checkFuse(): Boolean =
    state.isInstanceOf[DRAWING] && fuse.position.epsilonEquals(position, 1f)

  def checkPathCollision(enemy: GameFieldObject): Boolean = {
    if (state.isInstanceOf[DRAWING])
      enemy.isOnAreaBorder(path, enemy.position, enemy.size) ||
      (Intersector.distanceSegmentPoint(path.peek(), position, enemy.position) < enemy.size)
    else false
  }

  def reset() {
    if (path.nonEmpty)
      position.set(path.first())
    path.clear()
    state = NOT_DRAWING
    fuse.finish()
    isReady = false
    isTurned = false
    isBack = false
    isSlow = false
    isFast = true
    stop()
    Gdx.app.log(LOG, "reset")
  }

  override def update(delta: Float) {
    // calculate new parameters and position
    newPos = getNewPosition(delta)
    onBorder = isOnAreaBorder(field.areaVertices, newPos, 0.1f)
    isNearBorder = isOnAreaBorder(field.areaVertices, newPos, 0.5f * velocity.len() * delta)
    val nearestVertex = isNearVertex
    val isDisplacementInArea = isInArea(field.area, newPos.cpy().add(position).scl(0.5f))
    inArea = isInArea(field.area, newPos)

    state match {
      case NOT_DRAWING =>
        if ((onBorder || isNearBorder) && isDisplacementInArea) {
          if (isNearBorder)
            newPos = field.getExactPoint(newPos)
          updatePosition(newPos, needAlign = true)
        } else {
          if (isReady && inArea) {
            Gdx.app.log(LOG, s"position old $newPos")
            updatePosition(delta, needAlign = true)
            startDrawing(delta)
          } else {
            updatePosition(field.getExactPoint(newPos), needAlign = true)
          }
        }

      case _: DRAWING =>
        // check if try to close a new area
        if (!isReady) stop()
        if ((!inArea || onBorder
                || (nearestVertex != -1
                    && field.areaVertices.get(nearestVertex) != path.first()))
             && path.first().dst(newPos) > borderMargin) {
          finishDrawing(delta, newPos)
        } else {
          continueDrawing(delta)
        }

        // check if cross the current path or is too close
        if (!isOnAreaBorder(path, newPos, pathMargin)(path.size - 3)
            && !isCrossPath(path, newPos)(path.size - 3)) {
          updatePosition(delta, needAlign = true)
        }

        // update fuse while drawing
        if (isStoppedDrawing) fuse.continue()
        else fuse.pause()
        fuse.update(delta)
    }
    resetReady()
    s_texture.setPosition(position.x - pixelSize, position.y - pixelSize * 0.5f)
    s_textureSlow.setPosition(position.x - pixelSize, position.y - pixelSize * 0.5f)
    s_textureFast.setPosition(position.x - pixelSize, position.y - pixelSize * 0.5f)
  }

  override def dispose() {
    fuse.dispose()
    texture.dispose()
    textureFast.dispose()
    textureSlow.dispose()
  }
}