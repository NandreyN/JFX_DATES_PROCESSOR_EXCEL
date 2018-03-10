package classes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TableCell extends javafx.scene.control.TableCell {
    private static final int ALPHABET_SIZE = 26;
    private int row, column;
    private String columnAsString;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private String formula;

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet[i++] = c;
        }
    }

    public TableCell(int row, int column) {
        this.row = row;
        this.column = column;
        columnAsString = convertColumnToString(column);
        formula = "";
    }


    public void setFormula(String formula) {
        this.formula = formula;
        /*if (!validateFormula())
            throw new IllegalArgumentException("formula");
        recalculate();*/
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getColumnAsString() {
        return columnAsString;
    }

    private static String convertColumnToString(int col) {
        return "";
    }

    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }
}
