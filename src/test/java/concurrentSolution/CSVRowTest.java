package concurrentSolution;

import com.sun.tools.jdeprscan.CSV;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CSVRowTest {

    private CSVRow testRow1;
    private CSVRow testRow2;
    private CSVRow testRow3;
    private CSVRow outputRow;

    @Before
    public void setUp() throws Exception {
        this.testRow1 = new CSVRow("AAA", "1111", 1, 1, "1", 0);
        this.testRow2 = new CSVRow("AAA", "1111", 1, 1, "1", 0);
        this.testRow3 = new CSVRow("BBB", "1111", 1, 1, "1", 0);
        this.outputRow = new CSVRow("AAA_1111", "1", 0);
    }

    @Test
    public void getCodeKey() {
        String testConcat = "AAA_1111";
        String codeCalled = this.testRow1.getCodeKey();
        Assert.assertEquals(testConcat, codeCalled);
    }

    @Test
    public void getDate() {
        String testDate = "1";
        String dateCalled = this.testRow1.getDate();
        Assert.assertEquals(testDate, dateCalled);
    }

    @Test
    public void getClicks() {
        Integer testClick = 0;
        Integer clickCalled = this.testRow1.getClicks();
        Assert.assertEquals(testClick, clickCalled);
    }

    @Test
    public void testEquals() { Assert.assertEquals(this.testRow1, this.testRow2); }

    @Test
    public void testNotEquals() { Assert.assertNotEquals(this.testRow1, this.testRow3); }

    @Test
    public void testHashCode() { Assert.assertEquals(this.testRow1.hashCode(), this.testRow2.hashCode()); }

    @Test
    public void testToString() {

        String module = "AAA";
        String presentation = "1111";
        int student = 1;
        int site = 1;
        String date = "1";
        int clicks = 0;
        String codeKey = "AAA_1111";

        String testToString = "CSVRow{" +
                "module='" + module + '\'' +
                ", presentation='" + presentation + '\'' +
                ", student=" + student +
                ", site=" + site +
                ", date=" + date +
                ", clicks=" + clicks +
                ", codeKey='" + codeKey + '\'' +
                '}';

        String toStringCalled = this.testRow1.toString();

        Assert.assertEquals(testToString, toStringCalled);
    }
}