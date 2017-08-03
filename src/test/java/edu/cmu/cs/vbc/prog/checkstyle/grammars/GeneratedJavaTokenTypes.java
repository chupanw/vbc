// $ANTLR 2.7.7 (20060906): "java.g" -> "GeneratedJavaRecognizer.java"$

package edu.cmu.cs.vbc.prog.checkstyle.grammars;

public interface GeneratedJavaTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int BLOCK = 4;
	int MODIFIERS = 5;
	int OBJBLOCK = 6;
	int SLIST = 7;
	int CTOR_DEF = 8;
	int METHOD_DEF = 9;
	int VARIABLE_DEF = 10;
	int INSTANCE_INIT = 11;
	int STATIC_INIT = 12;
	int TYPE = 13;
	int CLASS_DEF = 14;
	int INTERFACE_DEF = 15;
	int PACKAGE_DEF = 16;
	int ARRAY_DECLARATOR = 17;
	int EXTENDS_CLAUSE = 18;
	int IMPLEMENTS_CLAUSE = 19;
	int PARAMETERS = 20;
	int PARAMETER_DEF = 21;
	int LABELED_STAT = 22;
	int TYPECAST = 23;
	int INDEX_OP = 24;
	int POST_INC = 25;
	int POST_DEC = 26;
	int METHOD_CALL = 27;
	int EXPR = 28;
	int ARRAY_INIT = 29;
	int IMPORT = 30;
	int UNARY_MINUS = 31;
	int UNARY_PLUS = 32;
	int CASE_GROUP = 33;
	int ELIST = 34;
	int FOR_INIT = 35;
	int FOR_CONDITION = 36;
	int FOR_ITERATOR = 37;
	int EMPTY_STAT = 38;
	int FINAL = 39;
	int ABSTRACT = 40;
	int STRICTFP = 41;
	int SUPER_CTOR_CALL = 42;
	int CTOR_CALL = 43;
	int LITERAL_package = 44;
	int SEMI = 45;
	int LITERAL_import = 46;
	int LBRACK = 47;
	int RBRACK = 48;
	int LITERAL_void = 49;
	int LITERAL_boolean = 50;
	int LITERAL_byte = 51;
	int LITERAL_char = 52;
	int LITERAL_short = 53;
	int LITERAL_int = 54;
	int LITERAL_float = 55;
	int LITERAL_long = 56;
	int LITERAL_double = 57;
	int IDENT = 58;
	int DOT = 59;
	int STAR = 60;
	int LITERAL_private = 61;
	int LITERAL_public = 62;
	int LITERAL_protected = 63;
	int LITERAL_static = 64;
	int LITERAL_transient = 65;
	int LITERAL_native = 66;
	int LITERAL_synchronized = 67;
	int LITERAL_volatile = 68;
	int LITERAL_class = 69;
	int LITERAL_extends = 70;
	int LITERAL_interface = 71;
	int LCURLY = 72;
	int RCURLY = 73;
	int COMMA = 74;
	int LITERAL_implements = 75;
	int LPAREN = 76;
	int RPAREN = 77;
	int LITERAL_this = 78;
	int LITERAL_super = 79;
	int ASSIGN = 80;
	int LITERAL_throws = 81;
	int COLON = 82;
	int LITERAL_if = 83;
	int LITERAL_while = 84;
	int LITERAL_do = 85;
	int LITERAL_break = 86;
	int LITERAL_continue = 87;
	int LITERAL_return = 88;
	int LITERAL_switch = 89;
	int LITERAL_throw = 90;
	int LITERAL_for = 91;
	int LITERAL_else = 92;
	int LITERAL_case = 93;
	int LITERAL_default = 94;
	int LITERAL_try = 95;
	int LITERAL_catch = 96;
	int LITERAL_finally = 97;
	int PLUS_ASSIGN = 98;
	int MINUS_ASSIGN = 99;
	int STAR_ASSIGN = 100;
	int DIV_ASSIGN = 101;
	int MOD_ASSIGN = 102;
	int SR_ASSIGN = 103;
	int BSR_ASSIGN = 104;
	int SL_ASSIGN = 105;
	int BAND_ASSIGN = 106;
	int BXOR_ASSIGN = 107;
	int BOR_ASSIGN = 108;
	int QUESTION = 109;
	int LOR = 110;
	int LAND = 111;
	int BOR = 112;
	int BXOR = 113;
	int BAND = 114;
	int NOT_EQUAL = 115;
	int EQUAL = 116;
	int LT = 117;
	int GT = 118;
	int LE = 119;
	int GE = 120;
	int LITERAL_instanceof = 121;
	int SL = 122;
	int SR = 123;
	int BSR = 124;
	int PLUS = 125;
	int MINUS = 126;
	int DIV = 127;
	int MOD = 128;
	int INC = 129;
	int DEC = 130;
	int BNOT = 131;
	int LNOT = 132;
	int LITERAL_true = 133;
	int LITERAL_false = 134;
	int LITERAL_null = 135;
	int LITERAL_new = 136;
	int NUM_INT = 137;
	int CHAR_LITERAL = 138;
	int STRING_LITERAL = 139;
	int NUM_FLOAT = 140;
	int NUM_LONG = 141;
	int NUM_DOUBLE = 142;
	int WS = 143;
	int SINGLE_LINE_COMMENT = 144;
	int BLOCK_COMMENT_BEGIN = 145;
	int ESC = 146;
	int HEX_DIGIT = 147;
	int VOCAB = 148;
	int EXPONENT = 149;
	int FLOAT_SUFFIX = 150;
	int ASSERT = 151;
	int STATIC_IMPORT = 152;
	int ENUM = 153;
	int ENUM_DEF = 154;
	int ENUM_CONSTANT_DEF = 155;
	int FOR_EACH_CLAUSE = 156;
	int ANNOTATION_DEF = 157;
	int ANNOTATIONS = 158;
	int ANNOTATION = 159;
	int ANNOTATION_MEMBER_VALUE_PAIR = 160;
	int ANNOTATION_FIELD_DEF = 161;
	int ANNOTATION_ARRAY_INIT = 162;
	int TYPE_ARGUMENTS = 163;
	int TYPE_ARGUMENT = 164;
	int TYPE_PARAMETERS = 165;
	int TYPE_PARAMETER = 166;
	int WILDCARD_TYPE = 167;
	int TYPE_UPPER_BOUNDS = 168;
	int TYPE_LOWER_BOUNDS = 169;
	int AT = 170;
	int ELLIPSIS = 171;
	int GENERIC_START = 172;
	int GENERIC_END = 173;
	int TYPE_EXTENSION_AND = 174;
	int DO_WHILE = 175;
	int RESOURCE_SPECIFICATION = 176;
	int RESOURCES = 177;
	int RESOURCE = 178;
	int DOUBLE_COLON = 179;
	int METHOD_REF = 180;
	int LAMBDA = 181;
	int BLOCK_COMMENT_END = 182;
	int COMMENT_CONTENT = 183;
	int SINGLE_LINE_COMMENT_CONTENT = 184;
	int BLOCK_COMMENT_CONTENT = 185;
	int STD_ESC = 186;
	int BINARY_DIGIT = 187;
	int ID_START = 188;
	int ID_PART = 189;
	int INT_LITERAL = 190;
	int LONG_LITERAL = 191;
	int FLOAT_LITERAL = 192;
	int DOUBLE_LITERAL = 193;
	int HEX_FLOAT_LITERAL = 194;
	int HEX_DOUBLE_LITERAL = 195;
	int SIGNED_INTEGER = 196;
	int BINARY_EXPONENT = 197;
}
