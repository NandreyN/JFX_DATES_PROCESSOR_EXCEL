package classes;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CellContent extends javafx.scene.control.TableCell {
    private static final int ALPHABET_SIZE = 26;
    private static final char[] alphabet = new char[ALPHABET_SIZE];
    private Date cellValue;
    private String formula;
    private SimpleStringProperty contentDisplayed;

    private TextField textField;

    public static enum States {
        FORMULA, VALUE
    }

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            alphabet[i++] = c;
        }
    }

    public CellContent() {
        formula = "";
        contentDisplayed = new SimpleStringProperty();
        setObservableContent(States.FORMULA);
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

    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        Platform.runLater(() -> {
            textField.requestFocus();
            textField.selectAll();
        });
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText((String) getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        //String formula = item.toString();
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getContentObservable().getValue());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setFormula(item.toString());
                try {
                    Date newDate = CommandHelper.processFormula(formula);
                    setCellValue(newDate);
                    setObservableContent(States.VALUE);

                } catch (ExpressionParser.ExpressionFormatException e) {
                    e.printStackTrace();
                }
                setText(getContentObservable().getValue());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }


    private void createTextField() {
        textField = new TextField(getCellValueFormatted());
        //textField.textProperty().bind(getContentObservable());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
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


    public void setCellValue(Date value) {
        this.cellValue = value;
    }

    public Date getCellValue() {
        return cellValue;
    }
}
