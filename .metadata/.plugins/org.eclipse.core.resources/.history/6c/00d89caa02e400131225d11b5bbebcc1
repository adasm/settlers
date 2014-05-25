// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class SettlersGame extends Game {
	public static boolean isOnDesktop = false;
	public InputManager input = new InputManager();
	public Skin skin;
	public Stage stage;
	public Label stats;
	public Label messageLabel = null;
	public boolean statsEnabled = true;
	public Console console;
	public LevelScreen level;
	public Globals globalInput = new Globals();
	
	public SettlersGame() {
		Globals.game = this;
	}

	@Override
	public void create() {
		if(isOnDesktop) {
			Logger.getInstance();
			ConsoleVariables.load("cfg.txt");
			skin = new Skin();
			skin.addRegions(new TextureAtlas(Gdx.files.internal("data/ui/skin/uiskin.atlas")));
			skin.load(Gdx.files.internal("data/ui/skin/uiskin.json"));
		}
		Gdx.input.setInputProcessor(input);
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		if(isOnDesktop) {
			stage = new Stage(width, height, false);
			stats = new Label("", skin);
			stats.setPosition(0, height - 25);
			stats.setAlignment(Align.left | Align.top);
			stats.getColor().a = 0;
			stage.addActor(stats);			
			messageLabel = new Label("", skin);
			messageLabel.setPosition(0, height/2);
			messageLabel.setSize(width, 20);
			messageLabel.setVisible(false);
			messageLabel.setAlignment(Align.center);
			messageLabel.setColor(1, 1, 1, 0);
			stage.addActor(messageLabel);
			InputManager.setStage(stage);
			console = new Console();
		}		
		level = new LevelScreen();
		setScreen(level);		
		InputManager.add(globalInput);
	}

	public void begin() {
		if(isOnDesktop) {
			if (Globals.editorMode)
				stats.setText("EDITOR_MODE (Press F1 to change)");
			else
				stats.setText("GAME_MODE (Press F1 to change)");
			addStats("Press ~ to enable console");
			if(!Globals.hideGraphicsStats.get()) {
				addStats("scr " + Gdx.graphics.getWidth() + "x"
					+ Gdx.graphics.getHeight() + " " + "fps " 
					+ Gdx.graphics.getFramesPerSecond() + "\n" + "ft " + Gdx.graphics.getDeltaTime());
			}
			
			if (console.isEnabled())
				Gdx.input.setInputProcessor(console);
			else
				Gdx.input.setInputProcessor(input);
		}
	}

	public void addStats(String s) {
		if(isOnDesktop)
			stats.setText(stats.getText() + "\n" + s);
	}
	
	ArrayList<String> messages = new ArrayList<String>();
	public void addMessage(String s) {
		messages.add(s);
	}
	
	float alpha = 1;

	public void end() {
		boolean statsEnabled = isOnDesktop ? !console.isEnabled() && !Globals.hideStats.get() : false;
		float deltaTime = Gdx.graphics.getDeltaTime();
		if(isOnDesktop) {
			if (statsEnabled) {
				stats.setVisible(true);
				if (stats.getColor().a < 0.8)
					stats.getColor().a += deltaTime;
			} else {
				if (stats.getColor().a > 0.1)
					stats.getColor().a -= deltaTime;
				if (stats.getColor().a < 0.1) {
					stats.getColor().a = 0.1f;
				}
			}
			if(messageLabel.getColor().a > 0) {
				messageLabel.getColor().a -= deltaTime * 1;
				if(messageLabel.getColor().a < 0)
					messageLabel.setVisible(false);
			} else if(messages.size() > 0) {
				messageLabel.setVisible(true);
				messageLabel.setText(messages.get(messages.size()-1));
				messageLabel.getColor().a = 1;
				messages.remove(messages.size()-1);
			}
			console.update(deltaTime);
			stage.act(deltaTime);
			stage.draw();
		}
		
		if(alpha > 0) {
			alpha -= deltaTime;
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			ShapeRenderer shapeRenderer = new ShapeRenderer();
			shapeRenderer.begin(ShapeType.FilledRectangle);
			shapeRenderer.setColor(0, 0, 0, alpha);
			shapeRenderer.filledRect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			shapeRenderer.end();
			Gdx.gl.glDisable(GL10.GL_BLEND);
		}
	}

	public void markCircle(Camera camera, float x, float y, float radius) {
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		shapeRenderer.setColor(1, 0.6f, 0.5f, 0.5f);
		shapeRenderer.filledCircle(x, y, radius, 32);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);
	}
	
	@Override
	public void resize(int w, int h) {
		if(stage != null) {
			stage.setViewport(w, h, false);
			stage.getCamera().position.set(w/2, h/2, 0);
			stage.act();
		}
		if(messageLabel != null) {
			messageLabel.setPosition(0, h/2);
			messageLabel.setSize(w, 20);
		}
		if(stats != null)
			stats.setPosition(0, h - 25);
		if(console != null)
			console.resize(w, h);
		if(console != null)
			console.resize(w, h);
	}
	
	@Override
	public void dispose() {
		ConsoleVariables.save("cfg.txt");
	}

}
