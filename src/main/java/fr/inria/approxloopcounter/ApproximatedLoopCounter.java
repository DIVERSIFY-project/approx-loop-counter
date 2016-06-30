package fr.inria.approxloopcounter;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;

/**
 * Created by elmarce on 30/06/16.
 */
public class ApproximatedLoopCounter extends AbstractProcessor<CtLoop> {

    private int loopCount = 0;
    private int approximableLoopCount = 0;

    private static final HashSet<String> numericTypes;

    static {
        numericTypes = new HashSet<String>();
        numericTypes.add(Integer.class.getCanonicalName());
        numericTypes.add(int.class.getCanonicalName());

        numericTypes.add(Byte.class.getCanonicalName());
        numericTypes.add(byte.class.getCanonicalName());

        numericTypes.add(Float.class.getCanonicalName());
        numericTypes.add(float.class.getCanonicalName());

        numericTypes.add(Double.class.getCanonicalName());
        numericTypes.add(double.class.getCanonicalName());

        numericTypes.add(Long.class.getCanonicalName());
        numericTypes.add(long.class.getCanonicalName());

        numericTypes.add(Short.class.getCanonicalName());
        numericTypes.add(short.class.getCanonicalName());
    }

    public void process(CtLoop loop) {
        //Detect array assignment
        if (loop.getBody() instanceof CtBlock) {
            CtBlock block = (CtBlock) loop.getBody();
            for (int i = 0; i < block.getStatements().size(); i++) {
                CtStatement st = block.getStatement(i);
                if (st instanceof CtAssignment && !(st instanceof CtOperatorAssignment)) {
                    CtExpression left = ((CtAssignment) st).getAssigned();
                    if (left instanceof CtArrayAccess && isSignalArray((CtArrayAccess) left, loop)) {
                        approximableLoopCount++;
                    }
                }
            }
        }
        loopCount++;
    }


    /**
     * Indicates if the access is a signal array.
     * <p>
     * A signal array is an array of numeric primitive data who's index can be connected to
     * the expression of the loop in a def-use data flow
     * <p>
     * Example:
     * <p>
     * for (int i = 0; i < size; i++) {
     * //Do stuff
     * // ....
     * numericArray[i] = someCalculation();
     * }
     *
     * @param arrayAccess Array access for what we want to know if it is a signal array
     * @param element
     * @return True if it is a numerical array
     */
    private boolean isSignalArray(CtArrayAccess arrayAccess, CtLoop element) {

        //Detect if the array is of numerical type
        if (isNumericPrimitiveType(arrayAccess.getType())) {
            //Detect all variables in the index expression of the array
            List<CtVariableAccess> indexAccesses = accessOfExpression(arrayAccess.getIndexExpression());

            //Detect all variables in the loop expression
            CtExpression exp = getLoopExpression(element);
            List<CtVariableAccess> loopExpAccesses = accessOfExpression(exp);

            //See if it is directly relate to the loop expression.
            //In the future calculate the def-use data flow from the index to the loop expression
            for (CtVariableAccess varAccess : indexAccesses) {
                for (CtVariableAccess a : loopExpAccesses) {
                    if (a.getVariable().equals(varAccess.getVariable())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<CtVariableAccess>(CtVariableAccess.class));
    }

    private CtExpression getLoopExpression(CtLoop element) {
        if (element instanceof CtWhile) {
            return ((CtWhile) element).getLoopingExpression();
        } else if (element instanceof CtFor) {
            return ((CtFor) element).getExpression();
        } else if (element instanceof CtForEach) {
            return ((CtForEach) element).getExpression();
        } else if (element instanceof CtDo) {
            return ((CtDo) element).getLoopingExpression();
        }
        return null;
    }

    /**
     * Indicate if the primitive type of the array (type of the 1D array) is of numerical type
     *
     * @param type: type of the array
     * @return Boolean if the access is of primitive type numeric
     */
    private boolean isNumericPrimitiveType(CtTypeReference type) {
        if (type instanceof CtArrayTypeReference) {
            return isNumericPrimitiveType(((CtArrayTypeReference) type).getComponentType());
        }
        return numericTypes.contains(type.getQualifiedName());
    }

    public int getLoopCount() {
        return loopCount;
    }

    public int getApproximableLoopCount() {
        return approximableLoopCount;
    }
}
