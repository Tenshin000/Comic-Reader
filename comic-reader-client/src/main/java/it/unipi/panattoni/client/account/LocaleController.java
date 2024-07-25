package it.unipi.panattoni.client.account;

import it.unipi.panattoni.client.App;
import it.unipi.panattoni.client.fumetti.Capitolo;
import it.unipi.panattoni.client.fumetti.Fumetto;
import it.unipi.panattoni.client.SessioneUtente;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller per la schermata di gestione dei fumetti locali
 * @author Francesco Panattoni
 */

public class LocaleController{
    // Campi
    private final double DELAY = 3.0; 
    
    @FXML
    private TextField ricerca; // Barra di ricerca
    @FXML
    private ChoiceBox scelte; // ChoiceBox per le opzioni di ricerca
    @FXML
    private ImageView lente;
    @FXML
    private Text notifica;
    @FXML
    private VBox box_tabelle;
    
    // Lista dei path delle opzioni del ChoiceBox
    private String[] listaPath;
    
    private Fumetto fumetto;
    
    // Metodi
    
    // Metodo chiamato per aggiornare la barra di ricerca
    @FXML
    private void aggiorna_barra(String testo){  
        // Creazione di un nuovo task per eseguire l'operazione di ricerca in background
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                HttpURLConnection con = null;    
                String encodedTesto = URLEncoder.encode(testo, StandardCharsets.UTF_8);
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/fumetto/local?testo=" + encodedTesto);
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
                        ArrayList<Fumetto> fumetti = gson.fromJson(content.toString(), new TypeToken<ArrayList<Fumetto>>(){}.getType());
                        
                        String[] titoli = converti_fumetti_titoli(fumetti);
                        listaPath = converti_fumetti_path(fumetti);
                        
                        // Aggiornamento dell'interfaccia grafica nel thread JavaFX principale
                        Platform.runLater(() -> {
                            ObservableList<String> lista = FXCollections.observableArrayList(titoli);
                            scelte.setItems(lista);
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
    
    // Metodo che converte una lista di fumetti in un array di stringhe contenente i titoli dei fumetti
    private String[] converti_fumetti_titoli(ArrayList<Fumetto> fumetti){
        ArrayList<String> titoli = new ArrayList<>();
        
        // Estrai i titoli dai fumetti e aggiungili all'ArrayList dei titoli
        for(Fumetto fumetto: fumetti)
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
    
    // Metodo da mettere sull'immagine della lente d'ingrandimento
    private void click_lente(String testo){
        costruisci_tabelle(testo);
    }
    
    // Metodo che costruisce le tabelle del fumetto e dei capitoli per modificare i fumetti locali
    public void costruisci_tabelle(String titolo){        
        // Verifica se la lista delle opzioni contiene il titolo inserito nella ricerca
        if(listaPath != null && scelte.getItems().contains(titolo)){
            // Il titolo è presente nelle opzioni della ChoiceBox
            // Ottieni l'indice del titolo nella lista delle opzioni
            int index = scelte.getItems().indexOf(titolo);
            // Ottieni il path corrispondente al titolo
            String path = listaPath[index];
            
            // Rimuovi precedente tabella se presente
            box_tabelle.getChildren().clear();
            nuove_tabelle(path);
        } 
        else
            System.out.println("Titolo non presente nella lista");
    }
    
    // Metodo che comincia ad inserire le tabelle inserendo la tabella dei fumetti
    private void nuove_tabelle(String path){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Tabella Fumetto avviato!");
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
                        
                        // Crea la tabella del fumetto con i dati del fumetto
                        Platform.runLater(() -> {aggiorna_interfaccia_fumetto();});
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Tabella Fumetto concluso!");
                }
                
                return null;
            }   
        };
        
        // Se riesce, costruisce la tabella dei capitoli
        task.setOnSucceeded(event -> {
            task_tabella_capitolo(); 
        });
        
        new Thread(task).start();
    }
    
    // Metodo che inserisce la tabella dei capitoli
    private void task_tabella_capitolo(){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Capitoli avviato!");
                HttpURLConnection con = null;    
                String encodedPath = URLEncoder.encode(fumetto.path, StandardCharsets.UTF_8);
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
                                                
                        // Crea la tabella dei capitoli con i dati di tutti i capitoli del fumetto
                        Platform.runLater(() -> { aggiorna_interfaccia_capitolo(capitoli); });
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
    
    // Metodo per aggiornare l'interfaccia grafica del fumetto
    private void aggiorna_interfaccia_fumetto(){
        // Creazione dei componenti UI
        Text text = new Text("Tabella Fumetto");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox.setMargin(text, new Insets(2, 0, 0, 0));
        
        // Creazione della tabella dei fumetti
        TableView<Fumetto> tabella = crea_tabella_fumetto();
        
        // Creazione del layout per i pulsanti Salva e Rimuovi
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        
        // Pulsante per salvare le modifiche al fumetto
        Button salvaButton = new Button("Salva");
        salvaButton.setOnAction(event -> salva_fumetto());
        salvaButton.setFont(Font.font("Arial", 15));
        salvaButton.setPadding(new Insets(5));
        salvaButton.setStyle("-fx-background-color: #66CCFF;");
        HBox.setMargin(salvaButton, new Insets(5, 5, 0, 5));
        
        // Pulsante per rimuovere il fumetto
        Button rimuoviButton = new Button("Rimuovi");        
        rimuoviButton.setOnAction(event -> rimuovi());
        rimuoviButton.setFont(Font.font("Arial", 15));
        rimuoviButton.setPadding(new Insets(5));
        rimuoviButton.setStyle("-fx-background-color: #66CCFF;");
        HBox.setMargin(rimuoviButton, new Insets(5, 5, 0, 5));
        
        // Aggiunta dei pulsanti al layout
        hbox.getChildren().addAll(salvaButton, rimuoviButton);
        HBox.setMargin(hbox, new Insets(5));

        // Aggiunta dei componenti al contenitore principale
        box_tabelle.getChildren().addAll(text, tabella, hbox);
    }
    
    // Metodo per aggiornare l'interfaccia grafica dei capitoli
    private void aggiorna_interfaccia_capitolo(ArrayList<Capitolo> capitoli){
        // Creazione dei componenti UI
        Text text = new Text("Tabella Capitoli");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox.setMargin(text, new Insets(10, 0, 0, 0));
        
        // Creazione della tabella dei capitoli
        TableView<Capitolo> tabella = crea_tabella_capitoli(capitoli);
        
        // Pulsante per salvare le modifiche ai capitoli
        Button salvaButton = new Button("Salva");
        salvaButton.setOnAction(event -> salva_capitoli(tabella));
        salvaButton.setFont(Font.font("Arial", 15));
        salvaButton.setPadding(new Insets(5));
        salvaButton.setStyle("-fx-background-color: #66CCFF;");
        VBox.setMargin(salvaButton, new Insets(5, 0, 5, 0));

        // Aggiunta dei componenti al contenitore principale
        box_tabelle.getChildren().addAll(text, tabella, salvaButton);
    }
    
    // Metodo che crea la tabella con i dati del fumetto
    private TableView crea_tabella_fumetto(){
        TableView<Fumetto> tableView = new TableView<>();

        // Aggiunta delle colonne alla TableView
        TableColumn<Fumetto, String> titoloCol = crea_colonna_fumetto(tableView, "Titolo", f -> f.titolo, (f, newValue) -> { f.titolo = newValue; }, 0.1);
        TableColumn<Fumetto, String> autoriCol = crea_colonna_fumetto(tableView, "Autori", f -> f.autori, (f, newValue) -> { f.autori = newValue; }, 0.1);
        TableColumn<Fumetto, String> copertinaCol = crea_colonna_fumetto(tableView, "Copertina", f -> f.copertina, (f, newValue) -> { f.copertina = newValue; }, 0.1);
        TableColumn<Fumetto, String> dataUscitaCol = crea_colonna_fumetto(tableView, "Data di Uscita", f -> f.dataUscita, (f, newValue) -> { f.dataUscita = newValue; }, 0.1);
        TableColumn<Fumetto, String> sinossiCol = crea_colonna_fumetto(tableView, "Sinossi", f -> f.sinossi, (f, newValue) -> { f.sinossi = newValue; }, 0.5);
        
        TableColumn<Fumetto, Boolean> mangaCol = new TableColumn<>("Manga");
        mangaCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
        mangaCol.setCellValueFactory(cellData -> {
            if(fumetto != null)
                return new SimpleBooleanProperty(fumetto.manga);
            else
                return null;

        });
        mangaCol.setCellFactory(column -> new TableCell<Fumetto, Boolean>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            {
                choiceBox.getItems().addAll("true", "false");
                choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    fumetto.manga = Boolean.parseBoolean(newValue);
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setGraphic(null);
                } 
                else{
                    // Imposta il valore della ChoiceBox sul valore booleano della cella
                    choiceBox.setValue(item.toString());
                    setGraphic(choiceBox);
                }
            }
        });
        mangaCol.setEditable(true);
        
        tableView.setEditable(true);
        tableView.getColumns().addAll(titoloCol, autoriCol, copertinaCol, dataUscitaCol, sinossiCol, mangaCol); 
        tableView.getStyleClass().add("tabella");

        // Popolamento della TableView con il fumetto recuperato
        ObservableList<Fumetto> fumettoList = FXCollections.observableArrayList();
        fumettoList.add(fumetto);
        tableView.setItems(fumettoList);

        // Assicurati che la tabella abbia una sola riga
        tableView.setFixedCellSize(25); // Imposta l'altezza fissa delle celle
        tableView.prefHeightProperty().bind(Bindings.size(fumettoList).multiply(tableView.getFixedCellSize()).add(30)); // Calcola l'altezza prefissata della tabella
        tableView.setMaxHeight(tableView.getFixedCellSize() + 30); // Imposta l'altezza massima della tabella
        
        return tableView;
    }
    
