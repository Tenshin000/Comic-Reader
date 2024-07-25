package it.unipi.panattoni.server;

import com.google.gson.Gson;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Classe che gestisce le richieste relative alle operazioni sulle letture dei fumetti da parte di un utente. 
 * Gestisce le operazioni di aggiunta, di aggiornamento e di rimozione di una lettura. 
 * 
 * @author Francesco Panattoni
 */

@Controller
@RequestMapping(path="/lettura")
public class LetturaController{
    @Autowired
    private LetturaRepository letturaRepository; 
    @Autowired
    private UtenteRepository utenteRepository;
    @Autowired
    private FumettoRepository fumettoRepository;
    
    /**
    Aggiungi Lettura
     * Aggiunge una nuova lettura o aggiorna una già esistente.
     * 
     * @param usr Username dell'utente
     * @param path Path del fumetto
     * @param capitolo Numero del capitolo letto
     * @return Risposta in formato JSON
     * 2000: Ok
     * 4004: Fumetto non presente
     * 4007: Utente non esistente
     * 4009: Lettura già presente
     */
    @PostMapping(path="/add")
    public @ResponseBody String aggiungi(@RequestParam String usr, @RequestParam String path, @RequestParam Integer capitolo){
        Gson gson = new Gson();
        
        if(capitolo < 0)
            return gson.toJson(new CodiceRisposta(4015));
        
        try{
            Fumetto f = fumettoRepository.findByPath(path);
            if(f == null)
                throw new FumettoNonEsistenteException();
            
            Utente u = utenteRepository.findByUsername(usr);
            if(u == null)
                throw new UtenteNonEsistenteException();
            
            Lettura l = letturaRepository.findByID_UtenteAndID_Fumetto(u, f);
            
            if(l == null){
                l = new Lettura(u, f, capitolo, null);
                letturaRepository.save(l);
                return gson.toJson(new CodiceRisposta(2000));
            }
            else if(l.getCapitolo() < capitolo){
                l.setCapitolo(capitolo);
                letturaRepository.save(l);
                return gson.toJson(new CodiceRisposta(2000));
            } 
            else
                return gson.toJson(new CodiceRisposta(4009));
        }
        catch(FumettoNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004));
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4007));
        }
    }
    
    /**
    Recupera la Valutazione
     * Restituisce la valutazione di un fumetto letto da un utente.
     * 
     * @param usr Username dell'utente
     * @param fumetto Path del fumetto
     * @return Valutazione media del fumetto in formato JSON
     */
    @GetMapping(path="/rating")
    public @ResponseBody Lettura recupera_valutazione(@RequestParam String usr, @RequestParam String fumetto){
        Utente u = utenteRepository.findByUsername(usr);
        Fumetto f = fumettoRepository.findByPath(fumetto);
        if(u == null || f == null)
            return null;
            
        Lettura l = letturaRepository.findByID_UtenteAndID_Fumetto(u, f);
        return l;
    }
    
    
    /**
    Aggiorna Valutazione
     * Aggiorna la valutazione di un fumetto letto da un utente.
     * 
     * @param usr Username dell'utente
     * @param fumetto Path del fumetto
     * @param val Valutazione da assegnare al fumetto
     * @return Valutazione media del fumetto in formato JSON
     */
    @PostMapping(path="/update")
    public @ResponseBody Double aggiorna(@RequestParam String usr, @RequestParam String fumetto, @RequestParam Integer val){
        if(val <= 0)
            return 0.0;
        
        Utente u = utenteRepository.findByUsername(usr);
        Fumetto f = fumettoRepository.findByPath(fumetto);
        if(u == null || f == null)
            return 0.0;
            
        Lettura l = letturaRepository.findByID_UtenteAndID_Fumetto(u, f);
        
        if(l != null){
            l.setValutazione(val);
            letturaRepository.save(l);
            Double voto = letturaRepository.calcolaMediaFumetto(f);
            f.setValutazione(voto);
            fumettoRepository.save(f);
            return voto;
        }
        
        return f.getValutazione();
    }
    
    /**
    Rimuovi Lettura
     * Rimuove una lettura di un fumetto associata a un utente.
     * 
     * @param usr Username dell'utente
     * @param path Path del fumetto
     * @return Risposta in formato JSON
     * 2000: Ok
     * 4004: Fumetto non presente
     * 4007: Utente non esistente
     * 4009: Lettura non esistente
     */
    @PostMapping(path="/remove")
    public @ResponseBody String rimuovi(@RequestParam String usr, @RequestParam String path){
        Gson gson = new Gson();
        
        try{
            Fumetto f = fumettoRepository.findByPath(path);
            if(f == null)
                throw new FumettoNonEsistenteException();
            
            Utente u = utenteRepository.findByUsername(usr);
            if(u == null)
                throw new UtenteNonEsistenteException();
            
            Lettura l = letturaRepository.findByID_UtenteAndID_Fumetto(u, f);
            
            if(l == null)
                return gson.toJson(new CodiceRisposta(4009));
            else{
                letturaRepository.delete(l);
                return gson.toJson(new CodiceRisposta(2000));
            }            
        }
        catch(FumettoNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004));
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4007));
        }
    }
    
    /**
    Ricerca
     * Restituisce una lista di righe rappresentanti i fumetti letti dall'utente specificato.
     * 
     * @param usr Lo username dell'utente di cui cercare i fumetti letti
     * @return Una ResponseEntity contenente un ArrayList di oggetti Riga rappresentanti i fumetti letti dall'utente
     * HttpStatus:
     * 200 -> OK: Se la ricerca ha avuto successo e vengono restituite le righe dei fumetti letti
     * 404 -> Not Found: Se l'utente non esiste o non ha fumetti letti
     */
    @GetMapping(path="/search")
    public ResponseEntity<ArrayList<Riga>> ricerca(@RequestParam String usr){        
        try{            
            Utente u = utenteRepository.findByUsername(usr);
            if(u == null)
                throw new UtenteNonEsistenteException();
            
            // Ottiene tutti i fumetti letti dall'utente
            ArrayList<Fumetto> fumettiLetti = letturaRepository.findAllFumettiByUtente(u);
            // Se l'utente non ha fumetti letti, restituisce che non vi sono fumetti
            if(fumettiLetti == null)
                return ResponseEntity.notFound().build();
            
            // Inizializza un ArrayList per contenere le righe dei fumetti letti dall'utente
            ArrayList<Riga> righe = new ArrayList();
            // Ciclo attraverso tutti i fumetti nella lista
            for(Fumetto fumetto : fumettiLetti){
                // Conta il numero di capitoli del fumetto
                int capitoli = fumettoRepository.countChapters(fumetto.getPath());
                // Trova la lettura corrispondente del fumetto per l'utente
                Lettura l = letturaRepository.findByID_UtenteAndID_Fumetto(u, fumetto);
                // Se la lettura non esiste, passa al prossimo fumetto
                if(l == null)
                    continue;
                // Crea una nuova riga per il fumetto e aggiungila alla lista delle righe
                Riga r = new Riga(fumetto.getTitolo(), l.getCapitolo(), capitoli, fumetto.getPath());
                righe.add(r);
            }
            
            // Restituisce una ResponseEntity contenente l'ArrayList delle righe dei fumetti letti dall'utente
            return ResponseEntity.ok(righe);
        }
        catch(UtenteNonEsistenteException e){
            return ResponseEntity.notFound().build();
        }
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
    }
}
