package it.unipi.panattoni.client.login;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.SessioneUtente;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.util.Duration;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Classe che gestisce il controller della schermata di registrazione
 * @author Francesco Panattoni
 */

public class RegisterController{
    // Campi
    private final double DELAY = 3.0; 

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField conferma;
    @FXML
    private ComboBox domanda;
    @FXML
    private TextField risposta;
    @FXML
    private Button registrazione;
    @FXML
    private Text notifica;
    @FXML
    private Button login;
    
    // Lista di domande per il ComboBox domanda
    final private String[] lista_domande = {"Domanda di Sicurezza", "In quale città sei nato?", 
                                            "Qual è il nome del tuo primo animale domestico?", "Qual è il tuo fumetto/manga preferito?", 
                                            "Qual è il nome del tuo colore preferito?", "Qual è la tua canzone preferita?",
                                            "Qual è il nome del tuo sport preferito?", "Qual è il nome del tuo cibo preferito?"};
    
    // Metodi
    
    // Metodo chiamato quando si preme il pulsante di registrazione
    @FXML 
    private void signup(){ 
        // Ottieni i valori dai campi di input
        String usr = username.getText();
        String pwd = password.getText();
        String pco = conferma.getText();
        String dom = (String) domanda.getValue();
        String ris = risposta.getText();
        
        // Controllo se la domanda di sicurezza è nulla
        if(dom == null)
            dom = "";
        
        // Effettua i controlli sui campi
        if(!fieldsControl(usr, pwd, pco, dom, ris))
            return;
        
        // Crea un oggetto Utente con i valori inseriti
        Utente u = new Utente(usr, pwd, dom, ris);
        
        signup_task(u);        
        disactivateFields();
    }
    
    // Metodo per eseguire il task di registrazione
    private void signup_task(Utente u){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Registrazione avviato!");
                HttpURLConnection con = null;
                try{  
                    // Crea la connessione HTTP
                    URL url = new URL("http://localhost:8080/utente/signup"); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
                    
                    System.out.println("Connessione creata!");
                    
                    // Converti l'oggetto Utente in formato JSON
                    Gson gson = new Gson();
                    String data = gson.toJson(u);
                    
                    // Invia i dati al server
                    try(OutputStream os = con.getOutputStream()){
                        byte[] input = data.getBytes("utf-8");
                        os.write(input, 0, input.length);	
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                    // Ottieni il codice di risposta HTTP
                    int code = con.getResponseCode();
                    System.out.println("Codice di risposta: " + code);
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        String inputLine;
                        StringBuilder content = new StringBuilder();

                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        // Parsifica la risposta JSON
                        JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                        String response = json.getAsJsonObject().get("id").getAsString();
                        
                        // Gestisce la risposta del server
                        switch(response){
                            case "4009": // Username già in uso
                                disappearNotifyAfterDelay("Utente già esistente. Username già preso.");
                                System.out.println("Utente già esistente. Username già preso.");
                                activateFields();
                                break;
                            case "2000": // Registrazione avvenuta con successo  
                                System.out.println("Nuovo Account Registrato!");
                                SessioneUtente.setInstance(u.username);
                                activateFields();
                                App.setRoot("home");
                                break;
                            default: // Risposta sconosciuta
                               disappearNotifyAfterDelay("Qualcosa è andato storto");
                               System.out.println("Qualcosa è andato storto");
                               activateFields();
                        }
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Registrazione concluso!");
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
        domanda.setPromptText("Domanda di Sicurezza");
    }
    
    // Metodo per controllare i campi di registrazione
    private boolean fieldsControl(String usr, String pwd, String pco, String dom, String ris){
        // Controllo se l'username contiene spazi
        if(usr.contains(" ")){
            disappearNotifyAfterDelay("Lo Username non può contenere spazi");
            return false;
        }
        
        // Controllo lunghezza username
        if(usr.length() < 5){
            disappearNotifyAfterDelay("Lo Username deve avere almeno 5 caratteri");
            return false;
        }
        
        if(usr.length() > 15){
            disappearNotifyAfterDelay("Lo Username deve avere meno di 15 caratteri");
            return false;
        }
        
        // Controllo lunghezza password
        if(pwd.length() < 5){
            disappearNotifyAfterDelay("La Password deve avere almeno 5 caratteri");
            return false;
        }
        
        if(pwd.length() > 15){
            disappearNotifyAfterDelay("La Password deve avere meno di 15 caratteri");
            return false;
        }
        
        // Controllo campi vuoti o con valori non validi
        if(usr.equals("") || usr.equals(" ") || pwd.equals("") || pwd.equals(" ") || pco.equals("") || pco.equals(" ") || dom.equals("") || dom.equals("Domanda di Sicurezza") || ris.equals("") || ris.equals(" ")){
            disappearNotifyAfterDelay("Inserimento errato o incompleto");
            return false;
        }
        
        // Controllo se la password confermata corrisponde alla password inserita
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
        });
        
        // Avvia la transizione di pausa
        delay.play();
    }
    
    // Metodo per attivare i campi dopo l'esecuzione di un'operazione
    @FXML
    private void activateFields(){
        registrazione.setDisable(false);
        username.setDisable(false);
        password.setDisable(false);
        conferma.setDisable(false);
        domanda.setDisable(false);
        risposta.setDisable(false);
        login.setDisable(false);
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        registrazione.setDisable(true);
        username.setDisable(true);
        password.setDisable(true);
        conferma.setDisable(true);
        domanda.setDisable(true);
        risposta.setDisable(true);
        login.setDisable(true);
    }
    
    // Metodo per tornare alla schermata di login
    @FXML
    private void switchToLogin() throws IOException{
        App.setRoot("login/login");
    }
    
    // Metodo chiamato durante l'inizializzazione del controller
    @FXML
    protected void initialize(){
        SessioneUtente.setInstance(null);
        questions();
    }
}
