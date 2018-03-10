package classes;

import java.util.Date;

public interface Expression {
    ExpressionParser.Operations operation = null;

    Date execute();

    public ExpressionParser.Operations getOperation();

    public void setOperation(ExpressionParser.Operations op);
}
