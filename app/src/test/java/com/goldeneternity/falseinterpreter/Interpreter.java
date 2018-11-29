package com.goldeneternity.falseinterpreter;

import android.util.Log;

import org.junit.Test;

import java.sql.Array;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Illia on 29.10.2015.
 */
public class Interpreter {


    private class StackElement {

        public boolean isInteger;

        public Integer i = -100;
        public String f = "";

        public StackElement (Integer _i) {

            isInteger = true;
            i = _i;
        }
        public StackElement (String _f) {

            isInteger = false;
            f = _f;
        }
        public StackElement (Boolean b) {

            isInteger = true;
            i = (b?_true : _false).i;
        }


    }

    public static  StackElement _true;
    public static  StackElement _false;
    public static  StackElement _null;

    public static String code;
    public static String input;
    public static String output;
    public static String stackString;

    private ArrayDeque<StackElement> stack;

    private StackElement get (boolean destroy) {

        if (stack.size() == 0)
            return  _null;


        StackElement r = stack.getFirst();
        if (destroy)
            stack.pop();

        return r;
    }

    private void push (StackElement val) {

        stack.push(val);
    }

    private boolean isNumber (char c) {

        return  c >= '0' && c <= '9';
    }

    @Test
    public void test0 () throws Exception {

        code = "{USE READ} {USE FACTORIAL} r;!f;!.{help}";
        input = "10";
        output = "";
        inputDelta = 0;
        String s = Process();

        throw new Exception("Process: " + s +"|Code: "+code +"|Output: "+output);
    }


    @Test
    public void test1 () throws Exception {

        code = "1_[0[^$$'01->\\'9>~&]['0-\\10*+]#%]r: [r;!$0=~][]# %1 [\\$1_=~][*]# %.";
        input = "1 2 3 4 5 6 7 8 9 10 ";
        output = "";
        inputDelta = 0;
        String s = Process();

        throw new Exception("Process: " + s +"|Code: "+code +"|Output: "+output);
    }

    @Test
    public void test2 () throws Exception {

        code = "^a:[^$1_=~][$a;=~[,]?]#";
        input = "xababagalamaga";
        output = "";
        inputDelta = 0;
        String s = Process();

        throw new Exception("Process: " + s +"|Code: "+code +"|Output: "+output);
    }

    @Test
    public void test3 () throws Exception {

        code = "1_[^$1_=~][]#%[$1_=~][,]#";
        input = "asdfskaldf lol code ded";
        output = "";
        inputDelta = 0;
        String s = Process();

        throw new Exception("Process: " + s +"|Code: "+code +"|Output: "+output);
    }

    private Integer inputDelta = 0;

    private StackElement[] vars;

    public StackElement ReadNumber () {

        char c;
        Integer number = 0;
        Integer sign = 1;

        if (inputDelta >= input.length())
            return _null;

        do {


            c = input.charAt(inputDelta);
            inputDelta ++;
        } while (inputDelta < input.length() && !isNumber(c) && c != '-');

        inputDelta --;
        while (inputDelta < input.length()) {

            c = input.charAt(inputDelta);
            if (c == '-')
                sign = -1;

            if (isNumber(c)) {
                number = number * 10 + ((int) c - (int) '0');
            } else {
                return new StackElement(number * sign);
            }
            inputDelta++;
        }

        return new StackElement(number * sign);
    }

    public StackElement ReadChar () {

        if (inputDelta >= input.length())
            return new StackElement(-1);

        char c;
        c = input.charAt(inputDelta);
        inputDelta ++;
        return new StackElement((int) c);
    }

    private boolean isVar (char c) {

        return  c >= 'a' && c <= 'z';
    }

    private void ProcessComment (String s) {

        if (s.equals("HELP")) {

            output += "I WILL HELP YOU\n HAHAHA";
        }

        if (s.equals("USE READ")) {

            ProcessCode("[0[^$$'01->\\'9>~&]['0-\\10*+]#%]r:");
        }
        if (s.equals("USE FACTORIAL")) {

            ProcessCode("[1[\\$0=~][$@*\\1-\\]#%]f:");
        }

    }

