# OULAD Student Click Aggregator

This project involves reading a very large dataset from the Open University Learning Analytics Dataset (OULAD) which is collected from students in online courses in the UK. The dataset used for this project is `studentVle.csv` which has over 10 million rows of data, containing module codes and presentation codes, the student and site IDs, as well as the date in days from assessment, and the sum total of clicks by a given student on that date. The tasks is to read this file (.5GB) with both a sequential and concurrent solution. Given the file is 10 million rows, the sequential solution will be very slow, while the concurrent solution should take seconds.  The goal ultimately is to take the large file and aggregate the clicks on the module/presentation codes and date. 

The sequential solution design is very basic and linear, while the concurrent solution required the use of multiple producer/consumer pairs to concurrently read, parse, aggregate, fetch, and write to CSV file output. With the concurrent solution, two versions are provided which output different files.  The first concurrent solution does the exact same thing as the sequential solution, which writes a different file (22 in total) for each unique combination of module and presentation codes. The second concurrent solution takes in a threshold value after the folder path that is used to filter the aggregated rows of data to days with clicks above the threshold. For example, if 10000 is passed as the second agrument, then all codes and days with clicks equal to or over 10000 will be written into a single CSV file.

## Setup

There are two core dependencies for this Java application to work, and both libraries come from the Apache Commons:

```
CSVParser -> https://mvnrepository.com/artifact/org.apache.commons/commons-csv
ThreadUtils -> https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
```

You will need to make sure that these libraries are downloaded and available in your global java library for the application to work if running the application within an IDE or CLI. If you are running using the compiled .jar file below with command line arguments, then it should run just fine.

**Make sure you are in assignment 5 folder when running the program in terminal!**

## Usage

In order to run this application, you simply need to enter one commandline with either one or two arguments, the first which is the full or implicit directory path to a folder containing CSV data files, and the second is a threshold:

```
Sequential Solution:
java -jar sequentialSolution.jar ./anonymisedData

Concurrent Solution 1:
java -jar concurrentSolution.jar ./anonymisedData

Concurrent Solution 2:
java -jar concurrentSolution.jar ./anonymisedData 10000
```

The program will then execute by itself and the user will quickly see files being created in the same input folder passed into the command line argument.

## Examples

Example command line arguments for generating aggregated CSV files from studnet data:

```
Example 1:
java -jar sequentialSolution.jar ./anonymisedData
The CSV file is: ./anonymisedData/studentVle.csv
[Program Finishes - there are now 22 CSV files in the folder]

Example 2:
java -jar concurrentSolution.jar ./anonymisedData 10000
Made the writer
Started the writer!
Closed the buffered writer!
[Program Finishes - there is now a new CSV file activity-10000.csv in the folder]
```

## Classes and Methods
#### Classes are in order of low-level to high level

## Classes and Key Methods

**SEQUENTIAL SOLUTION:**
- `SequentialDriver` **class**: the primary class that represents the driver/controller for the sequential solution which "drives" the CSV reading and writing processes.
 - `main(String[] args)` **method**: Main method is the entry point to running the sequential solution, controlling both the CSVReader and CSVWriter classes.
- `CSVReader` **class**: class to represent a CSV reader which encapsulates the CSVParser of the commons-csv library. It inputs a string (from SequentialDriver.java) pfolder path to the student data CSV files, and aggregates each row sequentially into a hashmap with key as the concatenated string of module and presentation codes, and a nested hashmap with key as the date and value as the total sum of clicks.
 - `readCSVFile()` **method**: primary method that instantiates the CSVParser, checks the existing records in the hashmap and creates new elements or adds the values together for existing key and subkey pairs that match. 
- `CSVWriter` **class**: class to represent a CSV writer which encapsulates the CSVPrinter of the commons-csv library. it takes the nested hashmap structure from the CSVReader class, and creates a new file for each distinct code key and date key pair, with dates and clicks in the rows.
 - `writeFiles(CSVReader reader)` **method**: primary method that instantiates the FileWriter, BufferedWriter, and CSVPrinter, and parses the hashmap into separate objects to write to CSV file.

