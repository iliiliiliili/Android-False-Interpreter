package com.goldeneternity.falseinterpreter;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayDeque;
import java.util.Deque;


/**
 * Created by Illia on 29.10.2015.
 */
public class FalseInterpreter {


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

    public  StackElement _true;
    public  StackElement _false;
    public  StackElement _null;

    public String help;
    public String code;
    public String input;
    public String output;
    private String stackString;
    private Integer maxCycles;
    public boolean isOneFile = false;

    private ArrayDeque<StackElement> stack;

    private StackElement Get(boolean destroy) {

        if (stack.size() == 0)
            return  _null;


        StackElement r = stack.getFirst();
        if (destroy)
            stack.pop();

        return r;
    }

    private void Push (StackElement val) {

        stack.push(val);
    }

    private boolean IsNumber (char c) {

        return  c >= '0' && c <= '9';
    }


    private Integer inputDelta = 0;

    private StackElement[] vars;


    private StackElement ReadChar () {

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
            output += help;
            return;
        }

        if (s.equals("USE NO LIMITS")) {

            maxCycles = -1;
            return;
        }

        if (s.equals("USE ONE FILE")) {

            isOneFile = true;
            return;
        }

        if (s.equals("USE READ")) {

            ProcessCode("[0[^$$'01->\\'9>~&]['0-\\10*+]#%]r:");
            return;
        }

        if (s.equals("USE FACTORIAL")) {

            ProcessCode("[1[\\$0=~][$@*\\1-\\]#%]f:");
            return;
        }

        String[] ss = s.split(" ");

        if (ss.length >= 3 && ss[0].equals("SET") && !ss[1].equals("") && !ss[2].equals("") ) {

            String res = "";
            for (int q = 2; q < ss.length; q++)
                res += ss[q] + " ";

            SharedPreferences.Editor editor = MainActivity.sharedPref.edit();
            editor.putString("SET" + ss[1], res);
            editor.commit();

            output += "Setted {USE "+ ss[1] +"} for \""+ res + "\"\n";
            return;
        }

