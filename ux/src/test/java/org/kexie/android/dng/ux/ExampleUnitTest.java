package org.kexie.android.dng.ux;

import org.junit.Test;

import androidx.lifecycle.Lifecycle;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void addition_isCorrect()
    {

       System.out.print(Lifecycle.State.CREATED.isAtLeast(Lifecycle.State.STARTED));

        assertEquals(4, 2 + 2);
    }
}