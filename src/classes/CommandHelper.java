package classes;

import java.util.Date;

public class CommandHelper {
    public static Date processFormula(String formula) throws ExpressionParser.ExpressionFormatException {
        Expression expression;
        try {
            expression = ExpressionParser.parse(formula);
        } catch (ExpressionParser.ExpressionFormatException e) {
            throw e;
        }

        Date d = expression.execute();
        return d;
    }
}
