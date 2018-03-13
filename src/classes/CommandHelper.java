package classes;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.text.ParseException;
import java.util.Date;

public class CommandHelper {
    @FXML
    private static TableView<TableRowModel> tableView;

    public static void setTableView(TableView<TableRowModel> table) {
        tableView = table;
    }

    public static Date processFormula(String formula) throws ExpressionParser.ExpressionFormatException {
        if (tableView == null)
            throw new ExpressionParser.ExpressionFormatException("Table was not initialized");

        formula = ExpressionParser.replaceCellIdentificators(tableView.getItems(), formula);
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
