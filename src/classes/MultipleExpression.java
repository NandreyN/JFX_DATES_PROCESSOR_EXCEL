package classes;

import java.util.Date;
import java.util.List;

public class MultipleExpression implements Expression {
    private List<Date> datesList;
    private ExpressionParser.Operations operation;

    public MultipleExpression(List<Date> datesList) {
        this.datesList = datesList;
    }

    @Override
    public Date execute() {
        return ExpressionExecutor.execute(datesList, operation);
    }

    @Override
    public ExpressionParser.Operations getOperation() {
        return operation;
    }

    @Override
    public void setOperation(ExpressionParser.Operations op) {
        operation = op;
    }
}
