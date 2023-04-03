package lispy.ast_interpreter.minimal_parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Lispy {
	
	String name;
	Environment envGlobal;
	
	public Lispy(String string) {
		name = string;
		envGlobal = new DefaultGlobalEnvironment();
	}
	
	public Lispy() {
		this("default");
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
				System.out.println("TOKEN: Variable --> " + expr);
				System.out.println("looking up variable: " + expr);
				return env.lookup((String)expr);
			} catch (IllegalAccessException e) {
				e.getMessage();
				throw new Error(expr + ": variable not defined/found in env=" + env);
				//return null;

			}
		}

		if (expr instanceof String && ((String)expr).matches("^\"[^\"]*\"")) {
			System.out.println("TOKEN: String --> " + expr);
			String str = (String) expr;

			return str.substring(1, str.length() - 1);
		}
		
		if (expr instanceof Number) return expr;
		
		if (expr instanceof  List) return evalList((List) expr, env);
		else throw new UnsupportedOperationException("Expression must be an atom (Number, String) or List of expressions. Got " + expr.getClass());
	}
	
	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

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

		if (op.equals("/")) {
			var arg1 = (Number) this.eval(expr.get(1), env);
			var arg2 = (Number) this.eval(expr.get(2), env);
			return arg1.doubleValue() / arg2.doubleValue();
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

		if (op.equals("print")) {
			var printFunction = (LispyCallable)this.eval(op, env); 
			var args = new ArrayList<Object>();
			for (int i = 1; i < expr.size(); i++) {
				args.add(this.eval(expr.get(i), env));
			}
			System.out.println("'print' lookup:-> " + printFunction);
			return printFunction.call(env, args);
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

		if (op.equals("def")) {
			var functionName = (String) expr.get(1);
			var parameters = expr.get(2);
			var body = expr.get(3);
			var lispyFunction = new LispyFunction(name, parameters, body, env);
			env.define(functionName, lispyFunction);
			System.out.println("function defined--> " + functionName + "; in env=" + env);
			return lispyFunction;
		}

		// defaulting to function call execution
		try {
			var lispyFunction = (LispyFunction)this.eval(op, env); 
			var activationEnv = new Environment(lispyFunction.env);
			for (int i = 1; i < expr.size(); i++) {
				var arg = this.eval(expr.get(i), env);
				String param = (String)((List<Object>)lispyFunction.parameters).get(i-1);
				activationEnv.record.put(param, arg);
			}

			return this.eval(lispyFunction.body, activationEnv);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		throw new UnsupportedOperationException("Not implemented error");
		
	}
	
	public Object evalBlock(List expr, Environment env) {
		Object result = null;
		var envBlock = new Environment(env);
		for (int i = 1; i < expr.size(); i++) result = this.eval(expr.get(i), envBlock);
		
		return result;
	}
	
	private boolean isVariableName(Object expr) {
		if (expr instanceof String) {
			var str = (String) expr;
			System.out.println("String begins with quote: " + str.substring(0, 1).contains("\""));
			if (str.substring(0, 1).contains("\"")) return false; //string literal
			if (str.matches("[a-z|A-z][a-z|A-z|0-9]*")) return true;
			//if (str.matches("[*+-/><=]")) return true;
			else return false;
		}
		else return false;
	}
	
}
