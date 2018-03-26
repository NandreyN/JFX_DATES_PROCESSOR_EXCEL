package classes;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private CommandHelper helper;
    private CellContent saved;

    public ColumnBuilder(int h, int w, TableView<TableRowModel> table, CommandHelper helper) {
        this.height = h;
        this.width = w;
        this.table = table;
        this.helper = helper;
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
            if (x == null)
                return;
            int col = Integer.parseInt(column.getId());
            CellContent c = table.getSelectionModel().getSelectedItem().getContent(col);
            c.setObservableContent(CellContent.States.FORMULA);
            saved = c;
        });

        column.setOnEditCommit(x -> {
            int row = x.getRowValue().getRowNumber();
            int col = Integer.parseInt(column.getId());

            CellContent c = table.getSelectionModel().getSelectedItem().getContent(col);

            try {
                Pair<Date, List<CellContent>> old = helper.processFormula(c.getPreviousFormula());
                Pair<Date, List<CellContent>> newV = helper.processFormula(x.getNewValue());

                if (old != null && old.getValue().size() > 0)
                    helper.unregisterDependencies(c, old.getValue());

                if (x.getNewValue().equals("")) {
                    c.setCellValue(null);
                    c.setFormula("");
                    c.setPreviousFormula("");
                    helper.notifySubscribersErrorFor(c);
                    c.setObservableContent(CellContent.States.VALUE);
                    c.setErrorDetected(false);
                    return;
                }

                if (newV == null)
                    throw new ExpressionParser.ExpressionFormatException("");

                c.setFormula(x.getNewValue());
                c.setPreviousFormula(x.getNewValue());
                c.setCellValue(newV.getKey());
                helper.refreshDependentCells(c, newV.getValue());

                for (CellContent dep : newV.getValue())
                    if (dep.isErrorDetected())
                        throw new ExpressionParser.ExpressionFormatException("");


                c.setObservableContent(CellContent.States.VALUE);
                c.setErrorDetected(false);

            } catch (ExpressionParser.ExpressionFormatException e) {
                AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
                c.setObservableContent(CellContent.States.VALUE);
                c.setObservableContent("Error");
                //c.setCellValue(null);
                c.setErrorDetected(true);
                helper.notifySubscribersErrorFor(c);
            } catch (CommandHelper.CycleReferenceException e) {
                AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
                c.setObservableContent(CellContent.States.VALUE);
                c.setFormula(x.getOldValue());
                //c.setCellValue(null);
                c.setErrorDetected(true);
                c.setObservableContent("Error");
                helper.notifySubscribersErrorFor(c);
            }
        });

        column.setOnEditCancel(x ->
        {
            if (x == null)
                return;
            saved.setObservableContent(CellContent.States.VALUE);
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
