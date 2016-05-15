package com.oeshiro.qixxy.Gameplay.Objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class Sparx(field: GameField) extends AbstractObject(field) {
  override def render(batch: SpriteBatch, shaper: ShapeRenderer) {

  }

  override val size: Float = 5
  override val pathMargin: Float = 111
  override val borderMargin: Float = 111
}
