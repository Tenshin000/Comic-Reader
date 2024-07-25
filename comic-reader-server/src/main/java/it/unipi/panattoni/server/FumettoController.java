package it.unipi.panattoni.server;

import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller per gestire le richieste relative agli utenti.
 * Gestisce le operazioni di creazione e recupero dei fumetti. 
 * 
 * @author Francesco Panattoni
 */

@Controller
@RequestMapping(path="/fumetto")
public class FumettoController{
    @Autowired
    private FumettoRepository fumettoRepository; 
    
    /**
    Aggiungi Fumetto
     * Metodo per aggiungere un nuovo fumetto nel sistema.
     * @param f Oggetto Fumetto da aggiungere
     * @return Stringa JSON rappresentante la risposta della richiesta
     * 2000: Ok
     * 4009: Il fumetto esiste già
     */
    @PostMapping(path="/add")
    public @ResponseBody String aggiungi_fumetto(@RequestBody Fumetto f){
        Gson gson = new Gson();
        try{
            Fumetto fu = fumettoRepository.findByPath(f.getPath());
            if(fu == null)
                throw new FumettoNonEsistenteException();
            return gson.toJson(new CodiceRisposta(4009));
        }
        catch(FumettoNonEsistenteException e){
            Fumetto fumetto = new Fumetto(f);
            fumettoRepository.save(fumetto);
            return gson.toJson(new CodiceRisposta(2000));
        }
    }
    
    /**
    Ricerca Fumetti
     * Metodo per cercare fumetti nel sistema in base al titolo e/o agli autori.
     * @param testo Testo da utilizzare per la ricerca
     * @return ResponseEntity contenente un ArrayList di fumetti che corrispondono alla ricerca
     */
    @GetMapping("/search")
    public ResponseEntity<ArrayList<Fumetto>> searchFumetto(@RequestParam("testo") String testo){
        // Implementa la logica per cercare i fumetti in base al testo
        ArrayList<Fumetto> fumetti = fumettoRepository.findByString(testo);
        return ResponseEntity.ok(fumetti);
    }
    
    /**
    Ricerca Fumetti inseriti in maniera locale
     * Metodo per cercare fumetti nel sistema in base al titolo e/o agli autori.
     * @param testo Testo da utilizzare per la ricerca
     * @return ResponseEntity contenente un ArrayList di fumetti che corrispondono alla ricerca
     */
    @GetMapping("/local")
    public ResponseEntity<ArrayList<Fumetto>> searchLocale(@RequestParam("testo") String testo){
        // Implementa la logica per cercare i fumetti in base al testo
        ArrayList<Fumetto> fumetti = fumettoRepository.findByLocal(testo);
        return ResponseEntity.ok(fumetti);
    }
    
    /**
    Recupera Fumetto
     * Metodo per recuperare un fumetto dal sistema.
     * @param path Path del fumetto
     * @return Fumetto, ossia il fumetto ricercato
     */
    @GetMapping(path="/recover")
    public @ResponseBody Fumetto recupera_fumetto(@RequestParam String path){
        Fumetto f = fumettoRepository.findByPath(path);
        return f;
    }
    
    /**
    Recupera flag Manga
     * Metodo per recuperare se il fumetto è un manga.
     * @param path Path del fumetto
     * @return il booleano che rappresenta se il fumetto è un manga
     */
    @GetMapping(path="/getManga")
    public @ResponseBody Boolean recupera_flag_manga(@RequestParam String path){
        Fumetto f = fumettoRepository.findByPath(path);
        if(f != null)
            return f.getManga();
        return false;
    }
    
    /**
    Aggiorna Fumetto Locale
     * Metodo per aggiornare un fumetto nel sistema.
     * @param f Oggetto Fumetto da aggiornare
     * @return Stringa JSON rappresentante la risposta della richiesta
     * 2000: Ok
     * 4004: Il fumetto non esiste
     * 4009: Il fumetto non è locale
     * 4010: La copertina non esiste
     */
    @PostMapping(path="/update")
    public @ResponseBody String aggiorna_fumetto(@RequestBody Fumetto f){
        Gson gson = new Gson(); 
        try{
            Fumetto fu = fumettoRepository.findByPath(f.getPath());
            if(fu == null)
                throw new FumettoNonEsistenteException();
            Fumetto fumetto = new Fumetto(f);
            if(!fumetto.getLocale())
                return gson.toJson(new CodiceRisposta(4009));
            File cover = new File(fumetto.getCopertina());
            if(!cover.exists())
                return gson.toJson(new CodiceRisposta(4010));
            fumettoRepository.save(fumetto);
            return gson.toJson(new CodiceRisposta(2000));
        }
        catch(FumettoNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004));
        }
    }

    /**
    Recupera Copertina
     * Restituisce l'immagine della copertina del fumetto corrispondente alla copertina specificata.
     * 
     * @param copertina Il percorso completo dell'immagine della copertina
     * @return Una ResponseEntity contenente la risorsa ByteArrayResource dell'immagine della copertina richiesta
     * HttpStatus:
     * 200 -> OK: Se l'immagine della copertina viene recuperata con successo
     * 404 -> Not Found: Se la copertina non è presente
     * 500 -> Internal Server Error: Se si verifica un errore durante il recupero dell'immagine della copertina
     */
    @PostMapping(path="/cover")
    public ResponseEntity<ByteArrayResource> recupera_copertina(@RequestParam String copertina){
        Fumetto fu = fumettoRepository.findByCopertina(copertina);
        if(fu == null)
            return ResponseEntity.notFound().build();
        
        // Estrae l'estensione del file dalla copertina
        String estensione = estrai_estensione(copertina);
        
        try{
            // Legge l'immagine della copertina da file
            BufferedImage cover = ImageIO.read(new File(copertina));
            // Scrive l'immagine in un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(cover, estensione, outputStream); 
            
            // Costruisce una ByteArrayResource dalla rappresentazione in byte dell'immagine
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
            // Restituisce una ResponseEntity contenente la risorsa ByteArrayResource dell'immagine della copertina richiesta
            return ResponseEntity.ok().contentLength(outputStream.size()).body(resource);
        } 
        catch(IOException ioe){
            // Se si verifica un errore durante il recupero dell'immagine, restituisce un errore server
            ioe.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Dato in input il path di una copertina, restituisce l'estensione della copertina in output
    private String estrai_estensione(String copertina){
        String estensione = "";
        
        int indicePunto = copertina.lastIndexOf('.');
        if(indicePunto > 0 && indicePunto < copertina.length() - 1)
            estensione = copertina.substring(indicePunto + 1);
        
        return estensione;
    }
}
