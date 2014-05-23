// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.util.ArrayList;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.softsquare.settlers.ConsoleVariables.IVariable;
import com.softsquare.settlers.Logger.LoggerListener;
import com.softsquare.settlers.Logger.TYPE;

public class Console extends InputManager.InputReceiver implements LoggerListener {
	private TextButton inputField;
	private Table container, historyTable;
	private ScrollPane scroll;
	private boolean enabled = true;
		
	Console(){
		Logger.addListener(this);
		
		inputField = new TextButton("> ", Globals.game.skin);
		inputField.setColor(Color.ORANGE);
		inputField.getLabel().setAlignment(Align.left);
		inputField.setDisabled(true);
		
		container = new Table();
		Globals.game.stage.addActor(container);
		container.setColor(0, 0, 0, 0);
		container.setX(0);
		container.setY(Globals.game.stage.getHeight()/2);
		container.setHeight(Globals.game.stage.getHeight()/2);
		container.setWidth(Globals.game.stage.getWidth());
		container.setFillParent(false);

		historyTable = new Table();
		scroll = new ScrollPane(historyTable, Globals.game.skin);
		scroll.setFlickScroll(true);
		scroll.setFadeScrollBars(true);
		historyTable.pad(2).defaults().expandX().space(4);
		historyTable.padBottom(35);

		container.add(scroll).expand().fill().colspan(4);
		container.row().space(0).padBottom(0);
		container.add(inputField).fillX().expandX();
		
		enable(false);
		parse("list");
		parse("help");
	}
	
	public void resize(int w, int h) {
		container.setX(0);
		container.setY(h/2);
		container.setHeight(h/2);
		container.setWidth(w);
	}
	
	
	public boolean parse(String cmd) {
		if(cmd.toLowerCase().equals("help")) {
			print("usage: name [value]");
		} else if(cmd.toLowerCase().equals("list")) {
			String s = "";
			for(IVariable<?> var : ConsoleVariables.listAll()) {
				s += "[" + var.getName() + " " + var.serialize() + "] ";
			}
			print(s);
		}
		else {
			String[] tokens = cmd.split(" ");
			if(tokens.length < 1 || tokens.length > 2) {
				print("usage: variable_name [new_value]");
			}
			else if(tokens.length == 1) {
				ArrayList<IVariable<?>> list = null;
				String s = "";
				if(cmd.contains(".")) {
					int t = cmd.indexOf('.');
					String group = cmd.substring(0, t);
					String name = cmd.substring(t + 1, cmd.length());
					if(ConsoleVariables.getGroup(group) == null) {
						print("Group not found: " + group);
					} else {
						list = ConsoleVariables.listAllInGroup(group, name);
						if(list.size() > 0) {
							s += group + ": ";
							for(IVariable<?> var : list) {
								s += "[" + var.getName() + " " + var.serialize() + "] ";
							}
							print(s);
						}
						else print("Group " + group + " empty");
					}
				}
				else  {
					list = ConsoleVariables.listAll(cmd);
					if(list.size() < 1) list = ConsoleVariables.listAllInGroup(cmd, "");
					
					if(list.size() > 0) {
						for(IVariable<?> var : list) {
							s += "[" + var.getName() + " " + var.serialize() + "] ";
						}
						print(s);
					}
					else print("Command not found: '" + cmd + "'");
				}
			}
			else if(tokens.length == 2) {
				ArrayList<IVariable<?>> list = null;
				IVariable<?> var = null;
				if(tokens[0].contains(".")) {
					int t = tokens[0].indexOf('.');
					String group = tokens[0].substring(0, t);
					String name = tokens[0].substring(t + 1, tokens[0].length());
					list = ConsoleVariables.listAllInGroup(group, name);
					var = ConsoleVariables.findInGroup(group, name);
				}
				else {
					list = ConsoleVariables.listAll(tokens[0]);
					var = ConsoleVariables.find(tokens[0]);
				}
				
				if(var == null) {
					print("Variable not found: " + tokens[0]);
				} else if(list.size() > 1) {
					String s = "";
					for(IVariable<?> v : list) {
						s += "[" + v.getName() + " " + v.serialize() + "] ";
					}
					print(s);
				}
				else {
					var.set(tokens[1]);
					print(var.getName() + " = " + var.serialize());
				}
			}
		}
		
		return false;
	}
	
