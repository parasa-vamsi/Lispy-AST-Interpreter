import com.syntax.*;
import java.text.ParseException;

public class CodeParser {
  public static void main(String[] args) {

    var p = new LispyParser();

    try {

      System.out.println(p.parse("*"));
      System.out.println(p.parse("/"));
      System.out.println(p.parse("(/ 5 10)"));
      System.out.println(p.parse("()"));
      System.out.println(p.parse("helloWorld"));
      System.out.println(p.parse("""
        "LISPY"
            """));

      var big = """
        (begin 
          (var x -5.)
          (var z "hello")
          (set x 10)
          (+ 15 -20)
          (if (> x +5.2)
            (+ x 10.)
            (- x -1.32)
          )
        )
          """;
      System.out.println(p.parse(big));

      var parsed_output = p.parse(args[0]);
      System.out.println(parsed_output); // 6
      //System.out.println(parsed_output.getClass());
    } catch (ParseException e) {
      System.out.println(e.getMessage());
    }
  }
}public class CodeParser {
    
}
