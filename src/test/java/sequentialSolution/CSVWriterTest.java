package sequentialSolution;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CSVWriterTest {

  private CSVWriter writer1;
  private CSVWriter writer2;
  private CSVWriter writer3;
  private CSVReader reader;
  private File sampleFile;
  private static final String[] HEADER = {"date", "total_clicks"};
  private static final String DATE_HEADER = "date";
  private static final String CLICKS_HEADER = "total_clicks";

  @Rule
  public TemporaryFolder sampleFileFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder outputDirectory = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    this.sampleFile = sampleFileFolder.newFile("studentVle_sample.csv");

    //Writing a CSV file in Java adapted from the following link:
    //https://stackabuse.com/reading-and-writing-csvs-in-java/
    List<List<String>> rows = Arrays.asList(
        Arrays.asList("FFF", "2014J", "646551", "882597", "227", "3"),
        Arrays.asList("FFF", "2014J", "487314", "798620", "227", "8"),
        Arrays.asList("FFF", "2014J", "511678", "703741", "90", "19"),
        Arrays.asList("CCC",	"2014J",	"645334",	"909098",	"240",	"1"),
        Arrays.asList("CCC",	"2014J",	"644246",	"909088",	"239",	"5"),
        Arrays.asList("AAA",	"2014J",	"323370",	"877026",	"95",	"2")
    );

    PrintWriter csvWriter = new PrintWriter(this.sampleFile);
    csvWriter.append("code_module");
    csvWriter.append(",");
    csvWriter.append("code_presentation");
    csvWriter.append(",");
    csvWriter.append("id_student");
    csvWriter.append(",");
    csvWriter.append("id_site");
    csvWriter.append(",");
    csvWriter.append("date");
    csvWriter.append(",");
    csvWriter.append("sum_click");
    csvWriter.append("\n");

    for (List<String> rowData : rows) {
      csvWriter.append(String.join(",", rowData));
      csvWriter.append("\n");
    }

    csvWriter.flush();
    csvWriter.close();

    writer1 = new CSVWriter(outputDirectory.getRoot().getAbsolutePath()+"/");
    writer2 = new CSVWriter(outputDirectory.getRoot().getAbsolutePath()+"/");
    writer3 = new CSVWriter(sampleFileFolder.getRoot().getAbsolutePath()+"/");
    reader = new CSVReader(sampleFileFolder.getRoot().toString(), "test");
    System.out.println("The sample file absolute path is: "+sampleFile.getAbsolutePath());
  }

  @Test (expected = NoSuchDirectoryException.class)
  public void invalidConstructor() throws NoSuchDirectoryException {
    writer2 = new CSVWriter("fake_directory");
  }

  @Test
  public void writeFiles() throws IOException {
    writer1.writeFiles(reader);
    Assert.assertEquals(3, outputDirectory.getRoot().listFiles().length);
    File[] outputFiles = outputDirectory.getRoot().listFiles();
    List<File> outputFileList = Arrays.asList(outputFiles);

    //Making a nested HashMap of the expected results
    HashMap<String, ArrayList<String>> expected = new HashMap<>();
    ArrayList<String> a = new ArrayList<>();
    ArrayList<String> c = new ArrayList<>();
    ArrayList<String> f = new ArrayList<>();
    expected.put("AAA_2014J.csv", a);
    expected.put("CCC_2014J.csv", c);
    expected.put("FFF_2014J.csv", f);
    a.add("date,total_clicks");
    a.add("95,2");
    c.add("date,total_clicks");
    c.add("239,5");
    c.add("240,1");
    f.add("date,total_clicks");
    f.add("227,11");
    f.add("90,19");


    Assert.assertEquals(3, outputFileList.size());

    FileReader fr = null;
    BufferedReader br = null;
    int counter = 0;
    int size = 0;
    for(File file : outputFileList) {
      Assert.assertTrue(expected.containsKey(file.getName()));
      fr = new FileReader(file.getAbsolutePath());
      br = new BufferedReader(fr);
      size = expected.get(file.getName()).size();

      String buffer;
      String text = "";
      while ((buffer = br.readLine()) != null && counter < size) {
        Assert.assertEquals(expected.get(file.getName()).get(counter), buffer);
        text += buffer;
        counter++;
      }
      counter = 0;
    }

    br.close();
    fr.close();

  }

  @Test
  public void testEqualsTrue() {
    Assert.assertEquals(writer1, writer2);
  }

  @Test
  public void testEqualsFalse() {
    Assert.assertNotEquals(writer1, writer3);
  }

  @Test
  public void testEqualsDifferentObject() {
    String s = "hello";
    Assert.assertNotEquals(writer1, s);
  }

  @Test
  public void testHashCodeSame() {
    Assert.assertEquals(writer1.hashCode(), writer2.hashCode());
  }

  @Test
  public void testToString() {
    String expected = "CSVWriter{" +
        "outputDir='" + outputDirectory.getRoot().getAbsolutePath() + "/" + '\'' +
        '}';
    Assert.assertEquals(expected, writer1.toString());
  }

  @After
  public void tearDown() {
    sampleFileFolder.delete();
    outputDirectory.delete();
  }
}