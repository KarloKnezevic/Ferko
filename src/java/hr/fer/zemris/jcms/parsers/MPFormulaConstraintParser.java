package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.MPFormulaConstraint;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCBooleanValueNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCCumulativeGroupNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCGroupWithSTagNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCIntValueNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCIntegerNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCOperEqualNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCOperGreaterEqNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCOperGreaterNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCOperLessEqNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCOperLessNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCSubNode;
import hr.fer.zemris.jcms.parsers.mpfcs.MPFCSumNode;

import java.text.ParseException;

public class MPFormulaConstraintParser {

	public static MPFormulaConstraint parse(String line) throws ParseException {
		Tokenizer tok = new Tokenizer(line);
		MPFCBooleanValueNode node = recParseStateS(tok);
		if(tok.tokenType != TokenTypes.EOF) {
			throw new ParseException("Neočekivan token "+tok.objToken+".",0);
		}
		return new MPFormulaConstraint(node);
	}

	private static MPFCBooleanValueNode recParseStateS(Tokenizer tok) throws ParseException {
		MPFCIntValueNode nodeLeft = recParseStateB(tok);
		if(tok.tokenType!=TokenTypes.SIMPLE) {
			throw new ParseException("Ocekivao sam <, <=, > ili >=.",0);
		}
		Object operator = tok.objToken;
		tok.next();
		MPFCIntValueNode nodeRight = recParseStateB(tok);
		if(operator.equals(Tokenizer.TOKEN_EQ)) {
			return new MPFCOperEqualNode(nodeLeft, nodeRight);
		}
		if(operator.equals(Tokenizer.TOKEN_GOREQ)) {
			return new MPFCOperGreaterEqNode(nodeLeft, nodeRight);
		}
		if(operator.equals(Tokenizer.TOKEN_GREATER)) {
			return new MPFCOperGreaterNode(nodeLeft, nodeRight);
		}
		if(operator.equals(Tokenizer.TOKEN_LESS)) {
			return new MPFCOperLessNode(nodeLeft, nodeRight);
		}
		if(operator.equals(Tokenizer.TOKEN_LOREQ)) {
			return new MPFCOperLessEqNode(nodeLeft, nodeRight);
		}
		throw new ParseException("Neocekivani operator: "+operator+".",0);
	}

	private static MPFCIntValueNode recParseStateB(Tokenizer tok) throws ParseException {
		MPFCIntValueNode nodeLeft = recParseStateA(tok);
		while(true) {
			if(tok.tokenType!=TokenTypes.SIMPLE) {
				return nodeLeft;
			}
			Object operator = tok.objToken;
			if(operator.equals(Tokenizer.TOKEN_EQ) || operator.equals(Tokenizer.TOKEN_GOREQ) || operator.equals(Tokenizer.TOKEN_GREATER) || operator.equals(Tokenizer.TOKEN_LOREQ) || operator.equals(Tokenizer.TOKEN_LESS)) {
				return nodeLeft;
			}
			if(!operator.equals(Tokenizer.TOKEN_MINUS) && !operator.equals(Tokenizer.TOKEN_PLUS)) {
				throw new ParseException("Ocekivao sam + ili -.",0);
			}
			tok.next();
			MPFCIntValueNode nodeRight = recParseStateA(tok);
			if(operator.equals(Tokenizer.TOKEN_PLUS)) {
				nodeLeft = new MPFCSumNode(nodeLeft, nodeRight);
			} else if(operator.equals(Tokenizer.TOKEN_MINUS)) {
				nodeLeft = new MPFCSubNode(nodeLeft, nodeRight);
			} else {
				throw new ParseException("Neocekivana pogreska - ovdje nismo smjeli pasti!", 0);
			}
		}
	}

	private static MPFCIntValueNode recParseStateA(Tokenizer tok) throws ParseException {
		MPFCIntValueNode val;
		if(tok.tokenType==TokenTypes.GROUP) {
			val = new MPFCCumulativeGroupNode((String)tok.objToken);
			tok.next();
		} else if(tok.tokenType==TokenTypes.GROUP_WITH_STUDENT_TAG) {
			String[] elems = (String[])tok.objToken; 
			val = new MPFCGroupWithSTagNode(elems[0], elems[1]);
			tok.next();
		} else if(tok.tokenType==TokenTypes.INTEGER) {
			val = new MPFCIntegerNode((Integer)tok.objToken);
			tok.next();
		} else {
			throw new ParseException("Neocekivani token: "+tok.objToken+".",0);
		}
		return val;
	}

