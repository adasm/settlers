// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.softsquare.settlers.Places.Place;
import com.softsquare.settlers.Places.Resource;
import com.softsquare.settlers.Places.Warehouse;

public class Level extends InputManager.InputReceiver  {
	public Camera camera;
	public Vector2 cameraOrigin = new Vector2();
	public boolean cameraChase = true;
	public boolean resetFluid = true;
	public Warehouse warehouse = new Warehouse();
	public ArrayList<Resource> resources = new ArrayList<Resource>();
	public ArrayList<Place> places = null;
	InteriorPoint interiorPoint = null;
	BitmapFont font;
	SpriteBatch batch;
	public static final String FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";
	RayHandler rayHandler;
	PointLight light;
	World world = new World(new Vector2(0,0), false);
	float borderAlpha = 0;

	public Level(Camera camera) {
		this.camera = camera;
		InputManager.add(this);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/font/lucon.ttf"));//Gdx.files.internal("../data/font/lucon.ttf"));
		font = generator.generateFont(10);
		generator.dispose();
		reset();
		loadCurrentState();
		rayHandler = new RayHandler(world);
		rayHandler.setBlur(true);
		rayHandler.setBlurNum(16);
		rayHandler.setAmbientLight(1, 1, 1, 0.1f);
		RayHandler.setColorPrecisionHighp();
		RayHandler.setGammaCorrection(true);
		light = new PointLight(rayHandler, 64);
		light.setPosition(0, 0);
		light.setDistance(10000);
		light.setColor(1, 1, 1, 0.7f);
	}
	
	public double zoomEditor = 1.2;
	public double zoomGame = 1.2;
	
	public void adjustZoom() {
		if(Globals.editorMode) {
			zoomGame = Globals.zoom_max.get();
			Globals.zoom_max.set(zoomEditor);
			cameraChase = false;
		} else {
			zoomEditor = Globals.zoom_max.get();
			Globals.zoom_max.set(zoomGame);
		}
		
	}
	
	void computeWarehouseOrigin() {
		if(resources.size() > 0) {
			float x0 = 0;
			float y0 = 0;
			for(Resource r : resources) { x0 += r.x; y0 += r.y; }
			x0 /= (float)resources.size();
			y0 /= (float)resources.size();
			warehouse.x = x0;
			warehouse.y = y0;
		} else {
			warehouse.x = Globals.warehouseStartX.get().floatValue();
			warehouse.y = Globals.warehouseStartY.get().floatValue();
		}
	}
	
	public void compute(boolean force) {
		if(force) {
			if(resources.size() > 0) {
				computeWarehouseOrigin();
				interiorPoint = new InteriorPoint(warehouse, resources);
				interiorPoint.recompute();
				warehouse.x = (float) interiorPoint.x0;
				warehouse.y = (float) interiorPoint.y0;
			}
		} else if(resources.size() > 0 && interiorPoint == null) {
			computeWarehouseOrigin();
			interiorPoint = new InteriorPoint(warehouse, resources);
			interiorPoint.recompute();
			warehouse.x = (float) interiorPoint.x0;
			warehouse.y = (float) interiorPoint.y0;
		}
	}

