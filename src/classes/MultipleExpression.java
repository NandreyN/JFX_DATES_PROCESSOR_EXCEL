package classes;

import java.util.Date;
import java.util.List;

public class MultipleExpression implements Expression {
    private List<Date> datesList;
    private List<String> cellIdsList;

    public MultipleExpression(List<Date> datesList, List<String> cellIdsList) {
        this.datesList = datesList;
        this.cellIdsList = cellIdsList;
    }

    @Override
    public Date execute() {
        return null;
    }

    @Override
    public ExpressionParser.Operations getOperation() {
        return null;
    }

    @Override
    public void setOperation(ExpressionParser.Operations op) {

    }

    public List<String> getCellIdsList() {
        return cellIdsList;
    }

    public List<Date> getDatesList() {
        return datesList;
    }
}