	private static enum TokenTypes {
		EOF,
		SIMPLE,
		INTEGER,
		INVALID,
		GROUP,
		GROUP_WITH_STUDENT_TAG
	}
	
	private static class Tokenizer {
		private char[] data;
		private int poc = 0;
		Object objToken;
		private boolean done;
		private TokenTypes tokenType;
		public static final Character TOKEN_EQ = Character.valueOf('=');
		public static final Character TOKEN_PLUS = Character.valueOf('+');
		public static final Character TOKEN_MINUS = Character.valueOf('-');
		public static final Character TOKEN_LESS = Character.valueOf('<');
		public static final String TOKEN_LOREQ = "<=";
		public static final Character TOKEN_GREATER = Character.valueOf('>');
		public static final String TOKEN_GOREQ = ">=";
		
		public Tokenizer(String line) {
			data = line.toCharArray();
			done = false;
			getToken();
		}
		
		public void next() {
			getToken();
		}
		
		private void getToken() {
			if(done) return;
			objToken = null;
			tokenType = TokenTypes.EOF;
			while(poc<data.length) {
				if(data[poc]==' ' || data[poc]=='\t') {
					poc++;
					continue;
				}
				break;
			}
			if(poc>=data.length) {
				done = true;
				return;
			}
			switch(data[poc]) {
			case '+': objToken = TOKEN_PLUS; tokenType=TokenTypes.SIMPLE; poc++; return;
			case '-': objToken = TOKEN_MINUS; tokenType=TokenTypes.SIMPLE; poc++; return;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9': procitajBroj(); return;
			case '=':	poc++;
						tokenType=TokenTypes.SIMPLE;
						objToken = TOKEN_EQ;
						return;
			case '<':	if(data[poc+1]=='=') {
							poc+=2;
							tokenType=TokenTypes.SIMPLE;
							objToken = TOKEN_LOREQ;
							return;
						}
						poc++;
						tokenType=TokenTypes.SIMPLE;
						objToken = TOKEN_LESS;
						return;
			case '>':	if(data[poc+1]=='=') {
							poc+=2;
							tokenType=TokenTypes.SIMPLE;
							objToken = TOKEN_GOREQ;
							return;
						}
						poc++;
						tokenType=TokenTypes.SIMPLE;
						objToken = TOKEN_GREATER;
						return;
			case '\"': procitajImeGrupe(); return;
			default: tokenType = TokenTypes.INVALID; objToken=null; poc++; return;
			}
		}

		private void procitajImeGrupe() {
			poc++;
			int pos = poc;
			while(poc<data.length && data[poc]!='\"') {
				poc++;
			}
			if(poc>=data.length) {
				tokenType = TokenTypes.INVALID; objToken=null; return;
			}
			String groupName = String.valueOf(data, pos, poc-pos);
			poc++;
			if(poc>=data.length || data[poc]!='.') {
				tokenType = TokenTypes.GROUP; objToken=groupName; return;
			}
			poc++;
			pos = poc;
			try {
				String tagName;
				if(data[poc]=='#') {
					poc++;
					tagName = "#";
				} else if(data[poc]=='\"') {
					poc++;
					pos = poc;
					while(data[poc]!='\"') {
						poc++;
					}
					tagName = String.valueOf(data, pos, poc-pos);
					poc++;
				} else {
					while(data[poc]=='_' || Character.isLetterOrDigit(data[poc])) {
						poc++;
					}
					tagName = String.valueOf(data, pos, poc-pos);
				}
				tokenType = TokenTypes.GROUP_WITH_STUDENT_TAG; 
				objToken = new String[] {groupName, tagName}; 
				return;
			} catch(Exception ex) {
				tokenType = TokenTypes.INVALID; objToken=null; return;
			}
		}
		
		private void procitajBroj() {
			int broj = data[poc]-'0';
			poc++;
			while(poc<data.length && data[poc]>='0' && data[poc]<='9') {
				broj*=10;
				broj += data[poc]-'0';
				poc++;
			}
			objToken = Integer.valueOf(broj);
			tokenType = TokenTypes.INTEGER;
		}
		
	}
}
