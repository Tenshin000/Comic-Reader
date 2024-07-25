package it.unipi.panattoni.client.login;

import it.unipi.panattoni.client.App;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Classe che gestisce il controller della schermata di cambio password
 * @author Francesco Panattoni
 */

public class ChangePasswordController{
    // Campi
    private final double DELAY = 3.0;
    final private String username;
    
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField conferma;
    @FXML
    private Button cambia;
    @FXML
    private Text notifica;
    
    // Metodi
    
    // Costruttore che riceve lo username come parametro
    public ChangePasswordController(String username){
        this.username = username;
        System.out.println("Username Ricevuto: " + username);
    }
    
    // Metodo chiamato quando si preme il pulsante di cambio password
    @FXML
    private void change_password(){
        // Ottieni la password e la sua conferma dai campi di input
        String pwd = password.getText();
        String pco = conferma.getText();
        
        // Effettua i controlli sui campi
        if(!fieldsControl(pwd, pco))
            return;
        
        String encodedUsr = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String encodedPwd = URLEncoder.encode(pwd, StandardCharsets.UTF_8);
        
        // URL per inviare la richiesta al server
        String s = "http://localhost:8080/utente/cambioPwd?usr=" + encodedUsr + "&pwd=" + encodedPwd;

        disactivateFields();
        change_task(s);
    }
    
    // Task che gestisce il cambio di password
    private void change_task(String link){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Password avviato!");
                HttpURLConnection con = null;
                try {
                    URL url = new URL(link);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
                    
                    // Invio richiesta e lettura risposta
                    StringBuilder content = new StringBuilder();
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                    // Parsifica la risposta JSON
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();
                    
                    // Gestisce la risposta del server
                    switch(response){
                        case "4003": // La Password è uguale a quella di prima
                        case "4004": // L'Username che è stato inserito non si trova nel Database
                            disappearNotifyAfterDelay("Credenziali Errate");
                            System.out.println("Credenziali Errate");
                            activateFields();
                            break;
                        case "2000": 
                            // Cambio password avvenuto con successo
                            notifica.setFill(Color.web("#2902EE"));
                            disappearNotifyAfterDelay("Cambio password avvenuto con successo");
                            System.out.println("Cambio password avvenuto con successo");
                            Thread.sleep(1000); // Per mostrare che il tutto ha avuto successo
                            activateFields();
                            App.setRoot("login/login");
                            break;
                        default:     
                            // Risposta sconosciuta
                            disappearNotifyAfterDelay("Qualcosa è andato storto");
                            System.out.println("Qualcosa è andato storto");
                            activateFields();
                    }   
                }
                catch(InterruptedException | IOException ex){
                    ex.printStackTrace();
                }
                finally{
                    activateFields();
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Cambio Password concluso!");
                }
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo per controllare i campi di cambio password
    private boolean fieldsControl(String pwd, String pco){
        if(username == null){
            disappearNotifyAfterDelay("Non è disponibile lo Username, si consiglia di riprovare");
            return false;
        }
        
        if(pwd.length() < 5){
            disappearNotifyAfterDelay("La Password deve avere almeno 5 caratteri");
            return false;
        }
        
        if(pwd.length() > 15){
            disappearNotifyAfterDelay("La Password deve avere meno di 15 caratteri");
            return false;
        }
        
        if(!pwd.equals(pco)){
            disappearNotifyAfterDelay("Le password non combaciano tra loro");
            return false;
        }
        
        return true;
    }
    
    // Metodo per far apparire una notifica e farla scomparire dopo DELAY secondi
    @FXML
    private void disappearNotifyAfterDelay(String message){
        // Fa apparire il messaggio
        notifica.setVisible(true);
        notifica.setText(message);
        
        // Crea una transizione di pausa di DELAY secondi
        PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
        
        // Imposta l'azione da eseguire dopo il ritardo
        delay.setOnFinished(event -> {
            // Nascondi il testo
            notifica.setText("");
            notifica.setVisible(false);
            notifica.setFill(Color.web("#F20202"));
        });
        
        // Avvia la transizione di pausa
        delay.play();
    }
    
    // Metodo per attivare i campi dopo l'esecuzione di un'operazione
    @FXML
    private void activateFields(){
        cambia.setDisable(false);
        password.setDisable(false);
        conferma.setDisable(false);        
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        cambia.setDisable(true);
        password.setDisable(true);
        conferma.setDisable(true);   
    }
    
    // Metodo per tornare alla schermata di login
    @FXML
    private void switchToLogin() throws IOException{
        App.setRoot("login/login");
    }
}
