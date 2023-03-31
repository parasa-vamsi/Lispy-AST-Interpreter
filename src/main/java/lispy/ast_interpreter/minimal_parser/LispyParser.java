/**
 * LR parser for Java generated by the Syntax tool.
 *
 * https://www.npmjs.com/package/syntax-cli
 *
 *   npm install -g syntax-cli
 *
 *   syntax-cli --help
 *
 * To regenerate run:
 *
 *   syntax-cli \
 *     --grammar ~/path-to-grammar-file \
 *     --mode LALR1 \
 *     --output ~/ParserClassName.java
 */

package lispy.ast_interpreter.minimal_parser;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

//import java.util.ArrayList;

/*

The `ParserEvents` class allows defining hooks for certain parse events,
such as initialization of the parser instance, beginning of the parsing, etc.

Default implementation:

  class ParserEvents {
    public static void init() {
      // Parser is created.
    }

    public static void onParseBegin(String _string) {
      // Parsing is started.
    }

    public static void onParseEnd(Object _result) {
      // Parsing is completed.
    }
  }

*/


  class ParserEvents {
    public static void init() {
      // Parser is created.
    }

    public static void onParseBegin(String _string) {
      // Parsing is started.
    }

    public static void onParseEnd(Object _result) {
      // Parsing is completed.
    }
  }


/**
 * Generic tokenizer used by the parser in the Syntax tool.
 *
 * https://www.npmjs.com/package/syntax-cli
 */

/* These should be inserted by the parser class already:

package com.syntax;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

*/

// --------------------------------------------
// Tokenizer.

/**
 * Location object.
 */
class YyLoc {
  public YyLoc() {}

  public int startOffset;
  public int endOffset;
  public int startLine;
  public int endLine;
  public int startColumn;
  public int endColumn;

  public YyLoc(int startOffset, int endOffset, int startLine,
               int endLine, int startColumn, int endColumn) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.startLine = startLine;
    this.endLine = endLine;
    this.startColumn = startColumn;
    this.endColumn = endColumn;
  }

  public static YyLoc yyloc(YyLoc start, YyLoc end) {
    // Epsilon doesn't produce location.
    if (start == null || end == null) {
      return start == null ? end : start;
    }

    return new YyLoc(
      start.startOffset,
      end.endOffset,
      start.startLine,
      end.endLine,
      start.startColumn,
      end.endColumn
    );
  }
}

/**
 * Token class: encapsulates token type, and the matched value.
 */
class Token {
  // Basic data.
  public int type;
  public String value;

  // Location data.
  YyLoc loc;

  public Token(int type, String value) {
    // Special token with no location data (e.g. EOF).
    this(type, value, null);
  }

  public Token(int type, String value, YyLoc loc) {
    this.type = type;
    this.value = value;
    this.loc = loc;
  }

  public String toString() {
    return "{type: " + type + ", value: " + value + "}";
  }
}

/**
 * Regexp-based tokenizer. Applies lexical rules in order, until gets
 * a match; otherwise, throws the "Unexpected token" exception.
 *
 * Tokenizer should implement at least the following API:
 *
 * - getNextToken(): Token
 * - hasMoreTokens(): boolean
 * - isEOF(): boolean
 *
 * For state-based tokenizer, also:
 *
 * - getCurrentState(): number
 * - pushState(String stateName): void
 * - popState(): void
 * - begin(String stateName): void - alias for pushState
 */
class Tokenizer {

  /**
   * Tokenizing String.
   */
  private String mString;

  /**
   * Matched text.
   */
  public String yytext = null;

  /**
   * Matched text length.
   */
  public int yyleng = 0;

  /**
   * EOF.
   */
  public static String EOF = "$";

  /**
   * Maps a String name of a token type to its encoded number (the first
   * token number starts after all numbers for non-terminal).
   *
   * Example:
   *
   *   put("+", 1);
   *   put("*", 2);
   *   put("NUMBER", 3);
   *   ...
   */
  private static final Map<String, Integer> mTokensMap = new HashMap<String, Integer>() {{
    put("NUMBER", 4); put("STRING", 5); put("SYMBOL", 6); put("'('", 7); put("')'", 8); put("$", 9);
  }};

