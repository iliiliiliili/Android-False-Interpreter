package com.goldeneternity.falseinterpreter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {


    private Snackbar snackbarStatic;

    public static SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        EditText codeEdit = (EditText) findViewById(R.id.editText);
        codeEdit.setText(sharedPref.getString("code",getString(R.string.start_code)));

        ((EditText) findViewById(R.id.editText2)).setText(sharedPref.getString("input",""));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (snackbarStatic != null) {

                    return;
                }


                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("code", ((EditText) findViewById(R.id.editText)).getText().toString());
                editor.putString("input", ((EditText) findViewById(R.id.editText2)).getText().toString());
                editor.commit();

                snackbarStatic = Snackbar.make(view, "Running...", Snackbar.LENGTH_LONG);
                snackbarStatic.show();
                snackbarStatic.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {

                        snackbarStatic = null;
                    }
                });

                snackbarStatic.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar snackbar) {


                        Runnable thread = new Runnable() {
                            @Override
                            public void run() {
                                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);

                        FalseInterpreter falseInterpreter = new FalseInterpreter();
                        falseInterpreter.help = "This is an interpreter for a FALSE language.\n" +
                                "You can read more about it on http://strlen.com/false-language.\n" +
                                "FALSE uses stack as memory.\n" +
                                "Each operator takes parameters from a stack and pushes result to it.\n" +
                                "1. Elementary functions:\n" +
                                "\"+\"\t\"-\"\t\"*\"\t\"/\"\t\"_\"\n" +
                                "These function as usual. \"_\" is the unary minus.\n" +
                                "\n" +
                                "\"=\"\t\">\"\n" +
                                "These result in 0 (false) or -1 (true)\n" +
                                "Unequal is \"=~\", and smaller than etc. can be made by swapping arguments\n" +
                                "And/or using \"~\"\n" +
                                "Example:\ta;1_=~\t\t{ a not equals -1 }\n" +
                                "\"&\"\t\"|\"\t\"~\"\n" +
                                "\"and\", \"or\" and \"not\", as usual.\n" +
                                "Example:\ta;0>a;99>~&\t{ (a greater than 0) and (a less than 100) }\n" +
                                "2. Values:\n" +
                                "Values are either integers like discussed before (\"1\", \"100\" etc.), or characters precede by a quote: 'A (equals 65).\n" +
                                "3. Global variables:\n" +
                                "Variables to store values are less needed in FALSE than in other languages.\n" +
                                "In FALSE they are used mostly for functions, explained below.\n" +
                                "A variable is a character \"a\" to \"z\" (just these).\n" +
                                "\":\" is the assignment function, and \";\" is contrary: it gets the variable's value:\n" +
                                "\n" +
                                "1a:\t{ a:=1 }\n" +
                                "a;1+b:\t{ b:=a+1 }\n" +
                                "\n" +
                                "I.e: \"a;\" is used where in other languages you would just write \"a\"\n" +
                                "4. Functions:\n" +
                                "A FALSE lambda function is a piece of code between []. For example:\n" +
                                "\n" +
                                "[1+]\n" +
                                "\n" +
                                "is a function that adds 1 to it's argument. A function is really defined by what it takes from the stack (in this case the first arg to \"+\"), and what it puts back, just like builtin functions. Note that FALSE lambda functions are not restricted to just one return value.\n" +
                                "\n" +
                                "What a [] expression really does, is push the function. this means in practise that it can be given to yet another function as argument etc., just like in functional languages. The symbol \"!\" is called \"apply\", and applies a function to it's arguments, for example:\n" +
                                "\n" +
                                "2[1+]!\n" +
                                "\n" +
                                "would result in \"3\".\n" +
                                "This wouldn't make much sense, since what you really want is define the function once, and then use it all-over. this is easy:\n" +
                                "\n" +
                                "[1+]i:\n" +
                                "\n" +
                                "this defines the function \"i\" (actually, it assigns the function to \"i\"), so that it can be used simply by applying \"i\" to it's arguments:\n" +
                                "\n" +
                                "2i;!\n" +
                                "5. Stack functions:\n" +
                                "\"$\"\t(x-x,x)\tduplicate topmost stackitem\n" +
                                "\"%\"\t(x-)\tdelete topmost stack item\n" +
                                "\"\\\"\t(x1,x2-x2,x1)\tswap to topmost stack-items.\n" +
                                "\"@\"\t(x,x1,x2-x1,x2,x)\trotate 3rd stack item to top.\n" +
                                "\"O\"or\"ø\" (n-x)\tcopy n-th item to top (0ш equals $)\n" +
                                "\n" +
                                "1$\t\tequals\t\t1 1\n" +
                                "1 2%\t\tequals\t\t1\n" +
                                "1 2\\\t\tequals\t\t2 1\n" +
                                "1 2 3@\t\tequals\t\t2 3 1\n" +
                                "7 8 9 2ø\tequals\t\t7 8 9 7\n" +
                                "6. Control structure:\n" +
                                "FALSE only has an IF and a WHILE.\n" +
                                "If is \"?\", and looks like this: (bool,fun-). Example:\n" +
                                "\n" +
                                "a;1=[\"hello!\"]?\t\t{ if a=1 then print \"hello!\" }\n" +
                                "\n" +
                                "The first argument is a boolean value, the second the lambda function to be executed (see below for \"\") there's no \"else\", so you'll have to mimic this with a second \"?\". This can be easily done by copying the truthvalue:\n" +
                                "\n" +
                                "a;1=$[\"true\"]?~[\"false\"]?\n" +
                                "\n" +
                                "After the first \"?\" (wether it's executed or not), a copy of the truthvalue is still on the stack, and we negate it for the else part. Beware that if the first \"if\" needs arguments on the stack from before the boolean expression, it's top is still the truthvalue.\n" +
                                "\n" +
                                "While is a \"#\", and gets two lambda functions as args, one that results in a boolean, and the second as body:\n" +
                                "\n" +
                                "[a;1=][2f;!]#\t\t{ while a=1 do f(2) }\n" +
                                "\n" +
                                "Note that with while, if and lambda's, you can build virtually any other control structure.\n" +
                                "7. Input/Output:\n" +
                                "\n" +
                                "- strings printing: strings simply print themselves\n" +
                                "\n" +
                                "\"Hello, World!\n" +
                                "\"\n" +
                                "\n" +
                                "- integers: \".\" prints the topmost stack item as integer value:\n" +
                                "\n" +
                                "123.\t\t{ prints string \"123\" on console }\n" +
                                "\n" +
                                "- characters: \",\"\n" +
                                "\n" +
                                "65,\t\t{ prints \"A\" }\n" +
                                "\n" +
                                "- reading a character from stdin: \"^\"\n" +
                                "\n" +
                                "^\t\t{ top stack is char read }\n" +
                                "\n" +
                                "- flush: \"B\" or \"ß\"\n" +
                                "\"ß\" flushes both input and output.\n" +
                                "8. Libraries:\n" +
                                "In this version of FALSE you can use libraries. Library is a code that you can execute by writing a special " +
                                "comment:\n" +
                                "{HELP} - prints help\n" +
                                "{USE READ} - implements r function that reads an integer from an input. It stops when a read char is not a " +
                                "number and \"eats\" first not-number symbol.\n" +
                                "Example: {USE READ} 1_[r;!$0=~][]# %1 [\\$1_=~][*]# %.\n" +
                                "Prints a multiply of a non-zero numbers from input.\n" +
                                "{USE FACTORIAL} - implements f function that takes a number and returns it's factorial.\n" +
                                "Example: {USE READ} {USE FACTORIAL} r;!f;!.\n" +
                                "Prints a factorial of an input number.\n" +
                                "You can set and use your own libraries by executing {SET NAME CODE}.\n" +
                                "Example: {SET FALSE \"Best language\"}\n" +
                                "After that, every execution of {USE FALSE} will print \"Best language\".\n" +
                                "{USE NO LIMITS} - sets cycle execution limit for infinity (default is 5000 executions of cycle, then error).\n" +
                                "9. All functions\n" +
                                "syntax:\t\tpops:\t\tpushes:\t\texample:\n" +
                                "\n" +
                                "{comment}\t-\t\t-\t\t\t{ this is a comment }\n" +
                                "[code]\t\t-\t\tfunction\t[1+]\t{ (lambda (x) (+ x 1)) }\n" +
                                "a .. z\t\t-\t\tvaradr\t\ta\t{ use a: or a; }\n" +
                                "integer\t\t-\t\tvalue\t\t1\n" +
                                "'char\t\t-\t\tvalue\t\t'A\t{ 65 }\n" +
                                ":\t\tn,varadr\t-\t\t1a:\t{ a:=1 }\n" +
                                ";\t\tvaradr\t\tvarvalue\ta;\t{ a }\n" +
                                "!\t\tfunction\t-\t\tf;!\t{ f() }\n" +
                                "\n" +
                                "+\t\tn1,n1\t\tn1+n2\t\t1 2+\t{ 1+2 }\n" +
                                "-\t\tn1,n2\t\tn1-n2\t\t1 2-\n" +
                                "*\t\tn1,n2\t\tn1*n2\t\t1 2*\n" +
                                "/\t\tn1,n2\t\tn1/n2\t\t1 2/\n" +
                                "_\t\tn\t\t-n\t\t1_\t{ -1 }\n" +
                                "\n" +
                                "=\t\tn1,n1\t\tn1=n2\t\t1 2=~\t{ 1 not equeals 2 }\n" +
                                ">\t\tn1,n2\t\tn1>n2\t\t1 2>\n" +
                                "\n" +
                                "&\t\tn1,n2\t\tn1 and n2\t1 2&\t{ 1 and 2 }\n" +
                                "|\t\tn1,n2\t\tn1 or n2\t1 2|\n" +
                                "~\t\tn\t\tnot n\t\t0~\t{ -1,TRUE }\n" +
                                "\n" +
                                "$\t\tn\t\tn,n\t\t1$\t{ dupl. top stack }\n" +
                                "%\t\tn\t\t-\t\t1%\t{ del. top stack }\n" +
                                "\\\t\tn1,n2\t\tn2,n1\t\t1 2\\\t{ swap }\n" +
                                "@\t\tn,n1,n2\t\tn1,n2,n\t\t1 2 3@\t{ rot }\n" +
                                "ø or O\tn\t\tv\t\t1 2 1ø\t{ pick }\n" +
                                "\n" +
                                "\n" +
                                "?\t\tbool,fun\t-\t\ta;2=[1f;!]?\n" +
                                "                    { if a=2 then f(1) }\n" +
                                "#\t\tboolf,fun\t-\t\t1[$100>~][1+]#\n" +
                                "                    { while 100 >= a do a:=a+1 }\n" +
                                "\n" +
                                ".\t\tn\t\t-\t\t1.\t{ printnum(1) }\n" +
                                "\"string\"\t-\t\t-\t\t\"hi!\"\t{ printstr(\"hi!\") }\n" +
                                ",\t\tch\t\t-\t\t10,\t{ putc(10) }\n" +
                                "^\t\t-\t\tch\t\t^\t{ getc() }\n" +
                                "ß or B\t-\t\t-\t\tß\t{ flush() }\n";
                        falseInterpreter.code = ((EditText) findViewById(R.id.editText)).getText().toString();
                        falseInterpreter.input = ((EditText) findViewById(R.id.editText2)).getText().toString();
                        falseInterpreter.Process();
                        ((EditText) findViewById(R.id.editText3)).setText(falseInterpreter.output);
                        if (falseInterpreter.isOneFile)
                            ((EditText) findViewById(R.id.editText2)).setText(falseInterpreter.input);

                        snackbarStatic.dismiss();
                        snackbarStatic = null;

                            }
                        };
                        thread.run();
                    }
                });

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {

            final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Made by Illia Oleksiienko.\nEmail: iliiliilya123@gmail.com.\nLanguage author is Wouter van Oortmerssen.");
            dlgAlert.setTitle("About");
            dlgAlert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();

            return true;
        }

        if (id == R.id.action_like) {

            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.goldeneternity.falseinterpreter");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
