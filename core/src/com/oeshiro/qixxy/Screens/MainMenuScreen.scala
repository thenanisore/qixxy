package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.graphics.g2d.{GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.{Gdx, Input}
import com.oeshiro.qixxy.{Utils, Qixxy}

import scala.collection.JavaConversions._

class MainMenuScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[MainMenuScreen].getSimpleName

  private val camera = new OrthographicCamera()
  camera.setToOrtho(false, Utils.viewportWidth,
    Utils.viewportHeight)

  private val batch = new SpriteBatch()

  // fonts and font settings
  private val titleFont = Utils.initializeFont(Utils.fontKa1, 96, Color.valueOf("f0e1fe"))
  private val itemFont = Utils.initializeFont(Utils.fontArcade, 30, Color.valueOf("f0e1fe"))

  // layout to calculate item's widths
  private val layout = new GlyphLayout()

  // menu creation
  // (I doubt scene2d is necessary here)
  private val menuItems = new Array[MenuItem]
  menuItems.addAll(
    new MenuItem("New game", NEW_GAME),
    new MenuItem("Leaderboard", LEADERBOARD),
    new MenuItem("Settings", SETTINGS),
    new MenuItem("Profiles", PROFILES),
    new MenuItem("Help", HELP),
    new MenuItem("Exit", EXIT)
  )
  private var selected = 0

  private def drawTitle() {
    val title = "q i x x y"
    layout.setText(titleFont, title)
    val x = (camera.viewportWidth - layout.width) / 2f
    val y = 450
    titleFont.draw(batch, title, x, y)
  }

  override def render(delta: Float) {
    handleInput()
    camera.update()
    batch.setProjectionMatrix(camera.combined)

    super.render(delta)

    batch.begin()
    drawTitle()
    drawMenu()
    batch.end()
  }

  private def drawMenu() {
    var x = camera.viewportWidth / 2f
    var y = 280f
    var yDel = 0f

    menuItems foreach { item =>
      // centralize
      layout.setText(itemFont, item.label)
      x = camera.viewportWidth / 2f - layout.width / 2f
      yDel = layout.height * 1.3f

      // "exit" is a bit lower
      if (item.rawLabel == "Exit") y -= yDel

      itemFont.draw(batch, item.label, x, y)
      y -= yDel
    }
  }

  // changes screen depending on a selected item
  private def selectItem() {
    menuItems.get(selected).role match {
      case NEW_GAME =>
        game.setScreen(new GameScreen(game))
      case LEADERBOARD => ???
      case SETTINGS => ???
      case PROFILES => ???
      case HELP =>
        game.setScreen(new HelpScreen(game))
      case EXIT =>
        Gdx.app.exit()
    }
  }

  private def handleInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      selected =
        if (selected == 0) menuItems.size - 1
        else selected - 1
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      selected =
        if (selected == menuItems.size - 1) 0
        else selected + 1
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      selectItem()
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      Gdx.app.exit()
    }
  }

  override def dispose() {
    batch.dispose()
    titleFont.dispose()
    itemFont.dispose()
  }

  override def show() {}
  override def hide() {}

  override def resize(width: Int, height: Int) {
    camera.update()
  }

  override def pause() {}
  override def resume() {}

  // enumeration of menu items
  sealed abstract class Role
  case object NEW_GAME extends Role
  case object SETTINGS extends Role
  case object LEADERBOARD extends Role
  case object PROFILES extends Role
  case object HELP extends Role
  case object EXIT extends Role

  class MenuItem(private val _label: String, val role: Role) {
    def isSelected: Boolean = menuItems.get(selected).eq(this)
    def label: String = if (isSelected) "> " + _label + " <"
                        else _label
    val rawLabel = _label
  }
}
