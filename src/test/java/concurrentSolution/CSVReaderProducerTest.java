package concurrentSolution;

import org.apache.commons.lang3.ThreadUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sequentialSolution.NoSuchDirectoryException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class CSVReaderProducerTest {

    private CSVReaderProducer testProducer1;
    private CSVReaderProducer testProducer2;
    private CSVReaderProducer testProducer3;
    private CSVRow testRow;
    private BlockingQueue<CSVRow> testQueue;
    private CSVRow poison;
    private static final String TEST_MODE = "test";
    private static final String PROD_MODE = "prod";
    private static final String path = "anonymisedData";

    @Before
    public void setUp() throws Exception {
        this.testQueue = new LinkedBlockingQueue<CSVRow>();
        this.poison = new CSVRow("thread", "killer", 0, 0, "", 0);
        this.testProducer1 = new CSVReaderProducer(path, TEST_MODE, testQueue, poison, 1);
        this.testProducer2 = new CSVReaderProducer(path, TEST_MODE, testQueue, poison, 1);
        this.testProducer3 = new CSVReaderProducer(path, PROD_MODE, testQueue, poison, 1);
        this.testRow = new CSVRow("AAA", "1111", 1, 1, "1", 0);
    }

    @Test (expected = NoSuchDirectoryException.class)
    public void noDirectoryError() throws NoSuchDirectoryException {
        CSVReaderProducer invalidPathTestProducer = new CSVReaderProducer("anonymizedData", "test", testQueue, poison, 1);
    }

    @Test
    public void parseCSVRow_Test() {
        String inputLine = "AAA,1111,1,1,1,0";
        CSVRow parsedRow = this.testProducer1.parseCSVRow(inputLine);
        Assert.assertEquals(testRow, parsedRow);
    }

    @Test
    public void run() throws InterruptedException {
        Thread testThread = new Thread(this.testProducer1);
        testThread.start();
        testThread.join();
        int queueSize = this.testQueue.size();
        int testFileSize = this.testProducer1.getFileSize();
        Assert.assertEquals(testFileSize, queueSize);
    }

    @Test //(expected = InterruptedException.class)
    public void runInterrupt() throws InterruptedException {
        final Thread testThread = new Thread(this.testProducer1, "test-1");
        testThread.start();
        assertEquals(1, ThreadUtils.findThreadsByName("test-1").size());
        testThread.interrupt();
//        try {
//            testThread.start();
//            assertEquals(1, ThreadUtils.findThreadsByName("test-1").size());
//        } finally {
//            testThread.interrupt();
//            testThread.join();
//        }
    }

    @Test
    public void testEquals() { Assert.assertEquals(this.testProducer1, this.testProducer2); }

    @Test
    public void testNotEquals() { Assert.assertNotEquals(this.testProducer1, this.testProducer3); }

    @Test
    public void testHashCode() { Assert.assertEquals(this.testProducer1.hashCode(), this.testProducer2.hashCode()); }

    @Test
    public void testHashCodeNotEquals() { Assert.assertNotEquals(this.testProducer1.hashCode(), this.testProducer3.hashCode()); }

    @Test
    public void testToString() {
        String testToString = "CSVReaderProducer{csvFile='" +
                this.testProducer1.getCsvFile() + "', folderPath='" +
                path + "', queue=[]}";
        String toStringCalled = this.testProducer1.toString();
        Assert.assertEquals(testToString, toStringCalled);
    }

}