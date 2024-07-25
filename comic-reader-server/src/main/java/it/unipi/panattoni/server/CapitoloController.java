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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Questa classe gestisce le richieste relative ai capitoli dei fumetti.
 * Più nello specifico gestisce l'aggiunta dei capitoli. 
 * 
 * @author Francesco Panattoni
 */

@Controller
@RequestMapping(path="/capitolo")
public class CapitoloController{
    @Autowired
    private CapitoloRepository capitoloRepository; 
    @Autowired
    private FumettoRepository fumettoRepository;
    
    /**
    Aggiungi Capitolo
     * Aggiunge un nuovo capitolo al fumetto specificato.
     * 
     * @param pathFumetto Path del fumetto a cui aggiungere il capitolo
     * @param num Numero del capitolo
     * @param nome Nome del capitolo
     * @param des Descrizione del capitolo
     * @param form Formato del capitolo
     * @param pag Numero delle pagine
     * @return Codice di risposta JSON
     * 2000: Ok
     * 4004: Fumetto non presente
     * 4009: Capitolo già presente
     */
    @PostMapping(path="/add")
    public @ResponseBody String aggiungi_capitolo(@RequestParam String pathFumetto, @RequestParam String num, @RequestParam String nome, @RequestParam String des, @RequestParam String form, @RequestParam Integer pag){
        Gson gson = new Gson();
        Fumetto f = fumettoRepository.findByPath(pathFumetto); 
        Integer numero = Integer.parseInt(num);
        try{
            if(f == null)
                throw new FumettoNonEsistenteException();
            else{
                Capitolo c1 = new Capitolo(f, numero, nome, des, form, pag);
                Capitolo c2 = capitoloRepository.findByID(c1.getID());
                if(c2 == null)
                    throw new CapitoloNonEsistenteException();
                return gson.toJson(new CodiceRisposta(4009));            
            }
        }
        catch(CapitoloNonEsistenteException ec){
            Capitolo capitolo = new Capitolo(f, numero, nome, des, form, pag);
            capitoloRepository.save(capitolo);
            return gson.toJson(new CodiceRisposta(2000));
        }
        catch(FumettoNonEsistenteException ef){
            return gson.toJson(new CodiceRisposta(4004)); 
        }
    }
    
    /**
    Recupero Capitoli
     * Metodo per cercare capitoli nel sistema in base al path del fumetto.
     * @param path path del fumetto
     * @return ResponseEntity contenente un ArrayList di capitoli che corrispondono alla ricerca
     */
    @GetMapping("/recover")
    public ResponseEntity<ArrayList<Capitolo>> recupera_capitoli(@RequestParam("path") String path){
        // Implementa la logica per cercare i capitoli in base al path del Fumetto
        ArrayList<Capitolo> capitoli = capitoloRepository.findByPath(path);
        return ResponseEntity.ok(capitoli);
    }
    
