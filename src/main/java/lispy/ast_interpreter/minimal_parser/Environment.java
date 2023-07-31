package lispy.ast_interpreter.minimal_parser;

import java.util.Map;
import java.util.HashMap;


public class Environment {
	
	Map<String, Object> record;
	Environment parent;
	
	public Environment(Map record, Environment parent) {
		this.record = record; 
		this.parent = parent;
		record.put("true", true);
		record.put("false", false);
		record.put("none", null);
		record.put("VERSION", 1.0);
	}

	public Environment(Environment parent) {
		this(new HashMap<>(), parent);
	}
	
	public Environment() {
		this(new HashMap<>(), null);
	}

	public Object define(String name, Object value) {
		record.put(name, value);
		return value;
	}

	public Object lookup(String name) throws IllegalAccessException {
//		if (record.containsKey(name)) return record.get(name);
//		else throw new IllegalAccessException("Variable not defined");
		return this.resolve(name).record.get(name);
		
	}
	
	public Environment resolve(String name) throws IllegalAccessException {
		if (this.record.containsKey(name)) return this;
		if (this.parent == null)  throw new IllegalAccessException(name + " :> Variable not defined in env --> " + this);
		return this.parent.resolve(name);
	}

	public Object assign(String name, Object value) {
		try {
			this.resolve(name).record.put(name, value);
		} catch (IllegalAccessException e) {
			e.getMessage();
		}
		return value;
	}

}
