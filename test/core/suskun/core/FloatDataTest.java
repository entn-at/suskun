package suskun.core;

import org.junit.Assert;
import org.junit.Test;

public class FloatDataTest {

    @Test
    public void appendTest() {
        float[] content = {5, 4, 3};
        float[] toAppend = {1, 2};
        float[] expected = {5, 4, 3, 1, 2};
        FloatData d = new FloatData(content);
        FloatData d2 = d.append(toAppend);
        Assert.assertArrayEquals(expected, d2.getContent(), 0.0001f);

        float[] expected2 = {5, 4, 3, 1, 2, 1, 2};
        d2.appendInPlace(toAppend);
        Assert.assertArrayEquals(expected2, d2.getContent(), 0.0001f);
    }

    @Test
    public void prependTest() {
        float[] content = {5, 4, 3};
        float[] toPrepend = {1, 2};
        float[] expected = {1, 2, 5, 4, 3};
        FloatData d = new FloatData(content);
        FloatData d2 = d.prepend(toPrepend);
        Assert.assertArrayEquals(expected, d2.getContent(), 0.0001f);

        float[] expected2 = {1, 2, 1, 2, 5, 4, 3};
        d2.prependInPlace(toPrepend);
        Assert.assertArrayEquals(expected2, d2.getContent(), 0.0001f);
    }

}
