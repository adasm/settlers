// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.util.ArrayList;
import com.badlogic.gdx.InputProcessor;

public class InputManager implements InputProcessor {
	public static abstract class InputReceiver implements InputProcessor {
		public abstract boolean wantInput();
	}
	static private ArrayList<InputReceiver> _listeners = new ArrayList<InputReceiver>();
	static private InputProcessor stage = null;
	
	public static void setStage(InputProcessor stage) {
		InputManager.stage = stage;
	}
	
	public static void add(InputReceiver listener) {
		if(_listeners.contains(listener) == false)
			_listeners.add(listener);
	}
	
	public static void remove(InputReceiver listener) {
		_listeners.remove(listener);
	}	

	@Override
	public boolean keyDown(int keycode) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.keyDown(keycode);
		if(stage != null)
			stage.keyDown(keycode);
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.keyUp(keycode);
		if(stage != null)
			stage.keyUp(keycode);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.keyTyped(character);
		if(stage != null)
			stage.keyTyped(character);
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.touchDown(screenX, screenY, pointer, button);
		if(stage != null)
			stage.touchDown(screenX, screenY, pointer, button);
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.touchUp(screenX, screenY, pointer, button);
		if(stage != null)
			stage.touchUp(screenX, screenY, pointer, button);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.touchDragged(screenX, screenY, pointer);
		if(stage != null)
			stage.touchDragged(screenX, screenY, pointer);
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.mouseMoved(screenX, screenY);
		if(stage != null)
			stage.mouseMoved(screenX, screenY);
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		for(InputReceiver listener : _listeners)
			if(listener.wantInput())
				listener.scrolled(amount);
		if(stage != null)
			stage.scrolled(amount);
		return true;
	}

}
