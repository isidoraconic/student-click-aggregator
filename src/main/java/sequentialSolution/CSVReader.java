package sequentialSolution;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Class to represent a CSV reader object encapsulating the commons-csv library
 * Inputs a string path to the student clicks csv file, and then outputs a hashmap of click aggregations
 * Downstream the CSV writer will take the hashmap and write unique keys to different files
 */
public class CSVReader {

    private static final String[] STUDENT_HEADERS = {"module", "presentation", "student", "site", "date", "clicks"};
    private static final String MODULE_HEADER = "module";
    private static final String PRESENTATION_HEADER = "presentation";
    private static final String DATE_HEADER = "date";
    private static final String CLICKS_HEADER = "clicks";
    private static final String STUDENT_CLICKS = "studentVle.csv";
    private static final String STUDENT_CLICKS_TEST = "studentVle_sample.csv";
    private String csvFile;
    private String folderPath;

    /**
     * Constructor method for sequentialSolution.CSVReader object
     * @param csvFolder String path to folder containing CSV files
     * @param mode String mode switch for test or production of code
     * @throws NoSuchDirectoryException custom NoSuchDirectoryException error
     */
    public CSVReader(String csvFolder, String mode) throws NoSuchDirectoryException {
        if (!(new File(csvFolder).exists())) {
            throw new NoSuchDirectoryException("The specified folder path does not exist. "
                    + "Please enter a valid folder path.");
        } else {
            this.folderPath = csvFolder;
            if (mode.equals("test")) {
                this.csvFile = csvFolder + "/" + STUDENT_CLICKS_TEST;
            } else if (mode.equals("prod")) {
                this.csvFile = csvFolder + "/" + STUDENT_CLICKS;
            }
        }
        System.out.println("The CSV file is: " + this.csvFile);
    }

    /**
     * Method to get the folder path passed as command line argument
     * @return String folder path where CSV files are located
     */
    public String getFolderPath() { return this.folderPath; }

    /**
     * Method to get the full concatenated string file path
     * @return String full path to CSV file for reading
     */
    public String getFilePath() { return this.csvFile; }

    /**
     * Method that reads the CSV file and returns a data object containing the key lookup for clicks aggregated on module, presentation and date
     * The method creates the data hashmap object, and loops through the rows of the parsed CSV file
     * For each row, it looks in the hashmap for module + presentation key
     * If the key is found it looks in the nested hashmap for date key
     * If the key is found, it adds the clicks together for new total
     * Else it adds a new entry to the nested hashmap
     * Lastly if the concat string key is not found, it adds an entirely new entry
     * @return HashMap with String key and HashMap of value with Integers as key and value for date and sum clicks
     * @throws IOException default IOException error
     * @throws IllegalArgumentException default IllegalArgumentException error
     */
    public HashMap<String, HashMap<String, Integer>> readCSVFile() throws IOException, IllegalArgumentException {
//        Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
//        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
        Reader csvReader = Files.newBufferedReader(Paths.get(this.csvFile));
        CSVParser csvParser = new CSVParser(csvReader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withHeader(STUDENT_HEADERS)
//                .withIgnoreHeaderCase()
                );
        HashMap<String, HashMap<String, Integer>> aggStudentData = new HashMap<>();
        for (CSVRecord record : csvParser) {
            String module = record.get(MODULE_HEADER);
//            System.out.println("module = "+module);
            String presentation = record.get(PRESENTATION_HEADER);
            String codeKey = module + "_" + presentation;
//            System.out.println("The codekey is: " + codeKey);
            String date = record.get(DATE_HEADER);
            int clicks = Integer.parseInt(record.get(CLICKS_HEADER));

            // module and presentation code exists in HashMap
            if (aggStudentData.containsKey(codeKey)) {
                // date exists in HashMap
                if (aggStudentData.get(codeKey).containsKey(date)) {
                    Integer storedClicks = aggStudentData.get(codeKey).get(date);
                    Integer newClicks = storedClicks + clicks;
                    aggStudentData.get(codeKey).put(date, newClicks);
                } else { //date does not exist in HashMap
                    aggStudentData.get(codeKey).put(date, clicks);
                }
            } else { // module and presentation code don't exist in HashMap
                aggStudentData.put(codeKey, new HashMap<String, Integer>());
                aggStudentData.get(codeKey).put(date, clicks);
            }
        }
        return aggStudentData;
    }

    /**
     * Override method for default equals()
     * @param o class object for sequentialSolution.CSVReader
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CSVReader csvReader = (CSVReader) o;
        return Objects.equals(csvFile, csvReader.csvFile);
    }

    /**
     * Override method for default hashCode()
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(csvFile);
    }

    /**
     * Override method for default toString()
     * @return String
     */
    @Override
    public String toString() {
        return "sequentialSolution.CSVReader{" +
                "STUDENT_HEADERS=" + Arrays.toString(STUDENT_HEADERS) +
                ", csvFile='" + csvFile + '\'' +
                '}';
    }

}
