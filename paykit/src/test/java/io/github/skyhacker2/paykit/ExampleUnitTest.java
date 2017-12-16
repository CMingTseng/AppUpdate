package io.github.skyhacker2.paykit;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testGenId() {
        for (int i = 0; i < 10; i++) {
            String id = Utils.genShortId();
            System.out.println(id);
        }
        assertEquals(1,1);
    }
}