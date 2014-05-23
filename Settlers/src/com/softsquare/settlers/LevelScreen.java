// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;

public class LevelScreen implements Screen {
	public Camera camera;
	public Level level;
	private boolean started = false;

	public LevelScreen() { }

	@Override
	public void show() {
		if (!started) {
			camera = new Camera();
			level = new Level(camera);
			started = true;
		}
	}

	public static float r = 0.5f, g = 0.6f, b = 0.4f;
	
	@Override
	public void render(float deltaTime) {
		Globals.game.begin();
		
		if(Globals.editorMode) {
			r = 0.95f*r + 0.05f*Globals.editorRed.get().floatValue();
			g = 0.95f*g + 0.05f*Globals.editorGreen.get().floatValue();
			b = 0.95f*b + 0.05f*Globals.editorBlue.get().floatValue();
		}
		else {
			r = 0.95f*r + 0.05f*Globals.mainRed.get().floatValue();
			g = 0.95f*g + 0.05f*Globals.mainGreen.get().floatValue();
			b = 0.95f*b + 0.05f*Globals.mainBlue.get().floatValue();
		}
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		level.update(deltaTime);
		//System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());
		level.render();
		Globals.game.end();

	}

	@Override
	public void resize(int width, int height) { }
	@Override
	public void hide() { }
	@Override
	public void pause() { }
	@Override
	public void resume() { }
	@Override
	public void dispose() {
		if(SettlersGame.isOnDesktop)
			ConsoleVariables.save("cfg.txt");
	}

}
