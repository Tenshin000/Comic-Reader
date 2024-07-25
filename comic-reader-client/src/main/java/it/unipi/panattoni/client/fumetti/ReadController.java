package it.unipi.panattoni.client.fumetti;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.SessioneUtente;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * Classe che gestisce il controller della schermata del Capitolo selezionato e permette la lettura del Capitolo
 * @author Francesco Panattoni
 */

public class ReadController{
    // Campi
    final private double MIN_SCALA = 0.5; // Minimo di scala per lo zoom
    final private double MAX_SCALA = 4; // Massimo di scala per lo zoom
    private static final double ZOOM_DELTA = 2; // Quanto si zooma ad ogni click del tasto sinistro del mouse
    private double scala = 1.0; // Scala attuale della pagina
    
    @FXML
    private ImageView pagina;
    @FXML
    private Text numero;
    @FXML
    private Button sinistra;
    @FXML
    private Button destra;
    
    private boolean attiva_manga; // Flag che attiva la lettura orientale per i manga
    private int capitoli; // Numero di capitoli del fumetto scelto
    private int numero_pagina; // Pagina attuale
    private int massimo_pagine; // Massimo pagine

    // Metodi
    
    // Funzione per inizializzare la pagina di un nuovo capitolo scelto
    private void nuovo_capitolo(boolean prima){
        Task<Void> task= new Task<>() {
            @Override
            public Void call(){
                System.out.println("Task Conta Pagine avviato!");
                HttpURLConnection con = null;
                
                 // Codifica il percorso del fumetto e il numero del capitolo
                final String pathFumetto = URLEncoder.encode(recupera_fumetto(SessioneUtente.getFumettoScelto()), StandardCharsets.UTF_8);
                final Integer capitolo = recupera_numero(SessioneUtente.getFumettoScelto());
                
                try{
                    URL url = new URL("http://localhost:8080/capitolo/pages?pathFumetto=" + pathFumetto + "&capitolo=" + capitolo); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
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
                    
                    // Parsifica la risposta JSON per ottenere il numero massimo di pagine del capitolo
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();
                    
                    // Imposta il numero massimo di pagine del capitolo
                    massimo_pagine = Integer.parseInt(response);
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    activateFields();
                    System.out.println("Task Conta Pagine concluso!");
                }
 
                return null;
            }   
        };
        
        // Aggiunge un'azione da eseguire quando il task ha successo
        task.setOnSucceeded(event -> {
            // Una volta completato il thread di conta_pagine, impostiamo numero_pagina e avviamo cambio_pagina()
            numero_pagina = (prima) ? 1 : massimo_pagine;
            // A seconda del numero_pagina, cambio_pagina() decide la pagina da mettere
            cambio_pagina();
            // Comunica al server che l'utente sta leggendo questo capitolo
            setLettura();
        });
        
        new Thread(task).start();
    }
    
