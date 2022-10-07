package concurrentSolution;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class to represent the CSVFileProducer
 * This producer (thread) will read from the HashMap of information (information parsed from the
 * studentVle.csv file) and create a CSVFile object for each one of the outer HashMap entries
 * These CSVFile objects will be passed to the consumers which will write the .csv files
 */
public class CSVFileProducer implements Runnable {

  private BlockingQueue<CSVFile> queue;
  private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> map;
  private CopyOnWriteArrayList<String> keyList;
  private final CSVFile POISON;
  private final int N_POISON_PER_PRODUCER;

  /**
   * Constructor for the CSVFileProducer
   * Note that the key list is a thread safe ArrayList of all the keys in the HashMap
   * @param queue BlockingQueue from Driver that the CSVFile objects will be stored in
   * @param poison CSVFile poison pill that will kill each consumer thread
   * @param map thread safe HashMap containing all the parsed info from studentVle.csv
   * @param N_POISON_PER_PRODUCER number of poison pills per producer
   * @param keyList list of keys from the map (HashMap of studentVle.csv info)
   */
  public CSVFileProducer(BlockingQueue<CSVFile> queue,
                         CSVFile poison, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> map,
                         int N_POISON_PER_PRODUCER, CopyOnWriteArrayList<String> keyList) {
    this.queue = queue;
    this.POISON = poison;
    this.map = map;
    this.N_POISON_PER_PRODUCER = N_POISON_PER_PRODUCER;
    this.keyList = keyList;
  }

  /**
   * Method that takes the top element in the list of keys, and then extracts those nested
   * HashMaps from the HashMap containing the parsed studentVle.csv info
   * @return CSVFile object with all the information from those nested HashMaps
   * @throws InterruptedException default InterruptedException error for Thread interruption
   */
  public synchronized CSVFile getMapElement() throws InterruptedException {
    String key = keyList.remove(0);
    ConcurrentHashMap<String, Integer> innerMap = this.map.remove(key);
    CSVFile outputFile = new CSVFile(key);

    for(String innerKey : innerMap.keySet()) {
      CopyOnWriteArrayList<String> row = new CopyOnWriteArrayList<>();
      String date = innerKey;
      String clicks = "" + innerMap.get(date);
      row.add(date);
      row.add(clicks);
      outputFile.addRow(row);
    }

    return  outputFile;

  }

  /**
   * Override of Runnable.run() method to take each key and alll nested HashMaps, creates the CSVFile
   * objects for each one, and puts them into the blocking queue for the consumers
   * At the end when there is nothing else to read, it stashes a poison pill for each consumer
   */
  @Override
  public void run() {
    try {
      CSVFile fileInfo;
      while(!map.isEmpty() && !keyList.isEmpty()) {
        fileInfo = getMapElement();
        queue.put(fileInfo);
//        System.out.println(Thread.currentThread().getName() + " just added CSVFile = " + fileInfo.getName() + " to the BlockingQueue.");
      }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
      while(true) {
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
    if (!(o instanceof CSVFileProducer)) {
      return false;
    }
    CSVFileProducer that = (CSVFileProducer) o;
    return N_POISON_PER_PRODUCER == that.N_POISON_PER_PRODUCER &&
        queue.equals(that.queue) &&
        map.equals(that.map) &&
        keyList.equals(that.keyList) &&
        POISON.equals(that.POISON);
  }

  /**
   * Override of default hashCode() method
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(queue, map, keyList, POISON, N_POISON_PER_PRODUCER);
  }

  /**
   * Override of default toString() method
   * @return String
   */
  @Override
  public String toString() {
    return "CSVFileProducer{" +
        "queue=" + queue +
        ", map=" + map +
        ", keyList=" + keyList +
        ", POISON=" + POISON +
        ", N_POISON_PER_PRODUCER=" + N_POISON_PER_PRODUCER +
        '}';
  }

}