**CONCURRENT SOLUTION:**

- `ConcurrentDriver` **class**: the primary class that represents the driver/controller for the concurrent solution which "drives" the CSV reading and writing processes. It also handles when there is more than one command line argument for the threshold.
 - `main(String[] args)` **method**: Main method is the entry point to running the concurrent solution, controlling the workflow for the reading and writing classes. No matter what, a concurrenthashmap is built by following the `readStudentData()` branch. Then based on the command line arguments, if there is no threshold, then the application runs the `writeMultipleFiles()` branch, else it runs the `writeSingleThresholdFile` branch.
 - `readStudentData(String path)` **method**: Method to instantiate the CSV reader producer and consumer threads that open and read the CSV file rows. Rows are stored in a blockingqueue while the producer and consumer work with them, and aggregated data is stored in a concurrenthashmap.
 - `writeMultipleFiles(String path, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> studentData)` **method**: Method to instantiate the CSV writing producer and consumer threads that open and write CSV files with aggregated student data. It takes files of data stored in the concurrenthashmap, and writes a new file for each String key post aggregation
 - `writeSingleThresholdFile(String path, Integer threshold, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> studentData)` **method**: Method to instantiate the CSV writing producer and consumer threads that open and write a single CSV file when click aggregations are over a provided threshold. It takes files of data stored in the concurrenthashmap, and writes rows of data filtered on the threshold value.
- `CSVRow` **class**: Simple class to represent the row of a CSV file with raw student click data. Only has a constructor and getter methods for primary properties.
- `CSVReaderProducer` **class**: Class to represent a Producer Thread that reads a CSV file of student click data. It is a Thread so it implements Runnable and overrides run() method. The class opens and parses the rows of a CSV file from provided String folder path in command line, and rows are parsed and written to a blockingqueue for multiple consumers to take from.
 - `patternMatch(String line)` **method**: Method to match the CSV comma delineation, depending on if the mode is test or prod. If test, then it uses StringTokenizer, otherwise it uses regex pattern to parse comma and quote delineated files.
 - `parseCSVRow(String line)` **method**: Method to split the parse single line from buffered reader into 6 elements that are used to create a new CSVRow object
 - `run()` **method**: Override method that executes when the producer Thread start() method is called from ConcurrentDriver. Puts a CSVRow onto the blockingqueue, and finally places poison pills based on how many consumer threads are created.
- `ClickAggregatorConsumer` **class**: Class to represent a Consumer Thread that takes CSVRow objects from the blockingqueue, and aggregates the row click data into the shared concurrenthashmap datastructure.
 - `writeToHash(String codeKey, String date, Integer clicks)` **method**: Method to write student clicks into the concurrenthashmap data structure. Puts a new key/value pair if it does not exist, otherwise will update existing key/value pairs with cumulative sum.
 - `getClicksAmount(String codeKey, String date)` **method**: Method to return the clicks aggregated for a code and date key pair.
 - `run()` **method**: Override method that executes when the consumer Thread start() method is called from ConcurrentDriver. Takes a CSVRow from the blockingqueue, and stores in the concurrethashmap using `writeTohash()` method. When the consumer eats a poison pill, it interrupts the thread and kills it.
- `CSVFile` **class**: Simple class to represent the CSV file with aggregated student click data. Only has a constructor and getter methods for primary properties. It is a Thread so it implements Runnable and overrides run() method. The class takes parsed CSVRow objects from the blockingqueue, and aggregates to the concurrenthashmap using the `writeToHash()` method.
- `CVFileProducer` **class**: Class to represent a Producer Thread that takes CSVFile objects off of the concurrenthashmap data structure, and puts them on a blockingqueue for downstream Consumer Thread. It is a Thread so it implements Runnable and overrides run() method.
 - `getMapElement()` **method**: Method that removes CSVFile objects off of the concurrenthashmap based on keys, creates actual CSVFile objects with that data, and then returns the file object to be stashed on the blockingqueue for Consumer Thread to take and write to CSV file output.
 - `run()` **method**: Override method that executes when the Producer Thread start() method is called from the ConcurrentDriver. It puts CSVFile objects on the blocking queue using `getMapElement()`, and finally places poison pills based on how many consumer threads are created.