    public String ProcessCode (String codeToProcess) {

        Integer index = 0;

        boolean isReadingNumber = false;
        Integer number = 0;

        Integer bracketsSign = 0;
        boolean isReadingFunction = false;
        String function = "";

        boolean isReadingString = false;
        String string = "";

        boolean isReadVar = false;
        Integer varIndex = -1;

        boolean isReadingChar = false;

        boolean isReadingComment = false;
        String comment = "";

        while (index < codeToProcess.length()) {

            char c = codeToProcess.charAt(index);

            if (isReadingComment) {

                if (c == '}') {

                    ProcessComment(comment);
                    comment = "";
                    isReadingComment = false;
                    index++;
                    continue;
                }

                comment += c;
                index++;
                continue;
            }

            if (isReadingFunction) {

                if (c == ']') {

                    bracketsSign--;
                    if (bracketsSign == 0) {

                        isReadingFunction = false;
                        push(new StackElement(function));
                        function = "";
                        bracketsSign = 0;
                        index++;
                        continue;
                    }

                    if (bracketsSign < 0)
                        return "Error: Wrong []brackets expression. At: " + index  + ".";

                }

                if (c == '[') {

                    bracketsSign++;
                }

                function += c;
                index++;
                continue;
            }

            if (c == '\"') {

                if (isReadingString) {

                    isReadingString = false;
                    output += string;
                    string = "";
                } else {

                    isReadingString = true;
                    string = "";
                }
                index ++;
                continue;
            }

            if (isReadingString) {

                string += c;
                index ++;
                continue;
            }

            if (isReadingChar) {

                push (new StackElement((int) c));
                isReadingChar = false;
                index ++;
                continue;
            }

            if (isVar(c)) {

                if (isReadVar)
                    return "Variable can only have a one-symbol name from 'a' to 'z'. At: " + index + ".";

                varIndex = (int) c - (int) 'a';
                isReadVar = true;

                index++;
                continue;
            }

            if (isNumber(c)) {
                isReadingNumber = true;
                number = number*10 + ((int)c - (int) '0');
            } else {

                if (isReadingNumber) {

                    isReadingNumber = false;
                    stack.push(new StackElement (number));
                    number = 0;
                }
            }


            StackElement a,b,p;
            switch (c) {

                case '+':

                    a = get(true); b = get (true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"+\". At: " + index + ".";
                    push (new StackElement (b.i + a.i));
                    break;
                case '-':

                    a = get(true); b = get (true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"-\". At: " + index + ".";
                    push (new StackElement (b.i - a.i));
                    break;
                case '*':

                    a = get(true); b = get (true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"*\". At: " + index + ".";
                    push (new StackElement (b.i * a.i));
                    break;
                case '/':

                    a = get(true); b = get (true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"/\". At: " + index + ".";
                    push (new StackElement (b.i / a.i));
                    break;
                case '_':

                    a = get(true);
                    if (!(a.isInteger))
                        return "Error: Parameter is not a number. Function: \"_\". At: " + index + ".";
                    push (new StackElement (-a.i));
                    break;

                case '=':

                    a = get(true); b = get (true);

                    if (a.isInteger != b.isInteger)
                        return "Error: Parameters have different types. At: " + index + ".";
                    push (new StackElement ((a.isInteger? a.i == b.i: a.f == b.f)));
                    break;

                case '>':

                    a = get(true); b = get (true);

                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \">\". At: " + index + ".";
                    push (new StackElement (a.i < b.i));
                    break;
                case '~':

                    a = get(true);

                    if (!a.isInteger && (a.i == 0 || a.i == -1))
                        return "Error: Parameter is not a boolean. Function: \"~\" (0 - true, -1 - false). At: " + index + ".";
                    push (new StackElement (-1 - a.i));
                    break;

                case '&':

                    a = get(true); b = get(true);

                    if ((!a.isInteger && (a.i == 0 || a.i == -1)) && (!b.isInteger && (b.i == 0 || b.i == -1)))
                        return "Error: Parameter is not a boolean. Function: \"&\" (0 - true, -1 - false). At: " + index + ".";
                    push (new StackElement ((a.i == 0) && (b.i == 0)));
                    break;
                case '|':

                    a = get(true); b = get(true);

                    if ((!a.isInteger && (a.i == 0 || a.i == -1)) && (!b.isInteger && (b.i == 0 || b.i == -1)))
                        return "Error: Parameter is not a boolean. Function: \"|\" (0 - true, -1 - false). At: " + index + ".";
                    push (new StackElement ((a.i == 0) || (b.i == 0)));
                    break;
                case '[':

                    isReadingFunction = true;
                    bracketsSign = 1;
                    break;
                case ']':
                    return "Error: Wrong []brackets expression. At: " + index + ".";

                case '$':

                    a = get(false);
                    push (a);
                    break;
                case '%':

                    get(true);
                    break;
                case '\\':

                    a = get(true);
                    b = get(true);

                    if (b.f == _null.f)
                        return "Error: Not enough parameters. Function: \"\\\". At: " + index + ".";

                    push (a);
                    push(b);
                    break;
                case '@':

                    a = get(true);
                    b = get(true);
                    p = get(true);

                    if (p.f == _null.f)
                        return "Error: Not enough parameters. Function: \"@\". At: " + index + ".";

                    push (b);
                    push (p);
                    push (a);
                    break;
                case 'ø':case 'O':

                    a = get(true);
                    b = a;

                    if (!a.isInteger)
                        return "Error: Parameter is not a number. Function: \"ø\\O\". At: "+index + ".";
                    a.i ++;
                    Deque <StackElement> d = stack.clone();

                    StackElement res = _null;
                    while (!d.isEmpty() && a.i > 0) {
                        res = d.pop();
                        a.i--;
                    }

                    if (a.i == 0 )
                        push(res);
                    else
                        return "Error: No such index \""+b.i+"\" in stack. Function: \"ø\\O\". At: "+index + ".";

                    break;

                case '!':

                    a = get(true);

                    if (a.isInteger || a.f == _null.f)
                        return "Error: Parameter is not a function. Function: \"!\". At: " + index + ".";

                    String resProcess = ProcessCode(a.f);
                    if (resProcess != "")
                        return resProcess + " Caused by \"["+a.f + "]\" At: "+ index + ".\n";

                    break;

                case '?':

                    a = get(true);
                    b = get(true);

                    if (a.isInteger || a.f == _null.f)
                        return "Error: Parameter is not a function. Function: \"?\". At: " + index + ".";
                    if (!(b.isInteger && (b.i == 0 || b.i == -1) ) )
                        return "Error: Parameter ("+b.i+") is not a boolean. Function: \"?\" (0 - true, -1 - false). At: " + index + ".";

                    if (b.i == 0) {

                        String resProcess1 = ProcessCode(a.f);

                        if (resProcess1 != "")
                            return resProcess1 + " Caused by \"[" + a.f + "]\" At: " + index + ".\n";
                    }

                    break;


                case '#':

                    StackElement a1 = get(true);
                    StackElement a2 = get(true);

                    if (a1.isInteger || a1.f == _null.f || a2.isInteger || a2.f == _null.f)
                        return "Error: Parameters are not a functions. Function: \"#\". At: " + index + ".";


                    Integer cycleCounter = 0;

                    do {
                        cycleCounter++;

                        if (cycleCounter >= 5000)
                            return "Error: Infinite cycle. At: "+ index + ".";

                        String resProcess2 = ProcessCode(a2.f);

                        if (resProcess2 != "")
                            return resProcess2 + " Caused by \"[" + a2.f + "]\" At: " + index + ".\n";

                        b = get(true);
                        if (!(b.isInteger && (b.i == 0 || b.i == -1)))
                            return "Error: Parameter is not a boolean. Function: \"#\" (0 - true, -1 - false). At: " + index + ".";

                        if (b.i == 0) {
                            String resProcess1 = ProcessCode(a1.f);

                            if (resProcess1 != "")
                                return resProcess1 + " Caused by \"[" + a1.f + "]\" At: " + index + ".\n";
                        }
                    } while (b.i == 0);



                    break;

                case '.':

                    a = get(true);

                    output += (a.isInteger?a.i : a.f);

                    break;

                case ',':

                    a = get(true);

                    if (!a.isInteger)
                        return "Error: Parameter is not a symbol. Function: \",\". At: "+index + ".";

                    output += (char) (int) a.i;

                    break;
                case '^':

                    push(ReadChar());

                    break;

                case ':':

                    a = get(true);


                    if (!isReadVar)
                        return "Error: No variables have found (use it like '1f:' - push 1; f := 1;). At: "+index + ".";

                    if (a.f == _null.f)
                        return "Error: Parameter is NULL. At: " + index + ".";

                    vars[varIndex] = a;

                    break;

                case ';':

                    if (!isReadVar)
                        return "Error: No variables have found (use it like 'f;' - push f;). At: "+index + ".";
                    push (vars[varIndex]);

                    break;


                case '\'':

                    isReadingChar = true;
                    break;
                case 'ß':case 'B':

                    output = "";
                    break;

                case '{':

                    isReadingComment = true;
                    comment = "";
                    break;

            }

            index ++;
            isReadVar = false;
        }
        if (isReadingFunction)
            return "Error: Wrong []brackets expression. At: the end.";

        if (isReadingNumber) {

            isReadingNumber = false;
            stack.push(new StackElement (number));
            number = 0;
        }
        if (isReadingString) {

            return "Error: Missing \" in string expression. At: the end.";
        }

        return "";
    }


    public String Process () {

        _true = new StackElement(0);
        _false = new StackElement(-1);
        _null = new StackElement("null");


        vars = new StackElement[26];
        for (int i = 0; i < vars.length; i++) {

            vars [i] = _null;
        }
        stack = new ArrayDeque<>();

        String res = ProcessCode (code);
        if (res != "")
            return res;

        stackString = "";
        StackElement q;
        while (!stack.isEmpty()) {
            q = get(true);
            stackString += (q.isInteger? q.i : "\""+q.f+"\"") + (stack.isEmpty()?". ":", ");
        }
        return stackString;
    }
}
