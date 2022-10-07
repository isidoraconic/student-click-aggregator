package concurrentSolution;

import static org.junit.Assert.*;

import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CSVFileTest {

  private CSVFile file1;
  private CSVFile file2;
  private CSVFile file3;


  @Before
  public void setUp() throws Exception {
    this.file1 = new CSVFile("BBB_2014J");
    CopyOnWriteArrayList<String> file1row1 = new CopyOnWriteArrayList<>();
    file1row1.add("-10");
    file1row1.add("464");
    CopyOnWriteArrayList<String> file1row2 = new CopyOnWriteArrayList<>();
    file1row2.add("26");
    file1row2.add("89");
    this.file1.addRow(file1row1);
    this.file1.addRow(file1row2);

    this.file2 = new CSVFile("BBB_2014J");
    CopyOnWriteArrayList<String> file2row1 = new CopyOnWriteArrayList<>();
    file2row1.add("-10");
    file2row1.add("464");
    CopyOnWriteArrayList<String> file2row2 = new CopyOnWriteArrayList<>();
    file2row2.add("26");
    file2row2.add("89");
    this.file2.addRow(file2row1);
    this.file2.addRow(file2row2);

    this.file3 = new CSVFile("DDD_2015H");

  }

  @Test
  public void addRow() {
    CopyOnWriteArrayList<String> file1row3 = new CopyOnWriteArrayList<>();
    file1row3.add("3");
    file1row3.add("16");
    Assert.assertEquals(2, this.file1.getFileContent().size());
    this.file1.addRow(file1row3);
    Assert.assertEquals(3, this.file1.getFileContent().size());
  }

  @Test
  public void getFileContent() {
    CopyOnWriteArrayList<CopyOnWriteArrayList<String>> data = this.file1.getFileContent();
    Assert.assertEquals(2, data.size());
    CopyOnWriteArrayList<String> file1row1 = data.get(0);
    CopyOnWriteArrayList<String> file1row2 = data.get(1);
    Assert.assertEquals("-10", file1row1.get(0));
    Assert.assertEquals("464", file1row1.get(1));
    Assert.assertEquals("26", file1row2.get(0));
    Assert.assertEquals("89", file1row2.get(1));
  }

  @Test
  public void getName() {
    Assert.assertEquals("BBB_2014J", this.file1.getName());
  }

  @Test
  public void testEqualsTrue() {
    Assert.assertTrue(this.file1.equals(this.file2));
  }

  @Test
  public void testEqualsFalse() {
    Assert.assertFalse(this.file1.equals(this.file3));
  }

  @Test
  public void testEqualsDiffObject() {
    String test = "test";
    Assert.assertFalse(this.file1.equals(test));
  }

  @Test
  public void testHashCode() {
    Assert.assertEquals(this.file1.hashCode(), this.file2.hashCode());
  }

  @Test
  public void testToString() {
    String toString = "CSVFile{" +
        "name='" + "BBB_2014J" + '\'' +
        '}';
    Assert.assertEquals(toString, this.file1.toString());
  }
}