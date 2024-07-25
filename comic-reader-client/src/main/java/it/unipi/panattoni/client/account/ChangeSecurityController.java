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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller per la schermata di cambio domanda di sicurezza e/o risposta di sicurezza.
 * @author Francesco Panattoni
 */

public class ChangeSecurityController{
    // Campi
    private final double DELAY = 3.0; 
    
    @FXML
    private ComboBox domanda;
    @FXML
    private TextField risposta;
    @FXML
    private Button cambia;
    @FXML
    private Text notifica;
    
    // Lista di domande per il ComboBox domanda
    final private String[] lista_domande = {"Domanda di Sicurezza", "In quale città sei nato?", 
                                            "Qual è il nome del tuo primo animale domestico?", "Qual è il tuo fumetto/manga preferito?", 
                                            "Qual è il nome del tuo colore preferito?", "Qual è la tua canzone preferita?",
                                            "Qual è il nome del tuo sport preferito?", "Qual è il nome del tuo cibo preferito?"};
    
    // Metodi
    
    // Metodo chiamato quando viene premuto il pulsante di cambio domanda e risposta di sicurezza
    @FXML
    private void change_security(){
        String usr = SessioneUtente.getUsername();
        String dom = (String) domanda.getValue();
        String ris = risposta.getText();
        
        if(dom == null)
            dom = "";
        
        // Controlla che tutti i campi siano validi prima di procedere
        if(!fieldsControl(usr, dom, ris))
            return;
        
        String encodedUsr = URLEncoder.encode(usr, StandardCharsets.UTF_8);
        String encodedDom = URLEncoder.encode(dom, StandardCharsets.UTF_8);
        String encodedRis = URLEncoder.encode(ris, StandardCharsets.UTF_8);
        // Costruisce l'URL per la richiesta di cambio domanda e risposta di sicurezza
        String s = "http://localhost:8080/utente/cambioSec?usr=" + encodedUsr + "&dom=" + encodedDom + "&ris=" + encodedRis;
        
        disactivateFields();
        change_task(s);
    }
    
    private void change_task(String link){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Domanda e/o Risposta di Sicurezza avviato!");
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
                    
                    activateFields();
                    
                    // Agisce in base alla risposta ricevuta dal server
                    switch(response){
                        case "4003": // Domanda o Risposta errata 
                            disappearNotifyAfterDelay("Credenziali identiche a quelle di prima");
                            System.out.println("Credenziali identiche a quelle di prima");
                            break;
                        case "4004": // L'Username che è stato inserito non si trova nel Database
                            disappearNotifyAfterDelay("Errore: Username non presente");
                            System.out.println("Errore: Username non presente");
                            break;
                        case "2000":
                            // Username, Domanda e Risposta cambiati
                            notifica.setFill(Color.web("#2902EE"));
                            disappearNotifyAfterDelay("Cambio credenziali avvenuto con successo");
                            Thread.sleep(1000);
                            System.out.println("Cambio credenziali avvenuto con successo");
                            // Reindirizza alla schermata di profilo
                            App.setRoot("account/profilo");
                            break;
                        default:     
                            // Risposta sconosciuta
                            disappearNotifyAfterDelay("Errore: Qualcosa è andato storto");
                            System.out.println("Errore: Qualcosa è andato storto");
                    }   
                }
                catch(InterruptedException | IOException ex){
                    ex.printStackTrace();
                }
                finally{
                    activateFields();
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Cambio Domanda e/o Risposta di Sicurezza concluso!");
                }
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo per popolare la ComboBox con le domande di sicurezza
    private void questions(){
        List<String> lista = new ArrayList();
        lista.addAll(Arrays.asList(lista_domande));
        
        ObservableList lista_data = FXCollections.observableArrayList(lista);
        domanda.setItems(lista_data);
    }
    
    // Metodo per controllare che tutti i campi siano stati compilati correttamente
    private boolean fieldsControl(String usr, String dom, String ris){
        if(usr == null || usr.equals("")){
            disappearNotifyAfterDelay("Non hai inserito un Username");
            return false;
        }        
        
        if(dom.equals("") || dom.equals("Domanda di Sicurezza") || ris.equals("") || ris.equals(" ")){
            disappearNotifyAfterDelay("Inserimento errato o incompleto");
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
        
        // Crea una transizione di pausa di 3 secondi
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
        domanda.setDisable(false);
        risposta.setDisable(false);
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        cambia.setDisable(true);
        domanda.setDisable(true);
        risposta.setDisable(true);
    }
    
    // Metodo per passare alla schermata di Profilo
    @FXML
    private void switchToProfilo() throws IOException {
        App.setRoot("account/profilo");
    }
    
    // Metodo chiamato durante l'inizializzazione del controller
    @FXML
    protected void initialize(){
        questions();
    }
}
