package sequentialSolution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class to represent the main file controller that will drive the CSV reading and writing processes
 */
public class SequentialDriver {

    /**
     * Main method to drive the program, controls both the CSVReader and CSVWriter classes
     * @param args CLI arguments passed when program runs
     * @throws IOException default IOException error
     * @throws NoSuchDirectoryException custom NoSuchDirectoryException error
     * @throws NullCommandLineArgument custom exception error when command line args is null
     */
    public static void main(String[] args) throws IOException, NoSuchDirectoryException, NullCommandLineArgument {
        if (args.length < 1) {
            throw new NullCommandLineArgument("The command line argument was null/empty. Please provide a valid folder path to CSV data.");
        }
        String inputFilePath = args[0];
        String outputDir = args[0];
        CSVReader reader = new CSVReader(inputFilePath, "prod"); // need to set to "prod" before submission!!!
        CSVWriter writer = new CSVWriter(outputDir);
        writer.writeFiles(reader);
    }
}
