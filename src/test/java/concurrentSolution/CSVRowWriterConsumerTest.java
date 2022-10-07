package concurrentSolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

public class CSVRowWriterConsumerTest {

    private BlockingQueue<CSVRow> queue = new LinkedBlockingQueue<>();
    private BlockingQueue<CSVRow> fakeQueue = new LinkedBlockingQueue<>();
    private static final String[] OUTPUT_HEADER = {"module_presentation", "date", "total_clicks"};
    private CSVRow POISON = new CSVRow("poison", "", 0);
    private CSVRowWriterConsumer consumer1;
    private CSVRowWriterConsumer consumer2;
    private CSVRowWriterConsumer consumer3;
    private CSVPrinter csvPrinter;
    private Integer threshold = 25;
    private File sampleOutputFile;
    private File outputFile;
    private BufferedWriter bufferedWriter;
    private CSVRow row1;
    private CSVRow row2;
    private CSVRow row3;
    private CSVRow row4;
    private CSVRow row5;
    private Thread thread;
    private Integer poisonCount = 1;

    @Rule
    public TemporaryFolder sampleFileFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        this.sampleOutputFile = sampleFileFolder.newFile("activity-" + this.threshold + ".csv");
        this.row1 = new CSVRow("AAA_2014J", "10", 608);
        this.row2 = new CSVRow("BBB_2016H", "-4", 26);
        this.row3 = new CSVRow("CCC_2013G", "22", 46);
        this.row4 = new CSVRow("DDD_2015L", "56", 974);
        this.row5 = new CSVRow("EEE_2014N", "-9", 83);

        this.queue.put(row1);
        this.queue.put(row2);
        this.queue.put(row3);
        this.queue.put(row4);
        this.queue.put(row5);
        this.queue.put(this.POISON);

        //Writing a CSV file in Java adapted from the following link:
        //https://stackabuse.com/reading-and-writing-csvs-in-java/
        List<List<String>> rows = Arrays.asList(
            Arrays.asList("AAA_2014J", "10", "608"),
            Arrays.asList("BBB_2016H", "-4", "26"),
            Arrays.asList("CCC_2013G", "22", "46"),
            Arrays.asList("DDD_2015L", "56", "974"),
            Arrays.asList("EEE_2014N", "-9", "83")
        );

        //We are writing to the expected output file that we can use for comparison later
        PrintWriter csvWriter = new PrintWriter(this.sampleOutputFile);
        csvWriter.append("module_presentation");
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

        //Setting up the Printer object so that CSVRowWriterConsumer can use it
        //Making the file so that the producers can write to it
        FileWriter fileWriter;
        this.bufferedWriter = null;
        this.outputFile = outputDirectory.newFile("activity-" + this.threshold + ".csv");

        //Creating the empty csv file with the appropriate header
        try {
            fileWriter = new FileWriter(this.outputFile);
            bufferedWriter = new BufferedWriter(fileWriter);
            csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader(OUTPUT_HEADER));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Pass the csvPrinter object to the thread so that we dont need to keep opening and closing
        //the file once it has been opened and written to
        csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withSkipHeaderRecord());

        this.consumer1 = new CSVRowWriterConsumer(this.queue, this.POISON, this.csvPrinter, this.poisonCount);
        this.consumer2 = new CSVRowWriterConsumer(this.queue, this.POISON, this.csvPrinter, this.poisonCount);
        this.consumer3 = new CSVRowWriterConsumer(this.fakeQueue, this.POISON, this.csvPrinter, 4);

        this.thread = new Thread(this.consumer1);
        //thread.start();
    }

    @Test
    public void writeFile() throws IOException {
        Assert.assertEquals(1, outputDirectory.getRoot().listFiles().length);
        consumer1.writeFile(this.row1);
        Assert.assertEquals(1, outputDirectory.getRoot().listFiles().length);

        //At the end, once the thread is done (i.e. not alive), we close the file
        while(this.thread.isAlive()) {
            //While the thread is alive, we wait, and then once it ends, we move to the next block
        }

        //We now close the file (buffered writer) because we know the thread has finished
        //Need to close the file so that we can read from it and check that it was written properly
        try {
            assert bufferedWriter != null;
            bufferedWriter.close();
            System.out.println("Closed the buffered writer!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Making a list of the expected results
        ArrayList<String> expected = new ArrayList<>();
        expected.add("module_presentation,date,total_clicks");
        expected.add("AAA_2014J,10,608");
        File[] outputFiles = outputDirectory.getRoot().listFiles();
        File file = new File(outputFiles[0].getPath());

        //Reading the output file and making sure each line is what is expected
        //We only wrote one row so there should only be the header and one row
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
    public void run() throws InterruptedException, IOException {
        Assert.assertEquals(6, this.queue.size());
        this.thread.start();
        this.thread.join();
        Assert.assertEquals(0, this.queue.size());

        //At the end, once the thread is done (i.e. not alive), we close the file
        //Need to close the file so that we can read from it and check that it was written properly
        while(this.thread.isAlive()) {
            //While the thread is alive, we wait, and then once it ends, we move to the next block
        }

        //We now close the file (buffered writer) because we know the thread has finished
        //Need to close the file so we can read from it
        try {
            assert bufferedWriter != null;
            bufferedWriter.close();
            System.out.println("Closed the buffered writer!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Making a list of the expected results
        ArrayList<String> expected = new ArrayList<>();
        expected.add("module_presentation,date,total_clicks");
        expected.add("AAA_2014J,10,608");
        expected.add("BBB_2016H,-4,26");
        expected.add("CCC_2013G,22,46");
        expected.add("DDD_2015L,56,974");
        expected.add("EEE_2014N,-9,83");
        File[] outputFiles = outputDirectory.getRoot().listFiles();
        File file = new File(outputFiles[0].getPath());

        //Reading the output file and making sure each line is what is expected
        //Now all the rows we initialized should be present
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
    public void testEqualsTrue() {
        Assert.assertTrue(this.consumer1.equals(consumer2));
    }

    @Test
    public void testEqualsFalse() {
        Assert.assertFalse(this.consumer1.equals(consumer3));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(this.consumer1.hashCode(), this.consumer2.hashCode());
        Assert.assertNotEquals(this.consumer1.hashCode(), this.consumer3.hashCode());
    }

    @Test
    public void testToString() {
        String toString = "CSVRowWriterConsumer{" +
            "queue=" + this.queue +
            ", POISON=" + this.POISON +
            ", printer=" + this.csvPrinter +
            ", poisonCount=" + this.poisonCount +
            '}';
        Assert.assertEquals(toString, this.consumer1.toString());
    }

    @After
    public void tearDown() {
        //At the end, once the thread is done (i.e. not alive), we close the file
        while(this.thread.isAlive()) {
            //While the thread is alive, we wait, and then once it ends, we move to the next block
        }

        //We now close the file (buffered writer) because we know the thread has finished
        //If it hasn't been closed
        try {
            assert bufferedWriter != null;
            bufferedWriter.close();
            System.out.println("Closed the buffered writer!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}