  /**
   * EOF Token.
   */
  public static Token EOF_TOKEN = new Token(
    mTokensMap.get(Tokenizer.EOF),
    Tokenizer.EOF
  );

  /**
   * Lex patterns, and their handler names.
   *
   * Example:
   *
   *   Pattern.compile("^\\s+"),
   *   Pattern.compile("^\\d+"),
   *   ...
   */
  private static final Pattern[] mLexPatterns = {
    Pattern.compile("^\\("),
    Pattern.compile("^\\)"),
    Pattern.compile("^\\s+"),
    Pattern.compile("^[+|-]?(\\d)+(\\.[\\d]*)?"),
    Pattern.compile("^[\\w\\-+*=<>/]+"),
    Pattern.compile("^\"[^\"]*\"")
  };

  /**
   * Cache for the lex rule methods.
   *
   * Example:
   *
   *   mLexHandlerMethods[0] = Tokenizer.class.getDeclaredMethod("_lexRule0");
   *   ...
   */
  private static final Method[] mLexHandlerMethods = new Method[6];
  static {
    try {
      mLexHandlerMethods[0] = Tokenizer.class.getDeclaredMethod("_lexRule0");
      mLexHandlerMethods[1] = Tokenizer.class.getDeclaredMethod("_lexRule1");
      mLexHandlerMethods[2] = Tokenizer.class.getDeclaredMethod("_lexRule2");
      mLexHandlerMethods[3] = Tokenizer.class.getDeclaredMethod("_lexRule3");
      mLexHandlerMethods[4] = Tokenizer.class.getDeclaredMethod("_lexRule4");
      mLexHandlerMethods[5] = Tokenizer.class.getDeclaredMethod("_lexRule5");
    } catch (Exception ignore) {
      // Ignore since the methods are exact.
    }
  };

  private static final Pattern NL_RE = Pattern.compile("\\n");

  /**
   * Lex rules grouped by tokenizer state.
   *
   * Example:
   *
   *   { "INITIAL", new Integer[] { 0, 1, 2, 3 } },
   *   ...
   */
  private static Map<String, Integer[]> mLexRulesByConditions = new HashMap<String, Integer[]>() {{
    put("INITIAL", new Integer[] { 0, 1, 2, 3, 4, 5 });
  }};

  /**
   * Stack of lexer states.
   */
  private Stack<String> mStates = null;

  /**
   *  Cursor tracking current position.
   */
  private int mCursor = 0;

  /**
   * Line-based location tracking.
   */
  int mCurrentLine;
  int mCurrentColumn;
  int mCurrentLineBeginOffset;

  /**
   * Location data of a matched token.
   */
  int mTokenStartOffset;
  int mTokenEndOffset;
  int mTokenStartLine;
  int mTokenEndLine;
  int mTokenStartColumn;
  int mTokenEndColumn;

  /**
   * In case if a token handler returns multiple tokens from one rule,
   * we still return tokens one by one in the `getNextToken`, putting
   * other "fake" tokens into the queue. If there is still something in
   * this queue, it's just returned.
   */
  private Queue<String> mTokensQueue = null;

  /**
   * Lex rule handlers.
   *
   * Example:
   *
   *   public String _lexRule1() {
   *     return "NUMBER";
   *   }
   */
    String _lexRule0() {
    return "'('";
  }

  String _lexRule1() {
    return "')'";
  }

  String _lexRule2() {
    return null;
  }

  String _lexRule3() {
    return "NUMBER";
  }

  String _lexRule4() {
    return "SYMBOL";
  }

  String _lexRule5() {
    return "STRING";
  }

  // --------------------------------------------
  // Constructor.

  public Tokenizer() {
    //
  }

  public Tokenizer(String tokenizingString) {
    initString(tokenizingString);
  }

