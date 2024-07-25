package it.unipi.panattoni.client.account;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.SessioneUtente;
import it.unipi.panattoni.client.fumetti.Fumetto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller per la schermata dei fumetti completati, non completati e che si vorrebbero leggere
 * @author Francesco Panattoni
 */

public class LetturaController {
    // Campi
    
    // Tabelle per i fumetti finiti, in corso di lettura e ancora da leggere
    @FXML
    private TableView tabFiniti;
    @FXML
    private TableView tabInCorso;
    @FXML
    private TableView tabDaLeggere;
    
    @FXML
    private TextField ricerca; // Barra di ricerca
    @FXML
    private ChoiceBox scelte; // Choice Box per le opzioni disponibili nel database 
    @FXML
    private Button aggiungi; // Pulsante che salva un'opzione nella tabella per i fumetti da leggere
    
    // Liste per i fumetti finiti, in corso di lettura e ancora da leggere
    private ObservableList<Riga> finiti;
    private ObservableList<Riga> inCorso;
    private ObservableList<Riga> daLeggere;
    
    // Lista dei path delle opzioni del ChoiceBox
    private String[] listaPath;
    
    // Metodi
    
    // Questo metodo è responsabile di inserire i dati nelle tabelle dei fumetti finiti, in corso e da leggere.
    private void inserisci_dati(){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Riempimento Tabelle avviato!");
                
                HttpURLConnection con = null;    
                final String encodedUsr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
                try{
                    URL url = new URL("http://localhost:8080/lettura/search?usr=" + encodedUsr);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    Gson gson = new Gson();
                    // Lettura della risposta dalla connessione e manipolazione dei dati ottenuti
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        
                        // Lettura della risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);
                        
                        in.close();
                        // Converte la risposta in un'ArrayList di oggetti Riga utilizzando Gson
                        Type listType = new TypeToken<ArrayList<Riga>>(){}.getType();
                        ArrayList<Riga> righe = gson.fromJson(content.toString(), listType);
                        // Aggiunta controllo per verificare se la risposta contiene dati
                        if(righe != null){
                            for(Riga riga: righe){
                                if(riga.getCapitolo() >= riga.getCapitoli())
                                    finiti.add(riga);
                                else if(riga.getCapitolo() > 0 && riga.getCapitolo() < riga.getCapitoli())
                                    inCorso.add(riga);
                                else if(riga.getCapitolo() == 0)
                                    daLeggere.add(riga);
                            }
                        }
                        else
                            System.out.println("La risposta non contiene dati.");
                    }                  
                } 
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    // Aggiorna le tabelle nell'interfaccia utente tramite Platform.runLater
                    Platform.runLater(() -> {                             
                        tabFiniti.setItems(finiti);
                        tabInCorso.setItems(inCorso);
                        tabDaLeggere.setItems(daLeggere);
                    });
                    System.out.println("Task Riempimento Tabelle concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Questo metodo è chiamato quando viene eseguita una ricerca nella barra
    @FXML
    public void aggiorna_barra(String testo){ 
        // Nel caso la barra non contenga nulla disabilita il pulsante aggiungi
        if(testo == null || testo.equals(""))
            aggiungi.setDisable(true);
        else
            aggiungi.setDisable(false);
        
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                HttpURLConnection con = null;    
                String encodedTesto = URLEncoder.encode(testo, StandardCharsets.UTF_8);
                
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/fumetto/search?testo=" + encodedTesto);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    
                    // Creazione di un oggetto Gson per la manipolazione di oggetti JSON
                    Gson gson = new Gson();
                    
                    // Lettura della risposta dalla connessione e manipolazione dei dati ottenuti
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))){
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        
                        // Lettura della risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        // Recupera l'ArrayList di fumetti
                        Type listType = new TypeToken<ArrayList<Fumetto>>(){}.getType();
                        // Converti la risposta JSON in un ArrayList di fumetti
                        ArrayList<Fumetto> listaFumetti = gson.fromJson(content.toString(), listType);
                        String[] titoli = converti_fumetti_titoli(listaFumetti);
                        listaPath = converti_fumetti_path(listaFumetti);
                        
                        // Aggiorna il ChoiceBox con le opzioni di ricerca in base al testo digitato dall'utente
                        Platform.runLater(() -> {
                            ObservableList lista = FXCollections.observableArrayList(titoli);
                            scelte.setItems(lista);
                            scelte.setValue(ricerca.getText());
                        });
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                }
                return null;
            }
        };

        new Thread(task).start();
    }
    
    // Questo metodo converte una lista di fumetti in un array di stringhe contenente i titoli dei fumetti
    private String[] converti_fumetti_titoli(ArrayList<Fumetto> fumetti){
        ArrayList<String> titoli = new ArrayList<>();
        
        // Estrai i titoli dai fumetti e aggiungili all'ArrayList dei titoli
        for(Fumetto fumetto : fumetti)
            titoli.add(fumetto.titolo);

        // Converti l'ArrayList di titoli in un array di stringhe
        String[] titoliArray = titoli.toArray(new String[0]);
        return titoliArray;
    }
    
    // Questo metodo converte una lista di fumetti in un array di stringhe contenente i path dei fumetti
    private String[] converti_fumetti_path(ArrayList<Fumetto> fumetti){
        ArrayList<String> paths = new ArrayList<>();
        
        // Estrai i titoli dai fumetti e aggiungili all'ArrayList dei titoli
        for(Fumetto fumetto : fumetti)
            paths.add(fumetto.path);

        // Converti l'ArrayList di titoli in un array di stringhe
        String[] pathArray = paths.toArray(new String[0]);
        return pathArray;
    }
    
    // Questo metodo gestisce l'aggiunta di un nuovo fumetto da leggere
    public void nuovo_da_leggere(){
        String titolo = (String) scelte.getSelectionModel().getSelectedItem();
        String testo = ricerca.getText();
        
        // Controlla se il titolo è già stato selezionato o se il titolo è già presente nella tabella "daLeggere"
        if(!testo.equals(titolo) || cerca_da_leggere(titolo)){
            ricerca.setEditable(true);
            return;
        }  
        
        // Controlla l'indice del fumetto selezionato in caso di omonimie
        int indice = scelte.getSelectionModel().getSelectedIndex();
        if(indice < 0){
            ricerca.setEditable(true);
            return;
        }
        
        // Disabilita il pulsante di aggiunta
        aggiungi.setDisable(true);
        // Ottiene il percorso del fumetto selezionato
        String path = listaPath[indice];
        
        // Codifica il nome utente per la trasmissione HTTP e chiama la funzione task_da_leggere
        String encodedUsr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
        
        task_da_leggere(encodedUsr, path, titolo);
    }
    
    // Si occupa di aggiungere un Fumetto nella tabella Lettura con capitolo 0.
    // Questo per indicare che l'utente desidera leggere quel fumetto e che ancora non l'ha letto. 
    private void task_da_leggere(String usr, String path, String title){
        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
        
        Task task = new Task<Void>(){
            @Override
            public Void call(){                
                HttpURLConnection con = null;
                
                try{
                    // Invia una richiesta al server per aggiungere il fumetto alla lista dei fumetti da leggere
                    System.out.println("Task Aggiungi Fumetto avviato!");
                    URL url = new URL("http://localhost:8080/lettura/add?usr=" + usr + "&path=" + encodedPath + "&capitolo=" + 0); 
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                    StringBuilder content = new StringBuilder();
                    // Crea un BufferedReader per leggere il contenuto dall'input stream della connessione
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        // Legge ogni riga dall'input stream fino a quando non raggiunge la fine
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine); // Aggiunge ogni riga letta al StringBuilder
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();
                    
                    // Gestiamo in base alla risposta del server
                    switch(response){
                        case "4004": // Il Fumetto che è stato inserito non si trova nel Database
                            System.out.println("Il Fumetto non è stato trovato nel Database");
                            break;
                        case "4007": // Utente non trovato
                            System.out.println("Username non trovato. Errore Grave!");
                            break;
                        case "4009": // Il fumetto è già stato letto dall'Utente
                            System.out.println("Lettura già esistente");
                            break;
                        case "4015": // Non dovrebbe mai succedere questo caso
                            System.out.println("Capitolo minore di 0");
                            break;
                        case "2000": // Ok
                            System.out.println("Operazione andata a buon fine!");
                            // Aggiorna la tabella dei fumetti da leggere
                            Platform.runLater(() -> {    
                                daLeggere.add(new Riga(title, 0, 1, path));
                                Comparator<Riga> comparator = Comparator.comparing(Riga::getTitolo);
                                daLeggere.sort(comparator);
                            });
                            break;
                        default:     
                            // Risposta sconosciuta
                            System.out.println("Qualcosa è andato storto!");
                    }   
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    ricerca.setEditable(true);
                    aggiungi.setDisable(false);
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Aggiungi Fumetto concluso!");
                }
                return null;
            }
        };

        new Thread(task).start();
    }
    
    // Questo metodo cerca se un fumetto è già presente nella lista dei fumetti da leggere
    private boolean cerca_da_leggere(String fumetto){
        // Controllo se daLeggere è null o vuoto
        if(fumetto == null || fumetto.equals("") || daLeggere == null || daLeggere.isEmpty())
            return false;

        // Itero attraverso gli elementi di daLeggere
        for(Riga riga : daLeggere){
            // Controllo se il titolo dell'elemento corrente è uguale alla stringa fumetto
            if(riga.getTitolo().equals(fumetto))
                return true; // Restituisco true se trovo una corrispondenza
        }

        // Se non trovo corrispondenze, restituisco false
        return false;
    }
    
    // Questo metodo rimuove un fumetto dalla lista dei fumetti finiti (e di conseguenza dalla tabella)
    @FXML
    private void rimuovi_finiti(){
        // Verifica se l'ObservableList finiti non è vuota
        if(!finiti.isEmpty()){
            // Ottieni l'elemento selezionato nella tabella
            Riga r = (Riga) tabFiniti.getSelectionModel().getSelectedItem();
            // Rimuovi l'elemento dalla lista dei fumetti finiti
            finiti.remove(r);
            // Esegui altre operazioni necessarie
            boolean b = aggiungi.isDisable();
            rimuovi(r, b);
        }
    }
    
    // Questo metodo rimuove un fumetto dalla lista dei fumetti in corso (e di conseguenza dalla tabella)
    @FXML
    private void rimuovi_inCorso(){
        // Verifica se l'ObservableList inCorso non è vuota
        if(!inCorso.isEmpty()){
            // Ottieni l'elemento selezionato nella tabella
            Riga r = (Riga) tabInCorso.getSelectionModel().getSelectedItem();
            // Rimuovi l'elemento dalla lista dei fumetti che stai ancora leggendo
            inCorso.remove(r);
            // Esegui altre operazioni necessarie
            boolean b = aggiungi.isDisable();
            rimuovi(r, b);
        }
    }
    
    // Questo metodo rimuove un fumetto dalla lista dei fumetti da leggere (e di conseguenza dalla tabella)
    @FXML
    private void rimuovi_daLeggere(){
        // Verifica se l'ObservableList daLeggere non è vuota
        if(!daLeggere.isEmpty()){
            // Ottieni l'elemento selezionato nella tabella
            Riga r = (Riga) tabDaLeggere.getSelectionModel().getSelectedItem();
            // Rimuovi l'elemento dalla lista dei fumetti da leggere
            daLeggere.remove(r);
            // Esegui altre operazioni necessarie
            boolean b = aggiungi.isDisable();
            rimuovi(r, b);
        }
    }
    
    // Rimuove una lettura del fumetto dal database
    @FXML
    private void rimuovi(Riga r, boolean sblocca_pulsante){
        String usr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
        String path = URLEncoder.encode(r.getPath(), StandardCharsets.UTF_8);
        
        // Disabilita la barra di ricerca e il pulsante per salvare
        ricerca.setEditable(false);
        aggiungi.setDisable(true);
        
        Task task = new Task<Void>(){
            @Override
            public Void call(){                
                HttpURLConnection con = null;    
                
                try{
                    // Invia una richiesta al server per rimuovere una lettura
                    System.out.println("Task Rimuovi Lettura avviato!");
                    URL url = new URL("http://localhost:8080/lettura/remove?usr=" + usr + "&path=" + path);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                    StringBuilder content = new StringBuilder();
                    // Crea un BufferedReader per leggere il contenuto dall'input stream della connessione
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        // Legge ogni riga dall'input stream fino a quando non raggiunge la fine
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine); // Aggiunge ogni riga letta al StringBuilder
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    
                    Gson gson = new Gson();
                    JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                    String response = json.getAsJsonObject().get("id").getAsString();                  
                    
                    // Arrivata la risposta del server, allora agiamo di conseguenza
                    switch(response){
                        case "4004": // Il Fumetto che è stato inserito non si trova nel Database
                            System.out.println("Il Fumetto non è stato trovato nel Database");
                            break;
                        case "4007":
                            System.out.println("Username non trovato. Errore Grave!");
                            break;
                        case "4009":
                            System.out.println("La lettura non è presente");
                            break;
                        case "2000":
                            // Ok
                            System.out.println("Operazione andata a buon fine!");
                            break;
                        default:     
                            // Risposta sconosciuta
                            System.out.println("Qualcosa è andato storto!");
                    }   
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    // Riattiva la barra di ricerca e il pulsante per salvare
                    ricerca.setEditable(true);
                    aggiungi.setDisable(sblocca_pulsante);
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Rimuovi Lettura concluso!");
                }
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    // Genera una tabella con le colonne specificate
    private void genera_tabella(TableView<Riga> tabella, boolean capitolo_visibile){
        // Crea le colonne
        TableColumn<Riga, String> titolo = genera_colonna_titolo();
        TableColumn<Riga, Integer> capitolo = genera_colonna_capitolo();
        TableColumn<Riga, Integer> num_capitoli = genera_colonna_num_capitoli();
        TableColumn<Riga, String> path = genera_colonna_path();

        // Imposta le larghezze delle colonne e se la colonna capitolo è visibile
        if(capitolo_visibile){
            titolo.prefWidthProperty().bind(tabella.widthProperty().multiply(5.0 / 6.0));
            capitolo.prefWidthProperty().bind(tabella.widthProperty().multiply(1.0 / 6.0));
        }
        else{
            titolo.prefWidthProperty().bind(tabella.widthProperty().multiply(1.0));
            capitolo.setVisible(capitolo_visibile);
        }
        
        // La colonna capitoli e path sono sempre rese invisibili
        num_capitoli.setVisible(false);
        path.setVisible(false); // Serve per recuperare il path per l'eliminazione dal database

        // Aggiungi le colonne alla tabella
        tabella.getColumns().addAll(titolo, capitolo, num_capitoli, path);
    }
    
    // Genera una colonna per il titolo
    private TableColumn<Riga, String> genera_colonna_titolo(){
        TableColumn<Riga, String> titolo = new TableColumn("Titolo");
        titolo.setCellValueFactory(new PropertyValueFactory<>("titolo"));
        
        // Imposta la cella della colonna per allineare il testo al centro
        titolo.setCellFactory(tc -> {
            TableCell<Riga, String> cell = new TableCell<>(){
                @Override
                protected void updateItem(String item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty || item == null){
                        setText(null);
                    } 
                    else{
                        setText(item);
                        setAlignment(Pos.CENTER); // Imposta l'allineamento al centro
                    }
                }
            };
            return cell;
        });
        
        return titolo;
    }
    
    // Genera una colonna per il capitolo
    private TableColumn<Riga, Integer> genera_colonna_capitolo(){
        TableColumn capitolo = new TableColumn("Capitolo");
        capitolo.setCellValueFactory(new PropertyValueFactory<>("capitolo"));
        
        // Imposta la cella della colonna per allineare il testo al centro
        capitolo.setCellFactory(tc -> {
            TableCell<Riga, Integer> cell = new TableCell<>(){
                @Override
                protected void updateItem(Integer item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty || item == null){
                        setText(null);
                    } 
                    else{
                        setText(item.toString());
                        setAlignment(Pos.CENTER); // Imposta l'allineamento al centro
                    }
                }
            };
            return cell;
        });
        
        return capitolo;
    }
    
    // Genera una colonna per i capitoli
    private TableColumn<Riga, Integer> genera_colonna_num_capitoli(){
        TableColumn capitoli = new TableColumn("Capitoli");
        capitoli.setCellValueFactory(new PropertyValueFactory<>("capitoli"));
        
        // Imposta la cella della colonna per allineare il testo al centro
        capitoli.setCellFactory(tc -> {
            TableCell<Riga, Integer> cell = new TableCell<>(){
                @Override
                protected void updateItem(Integer item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty || item == null){
                        setText(null);
                    } 
                    else{
                        setText(item.toString());
                        setAlignment(Pos.CENTER); // Imposta l'allineamento al centro
                    }
                }
            };
            return cell;
        });
        
        return capitoli;
    }
    
    // Genera una colonna per il path
    private TableColumn<Riga, String> genera_colonna_path(){
        TableColumn path = new TableColumn("Path");
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        
        // Imposta la cella della colonna per allineare il testo al centro
        path.setCellFactory(tc -> {
            TableCell<Riga, String> cell = new TableCell<>(){
                @Override
                protected void updateItem(String item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty || item == null){
                        setText(null);
                    } 
                    else{
                        setText(item);
                        setAlignment(Pos.CENTER); // Imposta l'allineamento al centro
                    }
                }
            };
            return cell;
        });
        
        return path;
    }
    
    // Crea le tabelle per i fumetti
    @FXML
    private void crea_tabelle(){
        genera_tabella(tabFiniti, false);
        genera_tabella(tabInCorso, true);
        genera_tabella(tabDaLeggere, false);
    }
    
    // Metodo per passare alla schermata principale
    @FXML
    private void switchToHome() throws IOException{
        App.setRoot("home");
    }   
    
    // Metodo chiamato all'inizializzazione del controller
    @FXML
    protected void initialize(){ 
        // Crea le tabelle e inserisce i dati
        crea_tabelle();
        
        finiti = FXCollections.observableArrayList();
        inCorso = FXCollections.observableArrayList();
        daLeggere = FXCollections.observableArrayList();
        
        inserisci_dati();
        
        aggiorna_barra("");
        
        // Aggiorna la barra di ricerca in base al testo inserito
        ricerca.textProperty().addListener((observable, oldValue, newValue) -> {
            aggiorna_barra(newValue); // Aggiorna le opzioni in base al testo digitato
            if(!newValue.isEmpty()){
                scelte.setVisible(true); // Mostra il ChoiceBox
                scelte.show();
            } 
            else{
                scelte.setVisible(false); // Nascondi il ChoiceBox se la search bar è vuota
                scelte.hide();
            }  
        });
        
        // Aggiungi il listener per la selezione del ChoiceBox "scelte"
        scelte.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Controlla se il nuovo valore non è nullo e imposta il testo nel TextField
            if(newValue != null){
                ricerca.setText(newValue.toString());
                // Imposta la posizione del cursore alla fine del testo nel TextField
                ricerca.positionCaret(ricerca.getText().length());
            }
        });
    }
    
    // Classe interna per rappresentare una riga di una tabella dei fumetti
    public class Riga{
        // Campi
        final public String titolo;
        final public Integer capitolo;
        final public Integer capitoli;
        final public String path;
        
        // Metodi
        public Riga(String t, int c, int cs, String p){
            if(c < 0)
                c = 0;
            
            titolo = t;
            capitolo = c;
            capitoli = cs;
            path = p;
        }
        
        public String getTitolo(){
            return titolo;
        }

        public int getCapitolo(){
            return capitolo;
        }
        
        public int getCapitoli(){
            return capitoli;
        }
        
        public String getPath(){
            return path;
        }
    }
}
