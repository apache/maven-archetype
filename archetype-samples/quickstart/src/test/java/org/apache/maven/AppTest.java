package org.apache.maven;

import org.junit.Test;
import static junit.framework.Assert.*;

public class AppTest {

    @Test
    public void sayHello()
    {
        assertEquals("Hello World!", new App().sayHello());
    }
}