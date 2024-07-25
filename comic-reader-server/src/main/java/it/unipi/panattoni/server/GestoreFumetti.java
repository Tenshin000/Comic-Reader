package it.unipi.panattoni.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Task;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Classe per gestire l'inserimento di fumetti e capitoli utilizzando il Singleton Pattern.
 * @author Francesco Panattoni
 */

@Component
public class GestoreFumetti{
    // Campi  
    private static GestoreFumetti instance = new GestoreFumetti();
    
    // Metodi    
    private GestoreFumetti(){}    
    
    // Metodo per inserire (nel caso non siano inseriti) i fumetti di default, già presenti nell'applicazione
    public synchronized static void gestisci_fumetti_iniziali(){
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080").openConnection();
            connection.connect();
            connection.disconnect();
            
            System.out.println("Gestore Fumetti attivato!");
            File comicsFolder = new File("src/main/fumetti");
            File coverFolder = new File("src/main/fumetti/Copertine");
            if(!comicsFolder.exists() || !coverFolder.exists())
                crea_cartelle();

            gestisci_fumetti_online();
            gestisci_fumetti_locali();
            System.out.println("Il Gestore Fumetti ha completato il suo compito");
        } 
        catch(IOException ioe){
            System.out.println("Connessione Rifiutata. Se in fase di build, verrà svolto correttamente all'avvio");
        }
    }
    
    // Crea le cartelle fumetti e Copertine nel caso non vi fossero già
    private static void crea_cartelle(){
        // Percorso della cartella principale
        String mainFolderPath = "src/main/";

        // Cartella "fumetti" all'interno della cartella principale
        String comicsFolderPath = mainFolderPath + "fumetti/";

        // Cartella "Copertine" all'interno della cartella "fumetti"
        String coversFolderPath = comicsFolderPath + "Copertine/";

        // Creazione della cartella "fumetti"
        File comicsFolder = new File(comicsFolderPath);
        if(!comicsFolder.exists()){
            comicsFolder.mkdirs();
            System.out.println("Cartella \"fumetti\" creata.");
        } 
        else
            System.out.println("La cartella \"fumetti\" esiste già.");

        // Creazione della cartella "Copertine" all'interno della cartella "fumetti"
        File coversFolder = new File(coversFolderPath);
        if(!coversFolder.exists()){
            coversFolder.mkdirs();
            System.out.println("Cartella \"Copertine\" creata.");
            salva_immagine("https://c8.alamy.com/comp/2BEA3WT/retro-magazine-cover-vintage-comic-book-vector-template-book-cover-for-comic-cartoon-magazine-page-illustration-2BEA3WT.jpg", coversFolderPath + "Comic.jpg");
        } 
        else
            System.out.println("La cartella \"Copertine\" esiste già.");
    }
    
    // Metodo che gestisce i fumetti presi Onlnine
    private static void gestisci_fumetti_online(){
        inserisci_XKCD();
    }
    
    // Metodo che inserisce il fumetto XKCD
    private static void inserisci_XKCD(){
        String chapterFolderPath = "src/main/fumetti/XKCD";
        File xkcdFolder = new File(chapterFolderPath);
        if(xkcdFolder.exists()){
            System.out.println("Il fumetto XKCD già esiste");
            return;
        }
        else{
            xkcdFolder.mkdirs();
            System.out.println("Cartella \"XKCD\" creata.");
        }
        
        salva_immagine("https://imgs.xkcd.com/comics/barrel_cropped_(1).jpg", "src/main/fumetti/Copertine/XKCD.jpg");
        
        String path = "src/main/fumetti/XKCD";
        String titolo = "XKCD";
        String autori = "https://xkcd.com/";
        String copertina = "src/main/fumetti/Copertine/XKCD.jpg";
        String dataUscita = "2006-01-01";
        String sinossi = "Vignette pubblicate sul sito xkcd.com";
        double valutazione = 6.0;
        boolean manga = true; // PER PROVARE LA LETTURA PER MANGA
        
        try{
            // Da String a SQL Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = sdf.parse(dataUscita);
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());

            Fumetto f = new Fumetto(path, titolo, autori, copertina, sqlDate, sinossi, valutazione, manga, false);
            
            add_fumetto(f);
            
            // URL dell'endpoint principale di XKCD per ottenere le informazioni sul fumetto più recente
            URL url = new URL("https://xkcd.com/info.0.json");

            // Apre una connessione HTTP
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            StringBuffer response;
            // Legge la risposta dalla connessione
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                response = new StringBuffer();
                String line;
                while((line = reader.readLine()) != null)
                    response.append(line);
            }

            // Analizza la risposta JSON per ottenere il numero totale di fumetti disponibili
            JSONObject json = new JSONObject(response.toString());
            int totaleFumetti = json.getInt("num");
            int anno = 0;
            int capitolo = 0; 
            int pagina = 1;
            
            // Itera su tutti i fumetti e ottieni le informazioni per ciascun fumetto
            for(int i = (2600 < totaleFumetti) ? 2600 : 0; i <= totaleFumetti; i+=25){
                // i+=25 per scaricare meno pagine e rendere minore il tempo di scaricamento
                URL comicUrl = new URL("https://xkcd.com/" + i + "/info.0.json");
                HttpURLConnection comicCon = (HttpURLConnection) comicUrl.openConnection();
                comicCon.setRequestMethod("GET");

                StringBuffer comicResponse;
                try(BufferedReader comicReader = new BufferedReader(new InputStreamReader(comicCon.getInputStream()))){
                    comicResponse = new StringBuffer();
                    String comicLine;
                    while((comicLine = comicReader.readLine()) != null)
                        comicResponse.append(comicLine);
                }

                // Analizza la risposta JSON per ottenere le informazioni sul fumetto corrente
                JSONObject comicJson = new JSONObject(comicResponse.toString());
                String comicImageUrl = comicJson.getString("img");
                String comicTitle = comicJson.getString("title");
                int comicYear = Integer.parseInt(comicJson.getString("year"));
                
                if(anno != comicYear){
                    if(capitolo != 0){
                        String nome = "Anno " + anno;
                        String descrizione = "I capitoli di xkcd dell'anno " + anno;
                        Capitolo c = new Capitolo(f, capitolo, nome, descrizione, "png", conta_pagine(f.getPath() + "/" + capitolo, "png"));
                        add_capitolo(c);
                        System.out.println("Il capitolo " + capitolo + " è stato salvato");
                    }
                    
                    capitolo++;
                    anno = comicYear;
                    chapterFolderPath = "src/main/fumetti/XKCD/" + capitolo + "/";
                    File chapertFolder = new File(chapterFolderPath);
                    if(!chapertFolder.exists())
                        chapertFolder.mkdirs();
                    pagina = 1;
                }

                // Scarica e salva l'immagine
                boolean imageSaved = salva_immagine(comicImageUrl, chapterFolderPath + pagina + ".png");
                
                if(imageSaved){
                    pagina++;
                    System.out.println("La pagina " + comicTitle + " è stata salvata");
                }
                else{
                    System.out.println(comicTitle + "non è stato salvato");
                }

                comicCon.disconnect();
            }
            
            String nome = "Anno " + anno;
            String descrizione = "I capitoli di xkcd dell'anno " + anno;
            Capitolo c = new Capitolo(f, capitolo, nome, descrizione, "png", conta_pagine(f.getPath() + "/" + capitolo, "png"));
            add_capitolo(c);
            System.out.println("Il capitolo " + capitolo + " è stato salvato");

            // Chiude la connessione
            con.disconnect();
        } 
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // Metodo che salva un'immagine da un URL locale
    private static boolean salva_immagine(String imageUrl, String destinationFile){
        try{
            // Apre una connessione all'URL dell'immagine
            URL url = new URL(imageUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            
            // Controlla se la connessione ha avuto successo
            if(con.getResponseCode() == 200){
                try(FileOutputStream outputStream = new FileOutputStream(destinationFile)){
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    // Legge i dati dall'input stream e li scrive sul file di destinazione
                    while((bytesRead = con.getInputStream().read(buffer)) != -1)
                        outputStream.write(buffer, 0, bytesRead);
                }
                con.disconnect();
                return true;
            } 
            else
                return false;
        } 
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    // Metodo che gestisce i fumetti locali presenti nella directory specificata
    private static void gestisci_fumetti_locali(){
         // Percorso della cartella contenente le cartelle dei fumetti
        String directoryPath = "src/main/fumetti";

        // Crea un oggetto File per la directory
        File cartella = new File(directoryPath);
        
        // Controlla se il percorso specificato esiste ed è una directory
        if(cartella.exists() && cartella.isDirectory()){
            // Ottieni il vettore dei nomi delle cartelle all'interno della directory
            String[] folders = cartella.list();

            // Crea una lista per memorizzare i nomi delle cartelle
            List<String> fumetti = new ArrayList<>();

            // Itera attraverso l'array dei nomi delle cartelle
            fumetti.addAll(Arrays.asList(folders)); // Aggiungi il nome della cartella alla lista

            for(String fumetto: fumetti){
                if(!fumetto.equals("Copertine")){
                    String path = directoryPath + "/" + fumetto;
                    String titolo = fumetto;
                    // Imposta il percorso della copertina del fumetto
                    String copertina = "src/main/fumetti/Copertine/" + titolo + ".jpg";
                    // Se la copertina non esiste, cerca altri formati
                    File cover = new File(copertina);
                    if(!cover.exists()){
                        copertina = "src/main/fumetti/Copertine/" + titolo + ".jpeg";
                        cover = new File(copertina);
                        if(!cover.exists()){
                            copertina = "src/main/fumetti/Copertine/" + titolo + ".png";
                            cover = new File(copertina);
                            if(!cover.exists())
                                copertina = "src/main/fumetti/Copertine/comic.jpg";
                        }
                    }
                    
                    // Se il percorso e il titolo non sono nulli o vuoti, crea un oggetto Fumetto
                    if(path != null && titolo != null && !titolo.equals("")){
                        Fumetto f = new Fumetto(path, titolo, "", copertina, null, null, null, false, true);
                        boolean b = add_fumetto(f);
                        if(b) // Aggiunge i capitoli iniziali se il fumetto è stato aggiunto con successo
                            inserisci_capitoli_iniziali(f);  
                        else // Controlla se sono stati aggiunti nuovi capitoli
                            controllo_nuovi_capitoli(f); 
                    }
                }     
            }
        }
        else
            System.out.println("La directory specificata non esiste o non è una cartella.");
     }
    
    // Metodo per l'inserimento dei capitoli di default di un fumetto
    private static void inserisci_capitoli_iniziali(Fumetto f){
        // Percorso della cartella contenente le cartelle dei fumetti
        String directoryPath = f.getPath();

        // Crea un oggetto File per la directory
        File cartella = new File(directoryPath);
        
        // Controlla se il percorso specificato esiste ed è una directory
        if(cartella.exists() && cartella.isDirectory()){
            // Ottieni il vettore dei nomi delle cartelle all'interno della directory
            String[] folders = cartella.list();

            // Crea una lista per memorizzare i nomi delle cartelle che sono i numeri dei capitoli
            List<String> capitoli = new ArrayList<>();

            // Itera attraverso l'array dei nomi delle cartelle
            capitoli.addAll(Arrays.asList(folders)); // Aggiungi il nome della cartella alla lista
            
            // Rinomina le cartelle numeriche in base ai titoli dei capitoli
            rinomina(f.getPath());
            
            for(String capitolo: capitoli){
                // Cerca il formato della prima pagina da estendere come formato del capitolo
                Integer numero = Integer.parseInt(capitolo);
                File immagine = new File(f.getPath() + "/" + capitolo + "/1.jpg");
                String formato = "";
                if(!immagine.exists()){
                    immagine = new File(f.getPath() + "/" + capitolo + "/1.png");
                    if(!immagine.exists()){
                        immagine = new File(f.getPath() + "/" + capitolo + "/1.jpeg");
                        if(!immagine.exists())
                            formato = "jpeg";
                        else
                            formato = "jpg";
                    }
                    else
                        formato = "png";
                }
                else
                    formato = "jpg";
                
                // Crea un oggetto Capitolo e lo aggiunge alla lista dei capitoli del fumetto
                Capitolo c = new Capitolo(f, numero, "", "", formato, conta_pagine(f.getPath() + "/" + numero, formato));
                add_capitolo(c);
            }
        }
        else
            System.out.println("La directory del capitolo specificata non esiste o non è una cartella.");
    }    
    
    // Metodo che controlla se sono stati aggiunti nuovi capitoli al fumetto
    private static void controllo_nuovi_capitoli(Fumetto f){
        if(!f.getLocale())
            return;
        try(Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/d604230", "root", "root");
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS Capitoli FROM Capitolo WHERE Fumetto = ?");){

            // Imposta il parametro nella query preparata con il valore del path del Fumetto
            ps.setString(1, f.getPath());
            ResultSet rs = ps.executeQuery();

            // Se esiste un risultato, confronta il numero di capitoli presenti con quelli nel database
            if(rs.next()){
                int num_capitoli = rs.getInt("Capitoli");
                int conto = conta_capitoli(f.getPath());
                if(conto > num_capitoli){
                    for(int i = num_capitoli + 1; i <= conto; i++){
                        Integer numero = i; 
                        File immagine = new File(f.getPath() + "/" + numero + "/1.jpg");
                        String formato = "";
                        if(!immagine.exists()){
                            immagine = new File(f.getPath() + "/" + numero + "/1.png");
                            if(!immagine.exists()){
                                immagine = new File(f.getPath() + "/" + numero + "/1.jpeg");
                                if(!immagine.exists())
                                    formato = "jpeg";
                                else
                                    formato = "jpg";
                            }
                            else
                                formato = "png";
                        }
                        else
                            formato = "jpg";

                        Capitolo c = new Capitolo(f, numero, "", "", formato, conta_pagine(f.getPath() + "/" + numero, formato));
                        add_capitolo(c);
                    }
                }  
            }
        } 
        catch(SQLException sqle){
            sqle.printStackTrace();
        }
        finally{
            System.out.println("Task Controllo Capitoli concluso!");
        }
    }
    
    // Metodo per l'inserimento di un nuovo fumetto tramite thread
    public synchronized static void inserisci_fumetto(Fumetto f){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task per aggiungere fumetto " + f.getTitolo());
                add_fumetto(f);
                return null;
            }
        };
         
        new Thread(task).start();
    }
    
    // Metodo per l'inserimento di un nuovo capitolo tramite thread
    public synchronized static void inserisci_capitolo(Capitolo c){
        Task task = new Task<Void>(){
            @Override
            public Void call(){
                System.out.println("Task per aggiungere capitolo " + c.getNumero() + " di " + c.getFumetto().getTitolo());
                add_capitolo(c);
                return null;
            }
        };

        new Thread(task).start();
    }
    
    // Metodo che conta le pagine di un capitolo dato il suo path
    private static int conta_pagine(String path, String formato){
        int pagine = 0;
     
        File cartella = new File(path);
        
        if(cartella.exists() && cartella.isDirectory()){
            File[] files = cartella.listFiles();
            for(File file: files){
                if(isImage(file, formato))
                    pagine++;
            }
        }
        else
            pagine = 0;
        
        return pagine;
    }
    
    // Metodo per verificare se un file è un'immagine in base all'estensione
    private static boolean isImage(File file, String formato){
        if(formato == null || (!formato.equals("jpg") && !formato.equals("jpeg") && !formato.equals("png")))
            return false;
        String nomeFile = file.getName();
        return nomeFile.endsWith("." + formato);
    } 
    
    // Metodo per rinominare le cartelle rappresentanti i capitoli e le immagini rappresentanti le pagine
    private static void rinomina(String path){
        // Ottieni la lista delle cartelle nella cartella principale
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles(File::isDirectory);

        if(folders != null){
            // Ordina le cartelle per nome
            Arrays.sort(folders);

            // Contatore per il nome delle cartelle
            int folderCounter = 1;

            // Itera attraverso tutte le cartelle
            for(File folder : folders){
                // Rinomina la cartella
                File newFolder = new File(mainFolder, folderCounter + "");
                folder.renameTo(newFolder);

                // Ottieni la lista dei file nella cartella
                File[] files = newFolder.listFiles();

                if(files != null){
                    // Ordina i file per nome
                    Arrays.sort(files);

                    // Contatore per il nome delle immagini
                    int imageCounter = 1;

                    // Itera attraverso tutti i file
                    for(File file : files){
                        // Se il file è un'immagine
                        if(file.isFile() && isImage(file, ottieni_estensione(file))){
                            // Rinomina l'immagine
                            String extension = ottieni_estensione(file);
                            File newFile = new File(newFolder, imageCounter + "." + extension);
                            file.renameTo(newFile);

                            // Incrementa il contatore per il nome delle immagini
                            imageCounter++;
                        }
                    }
                }

                // Incrementa il contatore per il nome delle cartelle
                folderCounter++;
            }
        }

        System.out.println("Operazione di rinominazione completata con successo.");
    }
    
    // Metodo per ottenere l'estensione di un file
    private static String ottieni_estensione(File file){
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1 || lastIndexOf == 0 || lastIndexOf == name.length() - 1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }
    
    // Metodo per ottenere il numero di cartelle rappresentanti i capitoli del fumetto
    public static int conta_capitoli(String path) {
        File directory = new File(path);
        int count = 0;

        // Verifica se il percorso esiste ed è una directory
        if(directory.exists() && directory.isDirectory()) {
            // Ottieni elenco dei file e delle directory all'interno della directory specificata
            File[] files = directory.listFiles();

            if(files != null){
                // Conta il numero di directory
                for(File file : files){
                    if(file.isDirectory())
                        count++;
                }
            }
        } 
        else
            System.out.println("Il percorso specificato non esiste o non è una directory.");

        return count;
    }
    
    // Metodo per aggiungere un nuovo fumetto al server
    private static boolean add_fumetto(Fumetto f){
        boolean risultato = false;            
        HttpURLConnection con = null;        
        try{  
            // Creazione della connessione 
            URL url = new URL("http://localhost:8080/fumetto/add");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
            con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON

            Gson gson = new Gson();
            String data = gson.toJson(f);
            
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
                    case "4009": // Fumetto Esistente
                        risultato = false;
                        break;
                    case "2000": // Registrazione Fumetto avvenuta 
                        System.out.println(f.getTitolo() + " è stato inserito");
                        risultato = true;
                        break;
                    default: // Risposta sconosciuta
                        System.out.println("Qualcosa è andato storto con la registrazione dei fumetti");
                        risultato = false;
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
        return risultato;
    }
    
    // Metodo per aggiungere un nuovo capitolo al server
    private static boolean add_capitolo(Capitolo c){
        boolean risultato = false;
        HttpURLConnection con = null;
        
        // Costruisce l'URL per la richiesta HTTP
        String num = URLEncoder.encode(c.getNumero().toString(), StandardCharsets.UTF_8);
        String nome = URLEncoder.encode(c.getNome(), StandardCharsets.UTF_8);
        String des = URLEncoder.encode(c.getDescrizione(), StandardCharsets.UTF_8);
        String form = URLEncoder.encode(c.getFormato(), StandardCharsets.UTF_8);
        String s = "http://localhost:8080/capitolo/add?pathFumetto=" + c.getFumetto().getPath() + "&num=" + num + "&nome=" + nome + "&des=" + des + "&form=" + form + "&pag=" + c.getPagine();
        
        try{  
            // Creazione della connessione 
            URL url = new URL(s);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json"); // Dichiariamo che inviamo JSON
            con.setRequestProperty("Accept", "application/json"); // Dichiariamo che riceviamo JSON
            
            // Legge la risposta dal server
            StringBuilder content = new StringBuilder();
            try(BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                String inputLine;
                // Legge la risposta riga per riga
                while((inputLine = in.readLine()) != null)
                    content.append(inputLine);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            Gson gson = new Gson();
            // Deserializza la risposta JSON in un oggetto JsonElement
            JsonElement json = gson.fromJson(content.toString(), JsonElement.class);
            // Estrae l'id dalla risposta JSON
            String response = json.getAsJsonObject().get("id").getAsString();
            
            // Gestisce in base alla risposta ricevuta
            switch(response){
                case "4004": // Fumetto Non Esistente
                    System.out.println("Il " + c.getFumetto().getTitolo() + " non esiste");
                    risultato = false;
                    break;
                case "4009": // Capitolo già esite
                    System.out.println("Il capitolo " + c.getNumero() + " di " + c.getFumetto().getTitolo() + " è già stato inserito");
                    risultato = false;
                    break;
                case "2000": // Registrazione Fumetto avvenuta 
                    System.out.println("Il capitolo " + c.getNumero() + " di " + c.getFumetto().getTitolo() + " è stato inserito");
                    risultato = true;
                    break;
                default: // Risposta sconosciuta
                    System.out.println("Qualcosa è andato storto con la registrazione dei capitoli");
                    risultato = false;
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        finally{
            if(con != null)
                con.disconnect();
        }
        return risultato;
    }
}
