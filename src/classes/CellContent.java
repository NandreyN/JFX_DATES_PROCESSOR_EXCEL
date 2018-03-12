package classes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CellContent extends javafx.scene.control.TableCell {
    private static final int ALPHABET_SIZE = 26;
    private int row, column;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private String formula;
    private SimpleStringProperty contentDisplayed;

    public static enum States {
        FORMULA, VALUE
    }

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet[i++] = c;
        }
    }

    public CellContent(int row, int column) {
        this.row = row;
        this.column = column;
        formula = "";
        contentDisplayed = new SimpleStringProperty(formula);
    }

    public ObservableValue<String> getContentObservable() {
        return contentDisplayed;
    }

    public void setObservableContent(States state) {
        switch (state) {
            case FORMULA:
                contentDisplayed.set(this.formula);
                break;
            case VALUE:
                contentDisplayed.set(getCellValueFormatted());
                break;
        }
    }

    private String getCellValueFormatted() {
        return (cellValue != null) ? ExpressionParser.sdf.format(cellValue) : "";
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormula() {
        return formula;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }
}