  public void initString(String tokenizingString) {
    mString = tokenizingString;
    mCursor = 0;

    mStates = new Stack<String>();
    begin("INITIAL");

    mTokensQueue = new LinkedList<String>();

    // Init locations.

    mCurrentLine = 1;
    mCurrentColumn = 0;
    mCurrentLineBeginOffset = 0;

    // Token locationis.
    mTokenStartOffset = 0;
    mTokenEndOffset = 0;
    mTokenStartLine = 0;
    mTokenEndLine = 0;
    mTokenStartColumn = 0;
    mTokenEndColumn = 0;
  }

  // --------------------------------------------
  // States.

  public String getCurrentState() {
    return mStates.peek();
  }

  public void pushState(String state) {
    mStates.push(state);
  }

  public void begin(String state) {
    pushState(state);
  }

  public String popState() {
    if (mStates.size() > 1) {
      return mStates.pop();
    }
    return getCurrentState();
  }

  // --------------------------------------------
  // Tokenizing.

  public Token getNextToken() throws ParseException {
    // Something was queued, return it.
    if (mTokensQueue.size() > 0) {
      return toToken(mTokensQueue.remove(), "");
    }

    if (!hasMoreTokens()) {
      return EOF_TOKEN;
    }

    String str = mString.substring(mCursor);
    Integer[] lexRulesForState = mLexRulesByConditions.get(getCurrentState());

    for (int i = 0; i < lexRulesForState.length; i++) {
      String matched = match(str, mLexPatterns[i]);

      // Manual handling of EOF token (the end of String). Return it
      // as `EOF` symbol.
      if (str.length() == 0 && matched != null && matched.length() == 0) {
        mCursor++;
      }

      if (matched != null) {
        this.yytext = matched;
        this.yyleng = matched.length();

        Object tokenType = null;

        try {
          tokenType = mLexHandlerMethods[i].invoke(this);
        } catch (Exception e) {
          e.printStackTrace();
          throw new ParseException(e.getMessage(), 0);
        }

        if (tokenType == null) {
          return getNextToken();
        }

        if (tokenType.getClass().isArray()) {
          String[] tokensArray = (String[])tokenType;
          tokenType = (String)tokensArray[0];
          if (tokensArray.length > 1) {
            for (int j = 1; j < tokensArray.length; j++) {
              mTokensQueue.add(tokensArray[j]);
            }
          }
        }

        return toToken((String)tokenType, matched);
      }
    }

    if (isEOF()) {
      mCursor++;
      return EOF_TOKEN;
    }

    throwUnexpectedToken(
      str.charAt(0),
      mCurrentLine,
      mCurrentColumn
    );

    return null;
  }

  /**
   * Throws default "Unexpected token" exception, showing the actual
   * line from the source, pointing with the ^ marker to the bad token.
   * In addition, shows `line:column` location.
   */
  public void throwUnexpectedToken(char symbol, int line, int column) throws ParseException {
    String lineSource = mString.split("\n")[line - 1];

    String pad = new String(new char[column]).replace("\0", " ");
    String lineData = "\n\n" + lineSource + "\n" + pad + "^\n";

    throw new ParseException(
      lineData + "Unexpected token: \"" + symbol +"\" " +
      "at " + line + ":" + column + ".", 0
    );
  }

  private void captureLocation(String matched) {
    // Absolute offsets.
    mTokenStartOffset = mCursor;

    // Line-based locations, start.
    mTokenStartLine = mCurrentLine;
    mTokenStartColumn = mTokenStartOffset - mCurrentLineBeginOffset;

    // Extract `\n` in the matched token.
    Matcher nlMatcher = NL_RE.matcher(matched);
    while (nlMatcher.find()) {
      mCurrentLine++;
      mCurrentLineBeginOffset = mTokenStartOffset + nlMatcher.start() + 1;
    }

    mTokenEndOffset = mCursor + matched.length();

    // Line-based locations, end.
    mTokenEndLine = mCurrentLine;
    mTokenEndColumn = mCurrentColumn =
      (mTokenEndOffset - mCurrentLineBeginOffset);
  }

