package fr.inria.approxloopcounter;

import fr.inria.diversify.syringe.detectors.AbstractDetector;
import fr.inria.diversify.syringe.detectors.Detector;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * Created by elmarce on 30/06/16.
 */
public class ApproxLoopCounterProcessor extends AbstractProcessor<CtLoop> {

    public static String ARRAY_ASSIGNMENT = "@@ARRAY_ASSIGNMENT@@";

    private int loopCount = 0;
    private int approximableLoopCount = 0;
    private ArrayList<String> approximableLoops;





    public ApproxLoopCounterProcessor() {
        approximableLoops = new ArrayList<String>();
    }

    public void process(CtLoop loop) {
        ApproxLoopFinder finder = new ApproxLoopFinder();
        if (finder.isApproximable(loop)) {
            approximableLoopCount++;
            approximableLoops.add(loop.getPosition().getCompilationUnit().toString() + ": \n" + loop.toString());
        }
        loopCount++;
    }



    public int getLoopCount() {
        return loopCount;
    }

    public int getApproximableLoopCount() {
        return approximableLoopCount;
    }

    public ArrayList<String> getApproximableLoops() {
        return approximableLoops;
    }
}
