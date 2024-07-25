package it.unipi.panattoni.client;

import it.unipi.panattoni.client.fumetti.Fumetto;

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
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Controller per la schermata Home
 * @author Francesco Panattoni
 */

public class HomeController{
    // Campi
    @FXML
    private Text message; // Testo di benvenuto
    @FXML
    private TextField ricerca; // Barra di ricerca
    @FXML
    private ChoiceBox scelte; // ChoiceBox per le opzioni di ricerca
    @FXML
    private ImageView menu; // Icona del menu
    @FXML
    private VBox panel; // Panel laterale
    @FXML
    private Button profilo; // Per accedere alle informazioni del Profilo
    @FXML
    private Button locale; // Per accedere alle informazioni sui fumetti locali
    @FXML
    private Button da_leggere; // Per accedere alla schermata di lettura
    @FXML
    private Button manuale; // Per accedere al manuale
    @FXML
    private Button logout; // Per il logout
    @FXML
    private GridPane griglia; // Griglia per visualizzare i fumetti
    
    private boolean isTransitioning = false; // Flag per indicare se è in corso una transizione del pannello laterale
    private int prossima_riga = 0; // Indice per la prossima riga nella griglia
    private int prossima_colonna = 0; // Indice per la prossima colonna nella griglia
    
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
                    URL url = new URL("http://localhost:8080/fumetto/search?testo=" + encodedTesto);
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
                        
                        // Aggiornamento dell'interfaccia grafica nel thread JavaFX principale
                        Platform.runLater(() -> aggiorna_interfaccia_grafica(testo, fumetti));
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
    
    // Metodo per aggiornare l'interfaccia grafica con i fumetti ottenuti dalla ricerca
    private void aggiorna_interfaccia_grafica(String testo, ArrayList<Fumetto> fumetti){
        // Creazione di un'observable list contenente i titoli dei fumetti ottenuti durante la ricerca
        ObservableList<String> lista = FXCollections.observableArrayList(converti_fumetti_stringhe(fumetti));
        scelte.setItems(lista);
        
        // Rimozione dei fumetti precedentemente visualizzati nella griglia
        rimuovi_griglia();
        
        if(testo != null && !testo.equals("")){
            // Iterazione attraverso ogni fumetto ottenuto dalla ricerca
            for(Fumetto fumetto : fumetti)
                aggiungi_fumetto_griglia(fumetto); // Aggiunta del fumetto alla griglia
        }
    }
    
    // Metodo per rimuovere i fumetti dalla griglia
    private void rimuovi_griglia(){
        griglia.getChildren().clear(); 
        prossima_riga = 0;
        prossima_colonna = 0;
    }
    
    // Metodo per convertire i fumetti in array di stringhe
    private String[] converti_fumetti_stringhe(ArrayList<Fumetto> fumetti){
        ArrayList<String> titoli = new ArrayList<>();
        
        // Estrai i titoli dai fumetti e aggiungili all'ArrayList dei titoli
        for(Fumetto fumetto: fumetti)
            titoli.add(fumetto.titolo);

        // Converti l'ArrayList di titoli in un array di stringhe
        String[] titoliArray = titoli.toArray(new String[0]);
        return titoliArray;
    }
    
    // Metodo chiamato per aggiungere un fumetto alla griglia
    @FXML
    private void aggiungi_fumetto_griglia(Fumetto f){
        if(f == null){
            System.out.println("Errore: Fumetto nullo.");
            return;
        }

        TitledPane titledPane = crea_TitledPane(f);
        titledPane.setMinWidth(Region.USE_PREF_SIZE); // Imposta il minimo spazio necessario per la larghezza del TitledPane
        griglia.add(titledPane, prossima_colonna, prossima_riga);
        
        aggiorna_posizione_griglia();
    }
    
