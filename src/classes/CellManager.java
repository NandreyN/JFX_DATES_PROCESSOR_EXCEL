package classes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;

import java.util.Date;
import java.util.stream.IntStream;

public class CellManager {
    private int height, width;
    private ObservableList<TableRowModel> tableContentModel;

    public CellManager(int h, int w) {
        this.height = h;
        this.width = w;
        tableContentModel = FXCollections.observableArrayList();
        IntStream.range(0, h).boxed().forEach(i -> {
            tableContentModel.add(new TableRowModel(i, width));
        });

    }

    private TableColumn<TableRowModel, String> getIndexColumn() {
        TableColumn<TableRowModel, String> indexColumn = new TableColumn<>("#");
        indexColumn.setSortable(false);
        indexColumn.setResizable(false);
        indexColumn.setEditable(false);
        indexColumn.setCellValueFactory(column -> new ReadOnlyObjectWrapper<String>(String.valueOf(column.getValue().getRowNumber())));
        return indexColumn;
    }

    public ObservableList<TableColumn<TableRowModel, String>> getEmptyTableColumns() {
        ObservableList<TableColumn<TableRowModel, String>> ret = FXCollections.observableArrayList();
        IntStream.range(0, width).boxed().forEach(w -> {
            TableColumn<TableRowModel, String> col = new TableColumn<>(toName(w + 1));
            col.setSortable(false);
            col.setId(w.toString());
            col.setCellValueFactory(v -> {
                int colNumber = 0;
                colNumber = Integer.parseInt(col.getId());
                return v.getValue().getContent(colNumber).getContentObservable();
            });
            configureColumnCellsBehavior(col);
            ret.add(col);
        });
        ret.add(0, getIndexColumn());
        return ret;
    }

    private static void configureColumnCellsBehavior(TableColumn<TableRowModel, String> column) {
        // edit event
        // on edit display formula
        // on commit display  value and errors
        column.setOnEditStart(x -> {
            CellContent c = x.getTableView().getItems().get(x.getTablePosition().getColumn()).getContent(x.getTablePosition().getColumn());
            c.setObservableContent(CellContent.States.FORMULA);
        });

        column.setOnEditCommit(x -> {
            CellContent c = x.getTableView().getItems().get(x.getTablePosition().getColumn()).getContent(x.getTablePosition().getColumn());
            // pass new Date
            c.setObservableContent(CellContent.States.VALUE);
        });

        column.setOnEditCancel(x -> {
            CellContent c = x.getTableView().getItems().get(x.getTablePosition().getColumn()).getContent(x.getTablePosition().getColumn());
            c.setObservableContent(CellContent.States.VALUE);
        });
    }

    public static int toNumber(String name) {
        int number = 0;
        for (int i = 0; i < name.length(); i++) {
            number = number * 26 + (name.charAt(i) - ('A' - 1));
        }
        return number;
    }

    public static String toName(int number) {
        StringBuilder sb = new StringBuilder();
        while (number-- > 0) {
            sb.append((char) ('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
