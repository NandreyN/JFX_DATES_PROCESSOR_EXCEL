package sample;

import classes.ColumnBuilder;
import classes.CommandHelper;
import classes.ExpressionParser;
import classes.TableRowModel;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.IntStream;

public class Controller {
    public class RowNumberValue extends TableCell<Integer, Number> {
        private IntegerProperty value;

        public RowNumberValue(int v) {
            value = new SimpleIntegerProperty(v);
        }

        public int getValue() {
            return value.get();
        }

        public IntegerProperty valueProperty() {
            return value;
        }

        public void setValue(int value) {
            this.value.set(value);
        }
    }

    private static final int WIDTH = 10;
    private static final int HEIGHT = 15;

    @FXML
    private TableView<TableRowModel> tableView;
    @FXML
    private TableView<RowNumberValue> rowIdTableView;

    private ColumnBuilder builder;

    public Controller() {
    }

    @FXML
    private void initialize() {
        configureRowIdTableView();
        configureTableData();
        try {
            CommandHelper.setTableView(tableView);
        } catch (ExpressionParser.ExpressionFormatException e) {
            e.printStackTrace();
        }
    }

    private void configureRowIdTableView() {
        rowIdTableView.getColumns().clear();
        rowIdTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rowIdTableView.setEditable(false);
        rowIdTableView.getSelectionModel().setCellSelectionEnabled(false);
        TableColumn<RowNumberValue, Number> col = new TableColumn<>("Row");
        col.setCellValueFactory(x -> x.getValue().valueProperty());
        rowIdTableView.getColumns().add(col);

        for (int i = 0; i < HEIGHT; i++)
            rowIdTableView.getItems().add(new RowNumberValue(i));
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
