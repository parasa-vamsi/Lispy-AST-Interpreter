package lispy.ast_interpreter.minimal_parser;

import java.util.List;
import lispy.ast_interpreter.minimal_parser.native_functions.*;

public class DefaultGlobalEnvironment extends Environment{

    public DefaultGlobalEnvironment() {
        super();
        System.out.println("Using default global envoirnment");
       
        this.record.put("+", new AddNativeFunction());
        this.record.put("-", new SubtractNativeFunction());
        this.record.put("*", new MultiplyNativeFunction());
        this.record.put("/", new DivideNativeFunction());
        this.record.put("print", new PrintNativeFunction());
    }

}