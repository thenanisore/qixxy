package com.oeshiro.qixxy

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter

object Utils {

  // font's paths
  val fontArcade = "fonts/arcade-n.ttf"
  val fontKa1 = "fonts/ka1.ttf"

  // global constants
  val viewportWidth = 800f
  val viewportHeight = 480f
  val viewportGuiWidth = 800f
  val viewportGuiHeight = 480f
  val livesStart = 3
  val preferences = "qixxy.prefs"

  // difficulty levels
  sealed abstract class DIFFICULTY

  case object EASY extends DIFFICULTY

  case object NORMAL extends DIFFICULTY

  case object HARD extends DIFFICULTY

  // generates a bitmap font from a ttf
  def initializeFont(path: String, size: Int=16, color: Color=Color.WHITE): BitmapFont = {
    val generator = new FreeTypeFontGenerator(Gdx.files.internal(path))
    val parameter = new FreeTypeFontParameter
    parameter.size = size
    parameter.color = color

    try generator.generateFont(parameter)
    finally generator.dispose()
  }
}
