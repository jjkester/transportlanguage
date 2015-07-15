grammar otld;

// First rule - whole program
program     : city depot? track? industry? company;

// City - program name
city        : 'City' ID ';';

// Depot - variables
depot       : 'Begin' 'depot' ';' (defwagon)* 'End' 'depot' ';';

deftrain    : 'Train' ID 'accepts' CARGO ';';
defwagon    : 'Wagon' ID 'accepts' CARGO ';';

// Industry - functions
industry    : 'Begin' 'industry' ';' (factory)* 'End' 'industry' ';';

factory     : 'Factory' ID 'accepts' (CARGO ',')* CARGO 'produces' CARGO ';' deffactory;
deffactory  : 'Begin' 'production' ';' code+ 'Final product' ID ';' 'End' 'production' ';';

// Track - signals (for conditionals) and waypoints (for loops)
track       : 'Begin' 'track' ';' defsignal* defwaypoint* 'End' 'track' ';';

defsignal   : 'Signal' ID 'is' BOOLEAN ';';
defwaypoint : 'Waypoint' ID ';' 'Begin' 'waypoint' ';' code+ 'End' 'waypoint' ';';

// Company - program code
company     : 'Begin' 'company' ';' code* 'End' 'company' ';';

// Main program rules
code        : defcircle | ifcond | input | write | stop | load | transfer | transport | invert | unarymin;

// Loop
defcircle   : 'Begin' 'circle' ID ';' code+ 'End' 'circle' ';';

// Conditional
ifcond      : 'Approach' 'signal' ID ';' ifcondcase+ 'Pass' 'signal' ';';
ifcondcase  : 'Case' BOOLEAN ':' code ;

// Break
stop        : ' Stop' ';';

// Input and output
input       : 'Ask control' STRING 'about' (('contents of' ID) | ('status of' ID)) ';';
write       : 'Write' STRING ID 'to' 'journal' ';'; //print statement

// Assignments
load        : 'Load' (INTEGER|BOOLEAN|CHARACTER) 'into' 'wagon' ID ';';
transfer    : 'Transfer' 'wagon' ID 'to' 'wagon' ID ';';

// Arithmetic operations and function calls
transport   : 'Transport' ID (',' ID)* 'to' 'factory' ID 'and' (('fully' 'load') | ('set' 'signal')) ID ';';
invert      : 'Switch' 'signal' ID ';';
unarymin    : 'Turn' 'wagon' ID 'around' ';';

// Tokens: types
BOOLEAN     : ('red' | 'green');
CARGO       : ('int'|'boolean'|'char');
INTEGER     : '-'?('0'|[1-9][0-9]*);
STRING      : '"' ~["]* ('"' '"' ~["]*)* '"';
CHARACTER   : '\''[a-zA-Z0-9]'\'';

// Tokens: identifiers
ID          : [a-zA-Z][a-zA-Z0-9]*;

// Tokens: ignored (whitespace and comments)
WS : [ \t\n\r] -> skip;
COMMENT : '/*' .*? '*/' -> skip;
