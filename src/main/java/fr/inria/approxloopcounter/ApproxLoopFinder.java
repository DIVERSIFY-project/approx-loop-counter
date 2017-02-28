package fr.inria.approxloopcounter;

import spoon.reflect.code.*;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by elmarce on 05/08/16.
 */
public class ApproxLoopFinder {

    private boolean hasInnerLoop = false;

    private ArrayList<String> errors;

    private static final HashSet<String> numericTypes;

    private ArrayList<CtArrayAccess> arrayAccesses;

    static {
        numericTypes = new HashSet<>();
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

    public ApproxLoopFinder() {
        errors = new ArrayList<String>();
        arrayAccesses = new ArrayList<CtArrayAccess>();
    }

    public boolean isApproximable(CtLoop loop) {
        hasInnerLoop = false;
        //Innermost loops containing exactly one array assignment
        if (arrayAssignmentsInBody(loop.getBody(), loop, 0) == 1) {
            return true;
        }
        return false;
    }

    /**
     * Find Array Assignments in single Statements
     *
     * @param st
     * @param loop
     * @return
     */
    private int arrayAssignmentsInSingleStatement(CtStatement st, CtLoop loop) {
        if (st instanceof CtAssignment && !(st instanceof CtOperatorAssignment)) {
            CtExpression left = ((CtAssignment) st).getAssigned();
            if (left instanceof CtArrayAccess && isSignalArray((CtArrayAccess) left, loop)) {
                arrayAccesses.add((CtArrayAccess) left);
                return 1;
            }
        }
        return 0;
    }

    /**
     * Find array assignments in a list of statements
     *
     * @param body
     * @param loop
     * @return
     */
    private int arrayAssignmentsInBody(CtStatement body, CtLoop loop, int result) {
        if (body == null || body.getElements(new TypeFilter<CtLoop>(CtLoop.class)).size() > 0
                || result > 2 || hasInnerLoop) {
            hasInnerLoop = true;
            return 0; //Innermost loop
        }

        CtBlock block;
        if (body instanceof CtBlock) block = (CtBlock) body;
        else return arrayAssignmentsInSingleStatement(body, loop);

        int i = 0;
        while (i < block.getStatements().size() && result < 2 && !hasInnerLoop) {
            CtStatement st = block.getStatement(i);
            //If and Switchs
            if (st instanceof CtIf) {
                result += arrayAssignmentsInBody(((CtIf) st).getThenStatement(), loop, result);
                result += arrayAssignmentsInBody(((CtIf) st).getElseStatement(), loop, result);
            } else if (st instanceof CtSwitch) {
                for (Object ct : ((CtSwitch) st).getCases()) {
                    CtCase c = (CtCase) ct;
                    for (CtStatement caseSt : c.getStatements())
                        result += arrayAssignmentsInBody(caseSt, loop, result);
                }
            } else if (result < 2 && !hasInnerLoop) result += arrayAssignmentsInSingleStatement(st, loop);
            i++;
        }
        return result;
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
            for (CtVariableAccess varAccess : indexAccesses)
                for (CtVariableAccess a : loopExpAccesses)
                    if (a.getVariable().equals(varAccess.getVariable())) return true;
        }
        return false;
    }

    private List<CtVariableAccess> accessOfExpression(CtExpression expression) {
        if (expression == null) return new ArrayList<>();//Special case when the loop has no increment expression
        //Detect all variables in the index expression of the array
        return expression.getElements(new TypeFilter<>(CtVariableAccess.class));
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
        if (type == null) return false;

        if (type instanceof CtArrayTypeReference) {
            return isNumericPrimitiveType(((CtArrayTypeReference) type).getComponentType());
        }
        String qName = null;

        try {
            qName = type.getQualifiedName();
        } catch (NullPointerException ex) {
            getErrors().add("Cannot get qualified name of type: " + type.getSimpleName());
        }

        return qName != null && numericTypes.contains(qName);
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public CtArrayAccess getArrayAccess(int i) {
        return arrayAccesses.get(i);
    }
}
