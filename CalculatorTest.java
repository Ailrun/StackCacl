import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.*;

public class CalculatorTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("q") == 0)
					break;

				command(input);
			}
			catch (Exception e)
			{
				System.out.println("ERROR");
			}
		}
	}

	private static void command(String input)
	{
		Expression expr = new Expression(input);
		expr.convertInfixToPostfix();
		long result = expr.evalPostfix();
		expr.printTokens();
		System.out.println(result);
	}

	private static final Pattern tokenPattern = Pattern.compile("\\s*(0|[1-9][0-9]*|.)\\s*");
		
	private static final Pattern numberCheckPattern  = Pattern.compile("\\s*(0|[1-9][0-9]*|[^\\s])\\s*");
	
}

class Expression {
	private enum ExpressionParsingState {
		EPSNothing, EPSOpenParen, EPSCloseParen, EPSUnaryOp, EPSBinaryOp, EPSNumber;

		public static boolean isRightSuccessor(ExpressionParsingState current, ExpressionParsingState next) {
			switch (current) {
			case EPSNothing:
			case EPSOpenParen:
				return next == EPSOpenParen || next == EPSUnaryOp || next == EPSNumber;
			case EPSCloseParen:
				return next == EPSNothing || next == EPSCloseParen || next == EPSBinaryOp;
			case EPSUnaryOp:
				return next == EPSOpenParen || next == EPSUnaryOp || next == EPSNumber;
			case EPSBinaryOp:
				return next == EPSOpenParen || next == EPSUnaryOp || next == EPSNumber;
			case EPSNumber:
				return next == EPSNothing || next == EPSCloseParen || next == EPSBinaryOp;
			default:
				throw new IllegalArgumentException("Input is not a ExpressionParsingState");
			}
		}
	}
	private enum ExpressionFix {
		EFPrefix, EFInfix, EFPostfix;
	}
	
	private static final Pattern tokenCheckPattern = Pattern.compile("\\s*(0|[1-9][0-9]*|[^\\s])\\s*");
	private Stack<Token> tokens;
	private ExpressionFix fix;

	public Expression(String input) {
		tokens = new Stack<>();
		fix = ExpressionFix.EFInfix;
		TokenType currentTokenType = TokenType.TTNothing;

		Matcher tokenCheckMatcher = tokenCheckPattern.matcher(input);
		String unparsedToken = null;
		int parenDepth = 0;
		Token newToken = null;
		while (tokenCheckMatcher.find()) {
			unparsedToken = tokenCheckMatcher.group(1);
			switch (unparsedToken.charAt(0)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if (isRightInfixSuccessor(currentTokenType,
									 TokenType.TTNumeric)) {
					currentTokenType = TokenType.TTNumeric;
				} else {
					throw new IllegalArgumentException("Wrong Expression");
				}
				newToken = (Token) new Numeric(unparsedToken);
				break;
			case '+':
			case '*':
			case '/':
			case '%':
			case '^':
				newToken = (Token) new Operator(unparsedToken);
				break;
			case '(':
				parenDepth++;
				newToken = (Token) new Operator(unparsedToken);
				break;
			case ')':
				parenDepth--;
				newToken = (Token) new Operator(unparsedToken);
				break;
			case '-':
				if (isRightInfixSuccessor(currentTokenType,
											TokenType.TTUnaryOp)) {
					unparsedToken = new String("~");
				}
				newToken = (Token) new Operator(unparsedToken);
				break;
			default:
				throw new IllegalArgumentException("Wrong Expression");
			}
			if (isRightInfixSuccessor(currentTokenType, newToken.getType())) {
				currentTokenType = newToken.getType();
			}
			tokens.push(newToken);
		}

		if (parenDepth != 0
			|| !isRightInfixSuccessor(currentTokenType,
									  TokenType.TTNothing)) {
			throw new IllegalArgumentException("Wrong Expression");
		}
	}

	private boolean isRightInfixSuccessor(TokenType current, TokenType next) {
		switch (current) {
		case TTNothing:
		case TTOpenParen:
			return next == TokenType.TTOpenParen
				|| next == TokenType.TTUnaryOp
				|| next == TokenType.TTNumeric;
		case TTCloseParen:
			return next == TokenType.TTNothing
				|| next == TokenType.TTBinaryOp;
		case TTUnaryOp:
			return next == TokenType.TTOpenParen
				|| next == TokenType.TTUnaryOp
				|| next == TokenType.TTNumeric;
		case TTBinaryOp:
			return next == TokenType.TTOpenParen
				|| next == TokenType.TTUnaryOp
				|| next == TokenType.TTNumeric;
		case TTNumeric:
			return next == TokenType.TTNothing
				|| next == TokenType.TTCloseParen
				|| next == TokenType.TTBinaryOp;
		default:
			throw new IllegalArgumentException("Wrong TokenType");
		}
	}

	public void convertInfixToPostfix() {
		if (fix == ExpressionFix.EFInfix) {
			Stack<Token> reversedTokens = new Stack<>();
			Stack<Operator> opStack = new Stack<>();

			while (!tokens.empty()) {
				reversedTokens.push(tokens.pop());
			}
			
			Token currentToken = null;
			while (!reversedTokens.empty()) {
				currentToken = reversedTokens.pop();
				if (Numeric.isNumeric(currentToken)) {
					tokens.push(currentToken);
				} else if (opStack.empty()) {
					opStack.push((Operator)currentToken);
				} else {
					while (!opStack.empty()
						   && ((Operator)currentToken).isWeakerPrecedence(opStack.peek())) {
						tokens.push(opStack.pop());
					}
					if (currentToken.getValue().equals(")")) {
						opStack.pop();
					} else {
						opStack.push((Operator)currentToken);
					}
				}
			}
			while (!opStack.empty()) {
				tokens.push(opStack.pop());
			}
			fix = ExpressionFix.EFPostfix;
		} else {
			throw new IllegalArgumentException("Wrong Conversion");
		}
	}

