package concurrentSolution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.CountDownLatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import sequentialSolution.NoSuchDirectoryException;
import sequentialSolution.NullCommandLineArgument;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class to represent the primary driver of the concurrent solution
 * Drives the instantiation of multiple producer and consumer threads for both reading and writing CSV files
 */
public class ConcurrentDriver {

    private static final CSVRow readerPoison = new CSVRow("thread", "killer", 0, 0, "", 0);
    private static final CSVFile writerFilePoison = new CSVFile("poison");
    private static final CSVRow writerRowPoison = new CSVRow("poison", "", 0);
    private static final String[] THRESHOLD_OUTPUT_HEADER = {"module_presentation", "date", "total_clicks"};
    private static final int QUEUE_BOUND = 10;
    private static final int N_PRODUCERS = 2;
    private static final int N_CONSUMERS = 3; //Runtime.getRuntime().availableProcessors();
    private static final int N_POISON_PER_PRODUCER = N_CONSUMERS / N_PRODUCERS;
    private static final int N_POISON_PILL_REMAIN = N_CONSUMERS % N_PRODUCERS;

    /**
     * Main method that will drive the producer-consumer design pattern for concurrent operation of CSV read/write
     * @param args String array of arguments passed in command line
     * @throws NoSuchDirectoryException custom exception error when no directory found
     * @throws InterruptedException default InterruptedException error for Threads
     * @throws NullCommandLineArgument custom exception error when the command line args are null
     * @throws InvalidThresholdValue custom exception error when the threshold value is not numeric
     * @throws IOException default IOException error
     */
    public static void main(String[] args)
        throws NoSuchDirectoryException, InterruptedException, NullCommandLineArgument, InvalidThresholdValue, IOException {

        if (args.length < 1) {
            throw new NullCommandLineArgument("The command line argument was null/empty. Please provide a valid folder path to CSV data.");
        } else if (args.length == 1) {
            String cliPath = args[0];
            ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> aggStudentData = readStudentData(cliPath);
            writeMultipleFiles(cliPath, aggStudentData);
        } else {
            try {
                String cliPath = args[0];
                Integer threshold = Integer.parseInt(args[1]); // need to check data type or throw error
                ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> aggStudentData = readStudentData(cliPath);
                writeSingleThresholdFile(cliPath, threshold, aggStudentData);
            } catch (NumberFormatException | IOException e) {
                throw new InvalidThresholdValue("Threshold value provided was not a valid integer. Please rerun program with integer value for the threshold parameter.");
            }
        }

    }

    /**
     * Method to instantiate the CSV reader producer and consumer threads that open and read the CSV file rows
     * Rows are stored in a blockingqueue while the producer and consumer work with them, and aggregated data is stored in a concurrenthashmap
     * @param path String folder path for file input
     * @return ConcurrentHashMap with String codeKey and value as nested ConcurrentHashmap with String date and Integer clicks as valaue
     * @throws NoSuchDirectoryException custom NoSuchDirectoryException when no directory exists
     * @throws InterruptedException default InterruptedException error for Thread interruption
     */
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> readStudentData(String path) throws NoSuchDirectoryException, InterruptedException {
        BlockingQueue<CSVRow> readerQueue = new LinkedBlockingQueue<CSVRow>(QUEUE_BOUND);
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> aggStudentData = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();

        // reader threads go here
        Thread producer = new Thread(new CSVReaderProducer(path, "prod", readerQueue, readerPoison, N_CONSUMERS));
        producer.start();
        for (int i = 0; i < N_CONSUMERS; i++) {
            new Thread(new ClickAggregatorConsumer(readerQueue, aggStudentData, readerPoison)).start();
        }

        // sleep between CSV reader and writer Thread workflows
        producer.join();

//        System.out.println("Student data has " + aggStudentData.size() + " keys");

        return aggStudentData;
    }

    /**
     * Method to instantiate the CSV writing producer and consumer threads that open and write CSV files with aggregated student data
     * It takes files of data stored in the concurrenthashmap, and writes a new file for each String key post aggregation
     * @param path String folder path for file output
     * @param studentData ConcurrentHashMap data structure with aggregated student clicks
     * @throws NoSuchDirectoryException custom NoSuchDirectoryException when no directory exists
     */
    public static void writeMultipleFiles(String path, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> studentData) throws NoSuchDirectoryException {
        BlockingQueue<CSVFile> writerQueue = new LinkedBlockingQueue<CSVFile>(QUEUE_BOUND);

        //List of keys for the writer producer and consumer to use
        CopyOnWriteArrayList<String> keyList = new CopyOnWriteArrayList<>(studentData.keySet());

        //Writer threads go here
        for(int i = 0; i < N_PRODUCERS; i++) {
            new Thread(new CSVFileProducer(writerQueue, writerFilePoison, studentData, N_CONSUMERS, keyList)).start();
        }

        for(int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new CSVFileWriterConsumer(path, writerQueue, writerFilePoison)).start();
        }
    }

    /**
     * Method to instantiate the CSV writing producer and consumer threads that open and write a single CSV file when click aggregations are over a provided threshold
     * It takes files of data stored in the concurrenthashmap, and writes rows of data filtered on the threshold value
     * @param path String folder path for file output
     * @param threshold Integer value for threshold
     * @param studentData ConcurrentHashMap data structure with aggregated student clicks
     * @throws IOException default IOException error
     * @throws InterruptedException default InterruptedException error for Thread interruption
     */
    public static void writeSingleThresholdFile(String path, Integer threshold, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> studentData)
            throws IOException, InterruptedException {
        BlockingQueue<CSVRow> writerQueue = new LinkedBlockingQueue<CSVRow>(QUEUE_BOUND);

        //List of keys for the writer producer and consumer to use
        CopyOnWriteArrayList<String> keyList = new CopyOnWriteArrayList<>(studentData.keySet());

        //Multiple producers
//        System.out.println("Student data has " + studentData.size() + " keys");
        for(int i = 0; i < N_PRODUCERS; i++) {
            new Thread(new CSVRowProducer(writerQueue, writerRowPoison, studentData, 1, keyList, threshold)).start();
        }

        //Making the file so that the producers can write to it
        FileWriter fileWriter;
        BufferedWriter bufferedWriter = null;
        CSVPrinter csvPrinter;

        //Creating the empty csv file with the appropriate header
        try {
            fileWriter = new FileWriter(path + "/" + "activity-" + threshold + ".csv");
            bufferedWriter = new BufferedWriter(fileWriter);
            csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader(THRESHOLD_OUTPUT_HEADER));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Pass the csvPrinter object to the thread so that we dont need to keep opening and closing
        //the file once it has been opened and written to
        csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withSkipHeaderRecord());
        Thread writer = new Thread(new CSVRowWriterConsumer(writerQueue, writerRowPoison, csvPrinter, 2));
        writer.start();

        //At the end, once the thread is done (i.e. not alive), we close the file
        while(writer.isAlive()) {
            //While the thread is alive, we wait, and then once it ends, we move to the next block
        }

        //We now close the file (buffered writer) because we know the thread has finished
        try {
            assert bufferedWriter != null;
            bufferedWriter.close();
            System.out.println("Closed the buffered writer!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}