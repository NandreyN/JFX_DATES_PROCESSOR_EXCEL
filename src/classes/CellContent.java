package classes;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.text.ParseException;
import java.util.*;

public class CellContent extends javafx.scene.control.TableCell<TableRowModel, String> {
    private static final int ALPHABET_SIZE = 26;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private SimpleStringProperty formula, previousFormula;
    private SimpleStringProperty contentDisplayed;
    private String id;
    private boolean errorDetected;

    public String getPreviousFormula() {
        return previousFormula.get();
    }

    public SimpleStringProperty previousFormulaProperty() {
        return previousFormula;
    }

    public void setPreviousFormula(String previousFormula) {
        this.previousFormula.set(previousFormula);
    }

    public boolean isErrorDetected() {
        return errorDetected;
    }

    public void setErrorDetected(boolean errorDetected) {
        this.errorDetected = errorDetected;
    }

    public enum States {
        FORMULA, VALUE
    }

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet[i++] = c;
        }
    }

    public CellContent(String id) {
        super.setEditable(true);
        this.id = id;
        formula = new SimpleStringProperty("");
        previousFormula = new SimpleStringProperty("");
        contentDisplayed = new SimpleStringProperty("");
        errorDetected = false;
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

    public void setObservableContent(String content) {
        contentDisplayed.setValue(content);
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


    @Override
    public int hashCode() {
        return (cellValue != null) ? cellValue.hashCode() * 3 : 1 + formula.hashCode() * 15;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CellContent))
            return false;
        if (o == this)
            return true;
        return ((CellContent) o).getCellId().equals(getCellId());
    }

    @Override
    public String toString() {
        return getCellId();
    }

    public String getCellId() {
        return id;
    }

    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }

    public void update() {
        try {
            cellValue = CommandHelper.updateValueOfCell(this);
            errorDetected = cellValue == null;
        } catch (ExpressionParser.ExpressionFormatException e) {
            AlertManager.showAlertAndWait("Error", e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            setObservableContent(States.VALUE);
        }
    }
}