    // Metodo che controlla se il fumetto è un manga e attiva i pulsanti di navigazione in base a questa informazione
    private void isManga(){
        String path = URLEncoder.encode(recupera_fumetto(SessioneUtente.getFumettoScelto()), StandardCharsets.UTF_8);

        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Lettura Manga avviato!");
                HttpURLConnection con = null;
                try{
                    URL url = new URL("http://localhost:8080/fumetto/getManga?path=" + path);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    
                    boolean manga = Boolean.parseBoolean(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
                    if(manga && SessioneUtente.getManga())
                        attiva_manga = true;
                    else
                        attiva_manga = false;
                    
                    if(!attiva_manga){
                        // Lettura standard
                        destra.setOnAction(event -> {
                            successiva();
                        });

                        sinistra.setOnAction(event -> {
                            precedente();
                        });
                    }
                    else{
                        // Lettura orientale per i manga
                        destra.setOnAction(event -> {
                            precedente();
                        });

                        sinistra.setOnAction(event -> {
                            successiva();
                        });
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Lettura Manga concluso!");
                }
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che trova la pagina corrente del capitolo.
    // @param prima: Flag che indica se la pagina è la prima o l'ultima del capitolo.
    private void cambio_pagina(){          
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Pagina avviato!");
                
                HttpURLConnection con = null;    
                final String encodedPath = URLEncoder.encode(SessioneUtente.getFumettoScelto(), StandardCharsets.UTF_8);
                
                try{
                    URL url = new URL("http://localhost:8080/capitolo/read?path=" + encodedPath + "&pagina=" + numero_pagina);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                    // Controllo dello stato della risposta
                    int responseCode = con.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        // Lettura dell'immagine dalla risposta
                        InputStream inputStream = con.getInputStream();
                        Image image = new Image(inputStream);
                        
                        if(image != null){
                            Platform.runLater(() -> {
                                numero.setText(numero_pagina + " / " + massimo_pagine);
                                pagina.setImage(image);
                                pagina.setScaleX(scala);
                                pagina.setScaleY(scala); 
                            });
                        }
                        else
                            System.out.println("Errore nel caricamento dell'immagine"); 
                    }
                    else{
                        // Gestione degli errori di risposta
                        System.out.println("Errore nella richiesta HTTP: " + responseCode);
                        return null; // Se c'è stto un errore, restituisci null
                    }
                } 
                catch (IOException ioe){
                    ioe.printStackTrace();
                    return null;
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    activateFields();
                }
                
                return null;                
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che informa il server che il fumetto è stato letto dall'Utente e a che capitolo si trova
    private void setLettura(){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Lettura avviato!");
                HttpURLConnection con = null;
                                
                final String usr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
                final String fumetto = URLEncoder.encode(recupera_fumetto(SessioneUtente.getFumettoScelto()), StandardCharsets.UTF_8);
                final Integer capitolo = recupera_numero(SessioneUtente.getFumettoScelto());
                
                try{
                    URL url = new URL("http://localhost:8080/lettura/add?usr=" + usr + "&path=" + fumetto + "&capitolo=" + capitolo); 
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
                    
                    // Gestiamo la risposta del server
                    switch(response){
                        case "4004": // Il Fumetto che è stato inserito non si trova nel Database
                            System.out.println("Il Fumetto non è stato trovato nel Database");
                            break;
                        case "4007": // Utente non trovato
                            System.out.println("Username non trovato. Errore Grave!");
                            break;
                        case "4015": // Capitolo minore di 0, non dovrebbe mai accadere
                            System.out.println("Capitolo minore di 0");
                            break;
                        case "4009": // Lettura già presente
                        case "2000": // Operazione avvenuta
                            System.out.println("Operazione andata a buon fine!");                            
                            break;
                        default:     
                            // Risposta sconosciuta, quindi non implementata
                            System.out.println("Qualcosa è andato storto!");
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Cambio Lettura concluso!");
                }
 
                return null;
            }   
        };

        new Thread(task).start();
    }
    
    // Metodo che conta i capitoli di un fumetto dato il suo path
    private void conta_capitoli_fumetto(){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Conta Capitoli avviato!");
                String pathFumetto = URLEncoder.encode(recupera_fumetto(SessioneUtente.getFumettoScelto()), StandardCharsets.UTF_8);
                
                HttpURLConnection con = null;
                try{
                    URL url = new URL("http://localhost:8080/capitolo/chapters?pathFumetto=" + pathFumetto);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    
                    capitoli = Integer.parseInt(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                    capitoli = 0;
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Conta Capitoli concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che recupera il path del fumetto dal path del capitolo
    private String recupera_fumetto(String input){
        String output = input.replaceAll("/\\d+$", "");
        return output;   
    }
    
    // Metodo che recupera il numero del capitolo dal path del capitolo
    private int recupera_numero(String input){
        String output = input.replaceAll(".*/(\\d+)$", "$1");
        int num = Integer.parseInt(output);
        return num;        
    }
    
    // Metodo che gestisce il passaggio alla pagina successiva
    private void successiva(){
        disactivateFields();
        if(numero_pagina < massimo_pagine){
            // Cambia pagina
            numero_pagina++;
            scala = 1;
            cambio_pagina();
            activateFields();
        }
        else{    
            // Cambia capitolo se possibile
            String pathFumetto = recupera_fumetto(SessioneUtente.getFumettoScelto());
            int numeroFumetto = recupera_numero(SessioneUtente.getFumettoScelto());
            numeroFumetto++;    
            
            // Controlla se esistono ulteriori capitoli
            if(numeroFumetto <= capitoli){
                // Passa al capitolo successivo
                numero.setText("Cambio Capitolo in corso ...");
                
                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            
                String new_path = pathFumetto + "/" + numeroFumetto;
                SessioneUtente.setFumettoScelto(new_path);
                nuovo_capitolo(true);
            }
            else
                activateFields();
        }     
    }
    
    // Metodo che gestisce il passaggio alla pagina precedente
    private void precedente(){
        disactivateFields();
        if(numero_pagina == 1){           
            String pathFumetto = recupera_fumetto(SessioneUtente.getFumettoScelto());
            int numeroFumetto = recupera_numero(SessioneUtente.getFumettoScelto());
            numeroFumetto--;
            
            if(numeroFumetto > 0){
                // Passa al capitolo precedente
                try{
                    numero.setText("Cambio Capitolo in corso ...");
                    Thread.sleep(1000);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
                
                String new_path = pathFumetto + "/" + numeroFumetto;
                SessioneUtente.setFumettoScelto(new_path);
                nuovo_capitolo(false);                
            }
            else
                activateFields();
        }
        else{
            // Cambia pagina
            numero_pagina--;
            scala = 1;
            cambio_pagina();
        }        
    }
    
    // Metodo per attivare i campi dopo l'esecuzione di un'operazione
    @FXML
    private void activateFields(){
        destra.setDisable(false);
        sinistra.setDisable(false);
    }
    
    // Metodo per disattivare i campi durante l'esecuzione di un'operazione
    @FXML
    private void disactivateFields(){
        destra.setDisable(true);
        sinistra.setDisable(true);
    }
    
    // Metodo che gestisce l'evento di zoom in base al pulsante del mouse premuto
    private void handleZoom(MouseEvent event){
        if(event.getButton() == MouseButton.PRIMARY)
            zoomIn();
        else if (event.getButton() == MouseButton.SECONDARY)
            zoomOut();
    }
    
    // Metodo che gestisce lo zoom in
    @FXML
    private void zoomIn(){
        if(scala < MAX_SCALA){
            scala *= ZOOM_DELTA;
            pagina.setScaleX(scala);
            pagina.setScaleY(scala);  
        }
    }
    
    // Metodo che gestisce lo zoom out
    @FXML
    private void zoomOut(){
        if(scala > MIN_SCALA){
            scala /= ZOOM_DELTA;
            pagina.setScaleX(scala);
            pagina.setScaleY(scala);
        }
    }
    
    // Metodo che gestisce il ritorno alla home
    @FXML
    private void switchToHome() throws IOException{
        // Resetta il fumetto selezionato e torna alla schermata home
        SessioneUtente.setFumettoScelto(null);
        App.setTitle("Comic Reader");
        App.setRoot("home");
    } 
    
    // Metodo che gestisce il ritorno alla schermata del fumetto
    @FXML
    private void switchToComic() throws IOException{ 
        // Resetta il capitolo selezionato e torna alla schermata del fumetto
        SessioneUtente.setFumettoScelto(recupera_fumetto(SessioneUtente.getFumettoScelto()));
        App.setRoot("fumetti/comic");
    }
    
    // Metodo di inizializzazione del controller
    @FXML
    protected void initialize(){
        disactivateFields();
        
        // Controlla se il fumetto è un manga
        attiva_manga = false;
        isManga();
        
        conta_capitoli_fumetto();
        nuovo_capitolo(true);
        
        // Gestisce lo zoom sulla pagina
        pagina.setOnMouseClicked(this::handleZoom);
    }
}
