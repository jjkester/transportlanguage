grammar otld;

program     : city depot* industry* track* company; //This is how a program should be formatted

// Program setup rules
city        : 'City'ID';';
depot       : 'Begin''depot'';'(defwagon)*'End''depot'';';

deftrain    : 'Train'ID'accepts'CARGO';'; //Shouldn't trains contain wagons?
defwagon    : 'Wagon' ID 'accepts' CARGO ';';

industry    : 'Begin' 'industry' ';' (factory)* 'End' 'industry' ';';

deffactory  : 'Factory'ID'accepts'(CARGO',')*(CARGO)'produces'CARGO';';
factory     : deffactory'Begin''production'';'Code*'Final product'ID';''End''production'';';

track       : 'Begin''track'';'defsignal*defwaypoint*'End''track'';';

defsignal   : 'Signal'ID'is'BOOLEAN';';
defwaypoint : 'Waypoint' ID 'Begin' 'Waypoint' ';' code 'End' 'Waypoint' ';'; // define conditional

// Main program rules
code        : defcircle | ifcond | write;

defcircle   : 'Begin' 'circle' ID ';' code 'Stop' ';' 'End' 'circle' ';'; // define execution code for conditional
ifcond : 'Approach' 'signal' ID ';' ('Case' 'red' ':' code )? ('Case' 'green' ':' code )? 'Pass' 'signal' ';';

write       : 'Write' '"' (CHARACTER|INTEGER)* '"' 'to' 'journal' ';'; //print statement

company     : 'Begin''company'';'code'End''company'';'; // wraps main code

// Tokens

CARGO       : ('int'|'boolean'|'char');
INTEGER     : '-'?('0'|[1-9][0-9]*);
BOOLEAN     : 'red' | 'green';
ID          : [a-z]CHARACTER*;
CHARACTER   : [a-zA-Z];

// ignore whitespace
WS : [ \t\n\r] -> skip;