	public long evalPostfix() {
		Stack<Token> reversedTokens = new Stack<>();

		while (!tokens.empty()) {
			reversedTokens.push(tokens.pop());
		}

		Stack<Numeric> numericStack = new Stack<>();
		Token currentToken = null;
		long op1 = 0;
		long op2 = 0;
		while (!reversedTokens.empty()) {
			currentToken = reversedTokens.pop();
			tokens.push(currentToken);
			if (Numeric.isNumeric(currentToken)) {
				numericStack.push((Numeric)currentToken);
			} else {
				if (currentToken.getType() == TokenType.TTBinaryOp) {
					op2 = numericStack.pop().getNumericValue();
				} else if (currentToken.getType() != TokenType.TTUnaryOp) {
					throw new IllegalArgumentException("Wrong Token Type");
				}
				op1 = numericStack.pop().getNumericValue();
				
				switch (currentToken.getValue()) {
				case "+":
					numericStack.push(new Numeric(op1 + op2));
					break;
				case "-":
					numericStack.push(new Numeric(op1 - op2));
					break;
				case "*":
					numericStack.push(new Numeric(op1 * op2));
					break;
				case "/":
					if (op2 == 0) {
						throw new ArithmeticException("divide by 0");
					}
					numericStack.push(new Numeric(op1 / op2));
					break;
				case "%":
					if (op2 == 0) {
						throw new ArithmeticException("divide by 0");
					}
					numericStack.push(new Numeric(op1 % op2));
					break;
				case "^":
					if (op1 == 0 && op2 < 0) {
						throw new ArithmeticException("divided by 0");
					}
					numericStack.push(new Numeric((long)Math.pow(op1, op2)));
					break;
				case "~":
					numericStack.push(new Numeric(-op1));
					break;
				default:
					throw new IllegalArgumentException("Wrong Operator");
				}
			}
		}

		if (numericStack.empty()) {
			throw new IllegalArgumentException("Wrong Expression");
		} 
		currentToken = numericStack.pop();
		return ((Numeric)currentToken).getNumericValue();
	}

	public void printTokens() {
		String equation = "";
		Stack<Token> reversedTokens = new Stack<>();

		while (!tokens.empty()) {
			reversedTokens.push(tokens.pop());
		}

		if (!reversedTokens.empty()) {
			equation = reversedTokens.peek().getValue();
			tokens.push(reversedTokens.pop());
		}

		while (!reversedTokens.empty()) {
			equation += " " + reversedTokens.peek().getValue();
			tokens.push(reversedTokens.pop());
		}
		
		System.out.println(equation);
	}
}

enum TokenType {
	TTNothing, TTOpenParen, TTCloseParen, TTUnaryOp, TTBinaryOp, TTNumeric;
}

abstract class Token {
	private String value;
	private TokenType type;

	public Token(String value, TokenType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public TokenType getType() {
		return type;
	}
}

class Numeric extends Token {
	private long numericValue;

	public Numeric(long numericValue) {
		super(Long.toString(numericValue), TokenType.TTNumeric);
		this.numericValue = numericValue;
	}
	
	public Numeric(String value) {
		super(value, TokenType.TTNumeric);
		numericValue = Long.parseLong(value);
	}

	public long getNumericValue() {
		return numericValue;
	}

	public static boolean isNumeric(Token token) {
		if (token.getType() == TokenType.TTNumeric) {
			return true;
		} else {
			return false;
		}
	}
}

enum OpAssociation {
	OALeft, OARight;
}

class Operator extends Token {
	private long opPrecedence;
	private OpAssociation opAssociation;

	public Operator(String value) {
		super(value, typeOfOp(value));
		switch (value) {
		case "+":
		case "-":
			opPrecedence = 10;
			opAssociation = OpAssociation.OALeft;
			break;
		case "*":
		case "%":
		case "/":
			opPrecedence = 20;
			opAssociation = OpAssociation.OALeft;
			break;
		case "~":
			opPrecedence = 30;
			opAssociation = OpAssociation.OARight;
			break;
		case "^":
			opPrecedence = 40;
			opAssociation = OpAssociation.OARight;
			break;
		case "(":
			opPrecedence = 50;
			opAssociation = OpAssociation.OALeft;
			break;
		case ")":
			opPrecedence = 0;
			opAssociation = OpAssociation.OALeft;
			break;
		default:
			throw new IllegalArgumentException("Wrong Token");
		}
	}

	public boolean isWeakerPrecedence(Operator stackOp) {
		if (stackOp.opPrecedence != 50) {
			if (opPrecedence < stackOp.opPrecedence) {
				return true;
			} else if (opPrecedence > stackOp.opPrecedence) {
				return false;
			} else {
				return opAssociation == OpAssociation.OALeft;
			}
		} else {
			return false;
		}
	}

	private static TokenType typeOfOp(String value) {
		switch (value) {
		case "+":
		case "-":
		case "*":
		case "%":
		case "/":
		case "^":
			return TokenType.TTBinaryOp;
		case "~":
			return TokenType.TTUnaryOp;
		case "(":
			return TokenType.TTOpenParen;
		case ")":
			return TokenType.TTCloseParen;
		default:
			throw new IllegalArgumentException("Wrong Token");
		}
	}
	
	public static boolean isOperator(Token token) {
		switch (token.getValue()) {
		case "+":
		case "-":
		case "*":
		case "%":
		case "/":
		case "^":
		case "~":
		case "(":
		case ")":
			return true;
		default:
			return false;
		}
	}
}
