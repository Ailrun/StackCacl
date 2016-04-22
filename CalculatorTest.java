import java.io.*;
import java.util.*;

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
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input)
	{
	}

	private enum TokenType {Num, ParenOp, UnaryOp, BinaryOp}

	private class Token {
		private String content;
		private TokenType type;

		private Token(String c, TokenType t) {
			content = c;
			type = t;
		}

		public String getContent() {
			return content;
		}

		public TokenType getType() {
			return type;
		}

		private Pattern 

		public Token[] tokenize(String toToken) {
			
		}
	}
	
	private class OperatorComparator {
		private class Operator {
			private char symbol;
			private int priority;
		}

		static private final Operator[] defaultOperators = new Operator[];
	}
}
