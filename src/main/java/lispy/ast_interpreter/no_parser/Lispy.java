package lispy.ast_interpreter.no_parser;

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
			return evalString((String) expr);
		}
		
		if (expr instanceof  Object[]) return evalList((Object[]) expr, env);
		else throw new UnsupportedOperationException("Expression must be an atom (Number, String) or List of expressions");
	}
	
	public String evalString(String expr) {
		return expr.substring(1, expr.length() - 1);
	}

	public Object evalList(Object[] expr, Environment env) {
		if (expr[0].equals("+")) {
			var arg1 = (Number) this.eval(expr[1], env);
			var arg2 = (Number) this.eval(expr[2], env);
			return arg1.doubleValue() + arg2.doubleValue();
		}
		
		if (expr[0].equals("*")) {
			var arg1 = (Number) this.eval(expr[1], env);
			var arg2 = (Number) this.eval(expr[2], env);
			return arg1.doubleValue() * arg2.doubleValue();
		}
		
		if (expr[0].equals(">")) {
			var arg1 = (Number) this.eval(expr[1], env);
			var arg2 = (Number) this.eval(expr[2], env);
			return arg1.doubleValue() > arg2.doubleValue();
		}
		

		if (expr[0].equals("<")) {
			var arg1 = (Number) this.eval(expr[1], env);
			var arg2 = (Number) this.eval(expr[2], env);
			return arg1.doubleValue() < arg2.doubleValue();
		}
		
		if (expr[0].equals("var")) {
			var name = (String) expr[1];
			var value = expr[2];
			return env.define(name, this.eval(value, env));
		}
		
		if (expr[0].equals("set")) {
			var name = (String) expr[1];
			var value = expr[2];
			return env.assign(name, this.eval(value, env));
			
		}
		
		if (expr[0].equals("begin")) {
			return evalBlock(expr, env);
			
		}
		
		if (expr[0].equals("if")) {
			var cond = expr[1];
			if ((boolean) this.eval(cond, env)) return this.eval(expr[2], env);
			else return this.eval(expr[3], env);
		}
		
		if (expr[0].equals("while")) {
			Object result = null;
			var cond = expr[1];
			var body = expr[2];
			
			while((boolean) this.eval(cond, env)) {
				result = this.eval(body, env);
				//System.out.println(result);
			}
			return result;
			
		}
		throw new UnsupportedOperationException("Not implemented error");
		
	}
	
	public Object evalBlock(Object[] expr, Environment env) {
		Object result = null;
		var envBlock = new Environment(env);
		for (int i = 0; i < expr.length; i++) result = this.eval(expr[i], envBlock);
		
		return result;
	}
	
	private boolean isVariableName(Object expr) {
		if (expr instanceof String) return true;
		else return false;
	}
	
}
