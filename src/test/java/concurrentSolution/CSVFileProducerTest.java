package concurrentSolution;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sequentialSolution.CSVReader;
import sequentialSolution.CSVWriter;

public class CSVFileProducerTest {

  private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> map;
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> fakeMap;
  private BlockingQueue<CSVFile> queue;
  private CopyOnWriteArrayList<String> keyList;
  private CopyOnWriteArrayList<String> fakeKeyList;
  private CSVFileProducer producer1;
  private CSVFileProducer producer2;
  private CSVFileProducer producer3;
  private CSVFile outputFile1;
  private CSVFile outputFile2;
  private CSVFile outputFile3;
  private CSVFile outputFile4;
  private CSVFile POISON;
  private final int N_POISON_PER_PRODUCER = 1;

  @Before
  public void setUp() throws Exception {

    //Creating the concurrent double HashMap that would have been outputted by the reader producer/consumers
    this.map = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> innerMap1 = new ConcurrentHashMap<>();
    innerMap1.put("5", 23);
    innerMap1.put("-10", 87);
    innerMap1.put("21", 658);
    ConcurrentHashMap<String, Integer> innerMap2 = new ConcurrentHashMap<>();
    innerMap2.put("1", 14);
    innerMap2.put("45", 206);
    innerMap2.put("-7", 155);
    innerMap2.put("24", 400);
    ConcurrentHashMap<String, Integer> innerMap3 = new ConcurrentHashMap<>();
    innerMap3.put("3", 99);
    ConcurrentHashMap<String, Integer> innerMap4 = new ConcurrentHashMap<>();
    innerMap4.put("-2", 107);
    innerMap4.put("12", 505);
    this.map.put("AAA_2014J", innerMap1);
    this.map.put("BBB_2016F", innerMap2);
    this.map.put("CCC_2015H", innerMap3);
    this.map.put("DDD_2013G", innerMap4);

    //Another hasmap for testing when .equals is not true
    this.fakeMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> fakeInnerMap1 = new ConcurrentHashMap<>();
    fakeInnerMap1.put("4", 100);
    ConcurrentHashMap<String, Integer> fakeInnerMap2 = new ConcurrentHashMap<>();
    fakeInnerMap2.put("-18", 24);
    this.fakeMap.put("AAA_2012J", fakeInnerMap1);
    this.fakeMap.put("BBB_2013H", fakeInnerMap2);

    //making the expected output CSVFile objects
    this.outputFile1 = new CSVFile("AAA_2014J");
    for(String key : innerMap1.keySet()) {
      CopyOnWriteArrayList<String> row = new CopyOnWriteArrayList<>();
      String date = key;
      String clicks = "" + innerMap1.get(date);
      row.add(date);
      row.add(clicks);
      outputFile1.addRow(row);
    }

    this.outputFile2 = new CSVFile("BBB_2016F");
    for(String key : innerMap2.keySet()) {
      CopyOnWriteArrayList<String> row = new CopyOnWriteArrayList<>();
      String date = key;
      String clicks = "" + innerMap2.get(date);
      row.add(date);
      row.add(clicks);
      outputFile2.addRow(row);
    }

    this.outputFile3 = new CSVFile("CCC_2015H");
    for(String key : innerMap3.keySet()) {
      CopyOnWriteArrayList<String> row = new CopyOnWriteArrayList<>();
      String date = key;
      String clicks = "" + innerMap3.get(date);
      row.add(date);
      row.add(clicks);
      outputFile3.addRow(row);
    }

    this.outputFile4 = new CSVFile("DDD_2013G");
    for(String key : innerMap4.keySet()) {
      CopyOnWriteArrayList<String> row = new CopyOnWriteArrayList<>();
      String date = key;
      String clicks = "" + innerMap4.get(date);
      row.add(date);
      row.add(clicks);
      outputFile4.addRow(row);
    }

    //initializing the blocking queue and rest of elements needed
    this.queue = new LinkedBlockingQueue<CSVFile>();
    this.POISON = new CSVFile("poison");
    this.keyList =  new CopyOnWriteArrayList<>(map.keySet());
    this.fakeKeyList =  new CopyOnWriteArrayList<>(fakeMap.keySet());

    //making the producers
    this.producer1 = new CSVFileProducer(this.queue, this.POISON, this.map,
        this.N_POISON_PER_PRODUCER, this.keyList);
    this.producer2 = new CSVFileProducer(this.queue, this.POISON, this.map,
        this.N_POISON_PER_PRODUCER, this.keyList);
    this.producer3 = new CSVFileProducer(this.queue, this.POISON, this.fakeMap,
        this.N_POISON_PER_PRODUCER, this.fakeKeyList);

  }

  @Test
  public void getMapElement() throws InterruptedException {
    Assert.assertEquals(4, this.map.size());
    Assert.assertEquals(4, this.keyList.size());
    CSVFile output = producer1.getMapElement();
    Assert.assertEquals(3, this.map.size());
    Assert.assertEquals(3, this.keyList.size());
    Assert.assertEquals(this.outputFile4, output);
  }

  @Test
  public void run() throws InterruptedException {
    Assert.assertEquals(0, this.queue.size());
    Thread testThread = new Thread(this.producer1);
    testThread.start();
    testThread.join();
    Assert.assertEquals(5, this.queue.size());
    Assert.assertTrue(this.queue.contains(this.outputFile1));
    Assert.assertTrue(this.queue.contains(this.outputFile2));
    Assert.assertTrue(this.queue.contains(this.outputFile3));
    Assert.assertTrue(this.queue.contains(this.outputFile4));
    Assert.assertTrue(this.queue.contains(this.POISON));
  }

  @Test
  public void testEqualsTrue() {
    Assert.assertTrue(this.producer1.equals(this.producer2));
  }

  @Test
  public void testEqualsFalse() {
    Assert.assertFalse(this.producer1.equals(this.producer3));
  }

  @Test
  public void testEqualsWrongObject() {
    String test = "hello";
    Assert.assertFalse(this.producer1.equals(test));
  }

  @Test
  public void testHashCodeSame() {
    Assert.assertEquals(this.producer1.hashCode(), this.producer2.hashCode());
  }

  @Test
  public void testHashCodeDifferent() {
    Assert.assertNotEquals(this.producer1.hashCode(), this.producer3.hashCode());
  }

  @Test
  public void testToString() {
    String toString = "CSVFileProducer{" +
        "queue=" + this.queue +
        ", map=" + this.map +
        ", keyList=" + this.keyList +
        ", POISON=" + this.POISON +
        ", N_POISON_PER_PRODUCER=" + this.N_POISON_PER_PRODUCER +
        '}';
    Assert.assertEquals(toString, this.producer1.toString());
  }


}