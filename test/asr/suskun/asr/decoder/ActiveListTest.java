package suskun.asr.decoder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ActiveListTest {

    @Test
    public void testConstructor1() {
        ActiveList list = ActiveList.builder(10).build();
        Assert.assertEquals(10, list.getBeamSize(), 0.01f);
        Assert.assertEquals(ActiveList.DEFAULT_INITIAL_CAPACITY, list.getCapacity());
        Assert.assertEquals(ActiveList.DEFAULT_MIN_HYPOTHESIS_COUNT, list.getMinHypothesisCount());
        Assert.assertEquals(ActiveList.DEFAULT_LOAD_FACTOR, list.getLoadFactor(), 0.01f);
        Assert.assertEquals(ActiveList.DEFAULT_CLUSTER_COUNT, list.getClusterCount());
        Assert.assertEquals(Float.POSITIVE_INFINITY, list.getMin(), 0.01f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, list.getMax(), 0.01f);
    }

    @Test
    public void testConstructor2() {
        ActiveList list = ActiveList
                .builder(10)
                .minimumHypothesisCount(1)
                .initialCapacity(11)
                .clusterCount(12)
                .loadFactor(0.6f)
                .build();
        Assert.assertEquals(10, list.getBeamSize(), 0.01f);
        Assert.assertEquals(16, list.getCapacity());
        Assert.assertEquals(1, list.getMinHypothesisCount());
        Assert.assertEquals(0.6f, list.getLoadFactor(), 0.01f);
        Assert.assertEquals(12, list.getClusterCount());
        Assert.assertEquals(Float.POSITIVE_INFINITY, list.getMin(), 0.01f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, list.getMax(), 0.01f);
    }

    @Test
    public void testAdd1() {
        ActiveList list = ActiveList
                .builder(10)
                .minimumHypothesisCount(1)
                .initialCapacity(11)
                .clusterCount(12)
                .loadFactor(0.6f)
                .build();
        list.add(new Hypothesis(null, 10, 1));
        list.add(new Hypothesis(null, 20, 2));
        list.add(new Hypothesis(null, 30, 3));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(1, list.getMin(), 0.01f);
        Assert.assertEquals(3, list.getMax(), 0.01f);

        List<Hypothesis> sorted = list.getAllHypotheses();
        Collections.sort(sorted);
        Assert.assertEquals(3, sorted.size());
        Assert.assertEquals(3, sorted.get(0).score, 0.01);
        Assert.assertEquals(1, sorted.get(2).score, 0.01);

        // should overwrite hypothesis [ state = 30, score = 3 ]
        list.add(new Hypothesis(null, 30, 5));
        sorted = list.getAllHypotheses();
        Collections.sort(sorted);
        Assert.assertEquals(3, sorted.size());
        Assert.assertEquals(5, sorted.get(0).score, 0.01);
    }

    @Test
    public void testExtend() {
        ActiveList list = ActiveList
                .builder(10)
                .minimumHypothesisCount(0)
                .initialCapacity(5)
                .clusterCount(2)
                .loadFactor(0.5f)
                .build();
        list.add(new Hypothesis(null, 10, 1));
        list.add(new Hypothesis(null, 20, 2));
        list.add(new Hypothesis(null, 30, 3));

        Assert.assertEquals(3, list.size());
        Assert.assertEquals(8, list.getCapacity());

        list.add(new Hypothesis(null, 40, 4));
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(16, list.getCapacity());

        list.add(new Hypothesis(null, 60, 6));
        list.add(new Hypothesis(null, 50, 5));

        List<Hypothesis> sorted = list.getAllHypotheses();
        Collections.sort(sorted);
        Assert.assertEquals(6, sorted.size());
        Assert.assertEquals(6, sorted.get(0).score, 0.01);
        Assert.assertEquals(1, sorted.get(5).score, 0.01);
        Assert.assertEquals(6, list.getMax(), 0.01);
        Assert.assertEquals(1, list.getMin(), 0.01);
    }


}
