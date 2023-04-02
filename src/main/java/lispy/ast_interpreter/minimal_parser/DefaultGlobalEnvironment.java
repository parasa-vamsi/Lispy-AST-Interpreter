package lispy.ast_interpreter.minimal_parser;

import java.util.List;

public class DefaultGlobalEnvironment extends Environment{

    public DefaultGlobalEnvironment() {
        super();
        System.out.println("Using default global envoirnment");
        setupNativePrint();
    }

    private void setupNativePrint() {
        var printCallable = new LispyCallable() {

            @Override
            public Object call(Environment env, List<Object> arguments) {
                
                for (var arg : arguments){
                    System.out.println(arg.getClass() + ":> " + arg);
                }
                return null;
            }

            @Override
            public String toString() {
                return "<<Native Function>> print";
            }
            
        };

        this.record.put("print", printCallable);
    }

    
  
}
