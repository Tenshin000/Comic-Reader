package it.unipi.panattoni.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Classe per gestire la sessione dell'utente utilizzando il Singleton Pattern.
 * @author Francesco Panattoni
 */

public class SessioneUtente{
    // Campi
    private static SessioneUtente instance; // Istanza della sessione utente
    
    private String username; // Username
    private Boolean manga; // Indica se l'utente ha abilitato la lettura orientale per i manga (da destra verso sinistra)
    private String fumetto_scelto; // Indica il path del fumetto che l'utente ha scelto di leggere
    
    // Metodi
    private SessioneUtente(String username){
        this.username = username;
    }
    
    // Metodo per impostare l'istanza della sessione utente
    synchronized public static void setInstance(String username){
        System.out.println("Avvio Nuova Sessione");
        
        if(instance == null){
            // Crea una nuova istanza se non esiste
            instance = new SessioneUtente(username);
            if(username != null)
                instance.setManga(); // Imposta il tipo di lettura per i manga
        }        
        else{
            // Aggiorna il nome utente se già esiste un'istanza
            instance.username = username;
            if(username != null)
                instance.setManga(); // Imposta il tipo di lettura per i manga
        }   
        
        if(username != null)
            System.out.println("Benvenuto " + instance.username + "!");
    }
    
    // Metodo per ottenere l'istanza della Sessione Utente
    public static SessioneUtente getInstance(){
	return instance;
    }
    
    // Metodo per ottenere il nome utente
    public static String getUsername(){
        if(instance != null)
            return instance.username;
        
        return null;
    }
    
    // Controlla se l'utente ha abilitato la lettura orientale per i manga (da destra verso sinistra)
    synchronized private void setManga(){
        HttpURLConnection con = null;
        String encodedUsr = URLEncoder.encode(instance.username, StandardCharsets.UTF_8);
        
        try{
            // Creazione di un'URL per la richiesta di ricerca del manga
            URL url = new URL("http://localhost:8080/utente/getManga?usr=" + encodedUsr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            // Riceve il flag manga e lo inserisce nella sessione dell'utente
            boolean response = Boolean.parseBoolean(new BufferedReader(new InputStreamReader(con.getInputStream())).readLine());
            instance.manga = response;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            instance.manga = false;
        }
        finally{
            if(con != null)
                con.disconnect();
        }
    }
    
    // Metodo per ottenere il tipo di lettura per manga
    public static Boolean getManga(){
        return instance.manga;
    }
    
    // Metodo per impostare il fumetto scelto
    synchronized public static void setFumettoScelto(String path){
        instance.fumetto_scelto = path;
    }
    
    // Metodo per ottenere il fumetto scelto
    public static String getFumettoScelto(){
        return instance.fumetto_scelto;
    }
    
    // Metodo per effettuare il logout
    synchronized public static void logout(){
        System.out.println("Logout");
        instance.username = null;
        instance.fumetto_scelto = null;
    }
}
