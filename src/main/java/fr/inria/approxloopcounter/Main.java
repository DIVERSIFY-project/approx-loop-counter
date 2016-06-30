package fr.inria.approxloopcounter;

import spoon.Launcher;
import spoon.processing.ProcessInterruption;

/**
 * Created by elmarce on 30/06/16.
 */
public class Main {

    public static String strInputPath = "./src/tets/java/spoon/processing/";

    public static void main(String[] args) {
        final Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.addInputResource(strInputPath);
        launcher.setSourceOutputDirectory("./target/trash");
        ApproximatedLoopCounter processor = new ApproximatedLoopCounter();
        launcher.addProcessor(processor);
        launcher.buildModel();
        launcher.process();
    }

}
