package concurrentSolution;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.csv.CSVPrinter;

/**
 * Class that represents a CSVRowWriterConsumer object.
 * It takes a CSVRow object and writes it to an open output file. The consumer keeps a count of how
 * many poison pills it should expect before it terminates (i.e. the total # of producers)
 * When it reaches one, it will decrement the poisonCount, until we reach 0, at which point we terminate
 */
public class CSVRowWriterConsumer implements Runnable {

  private BlockingQueue<CSVRow> queue;
  private static final String[] OUTPUT_HEADER = {"module_presentation", "date", "total_clicks"};
  private final CSVRow POISON;
  private CSVPrinter printer;
  private int poisonCount;

  /**
   * Constructor for the CSVRowWriterConsumer class
   * @param queue BlockingQueue from Driver that the CSVRow objects are stored in
   * @param poison CSVRow poison pill that will kill each consumer thread
   * @param printer The CSVPrinter object that the consumer uses to write
   * @param poisonCount Total number of poison pills we should expect
   */
  public CSVRowWriterConsumer(BlockingQueue<CSVRow> queue,
      CSVRow poison, CSVPrinter printer, int poisonCount)  {

    this.queue = queue;
    this.POISON = poison;
    this.printer = printer;
    this.poisonCount = poisonCount;
  }

  /**
   * Method that takes a CSVPrinter object from its field, and prints the passed CSVRow object to the file
   * @param row CSVRow object to write to file
   * @throws IOException default IOException error
   */
  public void writeFile(CSVRow row) throws IOException {
    try {
      printer.printRecord(row.getCodeKey(), row.getDate(), row.getClicks());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Override of Runnable.run() method to take each CSVRow object from the BlockingQueue
   * We call the writeFile method here as long as the queue has (valid) CSVFile objects
   * When the Thread finds all the poison pills (depending on the poisonCount), it will terminate
   * It decrements the poison count every time we encounter a poison pill, until we reach 0,
   * at which point the thread terminates
   */
  @Override
  public void run() {
    try {
      CSVRow row;
      while (poisonCount > 0) {
        row = queue.take();
        if(row.equals(this.POISON)) {
          poisonCount--;
        } else {
          writeFile(row);
        }
      }
    } catch (InterruptedException | IOException interruptedException) {
      Thread.currentThread().interrupt();
      System.out.println(Thread.currentThread().getName() + " was interrupted!");
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
    if (!(o instanceof CSVRowWriterConsumer)) {
      return false;
    }
    CSVRowWriterConsumer that = (CSVRowWriterConsumer) o;
    return poisonCount == that.poisonCount &&
        Objects.equals(queue, that.queue) &&
        Objects.equals(POISON, that.POISON) &&
        Objects.equals(printer, that.printer);
  }

  /**
   * Override of default hashCode() method
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(queue, POISON, printer, poisonCount);
  }

  /**
   * Override of default toString() method
   * @return String
   */
  @Override
  public String toString() {
    return "CSVRowWriterConsumer{" +
        "queue=" + queue +
        ", POISON=" + POISON +
        ", printer=" + printer +
        ", poisonCount=" + poisonCount +
        '}';
  }

}