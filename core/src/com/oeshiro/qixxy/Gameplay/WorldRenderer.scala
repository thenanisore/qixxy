package com.oeshiro.qixxy.Gameplay

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Disposable
import com.oeshiro.qixxy.Utils

class WorldRenderer(private val wController: WorldController)
  extends Disposable {

  val LOG = classOf[WorldRenderer].getSimpleName

  private var cameraGUI: OrthographicCamera = _
  private var camera: OrthographicCamera = _
  private var shaper: ShapeRenderer = _
  private var batch: SpriteBatch = _

  init()
  Gdx.app.log(LOG, "started")

  private def init() {
    batch = new SpriteBatch()
    shaper = new ShapeRenderer()
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

    batch.begin()
    wController.field.render(batch, shaper)
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
  }
}
