package lispy.ast_interpreter.minimal_parser;

import java.text.ParseException;
import java.util.List;

public class Lispy {
	
	String name;
	Environment envGlobal;
	
	public Lispy(String string) {
		name = string;
		envGlobal = new Environment();
		
	}
	
	public Lispy() {
		this(null);
	}
	
	public Object parse(String code) {
		LispyParser p = new LispyParser();
		Object parsed_output = null;
		try {
			parsed_output = p.parse(code);
			System.out.println(parsed_output);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return parsed_output;
	}

	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
	
	
	public Object eval(Object expr) {
		return eval(expr, envGlobal);
	}
	
	public Object eval(Object expr, Environment env) {
		
		if (isVariableName(expr)) {
			try {
				return env.lookup((String)expr);
			} catch (IllegalAccessException e) {
				e.getMessage();
			}
		}
		
		if (expr instanceof Number) return expr;
		
		if (expr instanceof String) {
			return (String) expr;
		}
		
		if (expr instanceof  List) return evalList((List) expr, env);
		else throw new UnsupportedOperationException("Expression must be an atom (Number, String) or List of expressions. Got " + expr.getClass());
	}
	
	public String evalString(String expr) {
		return expr.substring(1, expr.length() - 1);
	}

	public Object evalList(List expr, Environment env) {
		var op = expr.get(0);
		if (op.equals("+")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() + arg2.doubleValue();
		}

		if (op.equals("-")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() - arg2.doubleValue();
		}
		
		if (op.equals("*")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() * arg2.doubleValue();
		}
		
		if (op.equals(">")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() > arg2.doubleValue();
		}
		

		if (op.equals("<")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() < arg2.doubleValue();
		}
		
		if (op.equals("var")) {
			var name = (String) expr.get(1);
			var value = expr.get(2);
			return env.define(name, this.eval(value, env));
		}
		
		if (op.equals("set")) {
			var name = (String) expr.get(1);
			var value = expr.get(2);
			return env.assign(name, this.eval(value, env));
			
		}
		
		if (op.equals("begin")) {
			return evalBlock(expr, env);
			
		}
		
		if (op.equals("if")) {
			var cond = expr.get(1);
			if ((boolean) this.eval(cond, env)) return this.eval(expr.get(2), env);
			else return this.eval(expr.get(3), env);
		}
		
		if (op.equals("while")) {
			Object result = null;
			var cond = expr.get(1);
			var body = expr.get(2);
			
			while((boolean) this.eval(cond, env)) {
				result = this.eval(body, env);
				//System.out.println(result);
			}
			return result;
			
		}
		throw new UnsupportedOperationException("Not implemented error");
		
	}
	
	public Object evalBlock(List expr, Environment env) {
		Object result = null;
		var envBlock = new Environment(env);
		for (int i = 0; i < expr.size(); i++) result = this.eval(expr.get(i), envBlock);
		
		return result;
	}
	
	private boolean isVariableName(Object expr) {
		if (expr instanceof String) return true;
		else return false;
	}
	
}