	public void update(float deltaTime) {
		if(!Globals.editorMode) {
			selectedPlace = null;
			litPlace = null;
			if(Globals.autoCompute.get())
				compute(false);
		}
		else {
			computeWarehouseOrigin();
			if(interiorPoint != null)
				interiorPoint.stop();
			interiorPoint = null;
			places = null;
		}
		if(needCompute) {
			needCompute = false;
			compute(true);
		}
		if(needPause) {
			needPause = false;
			if(interiorPoint != null)
				interiorPoint.pauseResume();
		}
		if(needForceStop) {
			needForceStop = false;
			if(interiorPoint != null && !InteriorPoint.finished && !interiorPoint.paused)
				InteriorPoint.needForceStop = true;
		}
		if(needReset) {
			needReset = false;
			if(Globals.editorMode)
				reset();
			else {
				computeWarehouseOrigin();
				if(interiorPoint != null)
					interiorPoint.stop();
				interiorPoint = null;
				places = null;
			}
		}
		if(needCreate) {
			needCreate = false;
			Resource p = new Resource();
			p.x = newX;
			p.y = newY;
			p.radius = Globals.resourceStartRadius.get().floatValue();
			p.omega = Globals.resourceStartOmega.get().floatValue();
			p.cost = Globals.resourceStartCost.get().floatValue();
			resources.add(p);			
		}
		if(needDelete) {
			needDelete = false;
			if(selectedPlace != null && selectedPlace != warehouse) {
				resources.remove(selectedPlace);
				if(litPlace == selectedPlace)
					litPlace = null;
				selectedPlace = null;
			}
		}
		if(needSave) {
			needSave = false;
			saveCurrentState();
			Globals.game.addMessage("Saved current state");
		}
		if(needLoad) {
			needLoad = false;
			loadCurrentState();
			Globals.game.addMessage("Loaded current state");
		}
		if(cameraChase) {
			cameraOrigin.x = warehouse.x;
			cameraOrigin.y = warehouse.y;
		}
		camera.moveTo(cameraOrigin);
		camera.update(deltaTime, drag);
		if (drag)
			drag();
		if (move)
			move();
		Globals.game.addStats("");
		Globals.game.addStats("(C) Camera chase: " + cameraChase + " " + (cameraChase?"":"(Move RMB)"));
		if(Globals.editorShowInfo.get() && Globals.editorMode) {
			Globals.game.addStats("Warehouse: " + warehouse.x + " " + warehouse.y + " (" + warehouse.radius  + ")");
			Globals.game.addStats("Resources:");
			for(Resource p : resources) {
				Globals.game.addStats(" " + p.x + " " + p.y + " (" + p.radius + ") Omega: " + p.omega + " Cost: " + p.cost);
			}
		}
		if(selectedPlace != null) {
			Globals.game.addStats("");
			Globals.game.addStats("Selected item: " + selectedPlace.getClass().getSimpleName());
			Globals.game.addStats(" Position X = " + selectedPlace.x);
			Globals.game.addStats(" Position Y = " + selectedPlace.y);
			if(selectedPlace instanceof Warehouse) {
				Globals.game.addStats(" Building Radius = " + selectedPlace.radius + " (MouseScroll)");
			} else {
				Globals.game.addStats(" Resource Radius = " + selectedPlace.radius + " (MouseScroll)");
				Globals.game.addStats(" Omega = " + ((Resource)selectedPlace).omega + " (+F5/-7)");
				Globals.game.addStats(" Cost = " + ((Resource)selectedPlace).cost + " (+F6/-8)");
			}
		
		}

		//lit update
		float litCoef = 0.95f;
		if(warehouse == litPlace || warehouse == selectedPlace) warehouse.litAlpha = warehouse.litAlpha * litCoef + (1 - litCoef);
		else warehouse.litAlpha = warehouse.litAlpha * litCoef;
		for(Place p : resources) {
			if(p == litPlace || p == selectedPlace) p.litAlpha = p.litAlpha * litCoef + (1 - litCoef) * 1;
			else p.litAlpha = p.litAlpha * litCoef + (1 - litCoef) * 0;
		}
		
		//Computing
		Globals.game.addStats("");
		if(interiorPoint != null) {
			if(InteriorPoint.finished)
				Globals.game.addStats("InteriorPoint (Enter - recompute)");
			else
				Globals.game.addStats("InteriorPoint (P - pause)");
			Globals.game.addStats(" interiorPointSteps " + interiorPoint.interiorPointSteps);
			Globals.game.addStats(" newtonSteps " + interiorPoint.newtonSteps);
			Globals.game.addStats(" time " + String.format("%.3fms", interiorPoint.time / 1000000.0f) + (InteriorPoint.forceStop?".":""));
			if(Globals.showDebug.get()) {
				Globals.game.addStats(" trace " + interiorPoint.trackedNum  + "/" +interiorPoint.totalTrackedNum);
				Globals.game.addStats(" trace filter " + (Globals.traceElimination.get()?"on":"off") + " [epsilon " + Globals.traceEpsilon.serialize() + "]");
			}
			if(InteriorPoint.finished) {
				Globals.game.addStats(" avgNewtonSteps " + String.format("%.3f", interiorPoint.avgNewtonSteps));
				Globals.game.addStats(" finalCost " + String.format("%.3f", interiorPoint.finalCost));
			} else {
				Globals.game.addStats(" currentCost " + String.format("%.3f", interiorPoint.finalCost));
			}
			if(InteriorPoint.hadException && InteriorPoint.exception != null) {
				Globals.game.addStats(" ");
				Globals.game.addStats("Exception!!");
				Globals.game.addStats(InteriorPoint.exception.toString());
			}
		} else Globals.game.addStats("InteriorPoint (Enter - compute)");
	}
	
