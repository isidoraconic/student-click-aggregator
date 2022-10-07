package concurrentSolution;

import sequentialSolution.NoSuchDirectoryException;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to represent a Producer that reads a CSV file of student click data
 * Since it is designed to work in a Thread, it needs to implement Runnable and override run()
 * The class just opens and parses the rows of a CSV file from a folder path provided by user
 * The rows are parsed and written to a BlockingQueue for multiple consumers to take from
 */
public class CSVReaderProducer implements Runnable {

    private static final String STUDENT_CLICKS = "studentVle.csv";
    private static final String STUDENT_CLICKS_TEST = "studentVle_sample.csv";
    private static final String SPLIT_BY = "(?<=\\\")[^,;](.*?)(?=\\\")";
    private String csvFile;
    private final String folderPath;
    private final String mode;
    private final BlockingQueue<CSVRow> queue;
    private final CSVRow poison;
    private final int N_POISON_PER_PRODUCER;
    private AtomicInteger fileSize;

    /**
     * Constructor method for the CSVReaderProducer class
     * @param csvFolder String path to the folder
     * @param mode String of either test or prod mode to switch the hard-coded file name
     * @param queue BlockingQueue from Driver that the CSV rows will be stored in
     * @param poison CSV row poison pill that will kill each consumer thread
     * @param N_POISON_PER_PRODUCER
     * @throws NoSuchDirectoryException
     */
    CSVReaderProducer(String csvFolder, String mode, BlockingQueue<CSVRow> queue, CSVRow poison, int N_POISON_PER_PRODUCER) throws NoSuchDirectoryException {
        if (!(new File(csvFolder).exists())) {
            throw new NoSuchDirectoryException("The specified folder path does not exist. "
                    + "Please enter a valid folder path.");
        } else {
            this.mode = mode;
            this.folderPath = csvFolder;
            if (mode.equals("test")) {
                this.csvFile = csvFolder + "/" + STUDENT_CLICKS_TEST;
            } else if (mode.equals("prod")) {
                this.csvFile = csvFolder + "/" + STUDENT_CLICKS;
            }
            this.queue = queue;
            this.poison = poison;
            this.N_POISON_PER_PRODUCER = N_POISON_PER_PRODUCER;
            this.fileSize = new AtomicInteger(0);
        }
    }

    /**
     * Parses the input line from BufferedReader into a StringTokenizer which is stored in an ArrayList
     * @param inputString String from the BufferedReader
     * @return ArrayList of strings for each row element/column
     */
    private ArrayList<String> patternMatch(String inputString) {
        ArrayList<String> parsedString = new ArrayList<>();
        if (mode.equals("test")) {
            StringTokenizer tokenizer = new StringTokenizer(inputString, ",");
            while (tokenizer.hasMoreTokens()) {
                parsedString.add(tokenizer.nextToken());
            }
        } else {
            Pattern pattern = Pattern.compile(SPLIT_BY);
            Matcher matcher = pattern.matcher(inputString);
            while (matcher.find()) {
                parsedString.add(matcher.group());
            }
        }
        return parsedString;
    }

    /**
     * Takes the tokenized string from BufferedReader and creates a new InbouncCSVRow object
     * @param line String line from the BufferedReader
     * @return InbouncCSVRow custom class to store in BlockingQueue
     */
    public CSVRow parseCSVRow(String line) {
        ArrayList<String> parsedRow = patternMatch(line);
//        System.out.println(parsedRow);
        String module = parsedRow.get(0);
        String presentation = parsedRow.get(1);
        Integer student = Integer.parseInt(parsedRow.get(2));
        Integer site = Integer.parseInt(parsedRow.get(3));
        String date = parsedRow.get(4);
        Integer clicks = Integer.parseInt(parsedRow.get(5));
//        System.out.println(module + " " + presentation + " " + student + " " + site + " " + date + " " + clicks);
        return new CSVRow(module, presentation, student, site, date, clicks);
    }

    /**
     * Method to get the size of the CSV file in row count
     * @return int of the file size
     */
    public int getFileSize() { return this.fileSize.get(); }

    /**
     * Method to get the full string file path of the CSV
     * @return String of the file path in folder
     */
    public String getCsvFile() { return this.csvFile; }

    /**
     * Override of Runnable.run() method to take each line from CSV file and pass to BlockingQueue
     * At the end when there is nothing else to read, it stashes a poison pill for each consumer
     */
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.csvFile))) {
            String headers = reader.readLine();
            String newLine;
            while ((newLine = reader.readLine()) != null) {
//                System.out.println("Parsing row: " + newLine);
                queue.put(parseCSVRow(newLine));
                this.fileSize.getAndIncrement();
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            while (true) {
                try {
                    for (int i=0; i < this.N_POISON_PER_PRODUCER; i++) {
//                        System.out.println(Thread.currentThread().getName() + " adding poison pill to BlockingQueue");
                        queue.put(this.poison);
                        this.fileSize.getAndIncrement();
                    }
                    break;
                } catch (InterruptedException e) {
                    try {
                        throw new InterruptedException(Thread.currentThread().getName() + " has been interrupted.");
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Override of default equals() method
     * @param o object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CSVReaderProducer that = (CSVReaderProducer) o;
        return Objects.equals(csvFile, that.csvFile) &&
                Objects.equals(folderPath, that.folderPath) &&
                Objects.equals(queue, that.queue);
    }

    /**
     * Override of default hashCode() method
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(csvFile, folderPath, queue);
    }

    /**
     * Override of default toString() method
     * @return String
     */
    @Override
    public String toString() {
        return "CSVReaderProducer{" +
                "csvFile='" + csvFile + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", queue=" + queue +
                '}';
    }
}