- `CSVFileWriterConsumer` **class**: Class to represent a Consumer Thread that takes CSVFile objects from the blockingqueue, and writes the output to unique CSV files in a specified output directory path. It is a Thread so it implements Runnable and overrides run() method.
 - `writeFile(CSVFile fileContent)` **method**: Method that takes ina CSVFile object and writes it to CSV file using CSVPrinter from commons-csv library.
 - `run()` **method**: Override method that executes when the Producer Thread start() method is called from the ConcurrentDriver. It takes CSVFile objects from the blocking queue and writes them to CSV using the `writeFile()` method. When the consumer eats a poison pill, it interrupts the thread and kills it.
- `CSVRowProducer` **class**: Class to represent a Producer Thread that takes CSVFile objects off of the concurrenthashmap data structure, and puts them on a blockingqueue in the format of a CSVRow for downstream Consumer Thread. It is a Thread so it implements Runnable and overrides run() method.
 - `getMapElement()` **method**: Method that removes CSVFile objects off of the concurrenthashmap based on keys, creates a variant of the CSVRow object with that data, and then returns the row objects to be stashed on the blockingqueue for Consumer Thread to take and write to CSV file output.
 - `run()` **method**: Override method that executes when the Producer Thread start() method is called from the ConcurrentDriver. It puts CSVRow objects on the blocking queue using `getMapElement()`, and finally places poison pills based on how many consumer threads are created.
- `CSVRowWriterConsumer` **class**: Class to represent a Consumer Thread that takes CSVRow objects from the blockingqueue, and writes easy row to a single CSV file based on the click threshold filter. It is a Thread so it implements Runnable and overrides run() method.
 - `writeFile(CSVRow row)` **method**: Method that takes ina CSVRow object and writes it to CSV file using printRecord based on the CSVPrinter class from commons-csv library. It keeps the single CSV file open while continuously writing to it.
 - `run()` **method**: Override method that executes when the Producer Thread start() method is called from the ConcurrentDriver. It takes CSVFile objects from the blocking queue and writes them to CSV using the `writeFile()` method. When the consumer eats a poison pill, it interrupts the thread and kills it.


## Assumptions and Edge Cases

1) It is assumed that the user is passing a command line argument, and passing in a valid string path to a directory on their computer that stores the file `studentVle.csv`.
2) It is assumed that the file has data in it, and that the columns are ordered such that the columns are {"module", "presentation", "student", "site", "date", "clicks"}.
3) A variety of edge cases are handled in the normal producting execution of the application and in testing as well, e.g.:
 - When user doesn't provide a command line argument string
 - The folder path provided is invalid (aka does not exist on the computer)
 - The threshold argument value is non-numeric (needs to be an integer, or can be cast as one)
 - Flexibility in the number of producers and consumers with how many poison pills are implanted into the blockingqueues
 - Check that there are more lines of data for the BufferedReader to fetch from CSV data files, in other words scans for end of file implicitly
 - Check that both the concurrenthashmap and the String key list isn't empty when putting CSVFile objects on to the blockingqueue for writing files
 - Created two versions of the CSVReaderProducer on "test" and "prod" that allowed us to test on smaller sample of the larger file
 - Checking various scenarios for when constructing and destroying files and directories in unit tests to make sure output is as expected
 - Making sure that the blockingqueue sizes increase and grow to the correct size after Producer Threads put, and likewise the blockingqueue is empty after Consumer Threads finish taking.
