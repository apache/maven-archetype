package org.apache.maven.test;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        //A   #\{some}
        //B   #{some}
        //C   #{some other}
        //D   \#{some other}
        //E   #{}
        //F   {some}
        //G   ${someOtherProperty}
        //H   ${someValue}
        /*
        A   #\{some}
        B   #{some}
        C   #{some other}
        D   \#{some other}
        E   #{}
        F   {some}
        G   ${someOtherProperty}
        H   ${someValue}
         */
    }
}
