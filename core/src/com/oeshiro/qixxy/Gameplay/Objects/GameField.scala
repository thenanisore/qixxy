package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.math._
import com.badlogic.gdx.utils.Timer.Task
import com.badlogic.gdx.utils.{Array, Disposable, Timer}
import com.oeshiro.qixxy.Gameplay.WorldController
import com.oeshiro.qixxy.Utils._

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * A class representing the game field, containing
  * other game objects and all the information
  * about itself.
  *
  * @param controller - a reference to the world controller.
  */
class GameField(val controller: WorldController)
  extends Disposable {

  val LOG = classOf[GameField].getSimpleName

  val dif = controller.dif

  var player: Player = _
  var qix: Qix = _
  var sparx: Array[Spark] = _

  val MARGIN = 10f
  val UI_MARGIN = viewportWidth * 0.25f + 10f

  val borders = new Rectangle(MARGIN, MARGIN,
    viewportWidth - UI_MARGIN,
    viewportHeight - 2f * MARGIN)
  val bordersVertices = new Array[Vector2]()
  bordersVertices.addAll(
    new Vector2(borders.getX, borders.getY),
    new Vector2(borders.getX + borders.width, borders.getY),
    new Vector2(borders.getX + borders.width, borders.getY + borders.height),
    new Vector2(borders.getX, borders.getY + borders.height),
    new Vector2(borders.getX, borders.getY)
  )

  var areaVertices: Array[Vector2] = _
  var area: Polygon = _
  var claimedAreas: Array[Array[Vector2]] = _
  var claimedAreaTypes: Array[Boolean] = _

  val triangulator = new EarClippingTriangulator()
  var pix: Pixmap = _
  var textureFast: Texture = _
  var textureSlow: Texture = _
  var timer: Timer = _

  val maxPoints = 10000
  val scoreCoef: Float =  maxPoints / borders.area()
  val sparxDelayTime = 500

  init()

  def init() {
    claimedAreas = new Array[Array[Vector2]]()
    claimedAreaTypes = new Array[Boolean]()
    areaVertices = new Array[Vector2]()
    areaVertices.addAll(bordersVertices)
    area = getPolygonFromVertices(areaVertices)

    initTextures(Color.valueOf("9669D277"), Color.valueOf("007F7E88"))

    player = new Player(this)
    qix = new Qix(this)
    sparx = new Array[Spark]()
    timer = new Timer()
    addSparx()

    Gdx.app.log(LOG, "level loaded")
  }

  private def initTextures(colorFast: Color, colorSlow: Color) {
    pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pix.setColor(colorFast)
    pix.fill()
    textureFast = new Texture(pix)
    pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pix.setColor(colorSlow)
    pix.fill()
    textureSlow = new Texture(pix)
  }

  private def addSparx() {
    // TODO OBJECT POOL
    sparx.addAll(new Spark(this, true), new Spark(this, false))
    timer.postTask(new Task {
      override def run() { sparx foreach (_.start()) }
    })
    timer.delay(sparxDelayTime)
    timer.start()
  }

  private def clearSparx() {
    // TODO OBJECT POOL
    sparx.clear()
  }

  private def getPolygonFromVertices(vertices: Array[Vector2]): Polygon =
    new Polygon(vertices.flatMap(p => ArrayBuffer(p.x, p.y)).toArray)

  private def getFloatArray(arr: Array[Vector2]): scala.Array[Float] =
    arr.flatMap(p => List(p.x, p.y)).toArray

  def update(delta: Float) {
    player.update(delta)
    qix.update(delta)
    sparx foreach (_.update(delta))
    testCollisions()
  }

  private def testCollisions() {
    val collision =
      player.checkFuse() ||
        (player.checkCollision(qix) && player.isDrawing) ||
        player.checkPathCollision(qix) ||
        sparx.exists(player.checkCollision(_))

    if (collision && !controller.isPaused) {
      controller.loseLife()
      Gdx.app.log(LOG, "you lost life")
    }
  }

  def reset() {
    player.reset()
    // put sparx on the furthest vertices from the player
    val resetPos = areaVertices.get(findFurthestVertex(player.position))
    sparx foreach (_.reset(resetPos))
  }

  def renderObjects(batch: SpriteBatch) {
    player.render(batch)
    qix.render(batch)
    sparx foreach (_.render(batch))
    player.render(batch)
  }

  val lineWidth = 3

  def renderBackground(shaper: ShapeRenderer,
                       polygonBatch: PolygonSpriteBatch) {
    // draw the borders
    0 until bordersVertices.size - 1 foreach { i =>
      shaper.rectLine(bordersVertices.get(i), bordersVertices.get(i + 1), lineWidth)
    }

    // draw each claimed polygon
    0 until claimedAreas.size foreach { i =>
      renderArea(claimedAreas.get(i),
        shaper, polygonBatch, claimedAreaTypes.get(i))
    }
  }

  def renderArea(polygon: Array[Vector2],
                 shaper: ShapeRenderer,
                 polygonBatch: PolygonSpriteBatch,
                 isSlowArea: Boolean = false) {
    // draw the filling
    val floatArray = getFloatArray(polygon)
    val polyReg = new PolygonRegion(new TextureRegion(
      if (isSlowArea) textureSlow
                 else textureFast),
      floatArray,
      triangulator.computeTriangles(floatArray).toArray
    )
    val poly = new PolygonSprite(polyReg)
    poly.draw(polygonBatch)

    // draw the outline
    0 until polygon.size - 1 foreach { i =>
      shaper.rectLine(polygon.get(i), polygon.get(i + 1), lineWidth)
    }
  }

  private def calculateAreaScore(area: Array[Vector2], isSlow: Boolean): Int =
    (new Polygon(getFloatArray(area)).area() *
      scoreCoef * (if (isSlow) 2 else 1)).asInstanceOf[Int]

  private def calculateAreaPercentage(area: Array[Vector2]): Float =
    new Polygon(getFloatArray(area)).area() / borders.area() * 100

  private def findNearestVertex(pos: Vector2): Int = {
    var imin = 0
    var dist, min = viewportWidth
    for (i <- 0 until areaVertices.size) {
      dist = areaVertices.get(i).dst2(pos)
      if (dist < min) {
        min = dist
        imin = i
      }
    }
    imin
  }

  private def findFurthestVertex(pos: Vector2): Int = {
    var imax = 0
    var dist, max = 0f
    for (i <- 0 until areaVertices.size) {
      dist = areaVertices.get(i).dst2(pos)
      if (dist > max) {
        max = dist
        imax = i
      }
    }
    imax
  }

  def alignPath(path: Array[Vector2], pos: Vector2) {
    // TODO fix it
    val last = getExactPoint(pos)
    val imin = findNearestVertex(last)
    if (!pos.epsilonEquals(last, 0.01f) && areaVertices.get(imin).dst2(pos) < Math.pow(0.5f * player.size, 2)) {
      val dif_x = Math.abs(areaVertices.get(imin).x - pos.x)
      val dif_y = Math.abs(areaVertices.get(imin).y - pos.y)
      if (path.peek().x == pos.x) {
        last.x = areaVertices.get(imin).x
        path.peek().x = last.x
      } else {
        last.y = areaVertices.get(imin).y
        path.peek().y = last.y
      }
    }
    // align the last segment just in case
    val dif_x = Math.abs(last.x - path.peek().x)
    val dif_y = Math.abs(last.y - path.peek().y)
    if (dif_x != 0 && dif_y != 0) {
      if (dif_x < dif_y) {
        path.peek().x = last.x
      } else {
        path.peek().y = last.y
      }
    }
    path.add(last)
  }

  def processPath(path: Array[Vector2], isSlow: Boolean) {
    val i_s = findNearestSideIndices(path.first(), path.peek())
    val first_i = i_s.get(0)
    val last_i = i_s.get(1)
    val p_first = new Array[Vector2]()
    val p_second = new Array[Vector2]()

    if (first_i != last_i) {
      // first area = f =P> l -> l + 1 => f
      // second area = f => l =P> f
      p_first.addAll(path)
      p_first.addAll(getPath(last_i + 1, first_i))
      p_first.add(path.get(0))

      p_second.addAll(getPath(first_i + 1, last_i))
      path.reverse()
      p_second.addAll(path)
      p_second.add(areaVertices.get(first_i + 1))
    } else {
      if (areaVertices.get(first_i).dst2(path.first())
          < areaVertices.get(first_i).dst2(path.peek())) {
        p_first.addAll(path)
        p_first.addAll(getPath(first_i + 1, first_i))
        p_first.add(path.get(0))
        path.reverse()
        p_second.addAll(path)
        p_second.add(path.get(0))
      } else {
        p_first.addAll(path)
        p_first.add(path.get(0))
        path.reverse()
        p_second.addAll(path)
        p_second.addAll(getPath(first_i + 1, first_i))
        p_second.add(path.get(0))
      }
    }

    // claimed becomes the area not containing Qix
    val claimed = new Array[Vector2]()
    val free = new Array[Vector2]()
    if (Intersector.isPointInPolygon(p_first, qix.position)) {
      free.addAll(p_first)
      claimed.addAll(p_second)
    } else {
      free.addAll(p_second)
      claimed.addAll(p_first)
    }
    claimedAreas.add(claimed)
    claimedAreaTypes.add(isSlow)
    areaVertices = free
    area = getPolygonFromVertices(areaVertices)

    // update score and sparx
    controller.updateScore(
      calculateAreaScore(claimedAreas.peek(), isSlow),
      calculateAreaPercentage(claimedAreas.peek()))
    sparx foreach (_.updatePath())

    Gdx.app.log(LOG, p_first.toString())
    Gdx.app.log(LOG, p_second.toString())
  }

  def getPath(start: Int, end: Int): Array[Vector2] = {
    val path = new Array[Vector2]()
    if (start > end) {
      start until areaVertices.size foreach (i => path.add(areaVertices.get(i)))
      1 to end foreach (i => path.add(areaVertices.get(i)))
    } else {
      start to end foreach (i => path.add(areaVertices.get(i)))
    }
    path
  }

  def findNearestSideIndices(points: Vector2*): Array[Int] = {
    val x, y = new Vector2()
    val mins = new Array[Float]
    val imins = new Array[Int]
    0 until points.size foreach (_ => {
      mins.add(viewportWidth)
      imins.add(0)
    })
    var distance = 0f

    for (i <- 0 until areaVertices.size - 1) {
      x.set(areaVertices.get(i).x, areaVertices.get(i).y)
      y.set(areaVertices.get(i + 1).x, areaVertices.get(i + 1).y)
      for (j <- 0 until mins.size) {
        distance = Intersector.distanceSegmentPoint(x, y, points.get(j))
        if (distance < mins.get(j)) {
          mins.set(j, distance)
          imins.set(j, i)
        }
      }
    }

    imins
  }

  def getExactPoint(point: Vector2): Vector2 = {
    val imin = findNearestSideIndices(point).first()
    val first = areaVertices.get(imin)
    val second = areaVertices.get(imin + 1)

    val result = new Vector2()

    if (first.x == second.x) {
      result.set(first.x, MathUtils.clamp(
        point.y, Math.min(first.y, second.y),
        Math.max(first.y, second.y)))
    } else if (first.y == second.y) {
      result.set(MathUtils.clamp(
        point.x, Math.min(first.x, second.x),
        Math.max(first.x, second.x)), first.y)
    }

    result
  }

  def getRandomPoint: Vector2 = {
    val ran = new Vector2()
    val rect = area.getBoundingRectangle
    while (!area.contains(ran)) {
      ran.set(
        MathUtils.random(rect.x, rect.x + rect.width),
        MathUtils.random(rect.y, rect.y + rect.height)
      )
    }
    ran
  }

  override def dispose() {
    pix.dispose()
    qix.dispose()
    player.dispose()
    sparx foreach (_.dispose())
    textureFast.dispose()
    textureSlow.dispose()
  }
}
