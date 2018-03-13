package classes;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.text.ParseException;
import java.util.Date;

public class CellContent extends javafx.scene.control.TableCell<TableRowModel, String> {
    private static final int ALPHABET_SIZE = 26;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private SimpleStringProperty formula;
    private SimpleStringProperty contentDisplayed;

    public enum States {
        FORMULA, VALUE
    }

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet[i++] = c;
        }
    }

    public CellContent() {
        super.setEditable(true);
        formula = new SimpleStringProperty("");
        contentDisplayed = new SimpleStringProperty("");
        setObservableContent(States.FORMULA);
    }

    public ObservableValue<String> getContentObservable() {
        return contentDisplayed;
    }

    public void setObservableContent(States state) {
        switch (state) {
            case FORMULA:
                contentDisplayed.setValue(formula.getValue());
                break;
            case VALUE:
                contentDisplayed.setValue(getCellValueFormatted());
                break;
        }
    }

    private String getCellValueFormatted() {
        return (cellValue != null) ? ExpressionParser.sdf.format(cellValue) : "";
    }

    public void setFormula(String formula) {
        this.formula.setValue(formula);
    }

    public ObservableValue<String> getFormula() {
        return formula;
    }


    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }
}
