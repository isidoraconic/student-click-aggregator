package concurrentSolution;

import org.junit.Before;
import org.junit.Test;
import sequentialSolution.NoSuchDirectoryException;
import sequentialSolution.NullCommandLineArgument;

import java.io.IOException;

import static org.junit.Assert.*;

public class ConcurrentDriverTest {

    private String[] noArgs = new String[0];
    private String[] argsValidPath = {"anonymisedData"};
    private String[] argsInvalidPath = {"q1w2e3r4t5y6"};
    private String[] argsValidPathValidThresh = {"anonymisedData", "20000"};
    private String[] argsValidPathInvalidThresh = {"anonymisedData", "a"};

    @Test (expected = NullCommandLineArgument.class)
    public void noArgsFail() throws NoSuchDirectoryException, IOException, InvalidThresholdValue, InterruptedException, NullCommandLineArgument  {
        ConcurrentDriver.main(noArgs);
    }

    @Test
    public void validPathNoThreshRun() throws NoSuchDirectoryException, IOException, InvalidThresholdValue, InterruptedException, NullCommandLineArgument  {
        ConcurrentDriver.main(argsValidPath);
    }

    @Test (expected = NoSuchDirectoryException.class)
    public void invalidPathFail() throws NoSuchDirectoryException, IOException, InvalidThresholdValue, InterruptedException, NullCommandLineArgument {
        ConcurrentDriver.main(argsInvalidPath);
    }

    @Test
    public void validPathValidThreshRun() throws NoSuchDirectoryException, IOException, InvalidThresholdValue, InterruptedException, NullCommandLineArgument  {
        ConcurrentDriver.main(argsValidPathValidThresh);
    }

    @Test (expected = InvalidThresholdValue.class)
    public void validPathInvalidThreshFail() throws NoSuchDirectoryException, IOException, InvalidThresholdValue, InterruptedException, NullCommandLineArgument  {
        ConcurrentDriver.main(argsValidPathInvalidThresh);
    }
}