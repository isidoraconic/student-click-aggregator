package sequentialSolution;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class SequentialDriverTest {

    @Test
    public void main() throws NullCommandLineArgument, NoSuchDirectoryException, IOException {
        SequentialDriver.main(new String[]{"anonymisedData"});
    }
}