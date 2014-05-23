// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Camera {
	private float camera_mult = 0.5f;
	private Vector3 position;
	private Vector2 target;
	private OrthographicCamera camera;
	
	public Camera() {
		target = new Vector2(Gdx.graphics.getWidth()*0.5f, Gdx.graphics.getHeight()*0.5f);
		position = new Vector3(target.x, target.y,0);
		forceSetup(Globals.zoom_start.get().floatValue());
		Logger.logSuccess("Created camera " + position.toString());
	}
	
	public void forceSetup(float mult) {
		camera_mult = mult;
		float width = Gdx.graphics.getWidth() / camera_mult;
		float height = Gdx.graphics.getHeight() / camera_mult;
		camera = new OrthographicCamera(width, height);
		camera.position.set(position);
		camera.update();
	}
	
	public void moveTo(Vector2 target) {
		this.target = target;
	}
	
	public void setPosition(Vector3 target) {
		position = target;
		camera.position.set(position);
	}
	
	public final Vector3 getPosition() {
		return position;
	}
	
	public void update(float deltaTime, boolean drag) {
		Vector3 offset = new Vector3(target.x - position.x, target.y - position.y, 0);
		
		if(Globals.cameraSmooth.get() && !drag) {
			position.add(offset.mul(Globals.speed.get().floatValue() * deltaTime));
		} else {
			position.set(target.x, target.y, 0);
		}
		
		setPosition(position);
		
		if(drag) {
			float coef = Globals.zoom_drag_coef.get().floatValue() * deltaTime;
			float new_mult = Globals.zoom_min.get().floatValue()*coef + camera_mult*(1 - coef);
			if(Math.abs(camera_mult-new_mult) > 0.0001f)
				forceSetup(new_mult);
		}
		else if(Globals.zoom_dynamic.get()) {
			float mult = (float) Math.max(Globals.zoom_min.get(), Math.min(Globals.zoom_max.get(), 1 / offset.len() ));
			float coef = Globals.zoom_coef.get().floatValue() * deltaTime;
			float new_mult = camera_mult*(1 - coef) + coef*mult;
			if(Math.abs(camera_mult-new_mult) > 0.0001f)
				forceSetup(new_mult);
			
		} else {
			if(Math.abs(camera_mult - Globals.zoom_static.get()) > 0.000001f)
				forceSetup(Globals.zoom_static.get().floatValue());
		}
		camera.update();
		
		if(!Globals.hideCameraStats.get()) {
			Globals.game.addStats("cam offset len " + offset.len());
			Globals.game.addStats("cam mult " + camera_mult);
			Globals.game.addStats("   (" + Globals.zoom_min.get() + " " + Globals.zoom_max.get() + ")");
		}
	}
	
	public OrthographicCamera get() {
		return camera;
	}

	public float getMult() {
		return camera_mult;
	}
}
