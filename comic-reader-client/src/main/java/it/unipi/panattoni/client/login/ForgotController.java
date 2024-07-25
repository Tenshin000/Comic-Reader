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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Classe che gestisce il controller della schermata di password dimenticata
 * @author Francesco Panattoni
 */

public class ForgotController{
    // Campi
    private final double DELAY = 3.0; 
    
    @FXML
    private TextField username; 
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
    
    // Metodo chiamato quando si preme il pulsante di recupero password
    @FXML
    private void forgot(){
        String usr = username.getText();
        String dom = (String) domanda.getValue();
        String ris = risposta.getText();
        
        if(dom == null)
            dom = "";
        
        if(!fieldsControl(usr, dom, ris))
            return;
        
        String encodedUsr = URLEncoder.encode(usr, StandardCharsets.UTF_8);
        String encodedDom = URLEncoder.encode(dom, StandardCharsets.UTF_8);
        String encodedRis = URLEncoder.encode(ris, StandardCharsets.UTF_8);
        // Codifica la domanda per poterla inviare via URL
        String s = "http://localhost:8080/utente/forgot?usr=" + encodedUsr + "&dom=" + encodedDom + "&ris=" + encodedRis;

        disactivateFields();
        forgot_task(usr, s);
    }
    
    // Task che si occupa di controllare se le credenziali di recupero password sono corrette
    private void forgot_task(String usr, String link){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Password Dimenticata avviato!");
                HttpURLConnection con = null;
                try {
                    URL url = new URL(link);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
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
                    
                    // Gestisce la risposta del server
                    switch(response){
                        case "4003": // Domanda o Risposta errata 
                        case "4004": // L'Username che è stato inserito non si trova nel Database
                            disappearNotifyAfterDelay("Credenziali Errate");
                            System.out.println("Credenziali Errate");
                            break;
                        case "2000":
                            // Username, Domanda e Risposta corretti
                            System.out.println("Credenziali Corrette");
                            // Prepara la prossima pagina dandogli lo Username
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("changepwd.fxml"));   
                            ChangePasswordController controller = new ChangePasswordController(usr);
                            loader.setController(controller);
                            Parent root = loader.load();
                            App.setRoot(root);
                            break;
                        default:     
                            // Risposta sconosciuta, quindi non implementata
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
                    System.out.println("Task Password Dimenticata concluso!");
                }
                return null;
            }   
        };
                
        new Thread(task).start();
    }
    
    // Metodo per popolare la ComboBox con le domande di sicurezza
    @FXML
    private void questions(){
        List<String> lista = new ArrayList();
        lista.addAll(Arrays.asList(lista_domande));
        
        ObservableList lista_data = FXCollections.observableArrayList(lista);
        domanda.setItems(lista_data);
    }
    
    // Metodo per controllare i campi di recupero password
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
        });
        
        // Avvia la transizione di pausa
        delay.play();
    }
    
    // Metodo per attivare i campi dopo l'esecuzione di un'operazione
    @FXML
    private void activateFields(){
        cambia.setDisable(false);
        username.setDisable(false);
        domanda.setDisable(false);
        risposta.setDisable(false);
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        cambia.setDisable(true);
        username.setDisable(true);
        domanda.setDisable(true);
        risposta.setDisable(true);
    }
    
    // Metodo per tornare alla schermata di login
    @FXML
    private void switchToLogin() throws IOException{
        App.setRoot("login/login");
    }
    
    // Metodo chiamato durante l'inizializzazione del controller
    @FXML
    protected void initialize(){
        questions();
    }
}