        if (ss.length == 2 && ss[0].equals("USE") && !ss[1].equals("") ) {

            String rs = MainActivity.sharedPref.getString("SET"+ss[1],"");
            if (!rs.equals(""))
                ProcessCode(rs);

            return;
        }

    }

    private String ProcessCode (String codeToProcess) {

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
        Integer commentsSign = 0;
        String comment = "";

        while (index < codeToProcess.length()) {

            char c = codeToProcess.charAt(index);

            if (isReadingComment) {

                if (c == '}') {
                    commentsSign --;
                    if (commentsSign == 0) {
                        ProcessComment(comment);
                        comment = "";
                        isReadingComment = false;
                        index++;
                        continue;
                    }
                    if (commentsSign < 0)
                        return "Error: Wrong {}comment expression. At: " + index  + ".";
                }

                if (c == '{')
                    commentsSign ++;

                comment += c;
                index++;
                continue;
            }

            if (isReadingFunction) {

                if (c == ']') {

                    bracketsSign--;
                    if (bracketsSign == 0) {

                        isReadingFunction = false;
                        Push(new StackElement(function));
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

                Push(new StackElement((int) c));
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

            if (IsNumber(c)) {
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

                    a = Get(true); b = Get(true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"+\". At: " + index + ".";
                    Push(new StackElement(b.i + a.i));
                    break;
                case '-':

                    a = Get(true); b = Get(true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"-\". At: " + index + ".";
                    Push(new StackElement(b.i - a.i));
                    break;
                case '*':

                    a = Get(true); b = Get(true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"*\". At: " + index + ".";
                    Push(new StackElement(b.i * a.i));
                    break;
                case '/':

                    a = Get(true); b = Get(true);
                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \"/\". At: " + index + ".";

                    if ( a.i == 0)
                        return "Error: Division by zero. Function: \"/\". At: " + index + ".";
                    Push(new StackElement(b.i / a.i));
                    break;
                case '_':

                    a = Get(true);
                    if (!(a.isInteger))
                        return "Error: Parameter is not a number. Function: \"_\". At: " + index + ".";
                    Push(new StackElement(-a.i));
                    break;

                case '=':

                    a = Get(true); b = Get(true);

                    if (a.isInteger != b.isInteger)
                        return "Error: Parameters have different types. At: " + index + ".";
                    Push(new StackElement((a.isInteger ? a.i == b.i : a.f == b.f)));
                    break;

                case '>':

                    a = Get(true); b = Get(true);

                    if (!(a.isInteger && b.isInteger))
                        return "Error: One of parameters is not a number. Function: \">\". At: " + index + ".";
                    Push(new StackElement(a.i < b.i));
                    break;
                case '~':

                    a = Get(true);

                    if (!a.isInteger && (a.i == 0 || a.i == -1))
                        return "Error: Parameter is not a boolean. Function: \"~\" (0 - true, -1 - false). At: " + index + ".";
                    Push(new StackElement(-1 - a.i));
                    break;

                case '&':

                    a = Get(true); b = Get(true);

                    if ((!a.isInteger && (a.i == 0 || a.i == -1)) && (!b.isInteger && (b.i == 0 || b.i == -1)))
                        return "Error: Parameter is not a boolean. Function: \"&\" (0 - true, -1 - false). At: " + index + ".";
                    Push(new StackElement((a.i == 0) && (b.i == 0)));
                    break;
                case '|':

                    a = Get(true); b = Get(true);

                    if ((!a.isInteger && (a.i == 0 || a.i == -1)) && (!b.isInteger && (b.i == 0 || b.i == -1)))
                        return "Error: Parameter is not a boolean. Function: \"|\" (0 - true, -1 - false). At: " + index + ".";
                    Push(new StackElement((a.i == 0) || (b.i == 0)));
                    break;
                case '[':

                    isReadingFunction = true;
                    bracketsSign = 1;
                    break;
                case ']':
                    return "Error: Wrong []brackets expression. At: " + index + ".";

                case '$':

                    a = Get(false);
                    Push(a);
                    break;
                case '%':

                    Get(true);
                    break;
                case '\\':

                    a = Get(true);
                    b = Get(true);

                    if (b.f == _null.f)
                        return "Error: Not enough parameters. Function: \"\\\". At: " + index + ".";

                    Push(a);
                    Push(b);
                    break;
                case '@':

                    a = Get(true);
                    b = Get(true);
                    p = Get(true);

                    if (p.f == _null.f)
                        return "Error: Not enough parameters. Function: \"@\". At: " + index + ".";

                    Push(p);
                    Push(a);
                    Push(b);
                    break;
                case 'ø':case 'O':

                    a = Get(true);
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
                        Push(res);
                    else
                        return "Error: No such index \""+b.i+"\" in stack. Function: \"ø\\O\". At: "+index + ".";

                    break;

                case '!':

                    a = Get(true);

                    if (a.isInteger || a.f == _null.f)
                        return "Error: Parameter is not a function. Function: \"!\". At: " + index + ".";

                    String resProcess = ProcessCode(a.f);
                    if (resProcess != "")
                        return resProcess + " Caused by \"["+a.f + "]\" At: "+ index + ".\n";

                    break;

                case '?':

                    a = Get(true);
                    b = Get(true);

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

                    StackElement a1 = Get(true);
                    StackElement a2 = Get(true);

                    if (a1.isInteger || a1.f == _null.f || a2.isInteger || a2.f == _null.f)
                        return "Error: Parameters are not a functions. Function: \"#\". At: " + index + ".";


                    Integer cycleCounter = 0;

                    do {
                        cycleCounter++;

                        if (maxCycles != -1)
                            if (cycleCounter >= maxCycles)
                               return "Error: Infinite cycle. At: "+ index + ".";

                        String resProcess2 = ProcessCode(a2.f);

                        if (resProcess2 != "")
                            return resProcess2 + " Caused by \"[" + a2.f + "]\" At: " + index + ".\n";

                        b = Get(true);
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

                    a = Get(true);

                    output += (a.isInteger?a.i : a.f);

                    break;

                case ',':

                    a = Get(true);

                    if (!a.isInteger)
                        return "Error: Parameter is not a symbol. Function: \",\". At: "+index + ".";

                    output += (char) (int) a.i;

                    break;
                case '^':

                    Push(ReadChar());

                    break;

                case ':':

                    a = Get(true);


                    if (!isReadVar)
                        return "Error: No variables have found (use it like '1f:' - Push 1; f := 1;). At: "+index + ".";

                    if (a.f == _null.f)
                        return "Error: Parameter is NULL. At: " + index + ".";

                    vars[varIndex] = a;

                    break;

                case ';':

                    if (!isReadVar)
                        return "Error: No variables have found (use it like 'f;' - Push f;). At: "+index + ".";
                    Push(vars[varIndex]);

                    break;


                case '\'':

                    isReadingChar = true;
                    break;
                case 'ß':case 'B':

                    input = "";
                    output = "";
                    break;

                case '{':

                    isReadingComment = true;
                    comment = "";
                    commentsSign = 1;
                    break;

            }

            index ++;
            isReadVar = false;
        }
        if (isReadingFunction)
            return "Error: Wrong []brackets expression. At: the end.";

        if (isReadingNumber) {

            stack.push(new StackElement (number));
        }
        if (isReadingString) {

            return "Error: Missing \" in string expression. At: the end.";
        }

        return "";
    }


    public void Process () {

        _true = new StackElement(0);
        _false = new StackElement(-1);
        _null = new StackElement("null");
        maxCycles = 5000;

        output = "";

        vars = new StackElement[26];
        for (int i = 0; i < vars.length; i++) {

            vars [i] = _null;
        }
        stack = new ArrayDeque<>();

        String res = ProcessCode (code);
        if (!res.equals("")) {

            stackString = "";
            StackElement q;
            while (!stack.isEmpty()) {
                q = Get(true);
                stackString += (q.isInteger? q.i : "\""+q.f+"\"") + (stack.isEmpty()?". ":", ");
            }
            output = res + "\nStack: " + stackString;
        }

        input = "";

        if (isOneFile) {

            input = output;
            output = "";
        }

    }
}
