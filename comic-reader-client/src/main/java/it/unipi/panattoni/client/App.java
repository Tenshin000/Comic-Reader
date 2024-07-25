package it.unipi.panattoni.client;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;

/**
 * JavaFX App
 * @author Francesco Panattoni
 */

public class App extends Application{
    // Campi
    private static Scene scene;
    private Rectangle2D screenBounds; 
    
    // Metodi
    
    // Metodo start
    @Override
    public void start(Stage stage) throws IOException{
        System.out.println("Comic Reader in funzione ...");
        
        // Ottieni le dimensioni dello schermo
        screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Imposta le dimensioni della finestra come le dimensioni dello schermo
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        
        // Carica il file FXML per la schermata di login e imposta le dimensioni
        scene = new Scene(loadFXML("login/login"), screenBounds.getWidth(), screenBounds.getHeight());
        stage.setScene(scene);
        // Personalizzo il titolo
        stage.setTitle("Comic Reader");
        // Personalizzo l'icona
        stage.getIcons().add(new Image("/img/Logo.png"));
        SessioneUtente.setInstance(null);
        stage.show();
        System.out.println("Comic Reader Avviata");
    }
    
    // Metodo per impostare la radice della scena con un file FXML specificato
    static public void setRoot(String fxml) throws IOException{
        scene.setRoot(loadFXML(fxml));
    }
    
    // Metodo per impostare la radice della scena con un oggetto Parent specificato
    static public void setRoot(Parent fxml) throws IOException{
        scene.setRoot(fxml);
    }
    
    // Metodo privato per caricare un file FXML
    private static Parent loadFXML(String fxml) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    // Metodo per impostare il titolo della finestra
    public static void setTitle(String title){
        if(scene != null){
            Stage stage = (Stage) scene.getWindow();
            if(stage != null)
                stage.setTitle(title);
        }
    }
    
    // Metodo main per avviare l'applicazione
    public static void main(String[] args){
        launch();
    }
}