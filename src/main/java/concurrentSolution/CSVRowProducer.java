package concurrentSolution;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * A class to represent a CSVRowProducer
 * This producer reads from the the aggregated HashMap that the CSVReaderProducer and
 * ClickAggregatorConsumer make, turns it into a CSVRow object, and oushes it to the blocking queue
 * for writing to the final threshold file.
 * Only creates a CSVRow object if the # of clicks is equal to or greater than the threshold
 */
public class CSVRowProducer implements Runnable {

  private BlockingQueue<CSVRow> queue;
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> map;
  private CopyOnWriteArrayList<String> keyList;
  private Integer threshold;
  private final CSVRow POISON;
  private final int N_POISON_PER_PRODUCER;

  /**
   * Constructor
   * @param queue BlockingQueue from Driver that the CSV rows will be stored in
   * @param poison CSV row poison pill that will kill each consumer thread
   * @param map thread safe HashMap containing all the parsed info from studentVle.csv
   * @param N_POISON_PER_PRODUCER number of poison pills it will place
   * @param keyList list of keys from the map (HashMap of studentVle.csv info)
   * @param threshold the threshold that the # of clicks must meet
   */
  public CSVRowProducer(BlockingQueue<CSVRow> queue,
      CSVRow poison, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> map,
      int N_POISON_PER_PRODUCER, CopyOnWriteArrayList<String> keyList, Integer threshold) {
    this.queue = queue;
    this.POISON = poison;
    this.map = map;
    this.N_POISON_PER_PRODUCER = N_POISON_PER_PRODUCER;
    this.keyList = keyList;
    this.threshold = threshold;
  }

  /**
   * Method that takes one of the keys to access the nested HashMaps from map, and creates CSVRow
   * objects from the nested HashMaps, and then pushes them to the queue
   * @throws InterruptedException default InterruptedException for Thread interruption
   */
  public synchronized void getMapElement() throws InterruptedException {
    String key = keyList.remove(0);
//    System.out.println("Removing nested hashmap for key: " + key);
    ConcurrentHashMap<String, Integer> innerMap = this.map.remove(key);

    for(String innerKey : innerMap.keySet()) {
      String date = innerKey;
      Integer clicks = innerMap.get(date);
      if(clicks >= threshold) {
        CSVRow row = new CSVRow(key, date, clicks);
        queue.put(row);
      }
    }
  }

  /**
   * Override of Runnable.run() method to take each key and all nested HashMaps, creates the CSVRow
   * objects for each of the nested HashMaps in the getMapElement method (where they are pushed to
   * the queue).
   * At the end when there is nothing else to read, it stashes a poison pill
   */
  @Override
  public void run() {
    try {
      CopyOnWriteArrayList<CSVRow> rowList;
      while(!map.isEmpty() && !keyList.isEmpty()) {
        getMapElement();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println(Thread.currentThread().getName() + " has been interrupted in the catch!");
    } finally {
      while (true) {
        try {
          for (int i=0; i < this.N_POISON_PER_PRODUCER; i++) {
//            System.out.println(Thread.currentThread().getName() + " adding poison pill to queue in WriterProducer!");
            queue.put(this.POISON);
          }
          break;
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Override of default equals() method
   * @param o object
   * @return boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CSVRowProducer)) {
      return false;
    }
    CSVRowProducer that = (CSVRowProducer) o;
    return N_POISON_PER_PRODUCER == that.N_POISON_PER_PRODUCER &&
        Objects.equals(queue, that.queue) &&
        Objects.equals(map, that.map) &&
        Objects.equals(keyList, that.keyList) &&
        Objects.equals(threshold, that.threshold) &&
        Objects.equals(POISON, that.POISON);
  }

  /**
   * Override of default hashCode() method
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(queue, map, keyList, threshold, POISON, N_POISON_PER_PRODUCER);
  }

  /**
   * Override of default toString() method
   * @return String
   */
  @Override
  public String toString() {
    return "CSVRowProducer{" +
        "queue=" + queue +
        ", map=" + map +
        ", keyList=" + keyList +
        ", threshold=" + threshold +
        ", POISON=" + POISON +
        ", N_POISON_PER_PRODUCER=" + N_POISON_PER_PRODUCER +
        '}';
  }
}