  private Token toToken(String tokenType, String yytext) {
    return new Token(
      mTokensMap.get(tokenType),
      yytext,
      new YyLoc(
        mTokenStartOffset,
        mTokenEndOffset,
        mTokenStartLine,
        mTokenEndLine,
        mTokenStartColumn,
        mTokenEndColumn
      )
    );
  }

  public boolean hasMoreTokens() {
    return mCursor <= mString.length();
  }

  public boolean isEOF() {
    return mCursor == mString.length();
  }

  private String match(String str, Pattern re) {
    Matcher m = re.matcher(str);
    String v = null;
    if (m.find()) {
      v = m.group(0);
      captureLocation(v);
      mCursor += v.length();
    }
    return v;
  }

  public String get() {
    return mString;
  }
}

// --------------------------------------------
// Parser.

class StackEntry {
  public int symbol;
  public Object semanticValue;
  public YyLoc loc;

  public StackEntry(int symbol, Object semanticValue, YyLoc loc) {
    this.symbol = symbol;
    this.semanticValue = semanticValue;
    this.loc = loc;
  }
}

/**
 * Base class for the parser. Implements LR parsing algorithm.
 *
 * Should implement at least the following API:
 *
 * - parse(String StringToParse): object
 * - setTokenizer(Tokenizer tokenizer): void, or equivalent Tokenizer
 *   accessor property with a setter.
 */
public class LispyParser {

  /**
   * Tokenizer instance.
   */
  public Tokenizer tokenizer = null;

  /**
   * Encoded grammar productions table.
   * Format of a record:
   * { <Non-Terminal Index>, <RHS.Length>}
   *
   * Non-terminal indices are 0-Last Non-terminal. LR-algorithm uses
   * length of RHS to pop symbols from the stack; this length is stored
   * as the second element of a record. The last element is an optional
   * name of the semantic action handler. The first record is always
   * a special marker {-1, -1} entry representing an augmented production.
   */
  private static int[][] mProductions = {
    {-1, 1},
    {0, 1},
    {0, 1},
    {1, 1},
    {1, 1},
    {1, 1},
    {2, 3},
    {3, 2},
    {3, 0}
  };

  /**
   * Cache for the handler methods.
   *
   * Example:
   *
   *   mProductionHandlerMethods[0] = Parser.class.getDeclaredMethod("_handler0");
   *   ...
   */
  private static final Method[] mProductionHandlerMethods = new Method[9];
  static {
    try {
      mProductionHandlerMethods[0] = LispyParser.class.getDeclaredMethod("_handler0");
      mProductionHandlerMethods[1] = LispyParser.class.getDeclaredMethod("_handler1");
      mProductionHandlerMethods[2] = LispyParser.class.getDeclaredMethod("_handler2");
      mProductionHandlerMethods[3] = LispyParser.class.getDeclaredMethod("_handler3");
      mProductionHandlerMethods[4] = LispyParser.class.getDeclaredMethod("_handler4");
      mProductionHandlerMethods[5] = LispyParser.class.getDeclaredMethod("_handler5");
      mProductionHandlerMethods[6] = LispyParser.class.getDeclaredMethod("_handler6");
      mProductionHandlerMethods[7] = LispyParser.class.getDeclaredMethod("_handler7");
      mProductionHandlerMethods[8] = LispyParser.class.getDeclaredMethod("_handler8");
    } catch (Exception ignore) {
      // Ignore since the methods are exact.
    }
  };

