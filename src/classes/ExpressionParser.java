package classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {
    public static class ExpressionFormatException extends Exception {
        public ExpressionFormatException(String msg) {
            super(msg);
        }
    }

    public enum Operations {
        ADD, SUBTRACT, MIN, MAX
    }

    public enum ElementDetection {
        CELL, NUMBER, BINARY_OPERATION, FORMULA, DATE, MULTIPLE_OPERATION
    }

    private static final Map<ElementDetection, Pattern> patternMap;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

    private static final String YEARS = "([0-9]{4})";
    private static final String SEP = "[.]";
    private static final String OR = "|";
    private static final String TILL_30 = "((0[1-9])|(1[0-9])|(2[0-9])|30)";
    private static final String TILL_31 = "((0[1-9])|(1[0-9])|(2[0-9])|3[01])";
    private static final String TILL_29 = "((0[1-9])|(1[0-9])|(2[0-9]))";
    private static final String DATE_REGEX_STRING = "(((" + "((01)|(03)|(05)|(07)|(08)|(10)|(12))" + SEP + TILL_31 + ")" + OR + "(02" + SEP + TILL_29 + ")" + OR +
            "(" + "((04)|(06)|(09)|(11))" + SEP + TILL_30 + "))" + SEP + YEARS + ")";

    private static final String CELL_REGEX = "([A-Z]+)(1([0-9]*))";
    private static final String NUMBER_REGEX = "(([-+]?)\\d+)";

    private static final String BINARY_OP_REGEX = "(^=(" + DATE_REGEX_STRING + ")([+-])(" + NUMBER_REGEX + ")$)" + "|" +
            "(^=(" + NUMBER_REGEX + ")[+-](" + DATE_REGEX_STRING + ")$)";

    private static final String MULTIPLE_OP_REGEX = "\\([(min)(max)]\\)\\((\\d+)\\)";

    // =v1OPv2
    // =min(,,,, ...)
    // =max(,,,, ...)
    static {
        patternMap = new HashMap<>();
        patternMap.put(ElementDetection.DATE, Pattern.compile(DATE_REGEX_STRING));
        patternMap.put(ElementDetection.BINARY_OPERATION, Pattern.compile(BINARY_OP_REGEX));
        patternMap.put(ElementDetection.MULTIPLE_OPERATION, Pattern.compile(MULTIPLE_OP_REGEX));
        patternMap.put(ElementDetection.CELL, Pattern.compile(CELL_REGEX));
        patternMap.put(ElementDetection.NUMBER, Pattern.compile(NUMBER_REGEX));
    }

    public static boolean isDate(String s) {
        return true;
    }

    public static Expression parse(String s) throws ExpressionFormatException {
        Matcher matcher;
        Expression expression = null;

        matcher = patternMap.get(ElementDetection.BINARY_OPERATION).matcher(s);
        boolean binaryDetected = matcher.matches();

        if (binaryDetected) {
            String firstOperand = matcher.group(0);
            String secondOperand = matcher.group(2);
            String op = matcher.group(1);
            try {
                matcher = patternMap.get(ElementDetection.DATE).matcher(firstOperand);
                Date date;
                int days;

                if (matcher.matches()) {
                    date = sdf.parse(firstOperand);
                    if (patternMap.get(ElementDetection.NUMBER).matcher(secondOperand).matches())
                        days = Integer.parseInt(secondOperand);
                    else
                        throw new ParseException("Number conversion Ex", 0);
                } else {
                    date = sdf.parse(secondOperand);
                    if (patternMap.get(ElementDetection.NUMBER).matcher(firstOperand).matches())
                        days = Integer.parseInt(firstOperand);
                    else
                        throw new ParseException("Number conversion Ex", 0);
                }
                expression = new BinaryExpression(date, days, (op.equals("+")) ? Operations.ADD : Operations.SUBTRACT);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            matcher = patternMap.get(ElementDetection.MULTIPLE_OPERATION).matcher(s);
            if (!matcher.matches())
                throw new ExpressionFormatException("Unknown operation");
            String op = matcher.group(0);



            expression = new MultipleExpression();
            // Multiple operation detected
            // Continue parsing parameter list
        }

        return expression;
    }
}
