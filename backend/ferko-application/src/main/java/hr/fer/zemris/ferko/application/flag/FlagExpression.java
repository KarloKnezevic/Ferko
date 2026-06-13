package hr.fer.zemris.ferko.application.flag;

import java.util.ArrayList;
import java.util.List;

/**
 * Safe boolean expression language for assessment flags / prerequisites ("zastavice"), replacing
 * FERKO's arbitrary Java program with a sandboxed mini-interpreter. Supported grammar:
 *
 * <pre>
 *   expr        := orExpr
 *   orExpr      := andExpr ( '||' andExpr )*
 *   andExpr     := unary  ( '&&' unary )*
 *   unary       := '!' unary | primary
 *   primary     := 'true' | 'false' | '(' expr ')'
 *                | present | flag | comparison
 *   present     := 'present' '(' STRING ')'
 *   flag        := 'flag'    '(' STRING ')'
 *   comparison  := numeric ( '>=' | '<=' | '==' | '!=' | '>' | '<' ) numeric
 *   numeric     := 'points' '(' STRING ')' | NUMBER
 * </pre>
 *
 * <p>It performs no I/O and cannot execute arbitrary code; unknown syntax raises {@link
 * FlagExpressionException}.
 */
public final class FlagExpression {

  private final List<Token> tokens;
  private final FlagContext context;
  private int position;

  private FlagExpression(List<Token> tokens, FlagContext context) {
    this.tokens = tokens;
    this.context = context;
  }

  /** Parses and evaluates {@code expression} against {@code context}. */
  public static boolean evaluate(String expression, FlagContext context) {
    if (expression == null || expression.isBlank()) {
      return true; // no prerequisite => everyone qualifies
    }
    FlagExpression parser = new FlagExpression(tokenize(expression), context);
    boolean result = parser.orExpr();
    parser.expect(TokenType.EOF);
    return result;
  }

  // --- Parser / evaluator (recursive descent) ---

  private boolean orExpr() {
    boolean value = andExpr();
    while (peek().type == TokenType.OR) {
      next();
      value = andExpr() | value;
    }
    return value;
  }

  private boolean andExpr() {
    boolean value = unary();
    while (peek().type == TokenType.AND) {
      next();
      value = unary() & value;
    }
    return value;
  }

  private boolean unary() {
    if (peek().type == TokenType.NOT) {
      next();
      return !unary();
    }
    return primary();
  }

  private boolean primary() {
    Token token = peek();
    switch (token.type) {
      case LPAREN -> {
        next();
        boolean value = orExpr();
        expect(TokenType.RPAREN);
        return value;
      }
      case NUMBER, POINTS -> {
        return comparison();
      }
      case TRUE -> {
        next();
        return true;
      }
      case FALSE -> {
        next();
        return false;
      }
      case PRESENT -> {
        next();
        return context.present(argument());
      }
      case FLAG -> {
        next();
        return context.flag(argument());
      }
      default -> throw new FlagExpressionException("Neočekivani token: " + token.text);
    }
  }

  private boolean comparison() {
    double left = numeric();
    Token op = next();
    return switch (op.type) {
      case GE -> left >= numeric();
      case LE -> left <= numeric();
      case GT -> left > numeric();
      case LT -> left < numeric();
      case EQ -> left == numeric();
      case NE -> left != numeric();
      default ->
          throw new FlagExpressionException("Očekivan operator usporedbe, dobiven: " + op.text);
    };
  }

  private double numeric() {
    Token token = next();
    if (token.type == TokenType.NUMBER) {
      return Double.parseDouble(token.text);
    }
    if (token.type == TokenType.POINTS) {
      return context.points(argumentFrom());
    }
    throw new FlagExpressionException("Očekivan broj ili points(...), dobiven: " + token.text);
  }

  private String argument() {
    expect(TokenType.LPAREN);
    Token string = expect(TokenType.STRING);
    expect(TokenType.RPAREN);
    return string.text;
  }

