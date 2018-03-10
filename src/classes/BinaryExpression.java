package classes;

import java.util.Date;

public class BinaryExpression implements Expression {
    private Date dateParam;
    private int intParam;
    private ExpressionParser.Operations operation;

    public BinaryExpression(Date d, int v, ExpressionParser.Operations op) {
        this.dateParam = d;
        this.intParam = v;
        this.operation = op;
    }


    @Override
    public Date execute() {
        if (dateParam != null && operation != null && intParam != Integer.MAX_VALUE)
            return ExpressionExecutor.execute(dateParam, operation, intParam);
        else
            throw new IllegalArgumentException("Some values are not set");
    }

    @Override
    public ExpressionParser.Operations getOperation() {
        return operation;
    }

    @Override
    public void setOperation(ExpressionParser.Operations op) {
        this.operation = op;
    }

    public int getIntParam() {
        return intParam;
    }

    public void setIntParam(int intParam) {
        this.intParam = intParam;
    }

    public Date getDateParam() {
        return dateParam;
    }

    public void setDateParam(Date dateParam) {
        this.dateParam = dateParam;
    }
}