    // Metodo per creare un TitledPane per un fumetto
    private TitledPane crea_TitledPane(Fumetto f){
        String pathCopertina = f.copertina;
        // Crea un ImageView per visualizzare l'immagine della copertina
        ImageView copertina = new ImageView();
        // Imposta la larghezza dell'immagine della copertina a 100 pixel e preserva il rapporto di aspetto
        copertina.setFitWidth(100);
        copertina.setPreserveRatio(true);
        // Carica dal server l'immagine della copertina nel ImageView
        crea_copertina(copertina, pathCopertina);
        
        // Crea un VBox per contenere le informazioni aggiuntive sul fumetto
        VBox contenuto = crea_contenuto_VBox(f);
        
        TitledPane titledPane = new TitledPane();
        // Imposta l'altezza fissa desiderata per tutti i TitledPane
        titledPane.setPrefHeight(200); 
        // Imposta il testo del TitledPane con il titolo del fumetto
        titledPane.setText(f.titolo);
        // Imposta il contenuto con un HBox che contiene l'immagine della copertina e il contenuto aggiuntivo
        titledPane.setContent(new HBox(copertina, contenuto));
        // Disabilita la possibilità di espandere e comprimere il TitledPane
        titledPane.setCollapsible(false);
        // Aggiunge un gestore di eventi per il click del mouse sul TitledPane
        titledPane.setOnMouseClicked(event -> fumetto_scelto(f.path, f.titolo));
        
        return titledPane;
    }
    
    // Metodo per richiede l'immagine di copertina per un fumetto
    private void crea_copertina(ImageView copertina, String pathCopertina){
        Task task = new Task<Void>(){
            @Override
            public Void call() {
                HttpURLConnection con = null;    
                final String encodedPath = URLEncoder.encode(pathCopertina, StandardCharsets.UTF_8);
                try {
                    URL url = new URL("http://localhost:8080/fumetto/cover?copertina=" + encodedPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
                    con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

                   // Controlla lo stato della risposta HTTP
                    int responseCode = con.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        // Se la risposta è OK, legge l'immagine dalla risposta
                        InputStream inputStream = con.getInputStream();
                        Image image = new Image(inputStream);
                        
                        // Imposta l'immagine nella ImageView se è stata ottenuta con successo
                        if(image != null)
                            copertina.setImage(image);
                        else{
                            // Se l'immagine restituita è nulla, metti un immagine di repertorio
                            System.out.println("Errore nel caricamento dell'immagine"); 
                            // Imposta un'immagine di default nel caso di errore
                            image = new Image("src/main/resources/img/Comic.jpg");
                            copertina.setImage(image);
                        } 
                    }
                    else{
                        // Gestisce gli errori di risposta HTTP
                        System.out.println("Errore nella richiesta HTTP: " + responseCode);
                        // Imposta un'immagine di default nel caso di errore
                        Image image = new Image("src/main/resources/img/Comic.jpg");
                        copertina.setImage(image);
                        return null; // Se c'è stto un errore, restituisci null
                    }
                } 
                catch(IOException ioe){
                    ioe.printStackTrace();
                    // Imposta un'immagine di default nel caso di errore
                    Image image = new Image("src/main/resources/img/Comic.jpg");
                    copertina.setImage(image);
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
    
    // Metodo per creare il contenuto VBox con le informazioni per un fumetto
    private VBox crea_contenuto_VBox(Fumetto f){
        // Informazioni autore
        HBox autori;
        if(f.autori.contains(","))
            autori = crea_riga("Autori: ", f.autori);
        else
            autori = crea_riga("Autore: ", f.autori);
        
        // Informazioni Data di Uscita
        HBox data = crea_riga("Data di Uscita: ", f.dataUscita);
   
        // Informazioni Sinossi
        HBox sinossi = crea_riga("Sinossi: ", f.sinossi);
        
        // Creazione del VBox con all'interno le vare informazioni
        VBox vbox = new VBox(autori, data, sinossi);
        VBox.setMargin(autori, new Insets(2, 2, 2, 10));
        VBox.setMargin(data, new Insets(2, 2, 2, 10));
        VBox.setMargin(sinossi, new Insets(2, 2, 2, 10));
        return vbox;
    }
    
    // Metodo che crea la riga con definition in grassetto e info come se fosse un testo
    private HBox crea_riga(String definition, String info){
        Text definizione = new Text(definition);
        definizione.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        Text informazioni = new Text(info);
        informazioni.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        informazioni.setWrappingWidth(315);
        informazioni.setTextAlignment(TextAlignment.JUSTIFY);
        
        HBox hbox = new HBox(definizione, informazioni);
        return hbox;
    }
    
    // Metodo per aggiornare la posizione del prossimo fumetto nella griglia
    private void aggiorna_posizione_griglia(){
        prossima_colonna++;
        if(prossima_colonna == 2){
            prossima_colonna = 0;
            prossima_riga++;
            if(prossima_riga >= 3)
                griglia.addRow(griglia.getRowCount(), new Region(), new Region());
        }
    }
    
    // Metodo per mostrare il messaggio di benvenuto
    public void messaggio_benvenuto(){
        String s = "Comic Reader ti dà il benvenuto " + SessioneUtente.getUsername() + ".";
        message.setText(s);
    }
    
    // Metodo per gestire il click sul panel per far comparire un menu con "profilo", "da_leggere" e "logout"
    @FXML
    public void click_menu(){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                // Verifica se non si sta già eseguendo un'animazione
                if(!isTransitioning){
                    // Impostazione del flag di transizione in corso a true per eseguire l'animazione
                    isTransitioning = true; 
                    // Creazione di un'animazione di traslazione sul panel
                    TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.05), panel);
                    // Al termine dell'animazione riporta il flag a false
                    translateTransition.setOnFinished(event -> isTransitioning = false);
                    
                    if(panel.isVisible()){
                        // Nasconde il pannello e lo disabilita se visibile
                        panel.setVisible(false);
                        panel.setDisable(true);
                        // Esegue un'animazione di traslazione verso sinistra
                        translateTransition.setByX(-600);
                        translateTransition.play();
                    }
                    else{
                        // Mostra il pannello e lo abilita se non visibile
                        panel.setVisible(true);
                        panel.setDisable(false);
                        // Esegue un'animazione di traslazione verso destra
                        translateTransition.setByX(+600);
                        translateTransition.play();
                    }
                } 
                return null;
           }
        };
        
        new Thread(task).start();
    }
    
