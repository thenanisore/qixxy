package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap.Blending
import com.badlogic.gdx.graphics.{Texture, Color, Pixmap}
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math._
import com.badlogic.gdx.utils.Array
import com.oeshiro.qixxy.Utils

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

class GameField {

  val LOG = classOf[GameField].getSimpleName

  var player: Player = _
  var qix: Qix = _
  var sparxes: Array[Sparx] = _

  val MARGIN = 10f
  val UI_MARGIN = Utils.viewportWidth * 0.25f + 10f

  val borders = new Rectangle(MARGIN, MARGIN,
    Utils.viewportWidth - UI_MARGIN,
    Utils.viewportHeight - 2f * MARGIN)

  var areaVertices: Array[Vector2] = _
  var area: Polygon = _
  var claimedAreas: Array[Array[Vector2]] = _

  init()

  def init() {
    player = new Player(this)
    qix = new Qix()
    sparxes = new Array[Sparx]()

    claimedAreas = new Array[Array[Vector2]]()
    areaVertices = new Array[Vector2]()
    areaVertices.addAll(
      new Vector2(borders.getX, borders.getY),
      new Vector2(borders.getX + borders.width, borders.getY),
      new Vector2(borders.getX + borders.width, borders.getY + borders.height),
      new Vector2(borders.getX, borders.getY + borders.height),
      new Vector2(borders.getX, borders.getY)
    )

    area = getPolygonFromVertices(areaVertices)

    Gdx.app.log(LOG, "level loaded")
  }

  private def getPolygonFromVertices(vertices: Array[Vector2]): Polygon =
    new Polygon(vertices.flatMap(p => ArrayBuffer(p.x, p.y)).toArray)

  private def getFloatArray(arr: Array[Vector2]): scala.Array[Float] =
    arr.flatMap(p => List(p.x, p.y)).toArray

  def update(delta: Float) {
    player.update(delta)
    qix.update(delta)
    sparxes foreach (_.update(delta))
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    renderBackground(shaper)
    player.render(batch, shaper)
    qix.render(batch, shaper)
    sparxes foreach (_.render(batch, shaper))
  }

  def renderBackground(shaper: ShapeRenderer) {
    shaper.setAutoShapeType(true)
    shaper.rect(borders.getX, borders.getY,
      borders.getWidth, borders.getHeight)

    claimedAreas foreach (area => {
      renderArea(getFloatArray(area), shaper)
    })
  }

  private val triangulator = new EarClippingTriangulator()
  private val polygonSpriteBatch = new PolygonSpriteBatch()

  def renderArea(polygon: scala.Array[Float], shaper: ShapeRenderer) {
    val pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888)
    pix.setColor(Color.ORANGE)
    pix.fill()
    pix.setColor(Color.WHITE)
    val textureSolid = new Texture(pix)
    val polyReg = new PolygonRegion(new TextureRegion(textureSolid),
      polygon.toSeq.toArray,
      triangulator.computeTriangles(polygon).toArray
    )
    val poly = new PolygonSprite(polyReg)
    polygonSpriteBatch.begin()
    poly.draw(polygonSpriteBatch)
    polygonSpriteBatch.end()
    shaper.polygon(polygon)
  }

  def processPath(path: Array[Vector2]) {
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
    // TODO REWRITE
    val a1 = getPolygonFromVertices(p_first).area()
    val a2 = getPolygonFromVertices(p_second).area()
    val minArea = if (a1 < a2) p_first else p_second
    val maxArea = if (a1 > a2) p_first else p_second
    claimedAreas.add(minArea)
    areaVertices = maxArea
    area = getPolygonFromVertices(areaVertices)

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
      mins.add(Utils.viewportWidth)
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

    val result = new Vector2()
    if (areaVertices.get(imin).x == areaVertices.get(imin + 1).x) {
      result.set(areaVertices.get(imin).x, point.y)
    } else if (areaVertices.get(imin).y == areaVertices.get(imin + 1).y) {
      result.set(point.x, areaVertices.get(imin).y)
    }

    result
  }
}
