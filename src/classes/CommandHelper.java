package classes;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHelper {
    public static class CycleReferenceException extends Exception {
        public CycleReferenceException(String msg) {
            super(msg);
        }
    }

    @FXML
    private TableView<TableRowModel> tableView;
    private ListenableGraph<String, DefaultEdge> graph;
    private JGraph visualizationGraph;
    private JFrame graphFrame;
    private JGraphModelAdapter m_jgAdapter;

    public void setTableView(TableView<TableRowModel> table) throws ExpressionParser.ExpressionFormatException {
        tableView = table;
        assertTableInitialized();
        rebuildGraph();
        m_jgAdapter = new JGraphModelAdapter(graph);
        visualizationGraph = new JGraph(m_jgAdapter);
        visualize();
    }

    private void visualize() {
        graphFrame = new JFrame();
        graphFrame.add(visualizationGraph);
        graphFrame.setBounds(0, 0, 300, 300);
        graphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        graphFrame.setVisible(true);

        positionVertexAt("A0", 0, 0);
        positionVertexAt("A1", 0, 100);
        positionVertexAt("A2", 0, 200);
        positionVertexAt("B0", 100, 0);
        positionVertexAt("B1", 100, 100);
        positionVertexAt("B2", 100, 200);
        positionVertexAt("C0", 200, 0);
        positionVertexAt("C1", 200, 100);
        positionVertexAt("C2", 200, 200);
    }

    private void positionVertexAt(Object vertex, int x, int y) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell(vertex);
        Map attr = cell.getAttributes();
        Rectangle2D b = GraphConstants.getBounds(attr);

        GraphConstants.setBounds(attr, new Rectangle(x, y, (int) b.getWidth(), (int) b.getHeight()));

        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        m_jgAdapter.edit(cellAttr, null, null, null);
    }

    private void rebuildGraph() {
        graph = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        tableView.getItems().forEach(row -> {
            for (int i = 0; i < row.getCellCount(); i++)
                graph.addVertex(row.getContent(i).toString());
        });
    }

    public void notifySubscribersErrorFor(CellContent c) {
        BreadthFirstIterator<String, DefaultEdge> dfi = new BreadthFirstIterator<>(graph, c.toString());
        while (dfi.hasNext()) {
            String id = dfi.next();
            if (id.equals(c.getCellId()))
                continue;
            Pair<Integer, Integer> pos = ExpressionParser.getPosOfCellId(id);
            CellContent target = tableView.getItems().get(pos.getKey()).getContent(pos.getValue());

            //target.setCellValue(null);
            target.setErrorDetected(true);
            target.setObservableContent("Error");

        }
    }

    private void notifySubscribersFor(CellContent c) {
        // bfs
        BreadthFirstIterator<String, DefaultEdge> dfi = new BreadthFirstIterator<>(graph, c.toString());
        while (dfi.hasNext()) {
            String id = dfi.next();
            if (id.equals(c.getCellId()))
                continue;
            Pair<Integer, Integer> pos = ExpressionParser.getPosOfCellId(id);
            CellContent target = tableView.getItems().get(pos.getKey()).getContent(pos.getValue());
            target.update(this);
        }
    }

    public void registerDependencies(CellContent forCell, List<CellContent> dep) {
        // x->y means y uses x in it`s formula
        dep.forEach(x -> {
            if (!graph.containsEdge(x.toString(), forCell.toString()))
                graph.addEdge(x.toString(), forCell.toString());
        });
    }

    public void unregisterDependencies(CellContent forCell, List<CellContent> dep) {
        dep.forEach(x -> {
            if (graph.containsEdge(x.toString(), forCell.toString()))
                graph.removeEdge(x.toString(), forCell.toString());
        });
    }

    private boolean checkCycles() {
        CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(graph);
        return detector.detectCycles();
    }

    private void assertTableInitialized() throws ExpressionParser.ExpressionFormatException {
        if (tableView == null)
            throw new ExpressionParser.ExpressionFormatException("Table was not initialized");
    }

    public Date updateValueOfCell(CellContent c) throws ExpressionParser.ExpressionFormatException {
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

    public Pair<Date, List<CellContent>> processFormula(String formula) throws ExpressionParser.ExpressionFormatException, CycleReferenceException {
        assertTableInitialized();

        Pair<String, List<CellContent>> pair = ExpressionParser.replaceCellIdentificators(tableView.getItems(), formula);
        formula = pair.getKey();
        Expression expression = null;
        try {
            expression = ExpressionParser.parse(formula);
        } catch (ExpressionParser.ExpressionFormatException e) {
            throw e;
        } catch (ParseException e) {
            AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
        }

        if (expression != null) {
            return new Pair<>(expression.execute(), pair.getValue());
        } else
            return null;
    }

    public void refreshDependentCells(CellContent c, List<CellContent> dep) throws CycleReferenceException {
        registerDependencies(c, dep);
        if (checkCycles()) {
            unregisterDependencies(c, dep);
            throw new CommandHelper.CycleReferenceException("Cycle reference detected");
        }
        notifySubscribersFor(c);
        //unregisterDependencies(c, dep);
    }
}
