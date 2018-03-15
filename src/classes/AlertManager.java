package classes;

import javafx.scene.control.Alert;

public class AlertManager {
    public static void showAlertAndWait(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
