package concurrentSolution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class CSVRowProducerTest {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> testHashMap;
    private CSVRowProducer testRowProducer1;
    private CSVRowProducer testRowProducer2;
    private CSVRowProducer testRowProducer3;
    private BlockingQueue<CSVRow> testQueue;
    private CopyOnWriteArrayList<String> testKeyList;
    private CSVRow POISON;
    private int N_POISON_PER_PRODUCER;
    private static final Integer TEST_THRESH = 3;

    @Before
    public void setUp() throws Exception {
        this.POISON = new CSVRow("poison", "", 0);
        this.N_POISON_PER_PRODUCER = 1;
        this.testQueue = new LinkedBlockingQueue<CSVRow>();
        this.testHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();

        ConcurrentHashMap<String, Integer> nestedHashBelow = new ConcurrentHashMap<String, Integer>();
        nestedHashBelow.put("1", 1);

        ConcurrentHashMap<String, Integer> nestedHashAbove = new ConcurrentHashMap<String, Integer>();
        nestedHashAbove.put("1", 3);

        this.testHashMap.put("AAA_2020", nestedHashAbove);
        this.testHashMap.put("BBB_2020", nestedHashBelow);
        this.testHashMap.put("CCC_2020", nestedHashBelow);
        this.testHashMap.put("DDD_2020", nestedHashBelow);
        this.testHashMap.put("EEE_2020", nestedHashBelow);

        this.testKeyList = new CopyOnWriteArrayList<>(testHashMap.keySet());
        this.testRowProducer1 = new CSVRowProducer(this.testQueue, this.POISON, this.testHashMap, N_POISON_PER_PRODUCER, testKeyList, TEST_THRESH);
        this.testRowProducer2 = new CSVRowProducer(this.testQueue, this.POISON, this.testHashMap, N_POISON_PER_PRODUCER, testKeyList, TEST_THRESH);
        this.testRowProducer3 = new CSVRowProducer(this.testQueue, this.POISON, this.testHashMap, N_POISON_PER_PRODUCER, testKeyList, 0);
    }

    @Test
    public void getMapElementFiltered() throws InterruptedException {
        String key = this.testKeyList.get(0);
        System.out.println(key + " " + this.testHashMap.get(key));
        this.testRowProducer1.getMapElement();
        Assert.assertEquals(0, this.testQueue.size());
    }

    @Test
    public void getMapElementAdded() throws InterruptedException {
        String key = this.testKeyList.get(1);
        System.out.println(key + " " + this.testHashMap.get(key));
        this.testRowProducer1.getMapElement();
        this.testRowProducer1.getMapElement();
        Assert.assertEquals(1, this.testQueue.size());
    }

    @Test
    public void assertQueueAdded() throws InterruptedException {
        Thread rowThread = new Thread(this.testRowProducer1);
        rowThread.start();
        rowThread.join();
        assertFalse(this.testQueue.isEmpty());
        Assert.assertEquals(2, this.testQueue.size()); // expect 2, one for the filtered item AAA_2020 and one for the poison pill
    }

    @Test
    public void testEquals() { Assert.assertEquals(this.testRowProducer1, this.testRowProducer2); }

    @Test
    public void testNotEquals() { Assert.assertNotEquals(this.testRowProducer1, this.testRowProducer3); }

    @Test
    public void testHashCode() { Assert.assertEquals(this.testRowProducer1.hashCode(), this.testRowProducer2.hashCode()); }

    @Test
    public void testHashCodeNotEquals() { Assert.assertNotEquals(this.testRowProducer1.hashCode(), this.testRowProducer3.hashCode()); }

    @Test
    public void testToString() {
        System.out.println(this.testRowProducer1.toString());
        String testToString = "CSVRowProducer{" +
                "queue=" + this.testQueue +
                ", map=" + this.testHashMap +
                ", keyList=" + this.testKeyList +
                ", threshold=" + TEST_THRESH +
                ", POISON=" + this.POISON +
                ", N_POISON_PER_PRODUCER=" + this.N_POISON_PER_PRODUCER +
                '}';
        Assert.assertEquals(testToString, this.testRowProducer1.toString());
    }
}