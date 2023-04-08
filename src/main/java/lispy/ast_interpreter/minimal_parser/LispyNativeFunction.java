package lispy.ast_interpreter.minimal_parser;

import java.util.List;

public interface LispyNativeFunction extends LispyCallable {
    Object call(Environment env, List<Object> arguments);
}
