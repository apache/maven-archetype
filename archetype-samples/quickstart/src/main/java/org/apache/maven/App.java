package org.apache.maven;

public class App
{
    public static void main(String[] args)
    {
        System.out.println(new App().sayHello());
    }
    public String sayHello()
    {
        return "Hello World!";
    }
}