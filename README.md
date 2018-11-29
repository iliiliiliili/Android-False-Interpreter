# Android-False-Interpreter

This is an interpreter for a FALSE language.
FALSE is an esoteric programming language
You can read more about it [here](http://strlen.com/false-language).

FALSE uses stack as memory.
Each operator takes parameters from a stack and pushes result to it.
1. Elementary functions:
"+"     "-"     "*"     "/"     "\_"
These function as usual. "\_" is the unary minus.
"="     ">"
These result in 0 (false) or -1 (true)
Unequal is "=\~", and smaller than etc. can be made by swapping arguments
And/or using "\~"
```
Example:        a;1_=~          { a not equals -1 }
```
"&"     "|"     "~"
"and", "or" and "not", as usual.
```
Example:        a;0>a;99>~&     { (a greater than 0) and (a less than 100) }
```
2. Values:
Values are either integers like discussed before ("1", "100" etc.), or characters precede by a quote: 'A (equals 65).
3. Global variables:
Variables to store values are less needed in FALSE than in other languages.
In FALSE they are used mostly for functions, explained below.
A variable is a character "a" to "z" (just these).
":" is the assignment function, and ";" is contrary: it gets the variable's value:
```
1a:     { a:=1 }
a;1+b:  { b:=a+1 }
```

I.e: "a;" is used where in other languages you would just write "a"
4. Functions:
A FALSE lambda function is a piece of code between []. For example:

[1+]

is a function that adds 1 to it's argument. A function is really defined by what it takes from the stack (in this case the first arg to "+"), and what it puts back, just like builtin functions. Note that FALSE lambda functions are not restricted to just one return value.

What a [] expression really does, is push the function. this means in practise that it can be given to yet another function as argument etc., just like in functional languages. The symbol "!" is called "apply", and applies a function to it's arguments, for example:

2[1+]!

would result in "3".
This wouldn't make much sense, since what you really want is define the function once, and then use it all-over. this is easy:

[1+]i:

this defines the function "i" (actually, it assigns the function to "i"), so that it can be used simply by applying "i" to it's arguments:

2i;!
5. Stack functions:
"$"     (x->x,x) duplicate topmost stackitem
"%"     (x->)    delete topmost stack item
"\"     (x1,x2->x2,x1)   swap to topmost stack-items.
"@"     (x,x1,x2->x1,x2,x)       rotate 3rd stack item to top.
"O"or"o" (n->x)  copy n-th item to top (0o equals $)

```
1$              equals          1 1
1 2%            equals          1
1 2\            equals          2 1
1 2 3@          equals          2 3 1
7 8 9 2o        equals          7 8 9 7
```
6. Control structure:
FALSE only has an IF and a WHILE.
If is "?", and looks like this: (bool,fun-). Example:

```
a;1=["hello!"]?         { if a=1 then print "hello!" }
```

The first argument is a boolean value, the second the lambda function to be executed (see below for "") there's no "else", so you'll have to mimic this with a second "?". This can be easily done by copying the truthvalue:

```
a;1=$["true"]?~["false"]?
```

After the first "?" (wether it's executed or not), a copy of the truthvalue is still on the stack, and we negate it for the else part. Beware that if the first "if" needs arguments on the stack from before the boolean expression, it's top is still the truthvalue.

While is a "#", and gets two lambda functions as args, one that results in a boolean, and the second as body:

```
[a;1=][2f;!]#           { while a=1 do f(2) }
```
Note that with while, if and lambda's, you can build virtually any other control structure.

7. Input/Output:

- strings printing: strings simply print themselves

```
"Hello, World!"
```

- integers: "." prints the topmost stack item as integer value:

```
123.            { prints string "123" on console }
```

- characters: ","

```
65,             { prints "A" }
```

- reading a character from stdin: "^"

```
^               { top stack is char read }
```

- flush: "B" or "?"
"?" flushes both input and output.
8. Libraries:
In this version of FALSE you can use libraries. Library is a code that you can execute by writing a special comment:
{HELP} - prints help
{USE READ} - implements r function that reads an integer from an input. It stops when a read char is not a number and "eats" first not-number symbol.
```
Example: {USE READ} 1_[r;!$0=~][]# %1 [\$1_=~][*]# %.
```
Prints a multiply of a non-zero numbers from input.
{USE FACTORIAL} - implements f function that takes a number and returns it's factorial.
```
Example: {USE READ} {USE FACTORIAL} r;!f;!.
```
Prints a factorial of an input number.
You can set and use your own libraries by executing {SET NAME CODE}.
```
Example: {SET FALSE "Best language"}
```
After that, every execution of {USE FALSE} will print "Best language".
{USE NO LIMITS} - sets cycle execution limit for infinity (default is 5000 executions of cycle, then error).
9. All functions
```
syntax:         pops:           pushes:         example:

{comment}       -               -                       { this is a comment }
[code]          -               function        [1+]    { (lambda (x) (+ x 1)) }
a .. z          -               varadr          a       { use a: or a; }
integer         -               value           1
'char           -               value           'A      { 65 }
:               n,varadr        -               1a:     { a:=1 }
;               varadr          varvalue        a;      { a }
!               function        -               f;!     { f() }

+               n1,n1           n1+n2           1 2+    { 1+2 }
-               n1,n2           n1-n2           1 2-
*               n1,n2           n1*n2           1 2*
/               n1,n2           n1/n2           1 2/
_               n               -n              1_      { -1 }

=               n1,n1           n1=n2           1 2=~   { 1 not equeals 2 }
>               n1,n2           n1>n2           1 2>

&               n1,n2           n1 and n2       1 2&    { 1 and 2 }
|               n1,n2           n1 or n2        1 2|
~               n               not n           0~      { -1,TRUE }

$               n               n,n             1$      { dupl. top stack }
%               n               -               1%      { del. top stack }
\               n1,n2           n2,n1           1 2\    { swap }
@               n,n1,n2         n1,n2,n         1 2 3@  { rot }
o or O  n       v               1 2 1o  { pick }
?               bool,fun        -               a;2=[1f;!]? { if a=2 then f(1) }
#               boolf,fun       -               1[$100>~][1+]# { while 100 >= a do a:=a+1 }
.               n               -               1.      { printnum(1) }
"string"        -               -               "hi!"   { printstr("hi!") }
,               ch              -               10,     { putc(10) }
^               -               ch              ^       { getc() }
? or B  -               -               ?       { flush() }
```
