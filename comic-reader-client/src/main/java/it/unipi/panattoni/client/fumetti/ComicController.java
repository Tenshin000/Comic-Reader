package it.unipi.panattoni.client.fumetti;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.SessioneUtente;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * Classe che gestisce il controller della schermata del Fumetto Selezionato
 * @author Francesco Panattoni
 */

public class ComicController{
    // Campi
    private final double DELAY = 3.0;
    
    @FXML
    private Text titolo;
    @FXML
    private ImageView copertina;
    @FXML
    private Text text_autori;
    @FXML
    private Text autori;
    @FXML
    private Text data_uscita;
    @FXML
    private Text sinossi;
    @FXML
    private Text valutazione;
    @FXML
    private HBox rating;
    @FXML
    private Text voto_utente;
    @FXML
    private TableView tabella_capitoli;
    @FXML
    private TableColumn<Riga, String> colCapitolo;
    @FXML
    private TableColumn<Riga, String> colDescrizione;
    @FXML
    private TableColumn<Riga, Integer> colPagine;
    
    Fumetto fumetto;
    // Variabili di stato per la valutazione
    private Integer voto;
    private Boolean possibile_votazione;
    
    // Metodi
    
    // Metodo per recuperare i dati del fumetto
    @FXML
    private void recupera_dati(){
        // Ottiene il percorso del fumetto scelto dall'Utente
        String path = SessioneUtente.getFumettoScelto();
        
        // Attiva il task per il recupero dei fumetti
        recupera_fumetto(path);
    }
    
    // Metodo per recuperare i dati del fumetto dal database
    private void recupera_fumetto(String path){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Fumetto avviato!");
                HttpURLConnection con = null;    
                String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/fumetto/recover?path=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    // Creazione di un oggetto Gson per la manipolazione di oggetti JSON
                    Gson gson = new Gson();
                    
                    // Lettura della risposta dalla connessione e manipolazione dei dati ottenuti
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        
                        // Lettura della risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        
                        // Conversione dei dati JSON in una lista di fumetti
                        fumetto = gson.fromJson(content.toString(), new TypeToken<Fumetto>(){}.getType());
                        
                        Platform.runLater(() -> {    
                            titolo.setText(fumetto.titolo);
                            setCopertina(fumetto.copertina);
                            if(!fumetto.autori.contains(","))
                                text_autori.setText("Autore: ");
                            autori.setText(fumetto.autori);
                            data_uscita.setText(fumetto.dataUscita);
                            sinossi.setText(fumetto.sinossi);        
                            valutazione.setText(String.valueOf(fumetto.valutazione)); 
                        });
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Fumetto concluso!");
                }
                
