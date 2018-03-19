package classes;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Cell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Pair;

import java.util.Date;
import java.util.List;

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
                Pair<Date, List<CellContent>> oldPair = null;
                if (!x.getOldValue().equals(""))
                    oldPair = CommandHelper.processFormula(x.getOldValue());
                Pair<Date, List<CellContent>> newPair = CommandHelper.processFormula(x.getNewValue());

                if (newPair == null) {
                    throw new ExpressionParser.ExpressionFormatException("Got empty date");
                }
                c.setCellValue(newPair.getKey());
                c.setObservableContent(CellContent.States.VALUE);
                if (oldPair != null && oldPair.getValue().size() > 0)
                    CommandHelper.unregisterDependencies(c, oldPair.getValue());
                CommandHelper.refreshDependentCells(c, newPair.getValue());
                c.setFormula(x.getNewValue());

            } catch (ExpressionParser.ExpressionFormatException e) {
                AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
                c.setObservableContent("Error");
                c.setCellValue(null);
                CommandHelper.notifySubscribersErrorFor(c);
            } catch (CommandHelper.CycleReferenceException e) {
                AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
                c.setFormula(x.getOldValue());
                c.setCellValue(null);
                c.setObservableContent("Error");
                CommandHelper.notifySubscribersErrorFor(c);
            }
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
