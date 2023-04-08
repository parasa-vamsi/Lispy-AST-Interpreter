package lispy.ast_interpreter.minimal_parser.native_functions;

import java.util.List;
import lispy.ast_interpreter.minimal_parser.Environment;
import lispy.ast_interpreter.minimal_parser.LispyNativeFunction;

public class PrintNativeFunction implements LispyNativeFunction{

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public Object call(Environment env, List<Object> arguments) {
        
        for (var arg : arguments){
            System.out.println(arg.getClass() + ":> " + arg);
        }
        return true;
    }

    @Override
    public String toString() {
        return "<<Native Function>> print";
    }
  
}