	public void print(String str) {
		historyTable.row();
		Label l = new Label(str, Globals.game.skin);
		l.setWrap(true);
		historyTable.add(l).expandX().fillX();
		scroll.scrollTo(0, -9900, 0, 0);
	}
	
	public void print(String str, Color color) {
		historyTable.row();
		Label l = new Label(str, Globals.game.skin);
		l.setWrap(true);
		l.setColor(color);
		historyTable.add(l).expandX().fillX();
		scroll.scrollTo(0, -9900, 0, 0);
	}
	
	public void enable(boolean enabled) {
		this.enabled = enabled;
		Globals.game.statsEnabled = !enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void update(float deltaTime) {
		if(enabled){
			container.setVisible(true);
			if(container.getColor().a < 0.8)
				container.getColor().a += deltaTime;		
		} else {
			if(container.getColor().a > 0)
				container.getColor().a -= deltaTime;
			if(container.getColor().a < 0) {
				container.getColor().a = 0;
				container.setVisible(false);
			}
		}
	}
	
	String command = "";
	
	ArrayList<String> parsedCommands = new ArrayList<String>();
	int parsedCommandsIndex = 0;
	ArrayList<IVariable<?>> listTab = null;
	int listTabIndex = 0;
	

	@Override
	public boolean keyDown(int keycode) {
		if(Input.Keys.GRAVE == keycode) {
			enable(false);
		}
		if(Input.Keys.BACKSPACE == keycode) {
			if(command.length() > 0)
				command = command.substring(0, command.length() - 1);
			inputField.setText("> " + command);
		}
		else if(Input.Keys.UP == keycode) {
			if(listTabIndex >= parsedCommands.size() || listTabIndex < 0)
				listTabIndex = parsedCommands.size() - 1;
			if(listTabIndex < parsedCommands.size() && listTabIndex >= 0) {
				command = parsedCommands.get(listTabIndex);
				inputField.setText("> " + command);
			}
			listTabIndex--;
		}
		if(Input.Keys.TAB == keycode) {
			if(listTab == null) {
				if(command.contains(".")) {
					int t = command.indexOf('.');
					String group = command.substring(0, t);
					String name = command.substring(t + 1, command.length());
					listTab = ConsoleVariables.listAllInGroup(group, name);
				}
				else {
					listTab = ConsoleVariables.listAll(command);
				}
				if(listTab.size() < 1) {
					listTab  = ConsoleVariables.listAllGroup(command);
				}
				listTabIndex = -1;
			}
			
			
			if(listTab != null) {
				listTabIndex++;
				if(listTabIndex > listTab.size()-1)
					listTabIndex = 0;
				if(listTabIndex < listTab.size()) {
					command = listTab.get(listTabIndex).getGroup() + "." + listTab.get(listTabIndex).getName();
					inputField.setText("> " + command);
				}
			}
			
			
		}
		else listTab = null;
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(Input.Keys.ENTER == keycode){
			parse(command);
			parsedCommands.add(command);
			parsedCommandsIndex = parsedCommands.size()-1;
			command = "";
			listTab = null;
			inputField.setText("> ");
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if(enabled == true && (character == '.' || character == '-' || character == ' ' || character >= 'a' && character <= 'z' || character >=  'A'&& character <= 'Z' || character >= '0' && character <= '9')) {
			command = command + String.valueOf(character);
			listTab = null;
			inputField.setText("> " + command);
		}
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLog(TYPE type, String text) {
		switch (type) {
		case INFO:
			print(text, Color.WHITE);	
			break;
		case SUCCESS:
			print(text, Color.GREEN);
			break;
		case WARNING:
			print(text, Color.ORANGE);
			break;
		case ERROR:
			print(text, Color.RED);
			break;
		case DEBUG:
			print(text, Color.RED);
			break;
		case FATAL:
			print(text, Color.RED);
			break;
		default:
			print(text, Color.WHITE);
			break;
		}
			
	}

	@Override
	public boolean wantInput() {
		return true;
	}
}
