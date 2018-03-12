package classes;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.IntStream;

public class TableRowModel {
    private ObservableList<CellContent> columns;
    private int rowNumber;

    public TableRowModel(int rowNumber, int width) {
        this.rowNumber = rowNumber;
        columns = FXCollections.observableArrayList();

        IntStream.range(0, width).boxed().forEach(x -> {
            columns.add(new CellContent(rowNumber, x));
        });
    }

    public CellContent getContent(int idx) {
        return columns.get(idx);
    }

    public int getRowNumber() {
        return rowNumber;
    }
}
