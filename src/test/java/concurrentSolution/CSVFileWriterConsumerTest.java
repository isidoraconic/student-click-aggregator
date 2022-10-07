package concurrentSolution;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CSVFileWriterConsumerTest {

  private BlockingQueue<CSVFile> queue;
  private static final String[] OUTPUT_HEADER = {"date", "total_clicks"};
  private CSVFile POISON;
  private File sampleFile1;
  private File sampleFile2;
  private File sampleFile3;
  private String outputDir;
  private String fakeOutputDir;
  private CSVFile outputFile1;
  private CSVFile outputFile2;
  private CSVFile outputFile3;
  private CSVFileWriterConsumer consumer1;
  private CSVFileWriterConsumer consumer2;
  private CSVFileWriterConsumer consumer3;

  @Rule
  public TemporaryFolder sampleFileFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder outputDirectory = new TemporaryFolder();

  @Rule
  public TemporaryFolder fakeOutputDirectory = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    this.sampleFile1 = sampleFileFolder.newFile("AAA_2014J.csv");
    this.sampleFile2 = sampleFileFolder.newFile("BBB_2016F.csv");
    this.sampleFile3 = sampleFileFolder.newFile("CCC_2015H.csv");
    this.outputDir = outputDirectory.getRoot().getAbsolutePath();
    this.fakeOutputDir = fakeOutputDirectory.getRoot().getAbsolutePath();

    //Writing a CSV file in Java adapted from the following link:
    //https://stackabuse.com/reading-and-writing-csvs-in-java/
    List<List<String>> rows1 = Arrays.asList(
        Arrays.asList("5", "23"),
        Arrays.asList("-10", "87"),
        Arrays.asList("21", "658")
    );

    List<List<String>> rows2 = Arrays.asList(
        Arrays.asList("36", "472"),
        Arrays.asList("-4", "2")
    );

    List<List<String>> rows3 = Arrays.asList(
        Arrays.asList("10", "201"),
        Arrays.asList("-3", "700")
    );

    PrintWriter csvWriter1 = new PrintWriter(this.sampleFile1);
    csvWriter1.append("date");
    csvWriter1.append(",");
    csvWriter1.append("clicks");
    csvWriter1.append("\n");

    PrintWriter csvWriter2 = new PrintWriter(this.sampleFile2);
    csvWriter2.append("date");
    csvWriter2.append(",");
    csvWriter2.append("clicks");
    csvWriter2.append("\n");

    PrintWriter csvWriter3 = new PrintWriter(this.sampleFile3);
    csvWriter3.append("date");
    csvWriter3.append(",");
    csvWriter3.append("clicks");
    csvWriter3.append("\n");

    for (List<String> rowData : rows1) {
      csvWriter1.append(String.join(",", rowData));
      csvWriter1.append("\n");
    }

    for (List<String> rowData : rows2) {
      csvWriter2.append(String.join(",", rowData));
      csvWriter2.append("\n");
    }

    for (List<String> rowData : rows3) {
      csvWriter3.append(String.join(",", rowData));
      csvWriter3.append("\n");
    }

    csvWriter1.flush();
    csvWriter1.close();

    csvWriter2.flush();
    csvWriter2.close();

    csvWriter3.flush();
    csvWriter3.close();

    this.outputFile1 = new CSVFile("AAA_2014J");
    CopyOnWriteArrayList<String> file1row1 = new CopyOnWriteArrayList<>();
    file1row1.add("5");
    file1row1.add("23");
    CopyOnWriteArrayList<String> file1row2 = new CopyOnWriteArrayList<>();
    file1row2.add("-10");
    file1row2.add("87");
    CopyOnWriteArrayList<String> file1row3 = new CopyOnWriteArrayList<>();
    file1row3.add("21");
    file1row3.add("658");
    this.outputFile1.addRow(file1row1);
    this.outputFile1.addRow(file1row2);
    this.outputFile1.addRow(file1row3);

    this.outputFile2 = new CSVFile("BBB_2016F");
    CopyOnWriteArrayList<String> file2row1 = new CopyOnWriteArrayList<>();
    file2row1.add("36");
    file2row1.add("472");
    CopyOnWriteArrayList<String> file2row2 = new CopyOnWriteArrayList<>();
    file2row2.add("-4");
    file2row2.add("2");
    this.outputFile2.addRow(file2row1);
    this.outputFile2.addRow(file2row2);

    this.outputFile3 = new CSVFile("CCC_2015H");
    CopyOnWriteArrayList<String> file3row1 = new CopyOnWriteArrayList<>();
    file3row1.add("10");
    file3row1.add("201");
    CopyOnWriteArrayList<String> file3row2 = new CopyOnWriteArrayList<>();
    file3row2.add("-3");
    file3row2.add("700");
    this.outputFile3.addRow(file3row1);
    this.outputFile3.addRow(file3row2);

    this.POISON = new CSVFile("poison");

    this.queue = new LinkedBlockingQueue<>();

    this.queue.put(this.outputFile1);
    this.queue.put(this.outputFile2);
    this.queue.put(this.outputFile3);
    this.queue.put(this.POISON);

    this.consumer1 = new CSVFileWriterConsumer(this.outputDir, this.queue, this.POISON);
    this.consumer2 = new CSVFileWriterConsumer(this.outputDir, this.queue, this.POISON);
    this.consumer3 = new CSVFileWriterConsumer(this.fakeOutputDir, this.queue, this.POISON);

  }

  @Test
  public void writeFile() throws IOException {
    Assert.assertEquals(0, outputDirectory.getRoot().listFiles().length);
    consumer1.writeFile(this.outputFile1);
    Assert.assertEquals(1, outputDirectory.getRoot().listFiles().length);

    //Making a list of the expected results
    ArrayList<String> expected = new ArrayList<>();
    expected.add("date,total_clicks");
    expected.add("5,23");
    expected.add("-10,87");
    expected.add("21,658");
    File[] outputFiles = outputDirectory.getRoot().listFiles();
    File file = new File(outputFiles[0].getPath());

    //Reading the output file and making sure each line is what is expected
    FileReader fr = new FileReader(file);
    BufferedReader br = new BufferedReader(fr);
    String buffer;
    String text = "";
    int counter = 0;
    while ((buffer = br.readLine()) != null) {
      text += buffer;
      Assert.assertEquals(expected.get(counter), buffer);
      counter++;
    }

    br.close();
    fr.close();
  }

  @Test
  public void run() throws InterruptedException {
    Assert.assertEquals(4, this.queue.size());
    Assert.assertEquals(0, outputDirectory.getRoot().listFiles().length);
    Thread testThread = new Thread(this.consumer1);
    testThread.start();
    testThread.join();
    Assert.assertEquals(0, this.queue.size());
    Assert.assertEquals(3, outputDirectory.getRoot().listFiles().length);
  }

  @Test
  public void testEqualsTrue() {
    Assert.assertTrue(this.consumer1.equals(this.consumer2));
  }

  @Test
  public void testEqualsFalse() {
    Assert.assertFalse(this.consumer1.equals(this.consumer3));
  }

  @Test
  public void testHashCode() {
    Assert.assertEquals(this.consumer1.hashCode(), this.consumer2.hashCode());
  }

  @Test
  public void testToString() {
    String toString = "CSVFileWriterConsumer{" +
        "queue=" + this.queue +
        ", outputDir='" + this.outputDir + '\'' +
        ", POISON=" + this.POISON +
        '}';
    Assert.assertEquals(toString, this.consumer1.toString());
  }

}