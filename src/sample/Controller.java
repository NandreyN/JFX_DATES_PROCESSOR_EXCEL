package sample;

import classes.ColumnBuilder;
import classes.CommandHelper;
import classes.TableRowModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.stream.IntStream;

public class Controller {
    private static final int WIDTH = 10;
    private static final int HEIGHT = 15;

    @FXML
    private TableView<TableRowModel> tableView;
    private ColumnBuilder builder;

    public Controller() {
    }

    @FXML
    private void initialize() {
        CommandHelper.setTableView(tableView);
        configureTableData();
    }

    private void configureTableData() {
        builder = new ColumnBuilder(HEIGHT, WIDTH, tableView);
        ObservableList<TableColumn<TableRowModel, String>> columns = builder.getEmptyTableColumns();
        tableView.getColumns().clear();
        tableView.getColumns().addAll(columns);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setEditable(true);

        IntStream.range(0, HEIGHT).boxed().forEach(i -> tableView.getItems().add(new TableRowModel(i, WIDTH)));
    }
}
