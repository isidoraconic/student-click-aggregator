package sequentialSolution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sequentialSolution.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CSVReaderTest {

    private CSVReader testReader1;
    private CSVReader testReader2;
    private String testFolder;
    private static final int TEST_CLICKS = 38091;
    private static final String[] STUDENT_HEADERS = {"module", "presentation", "student", "site", "date", "clicks"};
    private static final String STUDENT_CLICKS_TEST = "studentVle_sample.csv";

    @Before
    public void setUp() throws Exception {
        this.testFolder = "anonymisedData";
        this.testReader1 = new CSVReader(this.testFolder, "test");
        this.testReader2 = new CSVReader(this.testFolder, "test");
    }

    @Test (expected = NoSuchDirectoryException.class)
    public void setupNoValidFile() throws NoSuchDirectoryException {
        CSVReader failTest = new CSVReader("notavalid", "test");
    }

    @Test
    public void readCSVFile() throws IOException {
        HashMap<String, HashMap<String, Integer>> testAggData = this.testReader1.readCSVFile();
        int testTotalClicks = 0;
        for (String key : testAggData.keySet()) {
            HashMap<String, Integer> nested = testAggData.get(key);
            for (String subkey : nested.keySet()) {
                testTotalClicks += nested.get(subkey);
            }
        }
        Assert.assertEquals(TEST_CLICKS, testTotalClicks);
    }

    @Test
    public void testEquals() { Assert.assertEquals(this.testReader2, this.testReader1); }

    @Test
    public void testHashCode()  {
        Assert.assertEquals(this.testReader2.hashCode(), this.testReader1.hashCode());
    }

    @Test
    public void testToString() {
        String testString = "sequentialSolution.CSVReader{STUDENT_HEADERS=" + Arrays.toString(STUDENT_HEADERS) +
                ", csvFile='" + this.testFolder + "/" + STUDENT_CLICKS_TEST + '\'' + '}';
        Assert.assertEquals(testString, this.testReader1.toString());
    }
}