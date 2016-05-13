package com.oeshiro.qixxy.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.oeshiro.qixxy.Qixxy;

/*
 * Create a desktop LWJGL application.
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.title = "Qixxy";

        // window's sizes
        config.width = 800;
		config.height = 480;

		// whether to use OpenGL ES 2.0
		boolean useOpenGLES2 = false;

		Gdx.app = new LwjglApplication(new Qixxy(), config);
	}
}
