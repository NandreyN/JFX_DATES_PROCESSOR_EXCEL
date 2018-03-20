package classes;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.util.Pair;
import sample.Controller;

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
        ADD, SUBTRACT, MIN, MAX, CONFER
    }

    public enum ElementDetection {
        CELL, NUMBER, BINARY_OPERATION, FORMULA, DATE, MULTIPLE_OPERATION, SINGLE_VALUE
    }

    private static final Map<ElementDetection, Pattern> patternMap;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");

    private static final String YEARS = "([0-9]{4})";
    private static final String SEP = "[.]";
    private static final String OR = "|";
    private static final String TILL_30 = "((0[1-9])|(1[0-9])|(2[0-9])|30)";
    private static final String TILL_31 = "((0[1-9])|(1[0-9])|(2[0-9])|3[01])";
    private static final String TILL_29 = "((0[1-9])|(1[0-9])|(2[0-9]))";
    private static final String DATE_REGEX_STRING = "(((" + "(01|03|05|07|08|10|12)" + SEP + TILL_31 + ")" + OR + "(02" + SEP + TILL_29 + ")" + OR +
            "(" + "(04|06|09|11)" + SEP + TILL_30 + "))" + SEP + YEARS + ")";

    private static final String CELL_REGEX = "([A-Z]+)([0-9]*)";
    private static final String NUMBER_REGEX = "(([-+]?)\\d+)";

    private static final String BINARY_OP_REGEX = "(^=(" + DATE_REGEX_STRING + ")([+-])(" + NUMBER_REGEX + ")$)" + "|" +
            "(^=(" + NUMBER_REGEX + ")[+-](" + DATE_REGEX_STRING + ")$)";

    private static final String MULTIPLE_OP_REGEX = "^\\=(min|max)\\((" + DATE_REGEX_STRING + "[,]?)*\\)$";
    private static final String SINGLE_VALUE_REGEX = "^=" + DATE_REGEX_STRING + "$";

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
        patternMap.put(ElementDetection.SINGLE_VALUE, Pattern.compile(SINGLE_VALUE_REGEX));
    }

    public static boolean isDate(String s) {
        return patternMap.get(ElementDetection.DATE).matcher(s).matches();
    }

    public static Pair<Integer, Integer> getPosOfCellId(String id) {
        int i = 0;
        for (; i < id.length(); i++) {
            if (!Character.isAlphabetic(id.charAt(i)))
                break;
        }

        int col = ColumnBuilder.toNumber(id.substring(0, i)) - 1;
        int row = Integer.parseInt(id.substring(i));
        return new Pair<>(row, col);
    }

    public static Pair<String, List<CellContent>> replaceCellIdentificators(ObservableList<TableRowModel> model, String formula) throws ExpressionFormatException {
        List<CellContent> cellCollection = new ArrayList<>();
        StringBuilder sb = new StringBuilder(formula);
        Matcher matcher = patternMap.get(ElementDetection.CELL).matcher(formula);
        while (matcher.find()) {
            String id = matcher.group(); // B1

            int i = 0;
            for (; i < id.length(); i++) {
                if (!Character.isAlphabetic(id.charAt(i)))
                    break;
            }

            int col = ColumnBuilder.toNumber(id.substring(0, i)) - 1;
            int row = Integer.parseInt(id.substring(i));

            if (col >= Controller.WIDTH || row >= Controller.HEIGHT)
                throw new ExpressionFormatException("No referenced cell exists");

            CellContent c = model.get(row).getContent(col);
            cellCollection.add(c);
            if (c.getCellValue() == null)
                throw new ExpressionFormatException("Empty referenced cell");

            String pasteValue = ExpressionParser.sdf.format(c.getCellValue());
            int idx = sb.indexOf(id);
            sb.replace(idx, idx + id.length(), pasteValue);
        }
        return new Pair<>(sb.toString(), cellCollection);
    }

    public static Expression parse(String s) throws ExpressionFormatException, ParseException {
        if (s.equals(""))
            return null;
        s = s.replaceAll("\\s+", "");
        Matcher matcher;
        Expression expression = null;
        s.replaceAll("\"", "");
        matcher = patternMap.get(ElementDetection.BINARY_OPERATION).matcher(s);
        if (patternMap.get(ElementDetection.SINGLE_VALUE).matcher(s).matches()) {
            String dateS = s.replaceAll("=", "");
            return new UnaryExpression(sdf.parse(dateS));
        }

        boolean binaryDetected = matcher.matches();

        if (binaryDetected) {
            String firstOperand = matcher.group(2);
            String secondOperand = matcher.group(24);
            String op = matcher.group(23);
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
                throw e;
            }
        } else {
            matcher = patternMap.get(ElementDetection.MULTIPLE_OPERATION).matcher(s);
            if (!matcher.matches())
                throw new ExpressionFormatException("Unknown operation or invalid date format. Use mm.dd.yyyy");

            matcher = Pattern.compile("min").matcher(s);
            Operations op = (matcher.find()) ? Operations.MIN : Operations.MAX;
            List<String> paramArray = new ArrayList<>(10);


            matcher = Pattern.compile(DATE_REGEX_STRING).matcher(s);
            while (matcher.find())
                paramArray.add(matcher.group());

            if (paramArray.size() < 1)
                throw new ExpressionFormatException("Invalid number of arguments in operation " + op.toString());

            List<Date> datesList = new ArrayList<>();

            for (int i = 0; i < paramArray.size(); i++) {
                try {
                    datesList.add(sdf.parse(paramArray.get(i)));
                } catch (ParseException e) {
                    throw e;
                }

            }

            expression = new MultipleExpression(datesList);
            expression.setOperation(op);
        }

        return expression;
    }
}
