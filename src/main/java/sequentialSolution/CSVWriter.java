package sequentialSolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Class to represent a CSV writer object encapsulating the commons-csv library
 * Inputs a string path to the an output directory for the summary files
 * Uses the HashMap created by the readFile method in CSVReader to create summary files.
 */
public class CSVWriter {

  private String outputDir;
  private static final String[] OUTPUT_HEADER = {"date", "total_clicks"};

  /**
   * Constructor for CSVWriter
   * Checks if the output directory exists, and assigns it to the outputDir field if it does
   * @param outputDir String output directory to write new CSV files to
   * @throws NoSuchDirectoryException if the passed directory does not exist
   */
  public CSVWriter(String outputDir) throws NoSuchDirectoryException {
    //Check if the output directory exists, if not, throw NoSuchDirectoryException
    if (!(new File(outputDir).exists())) {
      throw new NoSuchDirectoryException("The specified directory does not exist. "
          + "Please enter an existing directory.");
    } else {
      this.outputDir = outputDir;
    }
  }

  /**
   * Method that writes a CSV output file for each nested HashMap produced by the readFile method
   * in the CSVReader class, which contains all the (summed) clicks on a certain date.
   * The name of the file is in the format of "module_presentation.csv"
   * @param inputFile CSVReader object
   * @throws IOException default IOException error
   */
  public void writeFiles(CSVReader inputFile) throws IOException {
    HashMap<String, HashMap<String, Integer>> info = inputFile.readCSVFile();
    FileWriter fileWriter = null;
    BufferedWriter bufferedWriter = null;
    CSVPrinter csvPrinter = null;

    for (String fileName : info.keySet()) {
      try {
//        fileWriter = new FileWriter(this.outputDir + fileName + ".csv");
        fileWriter = new FileWriter(this.outputDir + "/" + fileName + ".csv");
        bufferedWriter = new BufferedWriter(fileWriter);
        csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader(OUTPUT_HEADER));
        for (String date : info.get(fileName).keySet()) {
          csvPrinter.printRecord(date, info.get(fileName).get(date));
        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          bufferedWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Override method for default equals()
   * @param o class object for sequentialSolution.CSVWriter
   * @return boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CSVWriter)) {
      return false;
    }
    CSVWriter csvWriter = (CSVWriter) o;
    return outputDir.equals(csvWriter.outputDir);
  }

  /**
   * Override method for default hashCode()
   * @return int
   */
  @Override
  public int hashCode() {
    return Objects.hash(outputDir);
  }

  /**
   * Override method for default toString()
   * @return String
   */
  @Override
  public String toString() {
    return "CSVWriter{" +
        "outputDir='" + outputDir + '\'' +
        '}';
  }

  /*
  public static void main(String[] args) throws NoSuchDirectoryException, IOException {
    CSVReader reader = new CSVReader("/Users/isidoraconic/Desktop/a5_test", "test");
    CSVWriter writer = new CSVWriter("/Users/isidoraconic/Desktop/a5_output_files/");
    writer.writeFiles(reader);
  }
   */
}