  /** Same as {@link #argument()} but used right after a {@code points} token already consumed. */
  private String argumentFrom() {
    return argument();
  }

  private Token peek() {
    return tokens.get(Math.min(position, tokens.size() - 1));
  }

  private Token next() {
    return tokens.get(Math.min(position++, tokens.size() - 1));
  }

  private Token expect(TokenType type) {
    Token token = next();
    if (token.type != type) {
      throw new FlagExpressionException("Očekivan " + type + ", dobiven: " + token.text);
    }
    return token;
  }

  // --- Lexer ---

  private enum TokenType {
    PRESENT,
    POINTS,
    FLAG,
    TRUE,
    FALSE,
    NUMBER,
    STRING,
    AND,
    OR,
    NOT,
    LPAREN,
    RPAREN,
    GE,
    LE,
    GT,
    LT,
    EQ,
    NE,
    EOF
  }

  private record Token(TokenType type, String text) {}

  private static List<Token> tokenize(String input) {
    List<Token> tokens = new ArrayList<>();
    int i = 0;
    int n = input.length();
    while (i < n) {
      char c = input.charAt(i);
      if (Character.isWhitespace(c)) {
        i++;
      } else if (c == '(') {
        tokens.add(new Token(TokenType.LPAREN, "("));
        i++;
      } else if (c == ')') {
        tokens.add(new Token(TokenType.RPAREN, ")"));
        i++;
      } else if (c == '&' && i + 1 < n && input.charAt(i + 1) == '&') {
        tokens.add(new Token(TokenType.AND, "&&"));
        i += 2;
      } else if (c == '|' && i + 1 < n && input.charAt(i + 1) == '|') {
        tokens.add(new Token(TokenType.OR, "||"));
        i += 2;
      } else if (c == '"') {
        int end = input.indexOf('"', i + 1);
        if (end < 0) {
          throw new FlagExpressionException("Nezatvoreni navodnici");
        }
        tokens.add(new Token(TokenType.STRING, input.substring(i + 1, end)));
        i = end + 1;
      } else if (c == '>' || c == '<' || c == '=' || c == '!') {
        boolean eq = i + 1 < n && input.charAt(i + 1) == '=';
        if (c == '>') {
          tokens.add(new Token(eq ? TokenType.GE : TokenType.GT, eq ? ">=" : ">"));
        } else if (c == '<') {
          tokens.add(new Token(eq ? TokenType.LE : TokenType.LT, eq ? "<=" : "<"));
        } else if (c == '=' && eq) {
          tokens.add(new Token(TokenType.EQ, "=="));
        } else if (c == '!' && eq) {
          tokens.add(new Token(TokenType.NE, "!="));
        } else if (c == '!') {
          tokens.add(new Token(TokenType.NOT, "!"));
          i++;
          continue;
        } else {
          throw new FlagExpressionException("Neispravan operator: " + c);
        }
        i += eq ? 2 : 1;
      } else if (Character.isDigit(c) || c == '.') {
        int start = i;
        while (i < n && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
          i++;
        }
        tokens.add(new Token(TokenType.NUMBER, input.substring(start, i)));
      } else if (Character.isLetter(c)) {
        int start = i;
        while (i < n && Character.isLetterOrDigit(input.charAt(i))) {
          i++;
        }
        String word = input.substring(start, i);
        tokens.add(new Token(keyword(word), word));
      } else {
        throw new FlagExpressionException("Neočekivani znak: " + c);
      }
    }
    tokens.add(new Token(TokenType.EOF, "<eof>"));
    return tokens;
  }

  private static TokenType keyword(String word) {
    return switch (word) {
      case "present" -> TokenType.PRESENT;
      case "points" -> TokenType.POINTS;
      case "flag" -> TokenType.FLAG;
      case "true" -> TokenType.TRUE;
      case "false" -> TokenType.FALSE;
      default -> throw new FlagExpressionException("Nepoznata ključna riječ: " + word);
    };
  }
}