                return null;
            }   
        };
        
        task.setOnSucceeded(event -> {
            recupera_capitoli(path);
            recupera_valutazione(path);
        });
        
        new Thread(task).start();
    }
    
    // Metodo per recuperare i capitoli del fumetto dal database
    private void recupera_capitoli(String path){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Capitoli avviato!");
                HttpURLConnection con = null;    
                String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/capitolo/recover?path=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    // Creazione di un oggetto Gson per la manipolazione di oggetti JSON
                    Gson gson = new Gson();
                    
                    // Lettura della risposta dalla connessione e manipolazione dei dati ottenuti
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        
                        // Lettura della risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        
                        // Conversione dei dati JSON in una lista di fumetti
                        ArrayList<Capitolo> capitoli = gson.fromJson(content.toString(), new TypeToken<ArrayList<Capitolo>>(){}.getType());
                        int numero = 1;
                        for(Capitolo capitolo: capitoli){
                            capitolo.fumetto = fumetto;
                            capitolo.numero = numero;
                            numero++;
                        }
                        
                        // Ordina l'array capitoli utilizzando il comparatore
                        ObservableList<Riga> ol = FXCollections.observableArrayList();
                        for(Capitolo capitolo: capitoli){
                            Riga r = crea_riga(capitolo);
                            ol.add(r);
                        }
                        
                        // Aggiunge le righe alla tabella
                        Platform.runLater(() -> {    
                            tabella_capitoli.getItems().addAll(ol);

                            int numero_righe = tabella_capitoli.getItems().size();
                            double altezza_media_riga = tabella_capitoli.getFixedCellSize();
                            double altezza_tabella = numero_righe * altezza_media_riga;

                            // Imposta l'altezza calcolata come altezza preferita per la TableView
                            tabella_capitoli.setPrefHeight(altezza_tabella);
                        });
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Capitoli concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo per recuperare la valutazione del fumetto dall'utente corrente
    private void recupera_valutazione(String path){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Valutazione avviato!");
                HttpURLConnection con = null;    
                String encodedUsr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
                String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/lettura/rating?usr=" + encodedUsr + "&fumetto=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    // Creazione di un oggetto Gson per la manipolazione di oggetti JSON
                    Gson gson = new Gson();
                    
                    // Lettura della risposta dalla connessione e manipolazione dei dati ottenuti
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        
                        // Lettura della risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        
                        Lettura l = gson.fromJson(content.toString(), Lettura.class);
                        String testo = "";
                        if(l != null){
                            // Se esiste una valutazione, imposta il voto e abilita la possibilità di votare
                            Integer ultimo_capitolo_letto = l.capitolo;
                            // Se è 0 o null non la visualizza
                            if(ultimo_capitolo_letto == null || ultimo_capitolo_letto == 0){
                                possibile_votazione = false;
                                voto = 0; 
                                testo = "Voto di " + SessioneUtente.getUsername() + ": - ";
                                voto_utente.setText(testo);
                            }
                            else{
                                // Se no la visualizza
                                possibile_votazione = true;
                                voto = l.valutazione;
                                
                                if(voto == null || voto == 0){
                                    // Non dovrebbe mai accadere, ma per sicurezza
                                    voto = 0;
                                    testo = "Voto di " + SessioneUtente.getUsername() + ": - ";
                                }
                                else
                                    testo = "Voto di " + SessioneUtente.getUsername() + ": " + voto;
                                
                            }
                        }
                        else{
                            // Se non esiste una valutazione, disabilita la possibilità di votare e imposta il voto a 0
                            possibile_votazione = false;
                            voto = 0; 
                            testo = "Voto di " + SessioneUtente.getUsername() + ": - ";
                            voto_utente.setText(testo);
                        }
                        
                        final String finalTesto = testo;
                        // Aggiorna l'interfaccia grafica con la valutazione dell'utente
                        Platform.runLater(() -> {
                            voto_utente.setText(finalTesto);
                            ObservableList<Node> stelle = rating.getChildren();
                            for(int i = 0; i < voto; i++)
                                stelle.get(i).getStyleClass().add("illuminata");
                        });
                       
                        // Inizializza le stelle per la valutazione
                        crea_stelle(); 
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Valutazione concluso!");
                }

                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo per impostare la copertina del fumetto nell'interfaccia grafica
    @FXML
    private void setCopertina(String path){
        Task task = new Task<Void>(){
            @Override
            public Void call() {
                HttpURLConnection con = null;    
                final String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
                try{
                    URL url = new URL("http://localhost:8080/fumetto/cover?copertina=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                    // Controllo dello stato della risposta
                    int responseCode = con.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        // Lettura dell'immagine dalla risposta
                        InputStream inputStream = con.getInputStream();
                        Image image = new Image(inputStream);

                        if(image != null)
                            Platform.runLater(() -> {copertina.setImage(image);});
                        else
                            System.out.println("Errore nel caricamento dell'immagine di copertina"); 
                    }
                    else{
                        // Gestione degli errori di risposta
                        System.out.println("Errore nella richiesta HTTP: " + responseCode);
                        return null; // Se c'è stto un errore, restituisci null
                    }
                } 
                catch(IOException ioe){
                    ioe.printStackTrace();
                    return null;
                }
                finally{
                    if (con != null)
                        con.disconnect();
                }
                
                return null;
            }
        };

        new Thread(task).start();
    }
    
    // Metodo per creare una riga della tabella dei capitoli
    private Riga crea_riga(Capitolo c){
        String n;
        if(!c.nome.equals(""))
            n = c.numero + ": " + c.nome;
        else
            n = String.valueOf(c.numero);
        
        return new Riga(n, c.descrizione, c.pagine);
    }
    
    // Metodo per navigare verso il capitolo selezionato
    private void vai_capitolo() throws IOException{
        Riga riga = colCapitolo.getTableView().getSelectionModel().getSelectedItem();
        if(riga != null){
            String stringa = riga.getNome();
            String numero_stringa;
            if(stringa.contains(":"))
                numero_stringa = stringa.substring(0, stringa.indexOf(":"));
            else
                numero_stringa = stringa;
                       
            // Converte la parte estratta in un numero intero
            int numero = Integer.parseInt(numero_stringa);
            
            String path = SessioneUtente.getFumettoScelto() + "/" + numero;
            SessioneUtente.setFumettoScelto(path);
            App.setRoot("fumetti/read");
        }
        else
            System.out.println("Errore");
    }
    
    // Metodo per creare le stelle per la valutazione
    @FXML
    private void crea_stelle(){
        ObservableList<Node> stelle = rating.getChildren();
        stelle.forEach(stella -> {
            // Evento quando il mouse entra nella stella
            stella.setOnMouseEntered(event -> {
                int indice = stelle.indexOf(stella);
                stelle.forEach(node -> node.getStyleClass().removeAll("illuminata"));
                
                for(int i = 0; i <= indice; i++)
                    stelle.get(i).getStyleClass().add("illuminata");
            });
            
            // Evento quando il mouse clicca sulla stella
            stella.setOnMouseClicked(event -> {
                int indice = stelle.indexOf(stella) + 1;
                if(possibile_votazione)
                    setVoto(indice);
                else
                    disappearNotifyAfterDelay("Devi aver letto almeno un capitolo per poter dare una valutazione");
            });
            
            // Evento quando il mouse esce dalla stella
            stella.setOnMouseExited(event -> {
                stelle.forEach(node -> node.getStyleClass().removeAll("illuminata"));
                for(int i = 0; i < voto; i++)
                    stelle.get(i).getStyleClass().add("illuminata");
            });
            
            // Evento quando il mouse si muove sopra la stella
            stella.setOnMouseMoved(event ->{
                int indice = stelle.indexOf(stella);
                stelle.forEach(node -> node.getStyleClass().removeAll("illuminata"));
                
                for(int i = 0; i <= indice; i++)
                    stelle.get(i).getStyleClass().add("illuminata");
            });
        });
    }
    
    // Metodo per far apparire una notifica e farla scomparire dopo DELAY secondi
    @FXML
    private void disappearNotifyAfterDelay(String message){
        String messaggio_precedente = voto_utente.getText();
        
        // Fa apparire il messaggio
        voto_utente.setText(message);
        
        // Crea una transizione di pausa di 3 secondi
        PauseTransition delay = new PauseTransition(Duration.seconds(DELAY));
        
        // Imposta l'azione da eseguire dopo il ritardo
        delay.setOnFinished(event -> {
            // Nascondi il testo
            voto_utente.setText(messaggio_precedente);
        });
        
        // Avvia la transizione di pausa
        delay.play();
    }
    
    // Metodo per impostare il voto del fumetto
    private void setVoto(int nuovo_voto){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task Cambio Voto avviato!");
                HttpURLConnection con = null;
                
                final String usr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
                final String fumetto = URLEncoder.encode(SessioneUtente.getFumettoScelto(), StandardCharsets.UTF_8);
                
                try{
                    // Crea una connessione HTTP per aggiornare il voto del fumetto
                    URL url = new URL("http://localhost:8080/lettura/update?usr=" + usr + "&fumetto=" + fumetto + "&val=" + nuovo_voto); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
                    
                    // Legge la risposta e aggiorna l'interfaccia grafica con il nuovo voto
                    double response = Double.parseDouble(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());

                    if(response > 0.0 && response <= 10.0){
                        voto = nuovo_voto;
                                                
                        Platform.runLater(() -> {      
                            // Aggiorna il punteggio del fumetto nell'interfaccia utente
                            valutazione.setText(Double.toString(response));
                            // Aggiorna il testo del voto utente
                            voto_utente.setText("Voto di " + SessioneUtente.getUsername() + ": " + voto);
                            // Ottiene la lista delle stelle di valutazione
                            ObservableList<Node> stelle = rating.getChildren();
                            // Rimuove la classe CSS "illuminata" da tutte le stelle
                            stelle.forEach(node -> node.getStyleClass().removeAll("illuminata"));
                            // Aggiunge la classe "illuminata" alle stelle fino al voto corrente
                            for(int i = 0; i < voto; i++)
                                stelle.get(i).getStyleClass().add("illuminata");
                        });
                    }
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Cambio Voto concluso!");
                }
 
                return null;
            }   
        };

        new Thread(task).start();
    }
    
    // Metodo per personalizzare le celle nella colonna colCapitolo
    private void caratteristiche_colCapitolo(){
        colCapitolo.setCellFactory(column -> {
        TableCell<Riga, String> cell = new TableCell<Riga, String>(){
                // Il metodo updateItem() che aggiorna il contenuto della cella
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item == null || empty) 
                        setText(null);
                    else{
                        // Imposta il testo della cella e la sua grafica con il testo
                        setText(item);
                        setFont(Font.font("Arial"));
                        setTextFill(Color.BLUE); // Imposta il font in grassetto
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if(!cell.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1){
                    try{
                      vai_capitolo();  
                    }
                    catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }    
            });
            
            // Aggiungi il listener per il passaggio del mouse sopra la cella
            cell.setOnMouseEntered(event -> {
                if(!cell.isEmpty()){
                    // Sottolinea il testo quando il mouse ci passa sopra
                    cell.setStyle("-fx-underline: true;");
                }
            });

            // Aggiungi il listener per il movimento del mouse fuori dalla cella
            cell.setOnMouseExited(event -> {
                // Rimuovi la sottolineatura quando il mouse va via
                cell.setStyle("-fx-underline: false;");
            });

            return cell;
        }); 
    }
    
    // Metodo per personalizzare le celle nella colonna colDescrizione
    private void caratteristiche_colDescrizione(){
        colDescrizione.setCellFactory(new Callback<TableColumn<Riga, String>, TableCell<Riga, String>>(){
            @Override
            public TableCell<Riga, String> call(TableColumn<Riga, String> param){
                return new TableCell<Riga, String>() {
                    private final Text text = new Text();

                    {   
                        // Imposta la larghezza del testo uguale alla larghezza della colonna
                        text.wrappingWidthProperty().bind(colDescrizione.widthProperty()); // Imposta la larghezza del testo
                        text.setFill(Color.BLACK); // Imposta il colore del testo
                    }
                    
                    // Il metodo updateItem() che aggiorna il contenuto della cella
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item == null || empty){
                            setText(null);
                            setGraphic(null);
                        } 
                        else{
                            // Imposta il testo della cella e la sua grafica con il testo
                            text.setText(item);
                            setGraphic(text);
                        }
                    }
                };
            }
        });   
    }
    
    // Metodo per tornare alla pagina principale
    @FXML
    private void switchToHome() throws IOException{
        SessioneUtente.setFumettoScelto(null);
        App.setTitle("Comic Reader");
        App.setRoot("home");
    }  
    
    // Metodo per inizializzare la classe ComicController
    @FXML
    protected void initialize(){
        // Assicurati che le colonne siano associate correttamente alle proprietà dell'oggetto Capitolo
        colCapitolo.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescrizione.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        colPagine.setCellValueFactory(new PropertyValueFactory<>("pagine"));
        
        caratteristiche_colCapitolo();
        caratteristiche_colDescrizione();
        
        recupera_dati();
    }
    
    // Classe che serve per tenere conto di una riga nella tabella
    public class Riga{
        // Campi
        final private String nome;
        final private String descrizione;
        final private Integer pagine;
        
        // Metodi
        public Riga(String n, String d, int p){
            nome = n;
            descrizione = d;
            pagine = p;
        }

        public String getNome() {
            return nome;
        }

        public String getDescrizione() {
            return descrizione;
        }

        public int getPagine() {
            return pagine;
        }
    }
}
