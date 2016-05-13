package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Polygon, Rectangle}
import com.badlogic.gdx.utils.Array
import com.oeshiro.qixxy.Utils

import scala.collection.JavaConversions._

class GameField {

  val LOG = classOf[GameField].getSimpleName

  var player = new Player()
  var qix = new Qix()
  var sparxes = new Array[Sparx]()

  val MARGIN = 10f
  val UI_MARGIN = Utils.viewportWidth * 0.25f + 10f

  val borders = new Rectangle(MARGIN, MARGIN,
    Utils.viewportWidth - UI_MARGIN,
    Utils.viewportHeight - 2f * MARGIN)

  import scala.collection.mutable.ArrayBuffer
  val areaVertices = ArrayBuffer(
    (MARGIN, MARGIN),
    (Utils.viewportWidth - UI_MARGIN, MARGIN),
    (Utils.viewportWidth - UI_MARGIN, Utils.viewportHeight - MARGIN),
    (MARGIN, Utils.viewportHeight - MARGIN),
    (MARGIN, MARGIN)
  )

  val area = new Polygon(areaVertices
    .flatMap(p => ArrayBuffer(p._1, p._2))
    .toArray
  )

  Gdx.app.log(LOG, "Level loaded")
  Gdx.app.log(LOG, area.area().toString)

  def update(delta: Float) {
    player.update(delta, this)
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
}
