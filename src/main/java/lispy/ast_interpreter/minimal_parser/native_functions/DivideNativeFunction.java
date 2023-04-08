package lispy.ast_interpreter.minimal_parser.native_functions;

import java.util.List;
import lispy.ast_interpreter.minimal_parser.Environment;
import lispy.ast_interpreter.minimal_parser.LispyNativeFunction;

public class DivideNativeFunction implements LispyNativeFunction{

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    public Object call(Environment env, List<Object> arguments) {
        return (double) arguments.get(0) / (double) arguments.get(1);
    }
  
}
