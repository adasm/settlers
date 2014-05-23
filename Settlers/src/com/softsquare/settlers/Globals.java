// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.softsquare.settlers.ConsoleVariables.VariableBoolean;
import com.softsquare.settlers.ConsoleVariables.VariableDouble;
import com.softsquare.settlers.ConsoleVariables.VariableInt;
import com.softsquare.settlers.ConsoleVariables.VariableString;

public class Globals extends InputManager.InputReceiver {
	//SideGame
	public static SettlersGame 		game = null;
	public static boolean 			editorMode = false;
	public static VariableBoolean 	hideGraphicsStats = new VariableBoolean("main", "hideGraphicsStats", false);
	public static VariableBoolean 	hideCameraStats = new VariableBoolean("main", "hideCameraStats", false);
	public static VariableBoolean 	hideStats = new VariableBoolean("main", "hideStats", false);
	public static VariableDouble 	mainRed = new VariableDouble("main", "red", 0.5, 0, 1);
	public static VariableDouble 	mainGreen = new VariableDouble("main", "green", 0.7, 0, 1);
	public static VariableDouble 	mainBlue = new VariableDouble("main", "blue", 0.3, 0, 1);
	//Map
	public static VariableString 	mapDir = new VariableString("map", "dir", "maps");
	public static VariableString 	mapName = new VariableString("map", "name", "map01.data");
	//Camera
	public static VariableBoolean 	zoom_dynamic = new VariableBoolean("camera", "zoomDynamic", true);
	public static VariableDouble 	zoom_start = new VariableDouble("camera", "zoomStart", 0.001, 0.001, 10.00);
	public static VariableDouble 	zoom_static = new VariableDouble("camera", "zoomStatic", 1, 0.001, 10.00);
	public static VariableDouble 	zoom_min = new VariableDouble("camera", "zoomMin", 0.25, 0.001, 1000.00);
	public static VariableDouble 	zoom_max = new VariableDouble("camera", "zoomMax", 2, 0.001, 1000.00);
	public static VariableDouble 	zoom_coef = new VariableDouble("camera", "zoomCoef", 2, 0.00, 10.00);
	public static VariableDouble 	zoom_drag_coef = new VariableDouble("camera", "zoomDragCoef", 0.25f, 0.00, 10.00);
	public static VariableDouble 	speed = new VariableDouble("camera", "speed", 3.5, 0.00, 100.00);
	public static VariableBoolean 	cameraSmooth = new VariableBoolean("camera", "smooth", true);
	//Grid
	public static VariableBoolean 	gridEnabled = new VariableBoolean("grid", "enabled", true);
	public static VariableDouble 	gridMax = new VariableDouble("grid", "max", 1000000, 0, 2000000);
	public static VariableDouble 	gridStep = new VariableDouble("grid", "step", 50, 1, 2000000);
	public static VariableDouble 	gridAlpha = new VariableDouble("grid", "alpha", 0.1, 0, 1);
	//Place
	public static VariableInt 		placeCircleStep = new VariableInt("place", "circleSteps", 64, 3, 256);
	//Warehouse
	public static VariableDouble 	warehouseStartX = new VariableDouble("warehouse", "startX", 0, -2000000, 2000000);
	public static VariableDouble 	warehouseStartY = new VariableDouble("warehouse", "startY", 0, -2000000, 2000000);
	public static VariableDouble 	warehouseStartRadius = new VariableDouble("warehouse", "startRadius", 180, 0, 2000000);
	public static VariableDouble 	warehouseColorRed = new VariableDouble("warehouse", "red", 1, 0, 1);
	public static VariableDouble 	warehouseColorGreen = new VariableDouble("warehouse", "green", 1, 0, 1);
	public static VariableDouble 	warehouseColorBlue = new VariableDouble("warehouse", "blue", 1, 0, 1);
	public static VariableDouble 	warehouseColorAlpha = new VariableDouble("warehouse", "alpha", 0.15, 0, 1);
	public static VariableDouble 	warehouseLightCoef = new VariableDouble("warehouse", "lightCoef", 5, 0, 2000000);
	//Resource
	public static VariableDouble 	resourceColorRed = new VariableDouble("resource", "red", 1, 0, 1);
	public static VariableDouble 	resourceColorGreen = new VariableDouble("resource", "green", 1, 0, 1);
	public static VariableDouble 	resourceColorBlue = new VariableDouble("resource", "blue", 1, 0, 1);
	public static VariableDouble 	resourceColorAlpha = new VariableDouble("resource", "alpha", 0.1, 0, 1);
	public static VariableDouble 	resourceRadiusStep = new VariableDouble("resource", "step", 10, 1, 2000000);
	public static VariableDouble 	resourceStartRadius = new VariableDouble("resource", "startRadius", 50, 0, 2000000);
	public static VariableDouble 	resourceStartOmega = new VariableDouble("resource", "startOmega", 50, 0, 2000000);
	public static VariableDouble 	resourceStartCost = new VariableDouble("resource", "startCost", 100, 0, 2000000);
	public static VariableDouble 	resourceOmegaStep = new VariableDouble("resource", "omegaStep", 5, 0, 2000000);
	public static VariableDouble 	resourceCostStep = new VariableDouble("resource", "costStep", 5, 0, 2000000);
	//Building
	public static VariableDouble 	factoryRadius = new VariableDouble("factory", "radius", 5, 0, 2000000);
	public static VariableDouble 	factoryColorRed = new VariableDouble("factory", "red", 1, 0, 1);
	public static VariableDouble 	factoryColorGreen = new VariableDouble("factory", "green", 0, 0, 1);
	public static VariableDouble 	factoryColorBlue = new VariableDouble("factory", "blue", 0, 0, 1);
	public static VariableDouble 	factoryColorAlpha = new VariableDouble("factory", "alpha", 0.8, 0, 1);
	public static VariableBoolean 	factoryShow = new VariableBoolean("factory", "show", true);
	//Editor
	public static VariableBoolean 	editorShowInfo = new VariableBoolean("editor", "showInfo", true);
	public static VariableDouble 	editorRed = new VariableDouble("editor", "red", 0.3, 0, 1);
	public static VariableDouble 	editorGreen = new VariableDouble("editor", "green", 0.5, 0, 1);
	public static VariableDouble 	editorBlue = new VariableDouble("editor", "blue", 0.7, 0, 1);
	public static VariableDouble 	editorHoverRed = new VariableDouble("editor", "hoverRed", 1, 0, 1);
	public static VariableDouble 	editorHoverGreen = new VariableDouble("editor", "hoverGreen", 0, 0, 1);
	public static VariableDouble 	editorHoverBlue = new VariableDouble("editor", "hoverBlue", 1, 0, 1);
	public static VariableDouble 	editorHoverAlpha = new VariableDouble("editor", "hoverAlpha", 0.4f, 0, 1);
	public static VariableDouble 	editorSelectedRed = new VariableDouble("editor", "selectedRed", 1, 0, 1);
	public static VariableDouble 	editorSelectedGreen = new VariableDouble("editor", "selectedGreen", 0.25, 0, 1);
	public static VariableDouble 	editorSelectedBlue = new VariableDouble("editor", "selectedBlue", 0.05, 0, 1);
	public static VariableDouble 	editorSelectedAlpha = new VariableDouble("editor", "selectedAlpha", 0.75f, 0, 1);
	//Math
	public static VariableDouble 	mathT0 = new VariableDouble("math", "t0", 0.001, 0, 2000000);
	public static VariableDouble 	mathMi = new VariableDouble("math", "mi", 10, 1.01, 2000000);
	public static VariableDouble 	mathMinStep = new VariableDouble("math", "minStep", 0.001, 0, 1);
	public static VariableDouble 	mathEpsilon = new VariableDouble("math", "epsilon", 1, 0, 2000000);
	public static VariableDouble 	mathAlpha = new VariableDouble("math", "alpha", 0.1, 0, 0.5);
	public static VariableDouble 	mathBeta = new VariableDouble("math", "beta", 0.1, 0, 1);
	public static VariableDouble 	mathGamma = new VariableDouble("math", "gamma", 0.001, 0, 2000000);
	public static VariableBoolean 	showDebug = new VariableBoolean("math", "showDebug", true);
	public static VariableDouble 	traceRadius = new VariableDouble("math", "traceRadius", 2, 0, 2000000);
	public static VariableDouble 	traceEpsilon = new VariableDouble("math", "traceEpsilon", 0.001, 0, 2000000);
	public static VariableBoolean 	traceElimination = new VariableBoolean("math", "traceElimination", true);
	public static VariableInt 		maxNewtonSteps = new VariableInt("math", "maxNewtonSteps", 10000, 0, 2000000);
	public static VariableInt 		maxInteriorSteps = new VariableInt("math", "maxInteriorSteps", 100, 0, 2000000);
	public static VariableBoolean 	autoCompute = new VariableBoolean("math", "autoCompute", false);
	public static VariableBoolean 	forceStopEnabled = new VariableBoolean("math", "forceStopEnabled", true);
	public static VariableBoolean 	forceStopPercentEnabled = new VariableBoolean("math", "forceStopPercentEnabled", true);
	public static VariableDouble 	forceStopPercent = new VariableDouble("math", "forceStopPercent", 0.001, 0, 2000000);
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.GRAVE){
			game.console.enable(true);
		}
		if (keycode == Keys.F1){
			editorMode = !editorMode;
			if(editorMode) game.level.level.adjustZoom();
			else game.level.level.adjustZoom();
			Logger.logInfo("Editor mode: " + (editorMode ? "on" : "off"));
		}
		if (keycode == Keys.ESCAPE) {
			Logger.logInfo("Pressed ESC - app.exit()");
			game.dispose();
			Gdx.app.exit();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean wantInput() {
		return true;
	}
	
}
