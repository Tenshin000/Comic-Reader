package it.unipi.panattoni.client.account;

import it.unipi.panattoni.client.App;

import java.io.IOException;
import javafx.fxml.FXML;

/**
 * Controller per la schermata di gestione del Manuale
 * @author Francesco Panattoni
 */

public class ManualeController{
    
    // Metodo per passare alla schermata principale
    @FXML
    private void switchToHome() throws IOException{
        App.setRoot("home");
    } 
}