    // Metodo che crea la tabella dei capitoli
    private TableView<Capitolo> crea_tabella_capitoli(ArrayList<Capitolo> capitoli){
        // Creazione delle colonne utilizzando la funzione crea_colonna_capitolo
        TableView<Capitolo> tableView = new TableView<>();
        
        TableColumn<Capitolo, String> numeroCol = crea_colonna_capitolo(tableView, "Numero", c -> c.numero, (c, newValue) -> { c.numero = newValue; }, 0.1, false);       
        TableColumn<Capitolo, String> nomeCol = crea_colonna_capitolo(tableView, "Nome", c -> c.nome, (c, newValue) -> { c.nome = newValue; }, 0.1, true);
        TableColumn<Capitolo, String> descrizioneCol = crea_colonna_capitolo(tableView, "Descrizione", c -> c.descrizione, (c, newValue) -> { c.descrizione = newValue;}, 0.7, true);
        
        // Creazione della colonna Formato con una ChoiceBox per selezionare il formato
        TableColumn<Capitolo, String> formatoCol = new TableColumn<>("Formato");
        formatoCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
        formatoCol.setCellFactory(column -> new TableCell<Capitolo, String>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();

            {
                choiceBox.getItems().addAll("jpg", "png", "jpeg");
                choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if(!isEmpty()){
                        Capitolo capitolo = getTableView().getItems().get(getIndex());
                        capitolo.formato = newValue;
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setGraphic(null);
                } 
                else{
                    // Imposta il valore della ChoiceBox
                    Capitolo capitolo = getTableView().getItems().get(getIndex());
                    choiceBox.setValue(capitolo.formato);
                    setGraphic(choiceBox);
                }
            }
        });
        formatoCol.setEditable(true);
        
