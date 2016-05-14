package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Intersector, Vector2, Polygon, Rectangle}
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

  var areaVertices: ArrayBuffer[(Float, Float)] = _
  var area: Polygon = _

  init()

  def init() {
    player = new Player(this)
    qix = new Qix()
    sparxes = new Array[Sparx]()

    areaVertices = ArrayBuffer(
      (borders.getX, borders.getY),
      (borders.getX + borders.width, borders.getY),
      (borders.getX + borders.width, borders.getY + borders.height),
      (borders.getX, borders.getY + borders.height),
      (borders.getX, borders.getY)
    )

    area = new Polygon(areaVertices
      .flatMap(p => ArrayBuffer(p._1, p._2))
      .toArray)

    Gdx.app.log(LOG, "level loaded")
  }

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
  }

  def processPath(path: Array[Vector2]) {
    val : (Int, Int)
  }

  def findNearestSideIndex(points: Vector2*): Array[Int] = {
    val x, y = new Vector2()
    val mins = new Array[Float]
    val imins = new Array[Int]
    0 until points.size foreach (_ => {
      mins.add(Utils.viewportWidth)
      imins.add(0)
    })
    var distance = 0f

    for (i <- 0 until areaVertices.length - 1) {
      x.set(areaVertices(i)._1, areaVertices(i)._2)
      y.set(areaVertices(i + 1)._1, areaVertices(i + 1)._2)
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
    val imin = findNearestSideIndex(point).first()

    val result = new Vector2()
    if (areaVertices(imin)._1 == areaVertices(imin + 1)._1) {
      result.set(areaVertices(imin)._1, point.y)
    } else if (areaVertices(imin)._2 == areaVertices(imin + 1)._2) {
      result.set(point.x, areaVertices(imin)._2)
    }

    result
  }
}
