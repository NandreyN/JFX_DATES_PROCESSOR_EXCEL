package sample;

import classes.CellManager;
import classes.TableRowModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.swing.*;
import java.util.stream.IntStream;

public class Controller {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 15;

    @FXML
    private TableView<TableRowModel> tableView;
    private CellManager manager;

    public Controller() {
    }

    @FXML
    private void initialize() {
        configureTableData();
    }

    private void configureTableData() {

        manager = new CellManager(HEIGHT, WIDTH, tableView);
        ObservableList<TableColumn<TableRowModel, String>> columns = manager.getEmptyTableColumns();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().clear();

        tableView.getSelectionModel().setCellSelectionEnabled(true);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setEditable(true);
        IntStream.range(0, HEIGHT).boxed().forEach(i -> tableView.getItems().add(new TableRowModel(i, WIDTH)));
        tableView.getColumns().addAll(columns);
    }
}
