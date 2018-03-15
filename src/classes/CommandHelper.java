package classes;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.util.Pair;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandHelper {
    @FXML
    private static TableView<TableRowModel> tableView;

    public static void setTableView(TableView<TableRowModel> table) {
        tableView = table;
    }

    public static Date processFormula(String formula, CellContent c) throws ExpressionParser.ExpressionFormatException {
        if (tableView == null)
            throw new ExpressionParser.ExpressionFormatException("Table was not initialized");

        Pair<String, List<CellContent>> pair = ExpressionParser.replaceCellIdentificators(tableView.getItems(), formula);
        formula = pair.getKey();
        Expression expression = null;
        try {
            expression = ExpressionParser.parse(formula);
        } catch (ExpressionParser.ExpressionFormatException e) {
            throw e;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (expression != null) {
            return expression.execute();
        } else
            return null;
    }
}
