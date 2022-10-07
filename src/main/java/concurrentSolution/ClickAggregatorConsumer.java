package concurrentSolution;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to represent a Consumer that takes student click data CSV rows from BlockingQueue
 * Since it is designed to work in a Thread, it needs to implement Runnable and override run()
 * The class just takes parsed rows of a CSV file placed into a Blocking Queue
 * Then the consumer aggregates the clicks for each unique module and presentation and date
 */
public class ClickAggregatorConsumer implements Runnable {

    private final BlockingQueue<CSVRow> queue;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> aggStudentData;
    private final CSVRow poison;

    /**
     * Constructor method for ClickAggregatorConsumer class
     * @param queue BlockingQueue from Driver that the CSV rows will be stored in
     * @param aggStudentData ConcurrentHashMap object to aggregate the student clicks
     * @param poison CSV row poison pill that will kill each consumer thread
     */
    ClickAggregatorConsumer(BlockingQueue<CSVRow> queue,
                            ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> aggStudentData,
                            CSVRow poison) {
        this.queue = queue;
        this.aggStudentData = aggStudentData;
        this.poison = poison;
    }

    /**
     * Method to write student clicks into the ConcurrentHashMap
     * Checks if codeKey (module + presentation) is present and date
     * Then puts new value or updates value of existing value
     * @param codeKey concatenated String of module and presentation codes
     * @param date String of the date since days of the assessment
     * @param clicks Integer clicks by the student
     */
    public void writeToHash(String codeKey, String date, Integer clicks) {
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
            aggStudentData.put(codeKey, new ConcurrentHashMap<>());
            aggStudentData.get(codeKey).put(date, clicks);
        }
    }

    /**
     * Method to get the click amount based on codekey and nested date keys
     * @param codeKey String key for parent concurrenthashmap
     * @param date String key for nested hashmap
     * @return int value of clicks
     */
    public int getClicksAmount(String codeKey, String date) {
        if (!this.aggStudentData.containsKey(codeKey)) {
            System.out.println("Queried codeKey does not exist in the hashmap");
        } else {
            if (!this.aggStudentData.get(codeKey).containsKey(date)) {
                System.out.println("Queried date does not exist in the hashmap");
            } else {
                return this.aggStudentData.get(codeKey).get(date);
            }
        }
        return -1;
    }

    /**
     * Override of Runnable.run() method to take each parsed row from the BlockingQueue
     * The student click data is aggregated with the writeToHash method
     * When the Thread finds a poison pill, it will terminate
     */
    @Override
    public void run() {
        try {
            CSVRow csvRow;
            while (!(csvRow = queue.take()).getCodeKey().equals(poison.getCodeKey())) {
                writeToHash(csvRow.getCodeKey(), csvRow.getDate(), csvRow.getClicks());
//                System.out.println(Thread.currentThread().getName() + " just wrote row: " + csvRow.toString() + " to the hashmap!");
            }
//            System.out.println(Thread.currentThread().getName() + " ate poison pill from BlockingQueue");
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
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
        ClickAggregatorConsumer that = (ClickAggregatorConsumer) o;
        return Objects.equals(queue, that.queue) &&
                Objects.equals(aggStudentData, that.aggStudentData) &&
                Objects.equals(poison, that.poison);
    }

    /**
     * Override of default hashCode() method
     * @return int
     */
    @Override
    public int hashCode() {
        return Objects.hash(queue, aggStudentData, poison);
    }

    /**
     * Override of default toString() method
     * @return String
     */
    @Override
    public String toString() {
        return "ClickAggregatorConsumer{" +
                "queue=" + queue +
                ", aggStudentData=" + aggStudentData +
                ", poison=" + poison +
                '}';
    }
}
