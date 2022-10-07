package concurrentSolution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class ClickAggregatorConsumerTest {

    private ClickAggregatorConsumer testConsumer1;
    private ClickAggregatorConsumer testConsumer2;
    private ClickAggregatorConsumer testConsumer3;
    private BlockingQueue<CSVRow> testQueue;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> testHashMap;
    private CSVRow poison;
    private CSVRow poison_diff;

    @Before
    public void setUp() throws Exception {
        this.poison = new CSVRow("thread", "killer", 0, 0, "", 0);
        this.poison_diff = new CSVRow("psycho", "killer", 0, 0, "", 0);

        this.testQueue = new LinkedBlockingQueue<CSVRow>();
        testQueue.put(new CSVRow("AAA", "2020", 1,1,"1",1));
        testQueue.put(new CSVRow("AAA", "2020", 2, 1,"1",1));
        testQueue.put(new CSVRow("BBB", "2020", 1,1,"2",1));
        testQueue.put(poison);

        this.testHashMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();

        this.testConsumer1 = new ClickAggregatorConsumer(testQueue, testHashMap, poison);
        this.testConsumer2 = new ClickAggregatorConsumer(testQueue, testHashMap, poison);
        this.testConsumer3 = new ClickAggregatorConsumer(testQueue, testHashMap, poison_diff);

        ConcurrentHashMap<String, Integer> nestedHash = new ConcurrentHashMap<>();
        nestedHash.put("0", 1);
        this.testHashMap.put("AAA_2019", nestedHash);
    }

    @Test
    public void getClicksAmount_ContainsKeyNoDate() { Assert.assertEquals(-1, this.testConsumer1.getClicksAmount("AAA_2000", "1"));}

    @Test
    public void getClicksAmount_NoKeyNoDate() { Assert.assertEquals(-1, this.testConsumer1.getClicksAmount("AAA_2019", "1"));}

    @Test
    public void writeToHash_ContainsKeyDate() {
        this.testConsumer1.writeToHash("AAA_2018", "1", 1);
        int this_click = this.testConsumer1.getClicksAmount("AAA_2018", "1");
        Assert.assertEquals(1, this_click);
    }

    @Test
    public void writeToHash_ContainsKeyNoDate() {
        this.testConsumer1.writeToHash("AAA_2019", "1", 1);
        int this_click = this.testConsumer1.getClicksAmount("AAA_2019", "1");
        Assert.assertEquals(1, this_click);
    }

    @Test
    public void writeToHash_NoKeyNoDate() {
        this.testConsumer1.writeToHash("AAA_2019", "0", 1);
        int this_click = this.testConsumer1.getClicksAmount("AAA_2019", "0");
        Assert.assertEquals(2, this_click);
    }

    @Test
    public void assertFullNotEmpty() {
        int queueSize = this.testQueue.size();
        Assert.assertEquals(4, queueSize);
    }

    @Test
    public void assertEmptyQueue() throws InterruptedException {
        Thread testThread = new Thread(this.testConsumer1);
        testThread.start();
        testThread.join();
        int queueSize = this.testQueue.size();
        Assert.assertEquals(0, queueSize);
    }

    @Test
    public void assertConcurrentHashMapClicks() throws InterruptedException{
        Thread testThread = new Thread(this.testConsumer1);
        testThread.start();
        testThread.join();
        int expectedSum = 4;
        int hashmapSum = 0;
        for (String key : this.testHashMap.keySet()) {
            for (String subKey : this.testHashMap.get(key).keySet()) {
                hashmapSum += this.testHashMap.get(key).get(subKey);
            }
        }
        Assert.assertEquals(expectedSum, hashmapSum);
    }

    @Test
    public void testEquals() { Assert.assertEquals(this.testConsumer1, this.testConsumer2); }

    @Test
    public void testNotEquals() { Assert.assertNotEquals(this.testConsumer1, this.testConsumer3); }

    @Test
    public void testHashCode() { Assert.assertEquals(this.testConsumer1.hashCode(), this.testConsumer2.hashCode()); }

    @Test
    public void testHashCodeNotEquals() { Assert.assertNotEquals(this.testConsumer1.hashCode(), this.testConsumer3.hashCode()); }

    @Test
    public void testToString() {
        String testToString = "ClickAggregatorConsumer{queue=" + this.testQueue + ", " +
                "aggStudentData=" + this.testHashMap + ", " +
                "poison=" + this.poison +
                "}";
        Assert.assertEquals(testToString, this.testConsumer1.toString());
    }
}