    // Metodo per far sottolineare il testo del pulsante quando il mouse entra
    @FXML
    private void setButtonStyle(Button button){
        button.setStyle("-fx-background-color: #66CCFF;");
        
        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: #220068;");
            button.setUnderline(true);
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: #66CCFF;");
            button.setUnderline(false);
        });
    }
    
    // Una volta scelto il fumetto, viene registrato nella sessione.
    // Si può così andare a scegliere il capitolo da leggere del fumetto scelto. 
    @FXML
    private void fumetto_scelto(String path, String title){
        SessioneUtente.setFumettoScelto(path);
        try{
           App.setTitle(title);
           App.setRoot("fumetti/comic"); 
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            SessioneUtente.setFumettoScelto(null);
        }
    }
    
    // Funzione per andare a visualizzare le informazioni sul profilo
    @FXML
    public void switchToProfilo() throws IOException{
        App.setRoot("account/profilo");
    }
    
    // Funzione per andare alla schermata della lettura
    @FXML
    public void switchToLocale() throws IOException{
        App.setRoot("account/locale");
    }
    
    // Funzione per andare alla schermata della lettura
    @FXML
    public void switchToLettura() throws IOException{
        App.setRoot("account/lettura");
    }
    
    // Funzione per andare al manuale dell'applicazione
    @FXML
    public void switchToManuale() throws IOException{
        App.setRoot("account/manuale");
    }
    
    // Funzione per il logout
    @FXML
    public void logout() throws IOException{
        SessioneUtente.logout();
        App.setRoot("login/login");
    }
    
    // Metodo iniziale per l'inizializzazione dei componenti e delle azioni
    @FXML
    protected void initialize(){
        messaggio_benvenuto();
                
        // Inizializzazione delle variabili di controllo della griglia
        prossima_riga = 0;
        prossima_colonna = 0;
        
        // Impostazione dello stile dei pulsanti nel menu panel
        setButtonStyle(profilo);
        setButtonStyle(locale);
        setButtonStyle(da_leggere);
        setButtonStyle(manuale);
        setButtonStyle(logout);
        
        // Animazione di chiusura iniziale del menu
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.05), panel);
        translateTransition.setByX(-600);
        translateTransition.play();
        isTransitioning = false;
        
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
        
        // Aggiunta di un listener per la selezione di un'opzione nel ChoiceBox
        scelte.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Controlla se il nuovo valore non è nullo e imposta il testo nel TextField
            if(newValue != null) {
                ricerca.setText(newValue.toString());
                // Imposta la posizione del cursore alla fine del testo nel TextField
                ricerca.positionCaret(ricerca.getText().length());
                aggiorna_barra(ricerca.getText());
            }
        });
    }
}
