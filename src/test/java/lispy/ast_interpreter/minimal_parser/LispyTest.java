package lispy.ast_interpreter.minimal_parser;

import static org.junit.jupiter.api.Assertions.*;
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
		assertEquals("\"string_literal\"", lispy.eval(expr));

		expr = lispy.parse("""
			KeyWord
			""");
		assertEquals("KeyWord", lispy.eval(expr));
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
						x
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
