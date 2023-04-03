package lispy.ast_interpreter.minimal_parser;

import java.util.List;

class LispyFunction implements LispyCallable{

    String name;
    Object parameters;
    Object body;
    Environment env;

    public LispyFunction(String name, Object parameters, Object body, Environment env) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.env = env;
    
    }

    @Override
    public Object call(Environment env, List<Object> arguments) {
        throw new UnsupportedOperationException("Unimplemented method 'call'");
    }
  
}
