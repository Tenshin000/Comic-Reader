package it.unipi.panattoni.client.login;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.SessioneUtente;

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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Classe che gestisce il controller della schermata di login
 * @author Francesco Panattoni
 */

public class LoginController{
    // Campi
    private final double DELAY = 3.0; 
    
    @FXML
    private TextField username; // TextField per inserire l'Username
    @FXML
    private PasswordField password; // PasswordField per inserire la Password
    @FXML
    private CheckBox mostra; // CheckBox per sostituire a "password" "mostra_password" e viceversa
    @FXML
    private TextField mostra_password; // TextField per togliere l'occultamento alla Password
    @FXML
    private Button login; // Per fare il login
    @FXML
    private Text notifica; // Notifica che informa l'utente di cosa avviene
    @FXML
    private Text dimenticata; // Un testo che se cliccato porta alla schermata di Password Dimenticata
    @FXML
    private Button registrazione; // Per fare la registrazione
    
    // Metodi    
    
    // Metodo chiamato quando si preme il pulsante di login
    @FXML 
    private void login(){
        String usr = username.getText();
        String pwd = password.getText();
        
        // Controlla se la password è vuota e mostra_password non è vuota
        if(pwd.equals("") && !mostra_password.getText().equals(""))
            pwd = mostra_password.getText();        
        
        // Effettua i controlli sui campi Username e Password
        if(!fieldsControl(usr, pwd))       
            return;
        
        usr = URLEncoder.encode(usr, StandardCharsets.UTF_8);
        pwd = URLEncoder.encode(pwd, StandardCharsets.UTF_8);
        String s = "http://localhost:8080/utente/login?usr=" + usr + "&pwd=" + pwd;
        
        disactivateFields();
        login_task(usr, pwd, s);  
    }
    
    // Funzione che contiente il task per il login
    private void login_task(String usr, String pwd, String link){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Login avviato!");
                HttpURLConnection con = null;
                try {
                    URL url = new URL(link);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
                    
                    System.out.println("Connessione creata!");
                    
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
                    
                    // Arrivata la risposta del server, allora agiamo di conseguenza
                    switch(response){
                        case "4003": // Password errata 
                        case "4004": // L'Username che è stato inserito non si trova nel Database
                            disappearNotifyAfterDelay("Credenziali Errate");
                            System.out.println("Credenziali Errate");
                            break;
                        case "2000":
                            // Login correttamente svolto. Si passa alla home. 
                            System.out.println("Login Effettuato!");
                            SessioneUtente.setInstance(usr);
                            Platform.runLater(() -> {
                                // Modifiche all'UI qui
                                activateFields();
                                // Cambio della schermata dopo il login
                                try{
                                    App.setRoot("home");
                                } 
                                catch(IOException ioe){
                                    System.out.println("Accesso alla pagina personale negato");
                                    ioe.printStackTrace();
                                }
                            });
    
                            break;
                        default:     
                            // Risposta sconosciuta
                            disappearNotifyAfterDelay("Qualcosa è andato storto");
                            System.out.println("Qualcosa è andato storto");
                    }   
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    activateFields();
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Login concluso!");
                }
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo per controllare i campi Username e Password
    private boolean fieldsControl(String usr, String pwd){
        // Controllo se l'username è vuoto
        if(usr == null || usr.equals("")){
            disappearNotifyAfterDelay("Non hai inserito un Username");
            return false;
        }
        
        // Controllo se la password è vuota
        if(pwd == null || pwd.equals("")){
            disappearNotifyAfterDelay("Non hai inserito una Password");
            return false;
        }
        
        // Controllo se l'username contiene spazi
        if(usr.contains(" ")){
            disappearNotifyAfterDelay("Lo Username non può contenere spazi");
            return false;
        } 
        
        return true;
    }
    
    // Metodo per alternare la visibilità della password
    @FXML
    private void togglePasswordVisibility(){
        if(mostra.isSelected()){
            // Se "mostra" è selezionato si toglie l'occultamento
            mostra_password.setText(password.getText());
            password.setText("");
            mostra_password.setVisible(true);
            password.setVisible(false);
        }
        else{
            // Se "mostra" non è selezionato l'occultamento rimane
            password.setText(mostra_password.getText());
            mostra_password.setText("");
            password.setVisible(true);
            mostra_password.setVisible(false);
        }
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
        });
        
        // Avvia la transizione di pausa
        delay.play();
    }
    
    // Metodo per attivare i campi dopo l'esecuzione di un'operazione
    @FXML
    private void activateFields(){
        login.setDisable(false);
        username.setDisable(false);
        password.setDisable(false);
        mostra_password.setDisable(false);
        mostra.setDisable(false);
        dimenticata.setDisable(false);
        registrazione.setDisable(false);
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        login.setDisable(true);
        username.setDisable(true);
        password.setDisable(true);
        mostra_password.setDisable(true);
        mostra.setDisable(true);
        dimenticata.setDisable(true);
        registrazione.setDisable(true);
    }
    
     // Metodo chiamato quando il mouse passa sopra al testo "Hai dimenticato la password?"
    @FXML
    private void underlineForgot(MouseEvent event){
        Text textNode = (Text) event.getSource();
        // Inserisce la sottolineatura quando il mouse entra
        textNode.setUnderline(true);    
        // Rimuove la sottolineatura quando il mouse esce
        textNode.setOnMouseExited(e -> textNode.setUnderline(false));
    }    
    
    // Metodo per passare alla schermata di registrazione
    @FXML
    private void switchToRegister() throws IOException{
        App.setRoot("login/register");
    }
    
    // Metodo per passare alla schermata di Password Dimenticata
    @FXML  
    private void switchToForgot() throws IOException{
        App.setRoot("login/forgot");
    }
}