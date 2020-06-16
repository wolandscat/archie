lexer grammar BaseLexer14;

// ADL keywords
SYM_ARCHETYPE            : [Aa][Rr][Cc][Hh][Ee][Tt][Yy][Pp][Ee] ;
SYM_TEMPLATE             : [Tt][Ee][Mm][Pp][Ll][Aa][Tt][Ee] ;
SYM_OPERATIONAL_TEMPLATE : [Oo][Pp][Ee][Rr][Aa][Tt][Ii][Oo][Nn][Aa][Ll]'_'[Tt][Ee][Mm][Pp][Ll][Aa][Tt][Ee] ;

SYM_SPECIALIZE  : (CMT_LINE |'\n')[Ss][Pp][Ee][Cc][Ii][Aa][Ll][Ii][SsZz][Ee] ;
SYM_LANGUAGE    : (CMT_LINE |'\n') [Ll][Aa][Nn][Gg][Uu][Aa][Gg][Ee] ;
SYM_DESCRIPTION : (CMT_LINE |'\n')[Dd][Ee][Ss][Cc][Rr][Ii][Pp][Tt][Ii][Oo][Nn] ;
SYM_DEFINITION  : (CMT_LINE |'\n')[Dd][Ee][Ff][Ii][Nn][Ii][Tt][Ii][Oo][Nn] ;
SYM_RULES       : (CMT_LINE |'\n')[Rr][Uu][Ll][Ee][Ss] ;
SYM_TERMINOLOGY : (CMT_LINE |'\n')[Tt][Ee][Rr][Mm][Ii][Nn][Oo][Ll][Oo][Gg][Yy] | '\n' [Oo][Nn][Tt][Oo][Ll][Oo][Gg][Yy];
SYM_ANNOTATIONS : (CMT_LINE |'\n')[Aa][Nn][Nn][Oo][Tt][Aa][Tt][Ii][Oo][Nn][Ss] ;

// CADL keywords
SYM_EXISTENCE   : [Ee][Xx][Ii][Ss][Tt][Ee][Nn][Cc][Ee] ;
SYM_OCCURRENCES : [Oo][Cc][Cc][Uu][Rr][Rr][Ee][Nn][Cc][Ee][Ss] ;
SYM_CARDINALITY : [Cc][Aa][Rr][Dd][Ii][Nn][Aa][Ll][Ii][Tt][Yy] ;
SYM_ORDERED     : [Oo][Rr][Dd][Ee][Rr][Ee][Dd] ;
SYM_UNORDERED   : [Uu][Nn][Oo][Rr][Dd][Ee][Rr][Ee][Dd] ;
SYM_UNIQUE      : [Uu][Nn][Ii][Qq][Uu][Ee] ;
SYM_USE_NODE    : [Uu][Ss][Ee][_][Nn][Oo][Dd][Ee] ;
SYM_USE_ARCHETYPE : [Uu][Ss][Ee][_][Aa][Rr][Cc][Hh][Ee][Tt][Yy][Pp][Ee] ;
SYM_ALLOW_ARCHETYPE : [Aa][Ll][Ll][Oo][Ww][_][Aa][Rr][Cc][Hh][Ee][Tt][Yy][Pp][Ee] ;
SYM_INCLUDE     : [Ii][Nn][Cc][Ll][Uu][Dd][Ee] ;
SYM_EXCLUDE     : [Ee][Xx][Cc][Ll][Uu][Dd][Ee] ;
SYM_AFTER       : [Aa][Ff][Tt][Ee][Rr] ;
SYM_BEFORE      : [Bb][Ee][Ff][Oo][Rr][Ee] ;
SYM_CLOSED      : [Cc][Ll][Oo][Ss][Ee][Dd] ;

SYM_THEN     : [Tt][Hh][Ee][Nn] ;
SYM_AND      : [Aa][Nn][Dd] | '∧';
SYM_OR       : [Oo][Rr] | '∨' ;
SYM_XOR      : [Xx][Oo][Rr] ;
SYM_NOT      : [Nn][Oo][Tt] | '!' | '∼' | '~' | '¬';
SYM_IMPLIES  : [Ii][Mm][Pp][Ll][Ii][Ee][Ss] | '®';
SYM_FOR_ALL  : [Ff][Oo][Rr][_][Aa][Ll][Ll] | '∀';
SYM_EXISTS   : [Ee][Xx][Ii][Ss][Tt][Ss] | '∃';