    /**
    Recupera la Pagina Richiesta
     * Recupera e restituisce l'immagine della pagina specificata del capitolo indicato.
     * 
     * @param path Il percorso completo del capitolo, incluso il numero del capitolo
     * @param pagina Il numero della pagina da recuperare
     * @return Una ResponseEntity contenente la risorsa ByteArrayResource dell'immagine della pagina richiesta
     * HttpStatus:
     * 200 -> OK: Se l'immagine della pagina viene recuperata con successo
     * 404 -> Not Found: Se il capitolo o l'immagine della pagina non sono presenti
     * 500 -> Internal Server Error: Se si verifica un errore durante il recupero dell'immagine della pagina
     */
    @PostMapping(path="/read")
    public ResponseEntity<ByteArrayResource> recupera_pagina(@RequestParam String path, @RequestParam Integer pagina){
        // Estrapola il path del fumetto e il numero del capitolo dal path fornito
        String pathFumetto = path.replaceAll("/\\d+$", "");
        int numero = Integer.parseInt(path.replaceAll(".*/(\\d+)$", "$1"));
        
        // Cerca il fumetto nel repository
        Fumetto f = fumettoRepository.findByPath(pathFumetto);
        // Costruisce l'ID del capitolo
        CapitoloID id = new CapitoloID(f, numero);
        
        try{
            // Cerca il capitolo nel repository
            Capitolo c = capitoloRepository.findByID(id);
            // Se il capitolo non esiste, solleva un'eccezione
            if(c == null)
                throw new CapitoloNonEsistenteException();
            
            // Costruisce il percorso completo dell'immagine della pagina
            String pathPagina = path + "/" + pagina + "." + c.getFormato();
            // Legge l'immagine della pagina da file
            BufferedImage page = ImageIO.read(new File(pathPagina));
            // Scrive l'immagine in un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(page, c.getFormato(), outputStream); 
            
            // Costruisce una ByteArrayResource dalla rappresentazione in byte dell'immagine
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
            // Restituisce una ResponseEntity contenente la risorsa ByteArrayResource dell'immagine della pagina richiesta
            return ResponseEntity.ok().contentLength(outputStream.size()).body(resource);
        } 
        catch(IOException ioe){
            // Se si verifica un errore durante il recupero dell'immagine, restituisce un errore server
            ioe.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        catch(CapitoloNonEsistenteException ec){
            // Se il capitolo non esiste, restituisce un errore 404
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
    Conta Capitoli
     * Restituisce il numero di capitoli nel database dato un fumetto. 
     * 
     * @param pathFumetto Il percorso del fumetto di cui contare le pagine del capitolo
     * @return Un Integer rappresentante il numero di capitoli del fumetto richiesto
     */
    @GetMapping(path="/chapters")
    public @ResponseBody Integer capitoli(@RequestParam String pathFumetto){
        Fumetto f = fumettoRepository.findByPath(pathFumetto); 
        try{
            if(f == null)
                throw new FumettoNonEsistenteException();
            else
                return capitoloRepository.countCapitoli(pathFumetto);
        }
        catch(FumettoNonEsistenteException e){
            // Restituisce una risposta JSON con numero di pagine 0 in caso di errore
            return 0;
        }
    }
    
    /**
    Conta Pagine Effettive
     * Restituisce il numero di pagine del capitolo specificato del fumetto indicato contandole nella cartella.
     * 
     * @param pathFumetto Il percorso del fumetto di cui contare le pagine del capitolo
     * @param capitolo Il numero del capitolo di cui contare le pagine
     * @return Una stringa JSON che rappresenta il numero di pagine del capitolo
     */
    @GetMapping(path="/pages")
    public @ResponseBody String pagine(@RequestParam String pathFumetto, @RequestParam Integer capitolo){
        Gson gson = new Gson();
        Fumetto f = fumettoRepository.findByPath(pathFumetto); 
        try{
            if(f == null)
                throw new FumettoNonEsistenteException();
            else{
                CapitoloID id = new CapitoloID(f, capitolo);
                Capitolo c = capitoloRepository.findByID(id);
                if(c == null)
                    throw new CapitoloNonEsistenteException();
                
                // Conta le pagine del capitolo e restituisce il numero di pagine in formato JSON
                return gson.toJson(conta_pagine(pathFumetto + "/" + capitolo, c.getFormato()));     
            }
        }
        catch(CapitoloNonEsistenteException | FumettoNonEsistenteException e){
            // Restituisce una risposta JSON con numero di pagine 0 in caso di errore
            return gson.toJson(new CodiceRisposta(0));
        }
    }
    
    /**
    Rimuovi Fumetto
     * Metodo per rimuovere il fumetto dal sistema.
     * @param path path del Fumetto
     * @return Stringa JSON rappresentante la risposta della richiesta
     * 2000: Ok
     * 4004: Il fumetto non esiste
     */
    @PostMapping(path="/removeComic")
    public @ResponseBody String rimuovi_fumetto(@RequestParam String path){
        Gson gson = new Gson(); 
        try{
            Fumetto fumetto = fumettoRepository.findByPath(path);
            if(fumetto == null)
                throw new FumettoNonEsistenteException();
            
            ArrayList<Capitolo> capitoli = capitoloRepository.findByPath(path);
            for(Capitolo capitolo: capitoli)
                capitoloRepository.delete(capitolo);
            fumettoRepository.delete(fumetto);
            return gson.toJson(new CodiceRisposta(2000));
        }
        catch(FumettoNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004));
        }
    }
    
    /**
    Aggiorna Capitolo
     * Aggiorna un capitolo.
     * 
     * @param path Path del fumetto da aggiornare
     * @param num Numero del capitolo
     * @param nome Nome del capitolo
     * @param des Descrizione del capitolo
     * @param form Formato del capitolo
     * @return Codice di risposta JSON
     * 2000: Ok
     * 4004: Fumetto non presente
     * 4009: Capitolo non presente
     */
    @PostMapping(path="/update")
    public @ResponseBody String aggiorna_capitolo(@RequestParam String path, @RequestParam Integer num, @RequestParam String nome, @RequestParam String des, @RequestParam String form){
        Gson gson = new Gson();
        Fumetto f = fumettoRepository.findByPath(path); 
        try{
            if(f == null)
                throw new FumettoNonEsistenteException();
            else{
                CapitoloID cid = new CapitoloID(f, num);
                Capitolo c = capitoloRepository.findByID(cid);
                if(c == null)
                    throw new CapitoloNonEsistenteException();
                c.setNome(nome);
                c.setDescrizione(des);
                c.setFormato(form);
                capitoloRepository.save(c);
                return gson.toJson(new CodiceRisposta(2000));          
            }
        }
        catch(CapitoloNonEsistenteException ec){
            return gson.toJson(new CodiceRisposta(4009)); 
            
        }
        catch(FumettoNonEsistenteException ef){
            return gson.toJson(new CodiceRisposta(4004)); 
        }
    }
    
    // Metodo che conta le pagine di un capitolo dato il suo path e il formato desiderato
    private CodiceRisposta conta_pagine(String path, String formato){
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
        return new CodiceRisposta(pagine);
    }
    
    // Metodo per verificare se un file è un'immagine in base all'estensione
    private boolean isImage(File file, String formato){
        if(formato == null || (!formato.equals("jpg") && !formato.equals("jpeg") && !formato.equals("png")))
            return false;
        String nomeFile = file.getName();
        return nomeFile.endsWith("." + formato);
    }  
}
