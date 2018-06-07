/*
 * Copyright © 2016 Oleg Ivanov
 * Licensed under the MIT license.
 */

package com.oeshiro.qixxy.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.oeshiro.qixxy.Qixxy;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new Qixxy(), config);
	}
}