	public void render() {
		light.setColor(LevelScreen.r, LevelScreen.g, LevelScreen.b, 0.8f);
		light.setPosition(warehouse.x, warehouse.y);
		light.setDistance(Globals.warehouseLightCoef.get().floatValue()*warehouse.radius);
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		rayHandler.update();
		rayHandler.setAmbientLight(0, 0, 0, 0);
		rayHandler.setCombinedMatrix(camera.get().combined, camera.get().position.x,
				camera.get().position.y, camera.get().viewportWidth * camera.get().zoom,
				camera.get().viewportHeight * camera.get().zoom);
		rayHandler.render();
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		if(Globals.gridEnabled.get()) {
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 1, Globals.gridAlpha.get().floatValue());
			float w = Globals.gridMax.get().floatValue();
			float step = Globals.gridStep.get().floatValue();
			for(float i = 0; i < 100000.0f; i += step) {
				shapeRenderer.line(-i, -w, -i, w);
				shapeRenderer.line(i, -w, i, w);
				shapeRenderer.line(-w, -i, w, -i);
				shapeRenderer.line(-w, i, w, i);
			}
			shapeRenderer.end();
		}
		
		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.setColor(
				Globals.warehouseColorRed.get().floatValue(), 
				Globals.warehouseColorGreen.get().floatValue(), 
				Globals.warehouseColorBlue.get().floatValue(), 
				Globals.warehouseColorAlpha.get().floatValue());
		shapeRenderer.filledCircle(warehouse.x, warehouse.y, warehouse.radius, Globals.placeCircleStep.get().intValue());
		shapeRenderer.filledCircle(warehouse.x, warehouse.y, 5, Globals.placeCircleStep.get().intValue());
		shapeRenderer.setColor(
				Globals.resourceColorRed.get().floatValue(), 
				Globals.resourceColorGreen.get().floatValue(), 
				Globals.resourceColorBlue.get().floatValue(), 
				Globals.resourceColorAlpha.get().floatValue());
		for(Place p : resources) {
			for(float rad = p.radius; rad > 0; rad -= Globals.resourceRadiusStep.get().floatValue()) {
				shapeRenderer.filledCircle(p.x, p.y, rad, Globals.placeCircleStep.get().intValue());
			}
		}
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.FilledCircle);
		float r = Globals.editorHoverRed.get().floatValue();
		float g = Globals.editorHoverGreen.get().floatValue();
		float b = Globals.editorHoverBlue.get().floatValue();
		float a = Globals.editorHoverAlpha.get().floatValue();
		if(warehouse.litAlpha > 0) {
			shapeRenderer.setColor(r, g, b, a * 0.25f * warehouse.litAlpha);
			shapeRenderer.filledCircle(warehouse.x, warehouse.y, warehouse.radius, Globals.placeCircleStep.get().intValue());
		}
		for(Place p : resources) {
			if(p.litAlpha > 0) {
				shapeRenderer.setColor(r, g, b, a * p.litAlpha);
				shapeRenderer.filledCircle(p.x, p.y, p.radius, Globals.placeCircleStep.get().intValue());
			}
		}
		
		r = Globals.editorSelectedRed.get().floatValue();
		g = Globals.editorSelectedGreen.get().floatValue();
		b = Globals.editorSelectedBlue.get().floatValue();
		a = Globals.editorSelectedAlpha.get().floatValue();
		shapeRenderer.end();
		shapeRenderer.begin(ShapeType.Circle);
		shapeRenderer.setColor(r, g, b, a);
		if(selectedPlace != null)
			shapeRenderer.circle(selectedPlace.x, selectedPlace.y, selectedPlace.radius, Globals.placeCircleStep.get().intValue());
		shapeRenderer.end();
		
		//InteriorPoint
		if(interiorPoint != null) {
			if(Globals.showDebug.get()) {
				shapeRenderer.begin(ShapeType.FilledCircle);
				float traceRadius = Globals.traceRadius.get().floatValue();
				synchronized(interiorPoint.trackPlaces) {
					for(Place p : interiorPoint.trackPlaces) {
						shapeRenderer.setColor(1, 1, 1, 0.5f);
						shapeRenderer.filledCircle(p.x, p.y, traceRadius, 6);
		
					}
				}
				shapeRenderer.end();
				shapeRenderer.begin(ShapeType.Line);
				synchronized(interiorPoint.trackPlaces) {
					for(int i = 0; i < interiorPoint.N; i++) {
						shapeRenderer.setColor(1.0f * i / (float)interiorPoint.N, 1 - 1.0f * i / (float)interiorPoint.N, 1.0f * i / (float)interiorPoint.N, 1.0f);
						for(int t = 0; t < interiorPoint.trackPlaces.size()/interiorPoint.N - 1; t++) {
							Place pa = interiorPoint.trackPlaces.get(i + interiorPoint.N*t);
							Place pb = interiorPoint.trackPlaces.get(i + interiorPoint.N*(t+1));
							shapeRenderer.line(pa.x, pa.y, pb.x, pb.y);
						}
					}
				}
				shapeRenderer.end();
				
				batch = new SpriteBatch();
				batch.begin();
				int y = 25;
				font.draw(batch, " [DEBUG] Computed places:", Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - y); y += 10;
				if(InteriorPoint.finished) {
					synchronized(interiorPoint.finalPlaces) {
						for(Place p : interiorPoint.finalPlaces) {
							font.draw(batch, "  " + Integer.toString(interiorPoint.finalPlaces.indexOf(p)) + " X: " + p.x + " Y: " + p.y, Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - y);
							y += 10;
						}
					}
				} else {
					synchronized(interiorPoint.trackPlaces) {
						int n = 0;
						if(interiorPoint.trackPlaces.size() > interiorPoint.N)
						for(int i = interiorPoint.trackPlaces.size() - interiorPoint.N; i < interiorPoint.trackPlaces.size(); i++) {
							Place p = interiorPoint.trackPlaces.get(i);
							font.draw(batch, "  " + Integer.toString(n++) + " X: " + p.x + " Y: " + p.y, Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - y);
							y += 10;
						}
					}

				}
				batch.end();
			}
			if(Globals.factoryShow.get()) {
				Gdx.gl.glEnable(GL10.GL_BLEND);
				Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				shapeRenderer.begin(ShapeType.FilledCircle);
				shapeRenderer.setColor(Globals.factoryColorRed.get().floatValue(), 
						Globals.factoryColorGreen.get().floatValue(), 
						Globals.factoryColorBlue.get().floatValue(), 
						Globals.factoryColorAlpha.get().floatValue());
				for(Place p : interiorPoint.finalPlaces) {
					shapeRenderer.filledCircle(p.x, p.y, p.radius, Globals.placeCircleStep.get().intValue());
				}
				shapeRenderer.end();
				batch = new SpriteBatch();
				batch.begin();
				for(Place p : interiorPoint.finalPlaces) {
					Vector3 pos = new Vector3(p.x, p.y, 0);
					camera.get().project(pos);
					font.draw(batch, Integer.toString(interiorPoint.finalPlaces.indexOf(p)), pos.x - 2.5f, pos.y + 3);
				}
				batch.end();
			}
		}
		
		//DRAW STATS
		batch = new SpriteBatch();
		batch.begin();		
		batch.setColor(1, 1, 1, 0.5f);
		Vector3 pos = new Vector3(warehouse.x, warehouse.y, 0);
		camera.get().project(pos);
		font.draw(batch, "X:"+(int)warehouse.x+" Y:"+(int)warehouse.y, pos.x, pos.y + 20);
		font.draw(batch, "R:"+warehouse.radius, pos.x, pos.y + 10);
		for(Place p : resources) {
			pos = new Vector3(p.x, p.y, 0);
			camera.get().project(pos);
			font.draw(batch, "X:"+(int)p.x+" Y:"+(int)p.y, pos.x - 50, pos.y + 50);
			font.draw(batch, "R:"+p.radius, pos.x - 50, pos.y + 40);
			font.draw(batch, "O:"+((Resource)p).omega+" C:"+((Resource)p).cost, pos.x - 50, pos.y + 30);
			font.draw(batch, Integer.toString(resources.indexOf(p) + 1), pos.x - 3, pos.y + 5);
		}
		batch.end();
		
		if(interiorPoint != null && !InteriorPoint.finished && !interiorPoint.paused) {
			borderAlpha += Gdx.graphics.getDeltaTime() * 0.5f;
			if(borderAlpha > 1) borderAlpha = 1;
		} else {
			borderAlpha -= Gdx.graphics.getDeltaTime() * 0.5f;
			if(borderAlpha < 0) borderAlpha = 0;
		}
		
		if(!Globals.editorMode) {
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			ShapeRenderer sr = new ShapeRenderer(); 
			sr.begin(ShapeType.FilledRectangle);
			sr.setColor(0, 0, 0, 0.25f * (1 - borderAlpha));
			sr.filledRect(0, 0, Gdx.graphics.getWidth(), 20);
			sr.filledRect(0, Gdx.graphics.getHeight() - 20, Gdx.graphics.getWidth(), 20);
			sr.setColor(0.55f, 0.11f, 0.11f, 0.65f * borderAlpha);
			sr.filledRect(0, 0, Gdx.graphics.getWidth(), 20);
			sr.filledRect(0, Gdx.graphics.getHeight() - 20, Gdx.graphics.getWidth(), 20);
			sr.end();
		}
		
		Gdx.gl.glDisable(GL10.GL_BLEND);
		
		if(drag)
			Globals.game.markCircle(camera, worldPos.x, worldPos.y, 10);
		
		
	}
	
	public void reset() {
		warehouse.x = Globals.warehouseStartX.get().floatValue();
		warehouse.y = Globals.warehouseStartY.get().floatValue();
		warehouse.radius = Globals.warehouseStartRadius.get().floatValue();
		resources.clear();
		litPlace = null;
		selectedPlace = null;
	}

	public void saveCurrentState() {
		try {
			String stateName = Globals.mapDir.get() + "/" + Globals.mapName.get();
			FileHandle file = Gdx.files.getFileHandle(stateName, FileType.Local);
			PrintWriter saver = new PrintWriter(file.file());
			saver.println(warehouse.x + " " + warehouse.y + " " + warehouse.radius);
			for(Resource p : resources) 
				saver.println(p.x + " " + p.y + " " + p.radius + " " + p.omega + " " + p.cost);
			saver.close();
			Logger.logSuccess("Saved current state");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadCurrentState() {
		reset();
		String stateName = Globals.mapDir.get() + "/" + Globals.mapName.get();
		UniversalParser parser = new UniversalParser(stateName);
		if(parser.isFileLoaded()) {
			String x;
			String y;
			String radius;
			String omega;
			String cost;
			if (!parser.isEOFReached() && !(x = parser.next()).equals("") && !(y = parser.next()).equals("") && !(radius = parser.next()).equals("")) {
				warehouse.x = Float.parseFloat(x);
				warehouse.y = Float.parseFloat(y);
				warehouse.radius = Float.parseFloat(radius);
			}
			while(!parser.isEOFReached() && !(x = parser.next()).equals("") && !(y = parser.next()).equals("") && !(radius = parser.next()).equals("")
					&& !(omega = parser.next()).equals("") && !(cost = parser.next()).equals("")) {
				Resource p = new Resource();
				p.x = Float.parseFloat(x);
				p.y = Float.parseFloat(y);
				p.radius = Float.parseFloat(radius);
				p.omega = Float.parseFloat(omega);
				p.cost = Float.parseFloat(cost);
				resources.add(p);
			}
			computeWarehouseOrigin();
		}
	}

	boolean needCompute = false;
	boolean needPause = false;
	boolean needForceStop = false;
	boolean canPlace = false;
	boolean needReset = false;
	boolean needCreate = false;
	boolean needDelete = false;
	boolean needLoad = false;
	boolean needSave = false;
	float newX, newY;
	
	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.C) {
			cameraChase = !cameraChase;
		}
		if(keycode == Keys.ENTER && !Globals.editorMode) {
			needCompute = true;
		}
		if(keycode == Keys.P && !Globals.editorMode) {
			needPause = true;
		}
		if(keycode == Keys.F && !Globals.editorMode) {
			needForceStop = true;
		}
		if(keycode == Keys.R) {
			needReset = true;
		}
		if(Globals.editorMode) {
			if((keycode == Keys.D || keycode == Keys.DEL) && selectedPlace != null) {
				needDelete = true;
			}
			if(keycode == Keys.SHIFT_LEFT) {
				canPlace = true;
			}
			if(keycode == Keys.L) {
				needLoad = true;
			}
			if(keycode == Keys.S) {
				needSave = true;
			}
			if(selectedPlace != null && selectedPlace instanceof Resource) {
				if(keycode == Keys.F5)
					((Resource)selectedPlace).omega += Globals.resourceOmegaStep.get().floatValue();
				if(keycode == Keys.NUM_7)
					((Resource)selectedPlace).omega -= Globals.resourceOmegaStep.get().floatValue();
				if(keycode == Keys.F6)
					((Resource)selectedPlace).cost += Globals.resourceCostStep.get().floatValue();
				if(keycode == Keys.NUM_8)
					((Resource)selectedPlace).cost -= Globals.resourceCostStep.get().floatValue();
			}
		}
		return false; 
	}
	@Override
	public boolean keyUp(int keycode) { 
		canPlace = false;
		return false; 
	}
	@Override
	public boolean keyTyped(char character)  { return false; }

	Vector3 target = new Vector3(0, 0, 0);
	Vector3 worldPos = new Vector3(0, 0, 0);
	float oldX, oldY;
	int tX, tY;
	boolean drag = false, move = false;
	Place selectedPlace = null;
	Place litPlace = null;
	
	public void drag() {
		target = new Vector3(tX, tY, 0);
		camera.get().unproject(target);
		cameraOrigin.x += -target.x + worldPos.x;
		cameraOrigin.y += -target.y + worldPos.y;
	}
	
	public void move() {
		target = new Vector3(tX, tY, 0);
		camera.get().unproject(target);
		if(selectedPlace != null) {
			selectedPlace.x = target.x - (worldPos.x - oldX);
			selectedPlace.y = target.y - (worldPos.y - oldY);
		}
	}
	
	public float dist(Place p, float x, float y) {
		x = x - p.x; y = y - p.y;
		return (float) Math.sqrt(x*x + y*y);
	}
	
	public Place getNearestPlace(float x, float y) {
		Place place = null;
		float minD = Float.MAX_VALUE;
		float d = dist(warehouse, x, y);
		if(d <= warehouse.radius) {
			place = warehouse;
			minD = d;
		}
		for(Place p : resources) {
			d = dist(p, x, y);
			if(d <= p.radius && d < minD) {
				minD = d;
				place = p;
			}
		}			
		return place;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		tX = screenX;
		tY = screenY;
		worldPos = new Vector3(tX, tY, 0);
		camera.get().unproject(worldPos);
		selectedPlace = null;
		
		if(Globals.editorMode) {
			if(canPlace && button == 0) {
				newX = worldPos.x;
				newY = worldPos.y;
				needCreate = true;
			}
			else if(button == 0) {
				selectedPlace = getNearestPlace(worldPos.x, worldPos.y);
				if(selectedPlace != null) {
					if(selectedPlace != warehouse)
						move = true;
					if(selectedPlace == warehouse)
						cameraChase = false;
					oldX = selectedPlace.x;
					oldY = selectedPlace.y;
				}
			}
			else if (button == 2) {
				selectedPlace = warehouse;
				selectedPlace.x = worldPos.x;
				selectedPlace.y = worldPos.y;
			}
			else if (!cameraChase && button == 1) {
				drag = true;
			}
		}
		else if (!cameraChase && button == 1) {
			drag = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		drag = false;
		move = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		tX = screenX;
		tY = screenY;
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if(Globals.editorMode) {
			tX = screenX;
			tY = screenY;
			worldPos = new Vector3(tX, tY, 0);
			camera.get().unproject(worldPos);
			litPlace = getNearestPlace(worldPos.x, worldPos.y);
		}
		return false; 
	}
	@Override
	public boolean scrolled(int amount) {
		if(selectedPlace != null) {
			selectedPlace.radius += 5 * amount;
			if(selectedPlace.radius < 0) selectedPlace.radius = 0;
		} else {
			Globals.zoom_max.set(Globals.zoom_max.get() - 0.1f * amount);
			if(Globals.zoom_max.get() < Globals.zoom_min.get())
				Globals.zoom_max.set(Globals.zoom_min.get());
		}
		return false;
	}

	@Override
	public boolean wantInput() { return true;  }
}
