package lispy.ast_interpreter.minimal_parser;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

//import lispy.ast_interpreter.no_parser.Lispy;

class LispyTest {
	
	Object list(Object ... objects) {
		return objects;
	}
	
	@Test
	void testNumbers() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("1");
		assertEquals(1., lispy.eval(expr));
		expr = lispy.parse("+10");
		assertEquals(10., lispy.eval(expr));
		expr = lispy.parse("-5.34");
		assertEquals(-5.34, lispy.eval(expr));
	}
	
	@Test
	void testStrings() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("""
				"string_literal" 
				""");
		assertEquals("string_literal", lispy.eval(expr));

		expr = lispy.parse("""
			"sometext"
			""");
		assertEquals("sometext", lispy.eval(expr));
	}

	@Test
	void testVariables() {
		Lispy lispy = new Lispy();

		// var expr = lispy.parse("name");
		//assertEquals(null, lispy.eval(expr));

		var expr = lispy.parse("""
			(var name "Ramu")
			""");
		assertEquals("Ramu", lispy.eval(expr));
		expr = lispy.parse("name");
		assertEquals("Ramu", lispy.eval(expr));
	}
	
	@Test
	void testMathOperations() {
		Lispy lispy = new Lispy();
		
		var mathOp = lispy.parse("(+ 1 5)");
		assertEquals(6., lispy.eval(mathOp));
		
		mathOp = lispy.parse("(- 7 -9)");
		assertEquals(16., lispy.eval(mathOp));

		mathOp = lispy.parse("(* 8 0.5)");
		assertEquals(4., lispy.eval(mathOp));

		mathOp = lispy.parse("(/ 7 2)");
		assertEquals(3.5, lispy.eval(mathOp));

		mathOp = lispy.parse("(> -7.2 +9.6)");
		assertEquals(false, lispy.eval(mathOp));

		mathOp = lispy.parse("(< -17.2 -9.)");
		assertEquals(true, lispy.eval(mathOp));
	}

	@Test
	void testNativeFunctions() {
		Lispy lispy = new Lispy();
		
		var expr = lispy.parse("""
			(print "hello" "world")
			""");
			
		assertEquals(true, lispy.eval(expr));

		expr = lispy.parse("""
			(print (+ 4 5) (- 5 3))
			""");
			
		assertEquals(true, lispy.eval(expr));
	}

	@Test
	void testFunctions() {
		Lispy lispy = new Lispy();
		
		var expr = lispy.parse("""
			(begin
				(def square (x)
					(* x x)
				)
				(square 5)
			)
			""");
			
		assertEquals(25., lispy.eval(expr));

		expr = lispy.parse("""
			(begin
				(def dist (x y)
					(+ (* x x) (* y y))
				)
				(dist 2 3)
			)
			""");
			
		assertEquals(13., lispy.eval(expr));
	}

	@Test
	void testClosures() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("""
			(begin
				(var y 10)
				(def square (x)
					(+ (* x x) y)
				)
				(square 5)
			)
			""");
			
		assertEquals(35., lispy.eval(expr));

		expr = lispy.parse("""
			(begin
				(var y 10)
				(def square (x)
					(+ (* x x) y)
				)
				(square 5)
			)
			""");
			
		assertEquals(35., lispy.eval(expr));
		
		expr = lispy.parse("""
			(begin
				(var a 2)
				(var b -1)
				(var c 0.5)
				(def quadratic (x)
					(begin
						(var temp (* a (* x x)))
						(set temp (+ temp (* b x)))
						(set temp (+ temp c))
						temp
					)
				)
				(quadratic 5)
			)
			""");
		double d = 5;	
		var exp = 2 * (d*d) - d + 0.5;	
		assertEquals(exp, lispy.eval(expr));
	}
	
	@Test
	void testListOfExpressions() {
		Lispy lispy = new Lispy("List of Expressions");
		
		var expr = lispy.parse("(+ (+ 3 2) 6)");
		assertEquals(11., lispy.eval(expr));
		
		expr = lispy.parse("(+ (- 4 6) (+ 4 5))");
		assertEquals(7., lispy.eval(expr));
		
		expr = lispy.parse("(+ (+ (- 3 2) 4) (+ 4 5))");
		//list("+", list("+", 4, list("+", 3, -2)), list("+", 4, 5));
		assertEquals(14., lispy.eval(expr));
	}
	
	@Test
	void testDefineVar() {
		Lispy lispy = new Lispy();
		
		var expr = lispy.parse("(var x 25)");
		assertEquals(25., lispy.eval(expr));
		expr = lispy.parse("x");
		assertEquals(25., lispy.eval(expr));
		
		expr = lispy.parse("(var y 100)");
		assertEquals(100., lispy.eval(expr));
		expr = lispy.parse("y");
		assertEquals(100., lispy.eval(expr));
		
		expr = lispy.parse("(var isTrue true)");
		assertEquals(true, lispy.eval(expr));
		expr = lispy.parse("isTrue");
		assertEquals(true, lispy.eval(expr));

		expr = lispy.parse("(var z (+ 2 3))");
		assertEquals(5., lispy.eval(expr));
		expr = lispy.parse("z");
		assertEquals(5., lispy.eval(expr));

		expr = lispy.parse("""
			(var alpha "bobby")
			""");
		assertEquals("bobby", lispy.eval(expr));
		expr = lispy.parse("alpha");
		assertEquals("bobby", lispy.eval(expr));

		expr = lispy.parse("""
			(var str "25")
			""");
		assertEquals("25", lispy.eval(expr));
		expr = lispy.parse("str");
		assertEquals("25", lispy.eval(expr));
	
	}
	
	@Test
	void testBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = lispy.parse("""
			(begin 
				(var x 50)
				(var y 2)
				(+ (* x y) 30)
			)
			""");
			
		assertEquals(130., lispy.eval(expr));
	}
	
	@Test
	void testNestedBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = lispy.parse("""
				(begin
					(var x 5)
					(begin
						(var x 25)
						x
					)
					x
				)
				""");
		assertEquals(5., lispy.eval(expr));
	}
	
	@Test
	void testAcessOuterBlocks() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("""
				(begin
					(var value 15)
					(var result (begin
									(var x (+ value 10))
									x
								)
					)
					result
				)
				""");
	
		assertEquals(25., lispy.eval(expr));
	}
	
	@Test
	void testSetWithBlocks() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("""
				(begin
					(var data 10)
					(begin
						(set data 100)
						data
					)
					data
				)
				""");

		assertEquals(100., lispy.eval(expr));
	}
	
	@Test
	void testIfExpr() {
		Lispy lispy = new Lispy();

		var expr = lispy.parse("""
				(begin
					(var x 50)
					(var y 2)
					(if (> x 100)
						(set y 20)
						(set y 45)
					)
					y
				)
				""");
		
		assertEquals(45., lispy.eval(expr));
	}
	
	@Test
	void testWhile() {
		Lispy lispy = new Lispy();
		var expr = lispy.parse("""
			(begin
				(var counter 0)
				(var result 0)
				(while (< counter 10)
					(begin
						(set result (+ result 3))
						(set counter (+ counter 1))
					)
				)
				result
			)
			""");
		assertEquals(30., lispy.eval(expr));

		expr = lispy.parse("""
			(begin
				(var counter 0)
				(var result 0)
				(while (< counter 10)
					(begin
						(set result (+ result 3))
						(set counter (+ counter 1))
					)
				)
				counter
			)
			""");
		assertEquals(10., lispy.eval(expr));
	}
	
	
	

}
