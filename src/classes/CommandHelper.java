package classes;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import jdk.jshell.spi.ExecutionControl;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class CommandHelper {
    @FXML
    private static TableView<TableRowModel> tableView;
    private static DirectedGraph<CellContent, DefaultEdge> graph;

    public static void setTableView(TableView<TableRowModel> table) throws ExpressionParser.ExpressionFormatException {
        tableView = table;
        assertTableInitialized();
        rebuildGraph();
    }

    private static void rebuildGraph() throws ExpressionParser.ExpressionFormatException {
        graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        tableView.getItems().forEach(row -> {
            for (int i = 0; i < row.getCellCount(); i++)
                graph.addVertex(row.getContent(i));
        });
    }

    private static void notifySubscribersFor(CellContent c) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private static void registerDependencies(CellContent forCell, List<CellContent> dep) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private static boolean checkCycles() throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private static void assertTableInitialized() throws ExpressionParser.ExpressionFormatException {
        if (tableView == null)
            throw new ExpressionParser.ExpressionFormatException("Table was not initialized");
    }

    public static Date processFormula(String formula, CellContent c) throws ExpressionParser.ExpressionFormatException {
        assertTableInitialized();

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
