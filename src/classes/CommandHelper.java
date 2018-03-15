package classes;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import jdk.jshell.spi.ExecutionControl;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class CommandHelper {
    public static class CycleReferenceException extends Exception {
        public CycleReferenceException(String msg) {
            super(msg);
        }
    }

    @FXML
    private static TableView<TableRowModel> tableView;
    private static DirectedGraph<String, DefaultEdge> graph;

    public static void setTableView(TableView<TableRowModel> table) throws ExpressionParser.ExpressionFormatException {
        tableView = table;
        assertTableInitialized();
        rebuildGraph();
    }

    private static void rebuildGraph() {
        graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        tableView.getItems().forEach(row -> {
            for (int i = 0; i < row.getCellCount(); i++)
                graph.addVertex(row.getContent(i).toString());
        });
    }

    private static void notifySubscribersFor(CellContent c) {
        // bfs
        DepthFirstIterator<String, DefaultEdge> dfi = new DepthFirstIterator<>(graph, c.toString());
        while (dfi.hasNext()) {
            String id = dfi.next();
            if (id.equals(c.getCellId()))
                continue;
            Pair<Integer, Integer> pos = ExpressionParser.getPosOfCellId(id);
            CellContent target = tableView.getItems().get(pos.getKey()).getContent(pos.getValue());
            target.update();
        }
    }

    private static void registerDependencies(CellContent forCell, List<CellContent> dep) {
        // x->y means y uses x in it`s formula
        dep.forEach(x -> {
            graph.addEdge(x.toString(), forCell.toString());
        });
    }

    private static void unregisterDependencies(CellContent forCell, List<CellContent> dep) {
        dep.forEach(x -> graph.removeEdge(x.toString(), forCell.toString()));
    }

    private static boolean checkCycles() {
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(graph);
        return detector.detectCycles();
    }

    private static void assertTableInitialized() throws ExpressionParser.ExpressionFormatException {
        if (tableView == null)
            throw new ExpressionParser.ExpressionFormatException("Table was not initialized");
    }

    public static Date updateValueOfCell(CellContent c) throws ExpressionParser.ExpressionFormatException {
        Pair<String, List<CellContent>> pair = ExpressionParser.replaceCellIdentificators(tableView.getItems(), c.getFormula().getValue());
        String formula = pair.getKey();
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

    public static Pair<Date, List<CellContent>> processFormula(String formula, CellContent c) throws ExpressionParser.ExpressionFormatException, CycleReferenceException {
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
            return new Pair<>(expression.execute(), pair.getValue());
        } else
            return null;
    }

    public static void refreshDependentCells(CellContent c, List<CellContent> dep) throws CycleReferenceException {
        registerDependencies(c, dep);
        if (checkCycles()) {
            unregisterDependencies(c, dep);
            throw new CommandHelper.CycleReferenceException("Cycle reference detected");
        }
        notifySubscribersFor(c);
    }
}
