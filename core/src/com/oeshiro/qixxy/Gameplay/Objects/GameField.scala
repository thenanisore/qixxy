package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Array
import com.oeshiro.qixxy.Utils

import scala.collection.JavaConversions._

class GameField {

  val LOG = classOf[GameField].getSimpleName

  var player = new Player()
  var qix = new Qix()
  var sparxes = new Array[Sparx]()

  val borders = new Rectangle(0, 0,
    Utils.viewportWidth * 0.80f, Utils.viewportHeight)

  Gdx.app.debug(LOG, "Level loaded")

  def update(delta: Float) {
    player.update(delta)
    qix.update(delta)
    sparxes foreach (_.update(delta))
  }

  def render(batch: SpriteBatch, shaper: ShapeRenderer) {
    renderBackground(shaper)
    player.render(batch)
    qix.render(batch)
    sparxes foreach (_.render(batch))
  }

  def renderBackground(shaper: ShapeRenderer) {
    shaper.setAutoShapeType(true)
    shaper.begin()
    shaper.rect(borders.getX, borders.getY, borders.getWidth, borders.getHeight)
    shaper.end()
  }
}
