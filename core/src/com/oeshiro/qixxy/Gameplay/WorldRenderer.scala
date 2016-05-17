package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.{BitmapFont, GlyphLayout, PolygonSpriteBatch, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.utils.Disposable
import com.oeshiro.qixxy.Utils

class WorldRenderer(private val wController: WorldController)
  extends Disposable {

  val LOG = classOf[WorldRenderer].getSimpleName

  var cameraGUI: OrthographicCamera = _
  var camera: OrthographicCamera = _
  var shaper: ShapeRenderer = _
  var polygonBatch: PolygonSpriteBatch = _
  var batch: SpriteBatch = _
  var guiBatch: SpriteBatch = _

  var itemFont: BitmapFont = _
  val layout = new GlyphLayout()

  init()

  Gdx.app.log(LOG, "started")

  private def init() {
    itemFont = Utils.initializeFont(Utils.fontArcade, 26, Color.valueOf("a8afff"))
    batch = new SpriteBatch()
    guiBatch = new SpriteBatch()
    shaper = new ShapeRenderer()
    polygonBatch = new PolygonSpriteBatch()
    camera = new OrthographicCamera(
      Utils.viewportWidth, Utils.viewportHeight)
    camera.update()
    cameraGUI = new OrthographicCamera()
    camera.setToOrtho(false, Utils.viewportWidth,
      Utils.viewportHeight)
    cameraGUI.update()

    batch.setProjectionMatrix(camera.combined)
    batch.setProjectionMatrix(cameraGUI.combined)
  }

  def render() {
    batch.begin()
    renderWorld()
    batch.end()

    guiBatch.begin()
    renderGui()
    guiBatch.end()
  }

  def renderWorld() {
    camera.update()
    shaper.setAutoShapeType(true)

    shaper.begin()
    polygonBatch.begin()
    wController.field.render(batch, shaper, polygonBatch)
    polygonBatch.end()
    shaper.end()
  }

  def renderGui() {
    cameraGUI.update()

    val score_ui = "score"
    val claimed_ui = "claimed"
    val lives_ui = "lives"
    val x = wController.field.MARGIN +
      wController.field.borders.width +
      wController.field.UI_MARGIN * 0.5f
    var y = 450

    layout.setText(itemFont, score_ui)
    itemFont.draw(guiBatch, score_ui, x - layout.width * 0.5f, y)
    y -= 40

    layout.setText(itemFont, wController.score.toString)
    itemFont.draw(guiBatch, wController.score.toString, x - layout.width * 0.5f, y)
    y -= 80

    layout.setText(itemFont, claimed_ui)
    itemFont.draw(guiBatch, claimed_ui, x - layout.width * 0.5f, y)
    y -= 40

    val claimed = wController.claimed.asInstanceOf[Int].toString + "%"
    layout.setText(itemFont, claimed)
    itemFont.draw(guiBatch, claimed, x - layout.width * 0.5f, y)
    y -= 200

    layout.setText(itemFont, lives_ui)
    itemFont.draw(guiBatch, lives_ui, x - layout.width * 0.5f, y)
    y -= 40

    val lives = " *" * wController.lives + " "
    layout.setText(itemFont, lives)
    itemFont.draw(guiBatch, lives, x - layout.width * 0.5f, y)
    y -= 200
  }

  def resize(width: Float, height: Float) {
    camera.update()
    cameraGUI.update()
  }

  override def dispose() {
    batch.dispose()
    guiBatch.dispose()
    shaper.dispose()
    polygonBatch.dispose()
  }
}