SYM_MATCHES : [Mm][Aa][Tt][Cc][Hh][Ee][Ss] | [Ii][Ss]'_'[Ii][Nn] | '∈';


//
// -------------------------- Parse Rules --------------------------
//

ADL_PATH          : ADL_ABSOLUTE_PATH | ADL_RELATIVE_PATH;
fragment ADL_ABSOLUTE_PATH : ('/' PATH_SEGMENT)+;
fragment ADL_RELATIVE_PATH : PATH_SEGMENT ('/' PATH_SEGMENT)+;

fragment PATH_SEGMENT      : ALPHA_LC_ID ('[' PATH_ATTRIBUTE ']')?;
fragment PATH_ATTRIBUTE    : AT_CODE | STRING | INTEGER | ARCHETYPE_REF;

//
//  ======================= Lexical rules ========================
//

// ---------- various ADL2 codes -------

ROOT_ID_CODE : 'id1' '.1'* ;
ID_CODE      : 'id' CODE_STR ;
AT_CODE      : 'at' CODE_STR ;
AC_CODE      : 'ac' CODE_STR ;
fragment CODE_STR : ( [0-9][0-9]*) ( '.' ('0' | [1-9][0-9]* ))* ;



//a
// regexp can only exist between {}. It can optionally have an assumed value, by adding ;"value"
CONTAINED_REGEXP: '{'WS* (SLASH_REGEXP | CARET_REGEXP) WS* (';' WS* STRING)? WS* '}';
fragment SLASH_REGEXP: '/' SLASH_REGEXP_CHAR+ '/';
fragment SLASH_REGEXP_CHAR: ~[/\n\r] | ESCAPE_SEQ | '\\/';

fragment CARET_REGEXP: '^' CARET_REGEXP_CHAR+ '^';
fragment CARET_REGEXP_CHAR: ~[^\n\r] | ESCAPE_SEQ | '\\^';

//
// -------------------------- Lexer patterns --------------------------
//

// ---------- whitespace & comments ----------

SYM_TEMPLATE_OVERLAY : H_CMT_LINE (WS|LINE)* SYM_TEMPLATE_OVERLAY_ONLY;
fragment H_CMT_LINE : '--------' '-'*? ('\n'|'\r''\n'|'\r')  ;  // special type of comment for splitting template overlays
fragment SYM_TEMPLATE_OVERLAY_ONLY     : [Tt][Ee][Mm][Pp][Ll][Aa][Tt][Ee]'_'[Oo][Vv][Ee][Rr][Ll][Aa][Yy] ;

WS         : [ \t\r]+    -> channel(HIDDEN) ;
LINE       : '\n'        -> channel(HIDDEN) ;     // increment line count
CMT_LINE   : '--' ~[\n\r]*? ('\n'|'\r''\n'|'\r')  -> skip ;  // (increment line count)

// ---------- ISO8601 Date/Time values ----------

// TODO: consider adding non-standard but unambiguous patterns like YEAR '-' ( MONTH | '??' ) '-' ( DAY | '??' )
ISO8601_DATE      : YEAR '-' MONTH ( '-' DAY )? ;
ISO8601_TIME      : HOUR SYM_COLON MINUTE ( SYM_COLON SECOND ( SYM_COMMA INTEGER )?)? ( TIMEZONE )? ;
ISO8601_DATE_TIME : YEAR '-' MONTH '-' DAY 'T' HOUR (SYM_COLON MINUTE (SYM_COLON SECOND ( SYM_COMMA DIGIT+ )?)?)? ( TIMEZONE )? ;
fragment TIMEZONE : 'Z' | ('+'|'-') HOUR_MIN ;   // hour offset, e.g. `+0930`, or else literal `Z` indicating +0000.
fragment YEAR     : [1-9][0-9]* ;
fragment MONTH    : ( [0][0-9] | [1][0-2] ) ;    // month in year
fragment DAY      : ( [012][0-9] | [3][0-2] ) ;  // day in month
fragment HOUR     : ( [01]?[0-9] | [2][0-3] ) ;  // hour in 24 hour clock
fragment MINUTE   : [0-5][0-9] ;                 // minutes
fragment HOUR_MIN : ( [01]?[0-9] | [2][0-3] ) [0-5][0-9] ;  // hour / minutes quad digit pattern
fragment SECOND   : [0-5][0-9] ;                 // seconds