  /**
   * Actual parsing table. An array of records, where
   * index is a state number, and a value is a dictionary
   * from an encoded symbol (number) to parsing action.
   * The parsing action can be "Shift/s", "Reduce/r", a state
   * transition number, or "Accept/acc".
   *
   * Example:
   *
   *   mTable.add(
   *     new HashMap<Integer, String>() {{
   *       put(0, "1");
   *       put(3, "s2");
   *       put(4, "s3");
   *     }}
   *   );
   *   ...
   */
  private static List<Map<Integer, String>> mTable = new ArrayList<Map<Integer, String>>();
  static {
    mTable.add(new HashMap<Integer, String>() {{ put(0, "1"); put(1, "2"); put(2, "3"); put(4, "s4"); put(5, "s5"); put(6, "s6"); put(7, "s7"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(9, "acc"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r1"); put(5, "r1"); put(6, "r1"); put(7, "r1"); put(8, "r1"); put(9, "r1"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r2"); put(5, "r2"); put(6, "r2"); put(7, "r2"); put(8, "r2"); put(9, "r2"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r3"); put(5, "r3"); put(6, "r3"); put(7, "r3"); put(8, "r3"); put(9, "r3"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r4"); put(5, "r4"); put(6, "r4"); put(7, "r4"); put(8, "r4"); put(9, "r4"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r5"); put(5, "r5"); put(6, "r5"); put(7, "r5"); put(8, "r5"); put(9, "r5"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(3, "8"); put(4, "r8"); put(5, "r8"); put(6, "r8"); put(7, "r8"); put(8, "r8"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(0, "10"); put(1, "2"); put(2, "3"); put(4, "s4"); put(5, "s5"); put(6, "s6"); put(7, "s7"); put(8, "s9"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r6"); put(5, "r6"); put(6, "r6"); put(7, "r6"); put(8, "r6"); put(9, "r6"); }});
    mTable.add(new HashMap<Integer, String>() {{ put(4, "r7"); put(5, "r7"); put(6, "r7"); put(7, "r7"); put(8, "r7"); }});
  };

  /**
   * Parsing stack. Stores instances of StackEntry.
   */
  Stack<StackEntry> mValueStack = null;

  /**
   * States stack.
   */
  Stack<Integer> mStatesStack = null;

  /**
   * __ holds a result value from a production
   * handler. In the grammar usually used as $$.
   */
  StackEntry __ = null;

  /**
   * Constructor.
   */
  public LispyParser() {
    // A tokenizer instance, which is reused for all
    // `parse` method calls. The actual String is set
    // in the tokenizer.initString("...").
    tokenizer = new Tokenizer();

    // Run init hook to setup callbacks, etc.
    ParserEvents.init();
  }

  /**
   * Production handles. The handlers receive arguments as _1, _2, etc.
   * The result is always stored in __.
   *
   * In grammar:
   *
   * { $$ = (Integer)$1 + (Integer)$3 }
   *
   * Generated:
   *
   * public void _handler0() {
   *   // Prologue
   *   StackEntry _3 = mValueStack.pop();
   *   mValueStack.pop();
   *   StackEntry _1 = mValueStack.pop();
   *
   *   __.semanticValue = (Integer)(_1.semanticValue) + (Integer)(_3.semanticValue);
   * }
   */
    void _handler0() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = (_1.semanticValue);
  }

