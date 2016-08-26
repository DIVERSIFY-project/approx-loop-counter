import fr.inria.approxloopcounter.ApproxLoopCounterProcessor;
import org.junit.Test;
import spoon.Launcher;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by elmarce on 03/08/16.
 */
public class ApproximateLoopCounterTest {

    @Test
    public void testProcessor() throws URISyntaxException {
        final Launcher launcher = new Launcher();
        String strInputPath = launcher.getClass().getResource("/JavaTestFiles").getPath();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(strInputPath);
        launcher.setSourceOutputDirectory("./target/trash");
        ApproxLoopCounterProcessor processor = new ApproxLoopCounterProcessor();
        launcher.addProcessor(processor);
        launcher.buildModel();
        launcher.process();
        assertEquals(1, processor.getApproximableLoopCount());
    }



}