// ISO8601 DURATION PnYnMnWnDTnnHnnMnn.nnnS
// here we allow a deviation from the standard to allow weeks to be // mixed in with the rest since this commonly occurs in medicine
// TODO: the following will incorrectly match just 'P'
ISO8601_DURATION : 'P' (DIGIT+ [yY])? (DIGIT+ [mM])? (DIGIT+ [wW])? (DIGIT+[dD])? ('T' (DIGIT+[hH])? (DIGIT+[mM])? (DIGIT+ ('.'DIGIT+)?[sS])?)? ;

// ------------------- special word symbols --------------
SYM_TRUE  : [Tt][Rr][Uu][Ee] ;
SYM_FALSE : [Ff][Aa][Ll][Ss][Ee] ;

// ---------------------- Identifiers ---------------------

ARCHETYPE_HRID      : ARCHETYPE_HRID_ROOT '.v' ARCHETYPE_VERSION_ID ;
ARCHETYPE_REF       : ARCHETYPE_HRID_ROOT '.v' INTEGER ( '.' DIGIT+ )* ;
fragment ARCHETYPE_HRID_ROOT : (NAMESPACE '::')? IDENTIFIER '-' IDENTIFIER '-' IDENTIFIER '.' LABEL ;
fragment ARCHETYPE_VERSION_ID: DIGIT+ ('.' DIGIT+ ('.' DIGIT+ ( ( '-rc' | '-alpha' ) ( '.' DIGIT+ )? )?)?)? ;
VERSION_ID          : DIGIT+ '.' DIGIT+ '.' DIGIT+ ( ( '-rc' | '-alpha' ) ( '.' DIGIT+ )? )? ;
fragment IDENTIFIER : ALPHA_CHAR WORD_CHAR* ;

// --------------------- composed primitive types -------------------

TERM_CODE_REF : '[' TERM_CODE_CHAR+ ( '(' TERM_CODE_CHAR+ ')' )? '::' TERM_CODE_CHAR+ ']' ;  // e.g. [ICD10AM(1998)::F23]; [ISO_639-1::en]
fragment TERM_CODE_CHAR: NAME_CHAR | '.';

VARIABLE_DECLARATION: SYM_VARIABLE_START RULE_IDENTIFIER SYM_COLON RULE_IDENTIFIER;
fragment RULE_IDENTIFIER: ALPHA_UC_ID | ALPHA_LC_ID;

// URIs - simple recogniser based on https://tools.ietf.org/html/rfc3986 and
// http://www.w3.org/Addressing/URL/5_URI_BNF.html
URI :  URN | URI_SCHEME SYM_COLON URI_HIER_PART ( '?' URI_QUERY+ )? ;

fragment URN: ASSIGNED_NAME RQ_COMPONENTS ('#' F_COMPONENT)?;
fragment ASSIGNED_NAME: 'urn:' NID ':' NSS;
fragment NID: ALPHANUM_CHAR (ALPHANUM_CHAR | '_')* ALPHANUM_CHAR;
fragment NSS: URI_XPALPHA (URI_XPALPHA | '/')*;
fragment RQ_COMPONENTS: ('?+' R_COMPONENT)? ('?=' Q_COMPONENT)?;
fragment R_COMPONENT: URI_XPALPHA (URI_XPALPHA | '/' | '?')*;
fragment Q_COMPONENT: URI_XPALPHA (URI_XPALPHA | '/' | '?')*;
fragment F_COMPONENT: URI_QUERY_FRAGMENT;