  void _handler1() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = (_1.semanticValue);
  }

  void _handler2() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = (_1.semanticValue);
  }

  void _handler3() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = Double.parseDouble((String)(_1.semanticValue));
  }

  void _handler4() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = (_1.semanticValue);
  }

  void _handler5() {
    // Semantic values prologue.
StackEntry _1 = mValueStack.pop();

__.semanticValue = (_1.semanticValue);
  }

  void _handler6() {
    // Semantic values prologue.
mValueStack.pop();
StackEntry _2 = mValueStack.pop();
mValueStack.pop();

__.semanticValue = (_2.semanticValue);
  }

  void _handler7() {
    // Semantic values prologue.
StackEntry _2 = mValueStack.pop();
StackEntry _1 = mValueStack.pop();

((List)(_1.semanticValue)).add((_2.semanticValue)); __.semanticValue = (_1.semanticValue);
  }

  void _handler8() {
    // Semantic values prologue.


__.semanticValue = new ArrayList<Object>();
  }

  /**
   * Main parsing method which applies LR-algorithm.
   */
  public Object parse(String str) throws ParseException {
    // On parse begin hook.
    ParserEvents.onParseBegin(str);

    tokenizer.initString(str);

    // Initialize the parsing stack to the initial state 0.
    mValueStack = new Stack<StackEntry>();
    mStatesStack = new Stack<Integer>();
    mStatesStack.push(0);

    Token token = tokenizer.getNextToken();
    Token shiftedToken = null;

    do {
      if (token == null) {
        unexpectedEndOfInput();
      }

      int state = mStatesStack.peek();
      int column = token.type;

      if (!mTable.get(state).containsKey(column)) {
        unexpectedToken(token);
        break;
      }

      String entry = mTable.get(state).get(column);

      // ---------------------------------------------------
      // "Shift". Shift-entries always have 's' as their
      // first char, after which goes *next state number*, e.g. "s5".
      // On shift we push the token, and the next state on the stack.
      if (entry.charAt(0) == 's') {
        YyLoc loc = null;

        // Push token.
        mValueStack.push(new StackEntry(token.type, token.value, token.loc));

        // Push next state number: "s5" -> 5
        mStatesStack.push(Integer.valueOf(entry.substring(1)));

        shiftedToken = token;
        token = tokenizer.getNextToken();
      }

      // ---------------------------------------------------
      // "Reduce". Reduce-entries always have 'r' as their
      // first char, after which goes *production number* to
      // reduce by, e.g. "r3" - reduce by production 3 in the grammar.
      // On reduce, we pop of the stack number of symbols on the RHS
      // of the production, and their pushed state numbers, i.e.
      // total RHS * 2 symbols.
      else if (entry.charAt(0) == 'r') {
        // "r3" -> 3
        int productionNumber = Integer.valueOf(entry.substring(1));
        int[] production = mProductions[productionNumber];

        // The length of RHS is stored in the production[1].
        int rhsLength = production[1];
        if (rhsLength != 0) {
          while (rhsLength-- > 0) {
            // Pop the state number.
            mStatesStack.pop();
          }
        }

        int previousState = mStatesStack.peek();
        int symbolToReduceWith = production[0];

        __ = new StackEntry(symbolToReduceWith, null, null);

        // Execute the semantic action handler.
        this.tokenizer.yytext = shiftedToken != null ? shiftedToken.value : null;
        this.tokenizer.yyleng = shiftedToken != null ? shiftedToken.value.length() : 0;

        try {
          mProductionHandlerMethods[productionNumber].invoke(this);
        } catch (Exception e) {
          e.printStackTrace();
          throw new ParseException(e.getMessage(), 0);
        }

        // Then push LHS onto the stack.
        mValueStack.push(__);

        // And the next state number.
        int nextState = Integer.valueOf(
          mTable.get(previousState).get(symbolToReduceWith)
        );

        mStatesStack.push(nextState);
      }
      // ---------------------------------------------------
      // Accept. Pop starting production and its state number.
      else if (entry.charAt(0) == 'a') {
        // Pop state number.
        mStatesStack.pop();

        // Pop the parsed value.
        StackEntry parsed = mValueStack.pop();

        if (
          mStatesStack.size() != 1 ||
          mStatesStack.peek() != 0 ||
          tokenizer.hasMoreTokens()
        ) {
          unexpectedToken(token);
        }

        Object parsedValue = parsed.semanticValue;
        ParserEvents.onParseEnd(parsedValue);

        return parsedValue;
      }

    } while (tokenizer.hasMoreTokens() || mStatesStack.size() > 1);

    return null;
  }

  private void unexpectedToken(Token token) throws ParseException {
    if (token.type == Tokenizer.EOF_TOKEN.type) {
      unexpectedEndOfInput();
    }

    tokenizer.throwUnexpectedToken(
      token.value.charAt(0),
      token.loc.startLine,
      token.loc.startColumn
    );
  }

  private void unexpectedEndOfInput() throws ParseException {
    parseError("Unexpected end of input.");
  }

  private void parseError(String message) throws ParseException {
    throw new ParseException("Parse error: " + message, 0);
  }
}