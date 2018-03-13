package classes;

import java.util.Date;

public class UnaryExpression implements Expression {
    private Date date;

    public UnaryExpression(Date d) {
        date = d;
    }

    @Override
    public Date execute() {
        return date;
    }

    @Override
    public ExpressionParser.Operations getOperation() {
        return ExpressionParser.Operations.CONFER;
    }

    @Override
    public void setOperation(ExpressionParser.Operations op) {
        throw new UnsupportedOperationException("Unable to set operation");
    }
}
