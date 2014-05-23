// By Adam Micha³owski (c) 2013 Settlers Simulation

package com.softsquare.settlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ConsoleVariables {
	private static HashMap<String, HashMap<String, IVariable<?>>> variables = new HashMap<String, HashMap<String, IVariable<?>>>();
	private static HashMap<String, HashMap<String, String>> variablesUndefined = new HashMap<String, HashMap<String, String>>();
	
	public static void load(String fileName) {
		try {
			UniversalParser parser = new UniversalParser(fileName);
			String name, value, group = "";
			while(!(name = parser.next()).equals("")) {
				value = parser.next();
				if(value.equals(""))continue;
				if(name.equals("group")) 
					group = value;
				else 
					onLoadVariable(group, name, value);
			}
			Logger.logSuccess("Loaded console variables from " + fileName);
		} catch (Exception e) {
			Logger.logSuccess("Cannot load console variables from " + fileName + " (" + e.toString() + ")");
			e.printStackTrace();
		}
	}
		
	private static void onLoadVariable(String group, String name, String value) {
		IVariable<?> variable = get(group, name);
		if(variable != null) {
			variable.set(value);
		} else {
			HashMap<String, String> map = variablesUndefined.get(group);
			if(map == null) {
				map = new HashMap<String, String>();
				variablesUndefined.put(group, map);
			}
			map.put(name, value);
		}
	}

	public static void save(String fileName) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
			for(String group : variables.keySet()) {
				HashMap<String, IVariable<?>> map = variables.get(group);
				out.println("");
				out.println("group " + group);				
				for(String name : map.keySet()) {
					IVariable<?> value = map.get(name);
					out.println(name + " " + value.serialize());
				}
			}
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	public static void add(IVariable<?> value) {
		if(value == null) return;
		
		HashMap<String, String> groupMap = variablesUndefined.get(value.getGroup());
		if(groupMap != null) {
			String newValue = groupMap.get(value.getName());
			if(newValue != null) {
				value.set(newValue);
				
				groupMap.remove(value.getName());
				if(groupMap.size() < 1)
					variablesUndefined.remove(value.getGroup());
			}
		}
		
		HashMap<String, IVariable<?>> map = variables.get(value.getGroup());
		if(map == null) {
			map = new HashMap<String, IVariable<?>>();
			variables.put(value.getGroup(), map);
		}
		map.put(value.getName(), value);
	}

	public static IVariable<?> get(String group, String name) {
		HashMap<String, IVariable<?>> groupMap = variables.get(group);
		return (groupMap == null) ? null : groupMap.get(name);
	}
	
	public static ArrayList<IVariable<?>> listAll() {
		ArrayList<IVariable<?>> list = new ArrayList<IVariable<?>>();
		for(String group : variables.keySet()) {
			HashMap<String, IVariable<?>> map = variables.get(group);	
			for(String name : map.keySet()) {
				list.add(map.get(name));
			}
		}
		return list;
	}
	
	public static ArrayList<IVariable<?>> listAll(String prefix) {
		prefix = prefix.toLowerCase();
		ArrayList<IVariable<?>> list = new ArrayList<IVariable<?>>();
		for(String group : variables.keySet()) {
			HashMap<String, IVariable<?>> map = variables.get(group);	
			for(String name : map.keySet()) {
				if(name.toLowerCase().startsWith(prefix))
					list.add(map.get(name));
			}
		}
		return list;
	}
	
	public static ArrayList<IVariable<?>> listAllInGroup(String group, String prefix) {
		prefix = prefix.toLowerCase();
		ArrayList<IVariable<?>> list = new ArrayList<IVariable<?>>();
		HashMap<String, IVariable<?>> map = variables.get(group);
		if(map == null) return list;
		for(String name : map.keySet()) {
			if(name.toLowerCase().startsWith(prefix))
				list.add(map.get(name));
		}
		return list;
	}
	
	public static ArrayList<IVariable<?>> listAllGroup(String prefix) {
		prefix = prefix.toLowerCase();
		ArrayList<IVariable<?>> list = new ArrayList<IVariable<?>>();
		for(String group : variables.keySet()) {
			if(group.startsWith(prefix)) {
				HashMap<String, IVariable<?>> map = variables.get(group);	
				for(String name : map.keySet()) {
					list.add(map.get(name));
				}
			}
		}
		return list;
	}
	
	public static IVariable<?> find(String prefix) {
		prefix = prefix.toLowerCase();
		for(String group : variables.keySet()) {
			HashMap<String, IVariable<?>> map = variables.get(group);	
			for(String name : map.keySet()) {
				if(name.toLowerCase().startsWith(prefix))
					return map.get(name);
			}
		}
		return null;
	}
	
	public static IVariable<?> findInGroup(String group, String prefix) {
		prefix = prefix.toLowerCase();
		HashMap<String, IVariable<?>> map = variables.get(group);
		if(map == null) return null;
		for(String name : map.keySet()) {
			if(name.toLowerCase().startsWith(prefix))
				return map.get(name);
		}
		return null;
	}
	
	public static Collection<IVariable<?>> getGroup(String group) {
		HashMap<String, IVariable<?>> groupMap = variables.get(group);
		return (groupMap == null) ? null : groupMap.values();
	}
	
	public static String getUndefined(String group, String name) {
		HashMap<String, String> groupMap = variablesUndefined.get(group);
		return (groupMap == null) ? null : groupMap.get(name);
	}
	
	public static Collection<String> getUndefinedGroup(String group) {
		HashMap<String, String> groupMap = variablesUndefined.get(group);
		return (groupMap == null) ? null : groupMap.values();
	}
	
	public static abstract class IVariable<T> {
		public abstract String getGroup();
		public abstract String getName();
		public abstract void set(T newValue);
		public abstract T get();
		public abstract String serialize();
		public abstract void set(String valueAsString);
		public void register() { add(this); }
	}
	
	public static class VariableBoolean extends IVariable<Boolean> {
		private String _name, _group;
		private Boolean _value;
		public VariableBoolean(String group, String name, boolean value) {
			_group = group;
			_name = name;
			_value = value;
			register();
		}
		@Override
		public String getName() {
			return _name;
		}
		@Override
		public void set(Boolean newValue) {
			_value = newValue;
		}
		@Override
		public Boolean get() {
			return _value;
		}
		@Override
		public void set(String valueAsString) {
			set(Boolean.valueOf(valueAsString));			
		}
		@Override
		public String serialize() {
			return _value.toString();
		}
		@Override
		public String getGroup() {
			return _group;
		}
	}
	
	public static class VariableDouble extends IVariable<Double> {
		private String _name, _group;
		private Double _value;
		private double _minVal, _maxVal;
		public VariableDouble(String group, String name, double value, double minVal, double maxVal) {
			_group = group;
			_name = name;
			_value = value;
			_minVal = minVal;
			_maxVal = maxVal;
			register();
		}
		@Override
		public String getName() {
			return _name;
		}
		@Override
		public void set(Double newValue) {
			if(newValue.doubleValue() > _maxVal) _value = _maxVal;
			else if(newValue.doubleValue() < _minVal) _value = _minVal;
			else _value = newValue;
		}
		@Override
		public Double get() {
			return _value;
		}
		@Override
		public void set(String valueAsString) {
			set(Double.valueOf(valueAsString));			
		}
		@Override
		public String serialize() {
			return  String.format("%f", _value.floatValue()).replace(',', '.');
		}
		@Override
		public String getGroup() {
			return _group;
		}
	}
	
	public static class VariableInt extends IVariable<Integer> {
		private String _name, _group;
		private Integer _value;
		private int _minVal, _maxVal;
		public VariableInt(String group, String name, int value, int minVal, int maxVal) {
			_group = group;
			_name = name;
			_value = value;
			_minVal = minVal;
			_maxVal = maxVal;
			register();
		}
		@Override
		public String getName() {
			return _name;
		}
		@Override
		public void set(Integer newValue) {
			if(newValue.intValue() > _maxVal) _value = _maxVal;
			else if(newValue.intValue() < _minVal) _value = _minVal;
			else _value = newValue;
		}
		@Override
		public Integer get() {
			return _value;
		}
		@Override
		public void set(String valueAsString) {
			set(Integer.valueOf(valueAsString));			
		}
		@Override
		public String serialize() {
			return _value.toString();
		}
		@Override
		public String getGroup() {
			return _group;
		}
	}
	
	public static class VariableString extends IVariable<String> {
		private String _name, _group;
		private String _value;
		public VariableString(String group, String name, String value) {
			_group = group;
			_name = name;
			_value = value;
			register();
		}
		@Override
		public String getName() {
			return _name;
		}
		@Override
		public void set(String newValue) {
			_value = newValue;
		}
		@Override
		public String get() {
			return _value;
		}
		@Override
		public String serialize() {
			return "\"" + _value.toString() + "\"";
		}
		@Override
		public String getGroup() {
			return _group;
		}
	}
}
