package classes;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.Date;

public class ColumnBuilder {
    private int height, width;
    private TableView<TableRowModel> table;

    public ColumnBuilder(int h, int w, TableView<TableRowModel> table) {
        this.height = h;
        this.width = w;
        this.table = table;
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
        for (int w = 0; w < width; w++) {
            TableColumn<TableRowModel, String> col = new TableColumn<>(toName(w + 1));
            configureColumnCellsBehavior(col, w);
            ret.add(col);
        }
        return ret;
    }

    private void configureColumnCellsBehavior(TableColumn<TableRowModel, String> column, int w) {
        // edit event
        // on edit display formula
        // on commit display  value and errors
        column.setSortable(false);
        column.setId(Integer.toString(w));
        column.setCellValueFactory(v -> {
            int colNumber = 0;
            colNumber = Integer.parseInt(column.getId());
            return v.getValue().getContent(colNumber).getContentObservable();
        });
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);
        column.setSortable(false);
        column.setResizable(true);

        column.setOnEditStart(x -> {
            int col = x.getTablePosition().getColumn();
            CellContent c = table.getSelectionModel().getSelectedItem().getContent(col);
            c.setObservableContent(CellContent.States.FORMULA);
        });

        column.setOnEditCommit(x -> {
            int row = x.getRowValue().getRowNumber();
            int col = Integer.parseInt(column.getId());

            CellContent c = table.getSelectionModel().getSelectedItem().getContent(col);

            try {
                Date newDate = CommandHelper.processFormula(x.getNewValue(), c);
                c.setCellValue(newDate);
                c.setObservableContent(CellContent.States.VALUE);
            } catch (ExpressionParser.ExpressionFormatException e) {
                e.printStackTrace();
                c.setObservableContent(CellContent.States.VALUE);
            }
            c.setFormula(x.getNewValue());
            c.setObservableContent(CellContent.States.VALUE);
        });

        column.setOnEditCancel(x -> {

            int col = x.getTablePosition().getColumn();
            CellContent c = table.getSelectionModel().getSelectedItem().getContent(col);
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
