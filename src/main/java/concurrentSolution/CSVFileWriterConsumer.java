package concurrentSolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import sequentialSolution.NoSuchDirectoryException;

/**\
 * Class to represent the consumers that will write the output files
 * The consumer takes a CSVFile object from the queue that was enqueued by the CSVFileProducer class
 * instances (producers), and writes that .csv file to the specified output directory
 */
public class CSVFileWriterConsumer implements Runnable {

  private BlockingQueue<CSVFile> queue;
  private String outputDir;
  private static final String[] OUTPUT_HEADER = {"date", "total_clicks"};
  private final CSVFile POISON;

  /**
   * Constructor for the CSVFileWriterConsumer class
   * @param outputDir output directory to store all the .csv files in
   * @param queue BlockingQueue from Driver that the CSVFile objects are stored in
   * @param poison CSVFile poison pill that will kill each consumer thread
   * @throws NoSuchDirectoryException custom exception when directory does not exist
   */
  public CSVFileWriterConsumer(String outputDir, BlockingQueue<CSVFile> queue,
                               CSVFile poison) throws NoSuchDirectoryException {
    //Check if the output directory exists, if not, throw NoSuchDirectoryException
    if (!(new File(outputDir).exists())) {
      throw new NoSuchDirectoryException("The specified directory does not exist. "
              + "Please enter an existing directory.");
    } else {
      this.outputDir = outputDir;
    }
    this.queue = queue;
    this.POISON = poison;
  }

  /**
   * Method that takes in a CSVFile object and writes a csv. file using the information stored in it
   * @param fileContent CSVFile object to write to a csv. file
   * @throws IOException default IOException error
   */
  public void writeFile(CSVFile fileContent) throws IOException {

    FileWriter fileWriter;
    BufferedWriter bufferedWriter = null;
    CSVPrinter csvPrinter;

    try {
      fileWriter = new FileWriter(this.outputDir + "/" + fileContent.getName() + ".csv");
      bufferedWriter = new BufferedWriter(fileWriter);
      csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader(OUTPUT_HEADER));

      for (CopyOnWriteArrayList<String> row : fileContent.getFileContent()) {
        csvPrinter.printRecord(row.get(0), row.get(1));
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        assert bufferedWriter != null;
        bufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Override of Runnable.run() method to take each CSVFile object from the BlockingQueue
   * We call the writeFile method here as long as the queue has (valid) CSVFile objects
   * When the Thread finds a poison pill, it will terminate
   */
  @Override
  public void run() {
    try {
      CSVFile outputFile;
      while (!(outputFile = queue.take()).equals(POISON)) {
//        System.out.println(Thread.currentThread().getName() + " taking " + outputFile.getName() + " from BlockingQueue");
        writeFile(outputFile);
      }
    } catch (InterruptedException | IOException interruptedException) {
        Thread.currentThread().interrupt();
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
    if (!(o instanceof CSVFileWriterConsumer)) {
      return false;
    }
    CSVFileWriterConsumer that = (CSVFileWriterConsumer) o;
    return queue.equals(that.queue) &&
        outputDir.equals(that.outputDir) &&
        POISON.equals(that.POISON);
  }

  /**
   * Override of default hashCode() method
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(queue, outputDir, POISON);
  }

  /**
   * Override of default toString() method
   * @return String
   */
  @Override
  public String toString() {
    return "CSVFileWriterConsumer{" +
        "queue=" + queue +
        ", outputDir='" + outputDir + '\'' +
        ", POISON=" + POISON +
        '}';
  }
}