fragment URI_HIER_PART : ( ('//')? URI_AUTHORITY ) URI_PATH? ;
fragment URI_AUTHORITY : ( URI_USER '@' )? URI_HOST ( SYM_COLON NATURAL )? ;
fragment URI_HOST : IP_LITERAL | NAMESPACE ;
fragment URI_USER : (URI_XALPHA ) + ;
fragment URI_SCHEME : ALPHANUM_CHAR URI_XALPHA* ;
fragment URI_PATH   : '/' | ( '/' URI_XPALPHA+ )+ ('/')?;
fragment URI_QUERY : URI_QUERY_PARAM ('&' URI_QUERY_PARAM) ?;
fragment URI_QUERY_PARAM : URI_QUERY_FRAGMENT ('=' URI_QUERY_FRAGMENT)?;
fragment URI_QUERY_FRAGMENT  : URI_XALPHA+ ( '+' URI_XALPHA+ )* ;

fragment IP_LITERAL   : IPV4_LITERAL | IPV6_LITERAL ;
fragment IPV4_LITERAL : NATURAL '.' NATURAL '.' NATURAL '.' NATURAL ;
fragment IPV6_LITERAL : HEX_QUAD (SYM_COLON HEX_QUAD )* SYM_COLON SYM_COLON HEX_QUAD (SYM_COLON HEX_QUAD )* ;

fragment URI_XPALPHA : URI_XALPHA | '+' ;
fragment URI_XALPHA : ALPHANUM_CHAR | URI_SAFE | URI_EXTRA | URI_ESCAPE ;
fragment URI_SAFE   : [$@\\._-] ;
fragment URI_EXTRA  : [!*"'()] ;
fragment URI_ESCAPE : '%' HEX_DIGIT HEX_DIGIT ;
//fragment URI_RESERVED : [=;/#?: ] ;

fragment NATURAL  : [1-9][0-9]* ;
fragment HEX_QUAD : HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT ;

// According to IETF http://tools.ietf.org/html/rfc1034[RFC 1034] and http://tools.ietf.org/html/rfc1035[RFC 1035],
// as clarified by http://tools.ietf.org/html/rfc2181[RFC 2181] (section 11)
fragment NAMESPACE : LABEL ('.' LABEL)* ;
fragment LABEL : ALPHA_CHAR (NAME_CHAR|URI_ESCAPE)* ;

GUID : HEX_DIGIT+ '-' HEX_DIGIT+ '-' HEX_DIGIT+ '-' HEX_DIGIT+ '-' HEX_DIGIT+ ;
OID : DIGIT+ '.' DIGIT+ '.' DIGIT+ ('.' DIGIT+)+;

ALPHA_UC_ID : ALPHA_UCHAR WORD_CHAR* ;           // used for type ids
ALPHA_LC_ID : ALPHA_LCHAR WORD_CHAR* ;           // used for attribute / method ids

// --------------------- atomic primitive types -------------------

INTEGER : DIGIT+ E_SUFFIX? ;
REAL :    DIGIT+ '.' DIGIT+ E_SUFFIX? ;
fragment E_SUFFIX : [eE][+-]? DIGIT+ ;

STRING : '"' STRING_CHAR*? '"' ;
fragment STRING_CHAR : ~["\\] | ESCAPE_SEQ | UTF8CHAR ; // strings can be multi-line

CHARACTER : '\'' CHAR '\'' ;
fragment CHAR : ~['\\\r\n] | ESCAPE_SEQ | UTF8CHAR  ;

fragment ESCAPE_SEQ: '\\' ['"?abfnrtv\\] ;

// ------------------- character fragments ------------------

fragment NAME_CHAR     : WORD_CHAR | [._\-] ;
fragment WORD_CHAR     : ALPHANUM_CHAR | '_' ;
fragment ALPHANUM_CHAR : ALPHA_CHAR | DIGIT ;

fragment ALPHA_CHAR  : [a-zA-Z] ;
fragment ALPHA_UCHAR : [A-Z] ;
fragment ALPHA_LCHAR : [a-z] ;
fragment UTF8CHAR    : '\\u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT ;

fragment DIGIT     : [0-9] ;
fragment HEX_DIGIT : [0-9a-fA-F] ;


SYM_VARIABLE_START: '$';
SYM_ASSIGNMENT: '::=';

SYM_SEMICOLON: ';';
SYM_LT: '<';
SYM_GT: '>';
SYM_LE: '<=';
SYM_GE: '>=';
SYM_EQ: '=';
SYM_LEFT_PAREN: '(';
SYM_RIGHT_PAREN: ')';
SYM_COLON: ':';
SYM_COMMA: ',';
