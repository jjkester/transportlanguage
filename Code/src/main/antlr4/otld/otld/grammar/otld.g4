grammar otld;

program     : city depot* track* industry* company; //This is how a program should be formatted

// Program setup rules
city        : 'City'ID';';
depot       : 'Begin''depot'';'(defwagon)*'End''depot'';';

deftrain    : 'Train'ID'accepts'CARGO';';
defwagon    : 'Wagon' ID 'accepts' CARGO ';';

industry    : 'Begin' 'industry' ';' (factory)* 'End' 'industry' ';';

factory     : 'Factory'ID'accepts'(CARGO',')*(CARGO)'produces'CARGO';'deffactory;
deffactory  : 'Begin''production'';'code+'Final product'ID';''End''production'';';

track       : 'Begin''track'';'defsignal*defwaypoint*'End''track'';';

defsignal   : 'Signal'ID'is'BOOLEAN';';
defwaypoint : 'Waypoint' ID ';' 'Begin' 'Waypoint' ';' code+ 'End' 'Waypoint' ';'; // define conditional

company     : 'Begin''company'';'code*'End''company'';'; // wraps main code

// Main program rules
code        : defcircle | ifcond | write | stop | load | transfer | transport | invert | unarymin;

//Conditionals
defcircle   : 'Begin' 'circle' ID ';' code+ 'End' 'circle' ';'; // define execution code for conditional
ifcond      : 'Approach' 'signal' ID ';' ifcondcase+ 'Pass' 'signal' ';';
ifcondcase  : 'Case' BOOLEAN ':' code ;

//Execute statements
input       : 'Ask control' STRING 'about' ('contents of' ID)|('status of' ID)';';
write       : 'Write' STRING ID 'to' 'journal' ';'; //print statement
stop        : ' Stop'';';

//Assign statements
load        : 'Load' (INTEGER|BOOLEAN|CHARACTER) 'into' 'wagon' ID';';
transfer    : 'Transfer' 'wagon' ID 'to' 'wagon' ID';';
transport   : 'Transport' ID(','ID)* 'to' 'factory' ID 'and' (('fully' 'load')|('set' 'signal')) ID';';
invert      : 'Switch' 'signal' ID';';
unarymin    : 'Turn' 'wagon' ID 'around'';';


// Tokens
BOOLEAN     : ('red' | 'green');
CARGO       : ('int'|'boolean'|'char');
INTEGER     : '-'?('0'|[1-9][0-9]*);
STRING      : '"' ~["]* ('"' '"' ~["]*)* '"';
CHARACTER   : '\''[a-zA-Z0-9]'\'';
ID          : [a-zA-Z][a-zA-Z0-9]*;

// ignore whitespace
WS : [ \t\n\r] -> skip;
COMMENT : '/*' .*? '*/' -> skip;
