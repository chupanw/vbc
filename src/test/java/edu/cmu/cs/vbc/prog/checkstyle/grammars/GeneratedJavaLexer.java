// $ANTLR 2.7.7 (20060906): "java.g" -> "GeneratedJavaLexer.java"$

package edu.cmu.cs.vbc.prog.checkstyle.grammars;

import antlr.*;
import antlr.collections.impl.BitSet;

import java.io.InputStream;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Hashtable;

public class GeneratedJavaLexer extends antlr.CharScanner implements GeneratedJavaTokenTypes, TokenStream
 {


    // explicitly set tab width to 1 (default in ANTLR 2.7.1)
    // in ANTLR 2.7.2a2 the default has changed from 1 to 8
    public void tab()
    {
        setColumn( getColumn() + 1 );
    }

    private CommentListener mCommentListener = null;

    // TODO: Check visibility of this method one parsing is done in central
    // utility method
    public void setCommentListener(CommentListener aCommentListener)
    {
        mCommentListener = aCommentListener;
    }

    private boolean mTreatAssertAsKeyword = true;

    public void setTreatAssertAsKeyword(boolean aTreatAsKeyword)
    {
        mTreatAssertAsKeyword = aTreatAsKeyword;
    }

    private boolean mTreatEnumAsKeyword = true;

    public void setTreatEnumAsKeyword(boolean aTreatAsKeyword)
    {
        mTreatEnumAsKeyword = aTreatAsKeyword;
    }

public GeneratedJavaLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public GeneratedJavaLexer(Reader in) {
	this(new CharBuffer(in));
}
public GeneratedJavaLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public GeneratedJavaLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = true;
	setCaseSensitive(true);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("byte", this), new Integer(51));
	literals.put(new ANTLRHashString("public", this), new Integer(62));
	literals.put(new ANTLRHashString("case", this), new Integer(93));
	literals.put(new ANTLRHashString("short", this), new Integer(53));
	literals.put(new ANTLRHashString("break", this), new Integer(86));
	literals.put(new ANTLRHashString("while", this), new Integer(84));
	literals.put(new ANTLRHashString("new", this), new Integer(136));
	literals.put(new ANTLRHashString("instanceof", this), new Integer(121));
	literals.put(new ANTLRHashString("implements", this), new Integer(75));
	literals.put(new ANTLRHashString("synchronized", this), new Integer(67));
	literals.put(new ANTLRHashString("float", this), new Integer(55));
	literals.put(new ANTLRHashString("package", this), new Integer(44));
	literals.put(new ANTLRHashString("return", this), new Integer(88));
	literals.put(new ANTLRHashString("throw", this), new Integer(90));
	literals.put(new ANTLRHashString("null", this), new Integer(135));
	literals.put(new ANTLRHashString("protected", this), new Integer(63));
	literals.put(new ANTLRHashString("class", this), new Integer(69));
	literals.put(new ANTLRHashString("throws", this), new Integer(81));
	literals.put(new ANTLRHashString("do", this), new Integer(85));
	literals.put(new ANTLRHashString("strictfp", this), new Integer(41));
	literals.put(new ANTLRHashString("super", this), new Integer(79));
	literals.put(new ANTLRHashString("transient", this), new Integer(65));
	literals.put(new ANTLRHashString("native", this), new Integer(66));
	literals.put(new ANTLRHashString("interface", this), new Integer(71));
	literals.put(new ANTLRHashString("final", this), new Integer(39));
	literals.put(new ANTLRHashString("if", this), new Integer(83));
	literals.put(new ANTLRHashString("double", this), new Integer(57));
	literals.put(new ANTLRHashString("volatile", this), new Integer(68));
	literals.put(new ANTLRHashString("catch", this), new Integer(96));
	literals.put(new ANTLRHashString("try", this), new Integer(95));
	literals.put(new ANTLRHashString("int", this), new Integer(54));
	literals.put(new ANTLRHashString("for", this), new Integer(91));
	literals.put(new ANTLRHashString("extends", this), new Integer(70));
	literals.put(new ANTLRHashString("boolean", this), new Integer(50));
	literals.put(new ANTLRHashString("char", this), new Integer(52));
	literals.put(new ANTLRHashString("private", this), new Integer(61));
	literals.put(new ANTLRHashString("default", this), new Integer(94));
	literals.put(new ANTLRHashString("false", this), new Integer(134));
	literals.put(new ANTLRHashString("this", this), new Integer(78));
	literals.put(new ANTLRHashString("static", this), new Integer(64));
	literals.put(new ANTLRHashString("abstract", this), new Integer(40));
	literals.put(new ANTLRHashString("continue", this), new Integer(87));
	literals.put(new ANTLRHashString("finally", this), new Integer(97));
	literals.put(new ANTLRHashString("else", this), new Integer(92));
	literals.put(new ANTLRHashString("import", this), new Integer(46));
	literals.put(new ANTLRHashString("void", this), new Integer(49));
	literals.put(new ANTLRHashString("switch", this), new Integer(89));
	literals.put(new ANTLRHashString("true", this), new Integer(133));
	literals.put(new ANTLRHashString("long", this), new Integer(56));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '?':
				{
					mQUESTION(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mLPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mRPAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '[':
				{
					mLBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case ']':
				{
					mRBRACK(true);
					theRetToken=_returnToken;
					break;
				}
				case '{':
				{
					mLCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case '}':
				{
					mRCURLY(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '~':
				{
					mBNOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '@':
				{
					mAT(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\u000c':  case '\r':
				case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				case '\'':
				{
					mCHAR_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '"':
				{
					mSTRING_LITERAL(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':  case '0':  case '1':  case '2':
				case '3':  case '4':  case '5':  case '6':
				case '7':  case '8':  case '9':
				{
					mNUM_INT(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (LA(4)=='=')) {
						mBSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='=')) {
						mSR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (LA(3)=='>') && (true)) {
						mBSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (LA(3)=='=')) {
						mSL_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)==':') && (LA(2)==':')) {
						mDOUBLE_COLON(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (LA(2)=='=')) {
						mEQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (LA(2)=='=')) {
						mNOT_EQUAL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='=')) {
						mDIV_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='=')) {
						mPLUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (LA(2)=='+')) {
						mINC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='=')) {
						mMINUS_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='-')) {
						mDEC(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (LA(2)=='=')) {
						mSTAR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (LA(2)=='=')) {
						mMOD_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='>') && (true)) {
						mSR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (LA(2)=='=')) {
						mGE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='<') && (true)) {
						mSL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (LA(2)=='=')) {
						mLE(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (LA(2)=='>')) {
						mLAMBDA(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (LA(2)=='=')) {
						mBXOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='=')) {
						mBOR_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (LA(2)=='|')) {
						mLOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='=')) {
						mBAND_ASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (LA(2)=='&')) {
						mLAND(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='/')) {
						mSINGLE_LINE_COMMENT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (LA(2)=='*')) {
						mBLOCK_COMMENT_BEGIN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)==':') && (true)) {
						mCOLON(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='=') && (true)) {
						mASSIGN(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='!') && (true)) {
						mLNOT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='/') && (true)) {
						mDIV(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='+') && (true)) {
						mPLUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='-') && (true)) {
						mMINUS(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='*') && (true)) {
						mSTAR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='%') && (true)) {
						mMOD(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='>') && (true)) {
						mGT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='<') && (true)) {
						mLT(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='^') && (true)) {
						mBXOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='|') && (true)) {
						mBOR(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='&') && (true)) {
						mBAND(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1)))) {
						mIDENT(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mQUESTION(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LPAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRPAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RPAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LBRACK;
		int _saveIndex;
		
		match('[');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRBRACK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RBRACK;
		int _saveIndex;
		
		match(']');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LCURLY;
		int _saveIndex;
		
		match('{');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mRCURLY(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = RCURLY;
		int _saveIndex;
		
		match('}');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLON;
		int _saveIndex;
		
		match(':');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOUBLE_COLON(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOUBLE_COLON;
		int _saveIndex;
		
		match("::");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASSIGN;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQUAL;
		int _saveIndex;
		
		match("==");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LNOT;
		int _saveIndex;
		
		match('!');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBNOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BNOT;
		int _saveIndex;
		
		match('~');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQUAL;
		int _saveIndex;
		
		match("!=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDIV_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIV_ASSIGN;
		int _saveIndex;
		
		match("/=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS_ASSIGN;
		int _saveIndex;
		
		match("+=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mINC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INC;
		int _saveIndex;
		
		match("++");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		match('-');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS_ASSIGN;
		int _saveIndex;
		
		match("-=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDEC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DEC;
		int _saveIndex;
		
		match("--");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTAR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STAR_ASSIGN;
		int _saveIndex;
		
		match("*=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMOD_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MOD_ASSIGN;
		int _saveIndex;
		
		match("%=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR;
		int _saveIndex;
		
		match(">>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SR_ASSIGN;
		int _saveIndex;
		
		match(">>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR;
		int _saveIndex;
		
		match(">>>");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBSR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BSR_ASSIGN;
		int _saveIndex;
		
		match(">>>=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GE;
		int _saveIndex;
		
		match(">=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match(">");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL;
		int _saveIndex;
		
		match("<<");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSL_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SL_ASSIGN;
		int _saveIndex;
		
		match("<<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LE;
		int _saveIndex;
		
		match("<=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LT;
		int _saveIndex;
		
		match('<');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLAMBDA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LAMBDA;
		int _saveIndex;
		
		match("->");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR;
		int _saveIndex;
		
		match('^');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBXOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BXOR_ASSIGN;
		int _saveIndex;
		
		match("^=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBOR_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BOR_ASSIGN;
		int _saveIndex;
		
		match("|=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLOR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LOR;
		int _saveIndex;
		
		match("||");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND;
		int _saveIndex;
		
		match('&');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBAND_ASSIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BAND_ASSIGN;
		int _saveIndex;
		
		match("&=");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mLAND(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LAND;
		int _saveIndex;
		
		match("&&");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		int _cnt422=0;
		_loop422:
		do {
			switch ( LA(1)) {
			case ' ':
			{
				match(' ');
				break;
			}
			case '\t':
			{
				match('\t');
				break;
			}
			case '\u000c':
			{
				match('\f');
				break;
			}
			case '\n':  case '\r':
			{
				{
				if ((LA(1)=='\r') && (LA(2)=='\n') && (true) && (true)) {
					match("\r\n");
				}
				else if ((LA(1)=='\r') && (true) && (true) && (true)) {
					match('\r');
				}
				else if ((LA(1)=='\n')) {
					match('\n');
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
				if ( inputState.guessing==0 ) {
					newline();
				}
				break;
			}
			default:
			{
				if ( _cnt422>=1 ) { break _loop422; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
			}
			}
			_cnt422++;
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSINGLE_LINE_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SINGLE_LINE_COMMENT;
		int _saveIndex;
		Token content=null;
		
		match("//");
		if ( inputState.guessing==0 ) {
			mCommentListener.reportSingleLineComment("//", getLine(),
			getColumn() - 3);
		}
		mSINGLE_LINE_COMMENT_CONTENT(true);
		content=_returnToken;
		if ( inputState.guessing==0 ) {
			text.setLength(_begin); text.append(content.getText());
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSINGLE_LINE_COMMENT_CONTENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SINGLE_LINE_COMMENT_CONTENT;
		int _saveIndex;
		
		{
		_loop427:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				{
				match(_tokenSet_1);
				}
			}
			else {
				break _loop427;
			}
			
		} while (true);
		}
		{
		switch ( LA(1)) {
		case '\n':
		{
			match('\n');
			break;
		}
		case '\r':
		{
			match('\r');
			{
			if ((LA(1)=='\n')) {
				match('\n');
			}
			else {
			}
			
			}
			break;
		}
		default:
			{
			}
		}
		}
		if ( inputState.guessing==0 ) {
			newline();
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mBLOCK_COMMENT_BEGIN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLOCK_COMMENT_BEGIN;
		int _saveIndex;
		Token content=null;
		
		int startLine = -1;
		int startCol = -1;
		
		
		match("/*");
		if ( inputState.guessing==0 ) {
			startLine = getLine(); startCol = getColumn() - 3;
		}
		mBLOCK_COMMENT_CONTENT(true);
		content=_returnToken;
		match("*/");
		if ( inputState.guessing==0 ) {
			
			mCommentListener.reportBlockComment("/*", startLine, startCol,
			getLine(), getColumn() - 2);
			text.setLength(_begin); text.append(content.getText());
			
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBLOCK_COMMENT_CONTENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BLOCK_COMMENT_CONTENT;
		int _saveIndex;
		
		{
		_loop434:
		do {
			if ((LA(1)=='\r') && (LA(2)=='\n') && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && ((LA(4) >= '\u0000' && LA(4) <= '\ufffe'))) {
				match('\r');
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if (((LA(1)=='*') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')))&&( LA(2)!='/' )) {
				match('*');
			}
			else if ((LA(1)=='\r') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && ((LA(3) >= '\u0000' && LA(3) <= '\ufffe')) && (true)) {
				match('\r');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((LA(1)=='\n')) {
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((_tokenSet_2.member(LA(1)))) {
				{
				match(_tokenSet_2);
				}
			}
			else {
				break _loop434;
			}
			
		} while (true);
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCHAR_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CHAR_LITERAL;
		int _saveIndex;
		
		match('\'');
		{
		if ((LA(1)=='\\')) {
			mESC(false);
		}
		else if ((_tokenSet_3.member(LA(1)))) {
			matchNot('\'');
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		match('\'');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ESC;
		int _saveIndex;
		
		match('\\');
		{
		switch ( LA(1)) {
		case 'u':
		{
			{
			int _cnt443=0;
			_loop443:
			do {
				if ((LA(1)=='u')) {
					match('u');
				}
				else {
					if ( _cnt443>=1 ) { break _loop443; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt443++;
			} while (true);
			}
			{
			if ((LA(1)=='0') && (LA(2)=='0') && (LA(3)=='5') && (LA(4)=='C'||LA(4)=='c')) {
				match('0');
				match('0');
				match('5');
				{
				switch ( LA(1)) {
				case 'c':
				{
					match('c');
					break;
				}
				case 'C':
				{
					match('C');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				if ((LA(1)=='\\') && (LA(2)=='u') && (LA(3)=='0'||LA(3)=='u') && (LA(4)=='0'||LA(4)=='u')) {
					match('\\');
					{
					int _cnt448=0;
					_loop448:
					do {
						if ((LA(1)=='u')) {
							match('u');
						}
						else {
							if ( _cnt448>=1 ) { break _loop448; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
						}
						
						_cnt448++;
					} while (true);
					}
					match('0');
					match('0');
					match('5');
					{
					switch ( LA(1)) {
					case 'c':
					{
						match('c');
						break;
					}
					case 'C':
					{
						match('C');
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
				}
				else if ((LA(1)=='"'||LA(1)=='\''||LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='\\'||LA(1)=='b'||LA(1)=='f'||LA(1)=='n'||LA(1)=='r'||LA(1)=='t') && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
					mSTD_ESC(false);
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
			}
			else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_4.member(LA(3))) && (_tokenSet_4.member(LA(4)))) {
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
				mHEX_DIGIT(false);
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			break;
		}
		case '"':  case '\'':  case '0':  case '1':
		case '2':  case '3':  case '4':  case '5':
		case '6':  case '7':  case '\\':  case 'b':
		case 'f':  case 'n':  case 'r':  case 't':
		{
			mSTD_ESC(false);
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STRING_LITERAL;
		int _saveIndex;
		
		match('"');
		{
		_loop439:
		do {
			if ((LA(1)=='\\')) {
				mESC(false);
			}
			else if ((_tokenSet_5.member(LA(1)))) {
				matchNot('"');
			}
			else {
				break _loop439;
			}
			
		} while (true);
		}
		match('"');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSTD_ESC(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = STD_ESC;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'n':
		{
			match('n');
			break;
		}
		case 'r':
		{
			match('r');
			break;
		}
		case 't':
		{
			match('t');
			break;
		}
		case 'b':
		{
			match('b');
			break;
		}
		case 'f':
		{
			match('f');
			break;
		}
		case '"':
		{
			match('"');
			break;
		}
		case '\'':
		{
			match('\'');
			break;
		}
		case '\\':
		{
			match('\\');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		{
			{
			matchRange('0','3');
			}
			{
			if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
				{
				matchRange('0','7');
				}
				{
				if (((LA(1) >= '0' && LA(1) <= '7')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
					matchRange('0','7');
				}
				else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
				}
				else {
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				
				}
			}
			else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			break;
		}
		case '4':  case '5':  case '6':  case '7':
		{
			{
			matchRange('4','7');
			}
			{
			if (((LA(1) >= '0' && LA(1) <= '9')) && ((LA(2) >= '\u0000' && LA(2) <= '\ufffe')) && (true) && (true)) {
				{
				matchRange('0','9');
				}
			}
			else if (((LA(1) >= '\u0000' && LA(1) <= '\ufffe')) && (true) && (true) && (true)) {
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEX_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_DIGIT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			matchRange('0','9');
			break;
		}
		case 'A':  case 'B':  case 'C':  case 'D':
		case 'E':  case 'F':
		{
			matchRange('A','F');
			break;
		}
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':
		{
			matchRange('a','f');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBINARY_DIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BINARY_DIGIT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':
		{
			match('0');
			break;
		}
		case '1':
		{
			match('1');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mVOCAB(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VOCAB;
		int _saveIndex;
		
		matchRange('\3','\377');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mID_START(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ID_START;
		int _saveIndex;
		
		try {      // for error handling
			switch ( LA(1)) {
			case '_':
			{
				match('_');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			default:
				if ((_tokenSet_6.member(LA(1)))) {
					{
					if (!(Character.isJavaIdentifierStart(LA(1))))
					  throw new SemanticException("Character.isJavaIdentifierStart(LA(1))");
					{
					match(_tokenSet_6);
					}
					}
				}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
		}
		catch (SemanticException ex) {
			if (inputState.guessing==0) {
				
				throw new SemanticException(
				MessageFormat.format(
				"Unexpected character {0} in identifier",
				new Object[] {"0x" + Integer.toHexString(LA(1))}),
				getFilename(), getLine(), getColumn());
				
			} else {
				throw ex;
			}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mID_PART(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ID_PART;
		int _saveIndex;
		
		try {      // for error handling
			switch ( LA(1)) {
			case '_':
			{
				match('_');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			default:
				if ((_tokenSet_7.member(LA(1)))) {
					{
					if (!(Character.isJavaIdentifierPart(LA(1))))
					  throw new SemanticException("Character.isJavaIdentifierPart(LA(1))");
					{
					match(_tokenSet_7);
					}
					}
				}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
		}
		catch (SemanticException ex) {
			if (inputState.guessing==0) {
				
				throw new SemanticException(
				MessageFormat.format(
				"Unexpected character {0} in identifier",
				new Object[] {"0x" + Integer.toHexString(LA(1))}),
				getFilename(), getLine(), getColumn());
				
			} else {
				throw ex;
			}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENT;
		int _saveIndex;
		
		mID_START(false);
		{
		_loop471:
		do {
			if ((_tokenSet_8.member(LA(1)))) {
				mID_PART(false);
			}
			else {
				break _loop471;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			
						if (mTreatAssertAsKeyword && "assert".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
							_ttype = ASSERT;
						}
						if (mTreatEnumAsKeyword && "enum".equals(new String(text.getBuffer(),_begin,text.length()-_begin))) {
							_ttype = ENUM;
						}
			
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNUM_INT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NUM_INT;
		int _saveIndex;
		
		boolean synPredMatched482 = false;
		if (((LA(1)=='0') && (LA(2)=='X'||LA(2)=='x') && (_tokenSet_9.member(LA(3))) && (_tokenSet_10.member(LA(4))))) {
			int _m482 = mark();
			synPredMatched482 = true;
			inputState.guessing++;
			try {
				{
				mHEX_DOUBLE_LITERAL(false);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched482 = false;
			}
			rewind(_m482);
inputState.guessing--;
		}
		if ( synPredMatched482 ) {
			mHEX_DOUBLE_LITERAL(false);
			if ( inputState.guessing==0 ) {
				_ttype = NUM_DOUBLE;
			}
		}
		else {
			boolean synPredMatched484 = false;
			if (((LA(1)=='0') && (LA(2)=='X'||LA(2)=='x') && (_tokenSet_9.member(LA(3))) && (_tokenSet_10.member(LA(4))))) {
				int _m484 = mark();
				synPredMatched484 = true;
				inputState.guessing++;
				try {
					{
					mHEX_FLOAT_LITERAL(false);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched484 = false;
				}
				rewind(_m484);
inputState.guessing--;
			}
			if ( synPredMatched484 ) {
				mHEX_FLOAT_LITERAL(false);
				if ( inputState.guessing==0 ) {
					_ttype = NUM_FLOAT;
				}
			}
			else {
				boolean synPredMatched474 = false;
				if (((LA(1)=='.') && (LA(2)=='.') && (LA(3)=='.') && (true))) {
					int _m474 = mark();
					synPredMatched474 = true;
					inputState.guessing++;
					try {
						{
						mELLIPSIS(false);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched474 = false;
					}
					rewind(_m474);
inputState.guessing--;
				}
				if ( synPredMatched474 ) {
					mELLIPSIS(false);
					if ( inputState.guessing==0 ) {
						_ttype = ELLIPSIS;
					}
				}
				else {
					boolean synPredMatched478 = false;
					if (((LA(1)=='.'||LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9') && (LA(2)=='.'||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='D'||LA(2)=='E'||LA(2)=='_'||LA(2)=='d'||LA(2)=='e') && (true) && (true))) {
						int _m478 = mark();
						synPredMatched478 = true;
						inputState.guessing++;
						try {
							{
							mDOUBLE_LITERAL(false);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched478 = false;
						}
						rewind(_m478);
inputState.guessing--;
					}
					if ( synPredMatched478 ) {
						mDOUBLE_LITERAL(false);
						if ( inputState.guessing==0 ) {
							_ttype = NUM_DOUBLE;
						}
					}
					else {
						boolean synPredMatched480 = false;
						if (((LA(1)=='.'||LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9') && (LA(2)=='.'||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='E'||LA(2)=='F'||LA(2)=='_'||LA(2)=='e'||LA(2)=='f') && (true) && (true))) {
							int _m480 = mark();
							synPredMatched480 = true;
							inputState.guessing++;
							try {
								{
								mFLOAT_LITERAL(false);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched480 = false;
							}
							rewind(_m480);
inputState.guessing--;
						}
						if ( synPredMatched480 ) {
							mFLOAT_LITERAL(false);
							if ( inputState.guessing==0 ) {
								_ttype = NUM_FLOAT;
							}
						}
						else {
							boolean synPredMatched486 = false;
							if ((((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='B'||LA(2)=='L'||LA(2)=='X'||LA(2)=='_'||LA(2)=='b'||LA(2)=='l'||LA(2)=='x') && (true) && (true))) {
								int _m486 = mark();
								synPredMatched486 = true;
								inputState.guessing++;
								try {
									{
									mLONG_LITERAL(false);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched486 = false;
								}
								rewind(_m486);
inputState.guessing--;
							}
							if ( synPredMatched486 ) {
								mLONG_LITERAL(false);
								if ( inputState.guessing==0 ) {
									_ttype = NUM_LONG;
								}
							}
							else {
								boolean synPredMatched476 = false;
								if (((LA(1)=='.') && (true))) {
									int _m476 = mark();
									synPredMatched476 = true;
									inputState.guessing++;
									try {
										{
										mDOT(false);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched476 = false;
									}
									rewind(_m476);
inputState.guessing--;
								}
								if ( synPredMatched476 ) {
									mDOT(false);
									if ( inputState.guessing==0 ) {
										_ttype = DOT;
									}
								}
								else {
									boolean synPredMatched488 = false;
									if ((((LA(1) >= '0' && LA(1) <= '9')) && (true) && (true) && (true))) {
										int _m488 = mark();
										synPredMatched488 = true;
										inputState.guessing++;
										try {
											{
											mINT_LITERAL(false);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched488 = false;
										}
										rewind(_m488);
inputState.guessing--;
									}
									if ( synPredMatched488 ) {
										mINT_LITERAL(false);
										if ( inputState.guessing==0 ) {
											_ttype = NUM_INT;
										}
									}
									else {
										throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
									}
									}}}}}}}
									if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
										_token = makeToken(_ttype);
										_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
									}
									_returnToken = _token;
								}
								
	protected final void mELLIPSIS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ELLIPSIS;
		int _saveIndex;
		
		match("...");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mDOUBLE_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOUBLE_LITERAL;
		int _saveIndex;
		
		{
		boolean synPredMatched590 = false;
		if (((LA(1)=='.'||LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9') && (LA(2)=='.'||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_') && (LA(3)=='.'||LA(3)=='0'||LA(3)=='1'||LA(3)=='2'||LA(3)=='3'||LA(3)=='4'||LA(3)=='5'||LA(3)=='6'||LA(3)=='7'||LA(3)=='8'||LA(3)=='9'||LA(3)=='D'||LA(3)=='E'||LA(3)=='_'||LA(3)=='d'||LA(3)=='e') && (true))) {
			int _m590 = mark();
			synPredMatched590 = true;
			inputState.guessing++;
			try {
				{
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					{
					matchRange('0','9');
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case '_':
					{
						{
						_loop588:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
								matchRange('0','9');
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop588;
							}
							
						} while (true);
						}
						{
						matchRange('0','9');
						}
						break;
					}
					case '.':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				match('.');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched590 = false;
			}
			rewind(_m590);
inputState.guessing--;
		}
		if ( synPredMatched590 ) {
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				{
				{
				matchRange('0','9');
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case '_':
				{
					{
					_loop596:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
							matchRange('0','9');
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop596;
						}
						
					} while (true);
					}
					{
					matchRange('0','9');
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				match('.');
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					{
					matchRange('0','9');
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case '_':
					{
						{
						_loop602:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
								matchRange('0','9');
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop602;
							}
							
						} while (true);
						}
						{
						matchRange('0','9');
						}
						break;
					}
					case 'D':  case 'E':  case 'd':  case 'e':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case 'D':  case 'E':  case 'd':  case 'e':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			case '.':
			{
				match('.');
				{
				{
				matchRange('0','9');
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case '_':
				{
					{
					_loop608:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
							matchRange('0','9');
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop608;
						}
						
					} while (true);
					}
					{
					matchRange('0','9');
					}
					break;
				}
				case 'D':  case 'E':  case 'd':  case 'e':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='D'||LA(2)=='E'||LA(2)=='_'||LA(2)=='d'||LA(2)=='e') && (true) && (true)) {
			{
			{
			matchRange('0','9');
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case '_':
			{
				{
				_loop614:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
						matchRange('0','9');
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop614;
					}
					
				} while (true);
				}
				{
				matchRange('0','9');
				}
				break;
			}
			case 'D':  case 'E':  case 'd':  case 'e':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		{
		switch ( LA(1)) {
		case 'E':  case 'e':
		{
			mEXPONENT(false);
			break;
		}
		case 'D':  case 'd':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mFLOAT_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT_LITERAL;
		int _saveIndex;
		
		{
		boolean synPredMatched549 = false;
		if (((LA(1)=='.'||LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9') && (LA(2)=='.'||LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_') && (true) && (true))) {
			int _m549 = mark();
			synPredMatched549 = true;
			inputState.guessing++;
			try {
				{
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					{
					matchRange('0','9');
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case '_':
					{
						{
						_loop547:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
								matchRange('0','9');
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop547;
							}
							
						} while (true);
						}
						{
						matchRange('0','9');
						}
						break;
					}
					case '.':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				match('.');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched549 = false;
			}
			rewind(_m549);
inputState.guessing--;
		}
		if ( synPredMatched549 ) {
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				{
				{
				matchRange('0','9');
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case '_':
				{
					{
					_loop555:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
							matchRange('0','9');
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop555;
						}
						
					} while (true);
					}
					{
					matchRange('0','9');
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				match('.');
				{
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					{
					matchRange('0','9');
					}
					{
					if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
						{
						_loop561:
						do {
							if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
								matchRange('0','9');
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop561;
							}
							
						} while (true);
						}
						{
						matchRange('0','9');
						}
					}
					else {
					}
					
					}
				}
				else {
				}
				
				}
				break;
			}
			case '.':
			{
				match('.');
				{
				{
				matchRange('0','9');
				}
				{
				if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
					{
					_loop567:
					do {
						if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
							matchRange('0','9');
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop567;
						}
						
					} while (true);
					}
					{
					matchRange('0','9');
					}
				}
				else {
				}
				
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			{
			if ((LA(1)=='E'||LA(1)=='e')) {
				mEXPONENT(false);
			}
			else {
			}
			
			}
			{
			switch ( LA(1)) {
			case 'f':
			{
				match('f');
				break;
			}
			case 'F':
			{
				match('F');
				break;
			}
			default:
				{
				}
			}
			}
		}
		else if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='E'||LA(2)=='F'||LA(2)=='_'||LA(2)=='e'||LA(2)=='f') && (true) && (true)) {
			{
			{
			matchRange('0','9');
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case '_':
			{
				{
				_loop575:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
						matchRange('0','9');
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop575;
					}
					
				} while (true);
				}
				{
				matchRange('0','9');
				}
				break;
			}
			case 'E':  case 'F':  case 'e':  case 'f':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			}
			{
			switch ( LA(1)) {
			case 'E':  case 'e':
			{
				{
				mEXPONENT(false);
				{
				switch ( LA(1)) {
				case 'f':
				{
					match('f');
					break;
				}
				case 'F':
				{
					match('F');
					break;
				}
				default:
					{
					}
				}
				}
				}
				break;
			}
			case 'F':  case 'f':
			{
				{
				switch ( LA(1)) {
				case 'f':
				{
					match('f');
					break;
				}
				case 'F':
				{
					match('F');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEX_DOUBLE_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_DOUBLE_LITERAL;
		int _saveIndex;
		
		match('0');
		{
		switch ( LA(1)) {
		case 'x':
		{
			match('x');
			break;
		}
		case 'X':
		{
			match('X');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		boolean synPredMatched665 = false;
		if (((_tokenSet_9.member(LA(1))) && (_tokenSet_11.member(LA(2))) && (_tokenSet_10.member(LA(3))) && (_tokenSet_12.member(LA(4))))) {
			int _m665 = mark();
			synPredMatched665 = true;
			inputState.guessing++;
			try {
				{
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':
				{
					{
					mHEX_DIGIT(false);
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case 'A':  case 'B':
					case 'C':  case 'D':  case 'E':  case 'F':
					case '_':  case 'a':  case 'b':  case 'c':
					case 'd':  case 'e':  case 'f':
					{
						{
						_loop663:
						do {
							if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
								mHEX_DIGIT(false);
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop663;
							}
							
						} while (true);
						}
						{
						mHEX_DIGIT(false);
						}
						break;
					}
					case '.':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				match('.');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched665 = false;
			}
			rewind(_m665);
inputState.guessing--;
		}
		if ( synPredMatched665 ) {
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':
			{
				{
				{
				mHEX_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':
				{
					{
					_loop671:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop671;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				match('.');
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':
				{
					{
					mHEX_DIGIT(false);
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case 'A':  case 'B':
					case 'C':  case 'D':  case 'E':  case 'F':
					case '_':  case 'a':  case 'b':  case 'c':
					case 'd':  case 'e':  case 'f':
					{
						{
						_loop677:
						do {
							if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
								mHEX_DIGIT(false);
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop677;
							}
							
						} while (true);
						}
						{
						mHEX_DIGIT(false);
						}
						break;
					}
					case 'P':  case 'p':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case 'P':  case 'p':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			case '.':
			{
				match('.');
				{
				{
				mHEX_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':
				{
					{
					_loop683:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop683;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
					break;
				}
				case 'P':  case 'p':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_14.member(LA(2))) && (_tokenSet_15.member(LA(3))) && (_tokenSet_15.member(LA(4)))) {
			{
			{
			mHEX_DIGIT(false);
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case '_':  case 'a':  case 'b':  case 'c':
			case 'd':  case 'e':  case 'f':
			{
				{
				_loop689:
				do {
					if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
						mHEX_DIGIT(false);
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop689;
					}
					
				} while (true);
				}
				{
				mHEX_DIGIT(false);
				}
				break;
			}
			case 'P':  case 'p':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		mBINARY_EXPONENT(false);
		{
		switch ( LA(1)) {
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mHEX_FLOAT_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = HEX_FLOAT_LITERAL;
		int _saveIndex;
		
		match('0');
		{
		switch ( LA(1)) {
		case 'x':
		{
			match('x');
			break;
		}
		case 'X':
		{
			match('X');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		boolean synPredMatched628 = false;
		if (((_tokenSet_9.member(LA(1))) && (_tokenSet_11.member(LA(2))) && (_tokenSet_10.member(LA(3))) && (_tokenSet_12.member(LA(4))))) {
			int _m628 = mark();
			synPredMatched628 = true;
			inputState.guessing++;
			try {
				{
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':
				{
					{
					mHEX_DIGIT(false);
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case 'A':  case 'B':
					case 'C':  case 'D':  case 'E':  case 'F':
					case '_':  case 'a':  case 'b':  case 'c':
					case 'd':  case 'e':  case 'f':
					{
						{
						_loop626:
						do {
							if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
								mHEX_DIGIT(false);
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop626;
							}
							
						} while (true);
						}
						{
						mHEX_DIGIT(false);
						}
						break;
					}
					case '.':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				match('.');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched628 = false;
			}
			rewind(_m628);
inputState.guessing--;
		}
		if ( synPredMatched628 ) {
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':
			{
				{
				{
				mHEX_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':
				{
					{
					_loop634:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop634;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
					break;
				}
				case '.':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				match('.');
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case 'a':  case 'b':  case 'c':  case 'd':
				case 'e':  case 'f':
				{
					{
					mHEX_DIGIT(false);
					}
					{
					switch ( LA(1)) {
					case '0':  case '1':  case '2':  case '3':
					case '4':  case '5':  case '6':  case '7':
					case '8':  case '9':  case 'A':  case 'B':
					case 'C':  case 'D':  case 'E':  case 'F':
					case '_':  case 'a':  case 'b':  case 'c':
					case 'd':  case 'e':  case 'f':
					{
						{
						_loop640:
						do {
							if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
								mHEX_DIGIT(false);
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop640;
							}
							
						} while (true);
						}
						{
						mHEX_DIGIT(false);
						}
						break;
					}
					case 'P':  case 'p':
					{
						break;
					}
					default:
					{
						throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
					}
					}
					}
					break;
				}
				case 'P':  case 'p':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			case '.':
			{
				match('.');
				{
				{
				mHEX_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':
				{
					{
					_loop646:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop646;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
					break;
				}
				case 'P':  case 'p':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
		}
		else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_14.member(LA(2))) && (_tokenSet_15.member(LA(3))) && (true)) {
			{
			{
			mHEX_DIGIT(false);
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case 'A':  case 'B':
			case 'C':  case 'D':  case 'E':  case 'F':
			case '_':  case 'a':  case 'b':  case 'c':
			case 'd':  case 'e':  case 'f':
			{
				{
				_loop652:
				do {
					if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
						mHEX_DIGIT(false);
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop652;
					}
					
				} while (true);
				}
				{
				mHEX_DIGIT(false);
				}
				break;
			}
			case 'P':  case 'p':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		}
		mBINARY_EXPONENT(false);
		{
		switch ( LA(1)) {
		case 'f':
		{
			match('f');
			break;
		}
		case 'F':
		{
			match('F');
			break;
		}
		default:
			{
			}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mLONG_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = LONG_LITERAL;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':
		{
			match('0');
			{
			switch ( LA(1)) {
			case 'X':  case 'x':
			{
				{
				switch ( LA(1)) {
				case 'x':
				{
					match('x');
					break;
				}
				case 'X':
				{
					match('X');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				mHEX_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':  case 'A':  case 'B':
				case 'C':  case 'D':  case 'E':  case 'F':
				case '_':  case 'a':  case 'b':  case 'c':
				case 'd':  case 'e':  case 'f':
				{
					{
					_loop521:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop521;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
					break;
				}
				case 'L':  case 'l':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			case 'B':  case 'b':
			{
				{
				switch ( LA(1)) {
				case 'b':
				{
					match('b');
					break;
				}
				case 'B':
				{
					match('B');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				mBINARY_DIGIT(false);
				}
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '_':
				{
					{
					_loop527:
					do {
						if ((LA(1)=='0'||LA(1)=='1') && (LA(2)=='0'||LA(2)=='1'||LA(2)=='_')) {
							mBINARY_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop527;
						}
						
					} while (true);
					}
					{
					mBINARY_DIGIT(false);
					}
					break;
				}
				case 'L':  case 'l':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case 'L':  case '_':  case 'l':
			{
				{
				switch ( LA(1)) {
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '_':
				{
					{
					_loop532:
					do {
						if (((LA(1) >= '0' && LA(1) <= '7')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='_')) {
							{
							matchRange('0','7');
							}
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop532;
						}
						
					} while (true);
					}
					{
					matchRange('0','7');
					}
					break;
				}
				case 'L':  case 'l':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			break;
		}
		case '1':  case '2':  case '3':  case '4':
		case '5':  case '6':  case '7':  case '8':
		case '9':
		{
			{
			matchRange('1','9');
			}
			{
			switch ( LA(1)) {
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':  case '_':
			{
				{
				_loop537:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
						matchRange('0','9');
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop537;
					}
					
				} while (true);
				}
				{
				matchRange('0','9');
				}
				break;
			}
			case 'L':  case 'l':
			{
				break;
			}
			default:
			{
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		switch ( LA(1)) {
		case 'l':
		{
			match('l');
			break;
		}
		case 'L':
		{
			match('L');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mINT_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = INT_LITERAL;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '0':
		{
			match('0');
			{
			switch ( LA(1)) {
			case 'X':  case 'x':
			{
				{
				switch ( LA(1)) {
				case 'x':
				{
					match('x');
					break;
				}
				case 'X':
				{
					match('X');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				mHEX_DIGIT(false);
				}
				{
				if ((_tokenSet_13.member(LA(1)))) {
					{
					_loop496:
					do {
						if ((_tokenSet_4.member(LA(1))) && (_tokenSet_13.member(LA(2)))) {
							mHEX_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop496;
						}
						
					} while (true);
					}
					{
					mHEX_DIGIT(false);
					}
				}
				else {
				}
				
				}
				break;
			}
			case 'B':  case 'b':
			{
				{
				switch ( LA(1)) {
				case 'b':
				{
					match('b');
					break;
				}
				case 'B':
				{
					match('B');
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				mBINARY_DIGIT(false);
				}
				{
				if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='_')) {
					{
					_loop502:
					do {
						if ((LA(1)=='0'||LA(1)=='1') && (LA(2)=='0'||LA(2)=='1'||LA(2)=='_')) {
							mBINARY_DIGIT(false);
						}
						else if ((LA(1)=='_')) {
							match('_');
						}
						else {
							break _loop502;
						}
						
					} while (true);
					}
					{
					mBINARY_DIGIT(false);
					}
				}
				else {
				}
				
				}
				break;
			}
			default:
				{
					{
					if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='_')) {
						{
						_loop507:
						do {
							if (((LA(1) >= '0' && LA(1) <= '7')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='_')) {
								{
								matchRange('0','7');
								}
							}
							else if ((LA(1)=='_')) {
								match('_');
							}
							else {
								break _loop507;
							}
							
						} while (true);
						}
						{
						matchRange('0','7');
						}
					}
					else {
					}
					
					}
				}
			}
			}
			break;
		}
		case '1':  case '2':  case '3':  case '4':
		case '5':  case '6':  case '7':  case '8':
		case '9':
		{
			{
			matchRange('1','9');
			}
			{
			if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
				{
				_loop512:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
						matchRange('0','9');
					}
					else if ((LA(1)=='_')) {
						match('_');
					}
					else {
						break _loop512;
					}
					
				} while (true);
				}
				{
				matchRange('0','9');
				}
			}
			else {
			}
			
			}
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mEXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EXPONENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'e':
		{
			match('e');
			break;
		}
		case 'E':
		{
			match('E');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		mSIGNED_INTEGER(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mBINARY_EXPONENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = BINARY_EXPONENT;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'p':
		{
			match('p');
			break;
		}
		case 'P':
		{
			match('P');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		mSIGNED_INTEGER(false);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mSIGNED_INTEGER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SIGNED_INTEGER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case '+':
		{
			match('+');
			break;
		}
		case '-':
		{
			match('-');
			break;
		}
		case '0':  case '1':  case '2':  case '3':
		case '4':  case '5':  case '6':  case '7':
		case '8':  case '9':
		{
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		{
		matchRange('0','9');
		}
		{
		if ((LA(1)=='0'||LA(1)=='1'||LA(1)=='2'||LA(1)=='3'||LA(1)=='4'||LA(1)=='5'||LA(1)=='6'||LA(1)=='7'||LA(1)=='8'||LA(1)=='9'||LA(1)=='_')) {
			{
			_loop702:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9')) && (LA(2)=='0'||LA(2)=='1'||LA(2)=='2'||LA(2)=='3'||LA(2)=='4'||LA(2)=='5'||LA(2)=='6'||LA(2)=='7'||LA(2)=='8'||LA(2)=='9'||LA(2)=='_')) {
					matchRange('0','9');
				}
				else if ((LA(1)=='_')) {
					match('_');
				}
				else {
					break _loop702;
				}
				
			} while (true);
			}
			{
			matchRange('0','9');
			}
		}
		else {
		}
		
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	protected final void mFLOAT_SUFFIX(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT_SUFFIX;
		int _saveIndex;
		
		switch ( LA(1)) {
		case 'f':
		{
			match('f');
			break;
		}
		case 'F':
		{
			match('F');
			break;
		}
		case 'd':
		{
			match('d');
			break;
		}
		case 'D':
		{
			match('D');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = new long[2048];
		data[0]=107374168575L;
		data[1]=-8646911286564618242L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = new long[2048];
		data[0]=-9217L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = new long[2048];
		data[0]=-4398046520321L;
		for (int i = 1; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = new long[2048];
		data[0]=-549755813889L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = new long[2048];
		data[0]=-17179869185L;
		data[1]=-268435457L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = new long[2048];
		data[0]=38654691839L;
		data[1]=-8646911288712101890L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = new long[2048];
		data[0]=287948939829692927L;
		data[1]=-8646911288712101890L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = new long[2048];
		data[0]=287949008549169663L;
		data[1]=-8646911286564618242L;
		for (int i = 2; i<=1022; i++) { data[i]=-1L; }
		data[1023]=9223372036854775807L;
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = new long[1025];
		data[0]=288019269919178752L;
		data[1]=541165879422L;
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = new long[1025];
		data[0]=288019269919178752L;
		data[1]=282018290139262L;
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = new long[1025];
		data[0]=288019269919178752L;
		data[1]=543313363070L;
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = new long[1025];
		data[0]=288063250384289792L;
		data[1]=282018290139262L;
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=543313363070L;
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = new long[1025];
		data[0]=287948901175001088L;
		data[1]=282018290139262L;
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = new long[1025];
		data[0]=287992881640112128L;
		data[1]=282018290139262L;
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	
	}
