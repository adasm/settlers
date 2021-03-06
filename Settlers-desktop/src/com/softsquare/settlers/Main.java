package com.softsquare.settlers;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Settlers (c) 2013 By Adam Michałowski";
		cfg.useGL20 = false;
		cfg.width = 1280;
		cfg.height = 720;
		cfg.vSyncEnabled = true;
		cfg.samples = 4;
		SettlersGame.isOnDesktop = true;
		new LwjglApplication(new SettlersGame(), cfg);
	}
}
