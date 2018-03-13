package classes;

import java.text.ParseException;
import java.util.Date;

public class CommandHelper {
    public static Date processFormula(String formula) throws ExpressionParser.ExpressionFormatException {
        Expression expression = null;
        try {
            expression = ExpressionParser.parse(formula);
        } catch (ExpressionParser.ExpressionFormatException e) {
            throw e;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (expression != null) ? expression.execute() : null;
    }
}
