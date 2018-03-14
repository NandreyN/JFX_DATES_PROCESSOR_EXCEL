package classes;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.text.ParseException;
import java.util.*;

public class CellContent extends javafx.scene.control.TableCell<TableRowModel, String> {
    private static final int ALPHABET_SIZE = 26;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private SimpleStringProperty formula;
    private SimpleStringProperty contentDisplayed;
    private String id;

    private Set<CellContent> subscribers;

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
        contentDisplayed = new SimpleStringProperty("");
        setObservableContent(States.FORMULA);
        subscribers = new HashSet<>();
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
        recalculate();

        notifySubscribers();
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
        return o instanceof CellContent && (o == this || ((CellContent) o).id.equals(id));
    }

    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }

    public void subscribe(CellContent c) {
        subscribers.add(c);
    }

    private void notifySubscribers() {
        subscribers.forEach(CellContent::recalculate);
    }

    public void clearSubscribersList() {
        subscribers.clear();
    }

    private void recalculate() {
        try {
            cellValue = CommandHelper.processFormula(getFormula().getValue(), this);
            setObservableContent(States.VALUE);
        } catch (ExpressionParser.ExpressionFormatException e) {
            e.printStackTrace();
        }
    }
}
