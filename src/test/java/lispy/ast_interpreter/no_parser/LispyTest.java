package lispy.ast_interpreter.no_parser;

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
		assertEquals(1, lispy.eval(1));
		assertEquals(10., lispy.eval(10.));
	}
	
	@Test
	void testStrings() {
		Lispy lispy = new Lispy();
		assertEquals("Lispy", lispy.eval("\"Lispy\""));
		
		String tst = """
				"hello" """; //"\"hello\""
		assertEquals("hello", lispy.eval(tst));
	}
	
	@Test
	void testPlus() {
		Lispy lispy = new Lispy();
		
		var mathOp = list("+", 1, 5);
		assertEquals(6., lispy.eval(mathOp));
		
		mathOp = list("+", -7L, 9.0f);
		assertEquals(2., lispy.eval(mathOp));
	}
	
	@Test
	void testListOfExpressions() {
		Lispy lispy = new Lispy("List of Expressions");
		
		var expr = list("+", list("+", 3, 2), 6);
		assertEquals(11., lispy.eval(expr));
		
		expr = list("+", list("+", 4, -6), list("+", 4, 5));
		assertEquals(7., lispy.eval(expr));
		
		expr = list("+", list("+", 4, list("+", 3, -2)), list("+", 4, 5));
		assertEquals(14., lispy.eval(expr));
	}
	
	@Test
	void testDefineVar() {
		Lispy lispy = new Lispy();
		
		var expr = list("var", "x", 25);
		assertEquals(25, lispy.eval(expr));
		assertEquals(25, lispy.eval("x"));
		
		expr = list("var", "y", 100);
		assertEquals(100, lispy.eval(expr));
		assertEquals(100, lispy.eval("y"));
		
		expr = list("var", "isTrue", "true");
		assertEquals(true, lispy.eval(expr));
		assertEquals(true, lispy.eval("isTrue"));
		
		expr = list("var", "z", list("+", 2.5 , 3));
		assertEquals(5.5, lispy.eval(expr));
		assertEquals(5.5, lispy.eval("z"));
	}
	
	@Test
	void testBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin", 
							list("var", "x", 50),
							list("var", "y", 0.5f),
							list("+", list("*", "x", "y"), 30)
						);
		assertEquals(55., lispy.eval(expr));
	}
	
	@Test
	void testNestedBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin", 
							list("var", "x", 5),
							list("begin",
									list("var", "x", 25),
									"x"
							),
							"x"
						);
		assertEquals(5, lispy.eval(expr));
	}
	
	@Test
	void testAcessOuterBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin", 
							list("var", "value", 15),
							list("var", "result", 
									list("begin",
											list("var", "x", list("+", "value", 10)),
									"x")
								 ),
							"result"
						);
		assertEquals(25., lispy.eval(expr));
	}
	
	@Test
	void testSetWithBlocks() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin", 
							list("var", "data", 10),
								 list("begin",
										list("set", "data", 100)
								      ),
							"data"
						);
		assertEquals(100, lispy.eval(expr));
	}
	
	@Test
	void testIfExpr() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin",
							list("var", "x", 50),
							list("var", "y", 0.5f),
							list("if", list(">", "x", 100),
									list("set", "y", 20),
									list("set", "y", 45)
								      ),
							"y"
						);
		assertEquals(45, lispy.eval(expr));
	}
	
	@Test
	void testWhile() {
		Lispy lispy = new Lispy();
		
		var expr = list("begin",
							list("var", "counter", 0),
							list("var", "result", 0),
							
							list("while", list("<", "counter", 10),
									list("begin",
											list("set", "result", list("+", "result", 3)),
											list("set", "counter", list("+", "counter", 1)),
											"result"
								      )
					        ),
							"result"
						);
		assertEquals(30., lispy.eval(expr));
	}
	
	
	

}
