package concurrentSolution;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a class to represent a CSVFile object
 * These objects are created by the CSVFileProducer, and adds them to the queue for writing
 * by the CSVFileWriterConsumer, which creates a .csv file out of each CSVFile object
 * Rows of the file are stores as elements in a nested list (thread safe), where each of the nested
 * lists represent all the different column elements of that row
 */
public class CSVFile {

  private CopyOnWriteArrayList<CopyOnWriteArrayList<String>> rows = new CopyOnWriteArrayList();
  private String name;

  /**
   * Constructor for CSVFile
   * @param name name of the file, which will be the name of the .csv file later
   */
  public CSVFile(String name) {
    this.name = name;
  }

  /**
   * Add a row of data to the CSVFile object
   * @param row list of elements for each column to go in that row
   */
  public void addRow(CopyOnWriteArrayList<String> row) {
    this.rows.add(row);
  }

  /**
   * Method that returns the entire contents of the CSVFile object (i.e. the nested list of data)
   * @return nested thread safe list of all data
   */
  public CopyOnWriteArrayList<CopyOnWriteArrayList<String>> getFileContent() {
    return this.rows;
  }

  /**
   * Method that returns the name of the file
   * @return String name of file
   */
  public String getName() {
    return this.name;
  }

  /**
   * Method to override the default equals()
   * @param o object
   * @return boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CSVFile)) {
      return false;
    }
    CSVFile csvFile = (CSVFile) o;
    return Objects.equals(rows, csvFile.rows) &&
        Objects.equals(name, csvFile.name);
  }

  /**
   * Method to override the default hashCode()
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(rows, name);
  }

  /**
   * Method to override the default toString()
   * @return String
   */
  @Override
  public String toString() {
    return "CSVFile{" +
        "name='" + name + '\'' +
        '}';
  }
}
