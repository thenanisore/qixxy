package com.oeshiro.qixxy.Screens

import com.badlogic.gdx.graphics.g2d.{GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera, Texture}
import com.badlogic.gdx.{Gdx, Input, Preferences}
import com.oeshiro.qixxy.{Qixxy, Utils}

/**
  * The OptionsScreen class contains the assets and logic of the options screen.
  *
  * @param game - a reference to the game class.
  */
class OptionsScreen(private val game: Qixxy)
  extends AbstractGameScreen(game) {

  private val LOG = classOf[HelpScreen].getSimpleName

  val camera = new OrthographicCamera()
  camera.setToOrtho(false, Utils.viewportWidth,
    Utils.viewportHeight)

  var batch: SpriteBatch = _
  var options1: Texture = _

  // fonts and font settings
  val textFont = Utils.initializeFont(Utils.fontArcade, 28, Color.valueOf("f0e1fe"))
  var selectedOption: Int = _

  // layout to calculate item's widths
  val layout = new GlyphLayout()

  // preferences
  var preferences: Preferences = _
  var optionList: List[optionItem] = _

  init()

  def init() {
    batch = new SpriteBatch()
    options1 = new Texture(Gdx.files.internal("raw/options_1.png"))
    preferences = Gdx.app.getPreferences("options")
    optionList = List[optionItem](
      new StringItem("difficulty", List("easy", "normal", "hard"))
    )
    selectedOption = 0
  }

  override def render(delta: Float) {
    handleInput()
    camera.update()
    batch.setProjectionMatrix(camera.combined)

    super.render(delta)
    val x = camera.viewportWidth * 0.75f
    var y = 350f
    var yDel = 50f

    batch.begin()
    batch.draw(options1, 0, 0)
    optionList foreach { item =>
      if (item.isSelected) {
        layout.setText(textFont, s"< ${item.getChoice.toString} >")
        textFont.setColor(Color.SKY)
      } else {
        layout.setText(textFont, item.getChoice.toString)
      }
      textFont.draw(batch, layout, x - layout.width * 0.5f, y)
      textFont.setColor(Color.valueOf("f0e1fe"))
      y -= yDel
    }
    batch.end()
  }

  private def handleInput() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      game.setScreen(new MainMenuScreen(game))
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      optionList foreach (_.applyPreference())
      preferences.flush()
      game.setScreen(new MainMenuScreen(game))
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
      selectedOption = if (selectedOption == 0) optionList.length - 1
      else selectedOption - 1
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
      selectedOption = if (selectedOption == optionList.size - 1) 0
      else selectedOption + 1
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
      optionList(selectedOption).nextChoice()
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
      optionList(selectedOption).prevChoice()
    }
  }

  override def dispose() {
    batch.dispose()
    options1.dispose()
    textFont.dispose()
  }

  trait optionItem {
    val preferenceName: String
    val choices: List[Any]
    protected var i: Int

    def nextChoice() {
      i = if (i == choices.length - 1) 0 else i + 1
    }

    def prevChoice() {
      i = if (i == 0) choices.length - 1 else i - 1
    }

    def getChoice = choices(i)

    def isSelected = optionList(selectedOption).eq(this)

    def applyPreference(): Unit
  }

  class IntItem(name: String, ch: List[Int])
    extends optionItem {
    override val preferenceName = name
    override val choices = ch
    override var i = if (preferences.contains(preferenceName))
      choices.indexOf(preferences.getInteger(preferenceName))
    else 0

    def applyPreference() {
      preferences.putInteger(preferenceName, choices(i))
    }
  }

  class StringItem(name: String, ch: List[String])
    extends optionItem {
    override val preferenceName = name
    override val choices = ch
    override var i = if (preferences.contains(preferenceName))
      choices.indexOf(preferences.getString(preferenceName))
    else 0

    def applyPreference() {
      preferences.putString(preferenceName, choices(i))
    }
  }

  class BooleanItem(name: String, ch: List[Boolean])
    extends optionItem {
    override val preferenceName = name
    override val choices = ch
    override var i = if (preferences.contains(preferenceName))
      choices.indexOf(preferences.getBoolean(preferenceName))
    else 0

    def applyPreference() {
      preferences.putBoolean(preferenceName, choices(i))
    }
  }

  override def show() {}
  override def hide() {}

  override def resize(width: Int, height: Int) {
    camera.update()
  }

  override def pause() {}
  override def resume() {}
}
