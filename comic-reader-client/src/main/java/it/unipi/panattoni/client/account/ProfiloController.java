package it.unipi.panattoni.client.account;

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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller per la schermata di gestione del profilo utente
 * @author Francesco Panattoni
 */

public class ProfiloController {
    // Campi
    private final double DELAY = 3.0; 
    
    @FXML
    private TextField username;
    @FXML
    private Button cambia;
    @FXML
    private CheckBox manga;
    @FXML
    private Text notifica;
    
    // Metodi
    
    // Metodo per inserire i dati dell'utente nel form
    private void inserisci_dati(){
        username.setText(SessioneUtente.getUsername());
        username.setEditable(false);
        
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Profilo avviato!");
                HttpURLConnection con = null;
                String encodedUsr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);

                try{
                    // Creazione di un'URL per la richiesta di ricerca del manga
                    URL url = new URL("http://localhost:8080/utente/getManga?usr=" + encodedUsr);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    // Ricevo come risposta il booleano per la lettura dei manga
                    boolean response = Boolean.parseBoolean(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
                    manga.setSelected(response);
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Profilo concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo chiamato quando si cambia lo stato della lettura manga
    @FXML
    private void cambia_lettura_manga(){
        final boolean b = manga.isSelected();
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Lettura Manga avviato!");
                HttpURLConnection con = null;
                String encodedUsr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
                try{
                    URL url = new URL("http://localhost:8080/utente/mangaLet?usr=" + encodedUsr + "&manga=" + b); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    
                    // Invio richiesta e lettura risposta
                    StringBuilder content = new StringBuilder();
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);
                    }
                    catch(Exception ex){
                        ex.printStackTrace();
                    }   
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    String usr = SessioneUtente.getUsername();
                    SessioneUtente.setInstance(usr);
                    System.out.println("Task Cambio Lettura Manga concluso!");
                }
 
                return null;
            }   
        };

        new Thread(task).start();
    }
    
    // Metodo chiamato quando si vuole cambiare lo username
    @FXML
    private void cambia_username(){
        // Abilita la modifica dello username e imposta lo stile
        username.setEditable(true);
        username.setStyle("-fx-border-color: blue;");
        // Imposta l'azione del pulsante per confermare il nuovo username
        cambia.setOnAction(event -> {
            conferma_username();
        }); 
        cambia.setText("Conferma Nuovo Username"); 
    }
    
    // Metodo chiamato quando si conferma il nuovo username
    @FXML
    private void conferma_username(){
        // Recupera il nuovo username inserito dall'utente
        final String nuovo_username = username.getText();
        // Esegue controlli sull'username inserito
        if(nuovo_username.contains(" ")){
             // Se l'username contiene spazi, mostra un messaggio di errore
            cambia.setOnAction(event -> {
                cambia_username();
            }); 
            disappearNotifyAfterDelay("Lo Username non può contenere spazi");
            return;
        }
        
        // Controllo lunghezza
        if(nuovo_username.length() < 5){
            cambia.setOnAction(event -> {
                cambia_username();
            }); 
            disappearNotifyAfterDelay("Lo Username deve avere almeno 5 caratteri");
            return;
        }
        
        if(nuovo_username.length() > 15){
            cambia.setOnAction(event -> {
                cambia_username();
            }); 
            disappearNotifyAfterDelay("Lo Username deve avere meno di 15 caratteri");
            return;
        }
        
        // Disabilita la modifica dello username e resetta lo stile
        username.setEditable(false);
        
        cambia.setText("Cambia Username");
        username.setStyle("");
        // Controlla se l'username inserito è diverso da quello attuale
        if(nuovo_username.equals(SessioneUtente.getUsername())){
            // Se l'username inserito è lo stesso, mostra un messaggio di errore
            cambia.setOnAction(event -> {
                cambia_username();
            }); 
            disappearNotifyAfterDelay("Hai inserito lo stesso Username");
        }
        else{
            // Altrimenti, esegue il cambio dell'username
            cambia.setOnAction(null);
            conferma_task(nuovo_username);
        }
    }
    
    // Metodo per eseguire il cambio dell'username
    private void conferma_task(String nuovo_username){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Username avviato!");
                HttpURLConnection con = null;
                try{
                    URL url = new URL("http://localhost:8080/utente/cambioUsr?old_usr=" + SessioneUtente.getUsername() + "&new_usr=" + nuovo_username); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
                    
                    // Invio richiesta e lettura risposta
                    StringBuilder content = new StringBuilder();
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);
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
                        case "4004": // Username non trovato
                            username.setText(SessioneUtente.getUsername());
                            disappearNotifyAfterDelay("Errore: Username non trovato");
                            System.out.println("Errore: Username non trovato!");
                            break;
                        case "4009": // Username già esistente
                            username.setText(SessioneUtente.getUsername());
                            disappearNotifyAfterDelay("Errore: Lo Username scelto è già esistente!");
                            System.out.println("Errore: Lo Username scelto è già esistente!");
                            break;
                        case "2000": // Cambio username avvenuto con successo
                            SessioneUtente.setInstance(nuovo_username);
                            username.setText(SessioneUtente.getUsername());
                            notifica.setFill(Color.web("#2902EE"));
                            disappearNotifyAfterDelay("Username cambiato");
                            System.out.println("Username cambiato");
                            break;
                        default: // Risposta sconosciuta
                            username.setText(SessioneUtente.getUsername());
                            disappearNotifyAfterDelay("Errore Grave");
                            System.out.println("Errore Grave!");
                    }   
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    
                    // Ripristina l'azione del pulsante per cambiare lo username
                    cambia.setOnAction(event -> {
                        cambia_username();
                    }); 
                    
                    System.out.println("Task Cambio Username concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
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
    
    // Metodo per passare alla schermata di cambio password
    @FXML
    private void switchToChangePwd() throws IOException{
        App.setRoot("account/changepwd");        
    }
    
    // Metodo per passare alla schermata di cambio domanda di sicurezza e risposta di sicurezza
    @FXML
    private void switchToChangeSecurity() throws IOException{
        App.setRoot("account/changesecurity");        
    }
    
    // Metodo per passare alla schermata principale
    @FXML
    private void switchToHome() throws IOException{
        App.setRoot("home");
    } 
    
    // Metodo chiamato all'inizializzazione del controller
    @FXML
    protected void initialize(){
        inserisci_dati();
    }
}
