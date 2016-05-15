package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.{PolygonSpriteBatch, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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

  init()

  Gdx.app.log(LOG, "started")

  private def init() {
    batch = new SpriteBatch()
    shaper = new ShapeRenderer()
    polygonBatch = new PolygonSpriteBatch()
    camera = new OrthographicCamera(
      Utils.viewportGuiWidth, Utils.viewportGuiHeight)
    camera.update()
    cameraGUI = new OrthographicCamera(Utils.viewportWidth,
      Utils.viewportHeight)
    cameraGUI.update()
  }

  def render() {
    renderWorld(batch)
    renderGui(batch)
  }

  def renderWorld(batch: SpriteBatch) {
    camera.update()
    batch.setProjectionMatrix(camera.combined)
    shaper.setAutoShapeType(true)

    batch.begin()
    shaper.begin()
    polygonBatch.begin()
    wController.field.render(batch, shaper, polygonBatch)
    polygonBatch.end()
    shaper.end()
    batch.end()
  }

  def renderGui(batch: SpriteBatch) {
    batch.setProjectionMatrix(cameraGUI.combined)
    batch.begin()
    // TODO: gui rendering
    batch.end()
  }

  def resize(width: Float, height: Float) {
    camera.update()
    cameraGUI.update()
  }

  override def dispose() {
    batch.dispose()
    shaper.dispose()
    polygonBatch.dispose()
  }
}