        // Aggiunta delle colonne alla tabella
        tableView.setEditable(true);
        tableView.getColumns().addAll(numeroCol, nomeCol, descrizioneCol, formatoCol); 
        tableView.getStyleClass().add("tabella");
        
        // Impostazione dei dati nella tabella
        tableView.setItems(FXCollections.observableArrayList(capitoli));
        
        // Assicurati che la tabella abbia una sola riga
        tableView.setFixedCellSize(25); // Imposta l'altezza fissa delle celle
        tableView.prefHeightProperty().bind(Bindings.size(FXCollections.observableArrayList(capitoli)).multiply(tableView.getFixedCellSize()).add(30)); // Calcola l'altezza prefissata della tabella
        
        return tableView;
    }
    
    // Funzione per creare una colonna generica con classe Fumetto
    private <T> TableColumn<Fumetto, String> crea_colonna_fumetto(TableView tableView, String nome, Function<Fumetto, T> valoreGetter, BiConsumer<Fumetto, T> valoreSetter, double range){
        TableColumn<Fumetto, String> colonna = new TableColumn<>(nome);
        colonna.prefWidthProperty().bind(tableView.widthProperty().multiply(range));
        colonna.setEditable(true);
        colonna.setCellFactory(TextFieldTableCell.forTableColumn());
        colonna.setCellValueFactory(cellData -> {
            T valore = valoreGetter.apply(cellData.getValue());
            return new SimpleStringProperty(valore != null ? valore.toString() : "");
        });
        colonna.setOnEditCommit(event -> {
            fumetto = event.getRowValue();
            T newValue = (T) event.getNewValue();
            valoreSetter.accept(fumetto, newValue); // Salva il nuovo valore nella riga
        });
        return colonna;
    }
    
    // Funzione per creare una colonna generica con classe Capitolo
    private <T> TableColumn<Capitolo, String> crea_colonna_capitolo(TableView<Capitolo> tableView, String nome, Function<Capitolo, T> valoreGetter, BiConsumer<Capitolo, T> valoreSetter, double range, boolean edit){
        TableColumn<Capitolo, String> colonna = new TableColumn<>(nome);
        colonna.prefWidthProperty().bind(tableView.widthProperty().multiply(range)); 
        colonna.setEditable(edit); // Imposta se la colonna è modificabile o meno
        // Imposta la cella della colonna come TextFieldTableCell per consentire la modifica
        colonna.setCellFactory(TextFieldTableCell.forTableColumn());
        // Imposta il valore della cella in base al valore restituito dalla funzione valoreGetter
        colonna.setCellValueFactory(cellData -> {
            Capitolo capitolo = (Capitolo) cellData.getValue();
            T valore = valoreGetter.apply(capitolo);
            return new SimpleStringProperty(valore != null ? valore.toString() : "");
        });
        
        // Gestisce l'evento di modifica della cella
        colonna.setOnEditCommit(event -> {
            Capitolo capitolo = event.getRowValue();
            T newValue = (T) event.getNewValue();
            valoreSetter.accept(capitolo, newValue); // Salva il nuovo valore nella riga
        });
        
        return colonna;
    }
    
    // Metodo che salva le modifiche del fumetto
    private void salva_fumetto(){
        if(!controllo_formato_data())
            return;
        
        // Task che invia una richiesta HTTP per aggiornare il fumetto nel database
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Aggiorna Fumetto avviato!");
                HttpURLConnection con = null;    
                
                try{  
                    // Creazione della connessione 
                    URL url = new URL("http://localhost:8080/fumetto/update");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                    Gson gson = new Gson();
                    String data = gson.toJson(fumetto);

                    try(OutputStream os = con.getOutputStream()){
                        // Converte la stringa JSON in un array di byte usando l'encoding UTF-8
                        byte[] input = data.getBytes("utf-8");
                        // Scrive l'array di byte sull'output stream della connessione HTTP
                        os.write(input, 0, input.length);	
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    // Legge la risposta dal server
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        String inputLine;
                        StringBuilder content = new StringBuilder();

                        // Legge la risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        // Deserializza la risposta JSON in un oggetto JsonElement
                        JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                        // Estrae l'id dalla risposta JSON
                        String response = json.getAsJsonObject().get("id").getAsString();

                        // Gestisce in base alla risposta ricevuta
                        switch(response){
                            case "4004": // Fumetto non esistente
                                disappearNotifyAfterDelay("Il fumetto non esiste");
                                System.out.println("Il fumetto non esiste");
                                break;
                            case "4009": // Fumetto Esistente
                                disappearNotifyAfterDelay("Il fumetto non è locale");
                                System.out.println("Il fumetto non è locale");
                                break;
                            case "4010": // La copertina non esiste
                                disappearNotifyAfterDelay("La copertina non esiste");
                                System.out.println("La copertina non esiste");
                                break;
                            case "2000": // Registrazione Fumetto avvenuta 
                                disappearNotifyAfterDelay(fumetto.titolo + " è stato aggiornato");
                                System.out.println(fumetto.titolo + " è stato aggiornato");
                                break;
                            default: // Risposta sconosciuta
                                disappearNotifyAfterDelay("Qualcosa è andato storto con l'aggiornamento del fumetto");
                                System.out.println("Qualcosa è andato storto con l'aggiornamento del fumetto");
                        }
                    }
                }
                catch(IOException ioe){
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Aggiorna Fumetto concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che controlla che il formato della data del fumetto sia corretto
    private boolean controllo_formato_data(){
        // Controllo del formato della data di uscita e eventuale conversione
        if(fumetto.dataUscita != null && !fumetto.dataUscita.matches("^\\w{3}\\s\\d{2},\\s\\d{4}$")){
            try {
                // Prova a parsare la stringa utilizzando il formato specificato
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");

                Date date = inputFormat.parse(fumetto.dataUscita);
                String formattedDate = outputFormat.format(date);

                fumetto.dataUscita = formattedDate;
            } 
            catch(ParseException e) {
                disappearNotifyAfterDelay("La stringa NON è formattabile come una data SQL");
                System.out.println("La stringa NON è formattabile come una data SQL");
                return false;
            }
        }
        
        return true;
    }
    
    // Metodo che rimuove prima la lettura del fumetto dal database e poi il fumetto se possibile
    private void rimuovi(){
        String usr = URLEncoder.encode(SessioneUtente.getUsername(), StandardCharsets.UTF_8);
        String path = URLEncoder.encode(fumetto.path, StandardCharsets.UTF_8);
        
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
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);
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
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Rimuovi Lettura concluso!");
                }
                return null;
            }
        };
        
        // Dopo che ha successo rimuove direttamente il fumetto
        task.setOnSucceeded(event -> {
            rimuovi_fumetto();
        });
        
        new Thread(task).start();
    }
    
    // Metodo che rimuove il fumetto
    private void rimuovi_fumetto(){
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Rimuovi Fumetto avviato!");
                HttpURLConnection con = null;    
                String encodedPath = URLEncoder.encode(fumetto.path, StandardCharsets.UTF_8);
                try{
                    // Creazione di un'URL per la richiesta di ricerca dei fumetti
                    URL url = new URL("http://localhost:8080/capitolo/removeComic?path=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    
                    // Creazione di un oggetto Gson per la manipolazione di oggetti JSON
                    Gson gson = new Gson();
                    
                    // Legge la risposta dal server
                    try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        String inputLine;
                        StringBuilder content = new StringBuilder();

                        // Legge la risposta riga per riga
                        while((inputLine = in.readLine()) != null)
                            content.append(inputLine);

                        in.close();
                        // Deserializza la risposta JSON in un oggetto JsonElement
                        JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                        // Estrae l'id dalla risposta JSON
                        String response = json.getAsJsonObject().get("id").getAsString();

                        // Gestisce in base alla risposta ricevuta
                        switch(response){
                            case "4004": // Fumetto non esistente
                                disappearNotifyAfterDelay("Il fumetto non esiste");
                                System.out.println("Il fumetto non esiste");
                                break;
                            case "2000": // Registrazione Fumetto avvenuta 
                                System.out.println(fumetto.titolo + " è stato rimosso");
                                Platform.runLater(() -> {
                                    disappearNotifyAfterDelay(fumetto.titolo + " è stato rimosso dal Database. Va eliminato localmente dalla cartella del Server: comic-reader-server\\src\\main\\fumetti");
                                    box_tabelle.getChildren().clear();
                                });
                                break;
                            default: // Risposta sconosciuta
                                disappearNotifyAfterDelay("Qualcosa è andato storto con la rimozione del fumetto");
                                System.out.println("Qualcosa è andato storto con la rimozione del fumetto");
                        }
                    }
                }
                catch(IOException ioe){
                    disappearNotifyAfterDelay("Il fumetto è stato letto da un altro account e pertanto non può essere tolto. La tua lettura è stata tolta.");
                    ioe.printStackTrace();
                }
                finally{
                    if(con != null)
                        con.disconnect();
                    System.out.println("Task Tabella Fumetto concluso!");
                }
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che salva le informazioni sui capitoli
    private void salva_capitoli(TableView<Capitolo> tabella){
        ArrayList<Capitolo> capitoli = ottieni_capitoli_tabella(tabella);
        
        Task task = new Task<Void>(){   
            @Override
            public Void call(){
                System.out.println("Task Aggiorna Fumetto avviato!");
                HttpURLConnection con = null;    
                
                int num_capitoli = capitoli.size();
                int cap_ok = 0;
                
                String encodedPath = URLEncoder.encode(fumetto.path, StandardCharsets.UTF_8);
                
                for(Capitolo capitolo: capitoli){
                    String nome = URLEncoder.encode(capitolo.nome, StandardCharsets.UTF_8);
                    String des = URLEncoder.encode(capitolo.descrizione, StandardCharsets.UTF_8);
                    String form = URLEncoder.encode(capitolo.formato, StandardCharsets.UTF_8);
                    
                    try{  
                        // Creazione della connessione 
                        URL url = new URL("http://localhost:8080/capitolo/update?path=" + encodedPath + "&num=" + capitolo.numero + "&nome=" + nome + "&des=" + des +"&form=" + form);
                        con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setDoOutput(true);
                        con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                        con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                        Gson gson = new Gson();

                        // Legge la risposta dal server
                        try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                            String inputLine;
                            StringBuilder content = new StringBuilder();

                            // Legge la risposta riga per riga
                            while((inputLine = in.readLine()) != null)
                                content.append(inputLine);

                            in.close();
                            // Deserializza la risposta JSON in un oggetto JsonElement
                            JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
                            // Estrae l'id dalla risposta JSON
                            String response = json.getAsJsonObject().get("id").getAsString();

                            // Gestisce in base alla risposta ricevuta
                            switch(response){
                                case "4004": // Fumetto non esistente
                                    System.out.println("Il fumetto non esiste");
                                    break;
                                case "4009": // Capitolo non Esistente
                                    System.out.println("Il capitolo non esiste");
                                    break;
                                case "2000": // Registrazione Fumetto avvenuta 
                                    System.out.println("Il capitolo " + capitolo.numero + " è stato aggiornato");
                                    cap_ok++;
                                    break;
                                default: // Risposta sconosciuta
                                    System.out.println("Qualcosa è andato storto con l'aggiornamento del fumetto");
                            }
                        }
                    }
                    catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                    finally{
                        if(con != null)
                            con.disconnect();
                    }
                }
                
                System.out.println("Task Aggiorna Fumetto concluso!");
                if(cap_ok >= num_capitoli)
                    disappearNotifyAfterDelay("Tutti i capitoli sono stati aggiornati");
                else if(cap_ok > 1 && cap_ok < num_capitoli)
                    disappearNotifyAfterDelay("Solo " + cap_ok + " capitoli sono stati aggiornati");
                else if(cap_ok == 1)
                    disappearNotifyAfterDelay("Solo " + cap_ok + " capitolo è stato aggiornato");
                else if(cap_ok <= 0)
                    disappearNotifyAfterDelay("Nessun capitolo è stato aggiornato");
                
                return null;
            }   
        };
        
        new Thread(task).start();
    }
    
    // Metodo che ottiene un ArrayList<Capitolo> dalla tabella dei capitoli
    private ArrayList<Capitolo> ottieni_capitoli_tabella(TableView<Capitolo> tableView) {
        ArrayList<Capitolo> capitoliAggiornati = new ArrayList<>();
        ObservableList<Capitolo> items = tableView.getItems();

        for(Capitolo capitolo : items)
            capitoliAggiornati.add(capitolo);

        return capitoliAggiornati;
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

    // Metodo per passare alla schermata principale
    @FXML
    private void switchToHome() throws IOException{
        App.setRoot("home");
    }
    
    // Metodo iniziale per l'inizializzazione dei componenti e delle azioni
    @FXML
    protected void initialize(){
        // Aggiorna la barra di ricerca con una stringa vuota
        aggiorna_barra("");
        
        // Aggiunta di un listener per il testo digitato nella barra di ricerca
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
        
        // Aggiungi un listener per l'evento di pressione del tasto INVIO sulla casella di testo ricerca
        ricerca.setOnKeyPressed(event -> {
            // Verifica se il tasto premuto è INVIO
            if(event.getCode().equals(KeyCode.ENTER))
                costruisci_tabelle(ricerca.getText()); // Chiama la funzione costruisci_tabella() quando viene premuto INVIO
        });
        
        // Aggiunta di un listener per la selezione di un'opzione nel ChoiceBox
        scelte.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Controlla se il nuovo valore non è nullo e imposta il testo nel TextField
            if(newValue != null) {
                costruisci_tabelle(newValue.toString());
                ricerca.setText(newValue.toString());
                // Imposta la posizione del cursore alla fine del testo nel TextField
                ricerca.positionCaret(ricerca.getText().length());
            }
        });
        
        lente.setOnMouseClicked(event -> {
            // Ottieni il testo dalla barra di ricerca
            String testo = ricerca.getText();
            // Chiama la funzione con il testo della ricerca come parametro
            click_lente(testo);
        });
    }
}
