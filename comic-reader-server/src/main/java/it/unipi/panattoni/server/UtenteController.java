package it.unipi.panattoni.server;

import com.google.gson.Gson;
import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller per gestire le richieste relative agli utenti.
 * Gestisce le operazioni di login, registrazione, recupero password e modifiche dell'account.
 * 
 * @author Francesco Panattoni
 */

@Controller
@RequestMapping(path="/utente")
public class UtenteController{
    @Autowired
    private UtenteRepository utenteRepository; 
    
    @Autowired
    private LetturaRepository letturaRepository;
    
    /**
    Login
     * Gestisce la richiesta di login degli utenti.
     * Verifica se le credenziali inserite corrispondono a quelle memorizzate nel database.
     * @param usr Username dell'utente
     * @param pwd Password dell'utente
     * @return Risposta JSON indicante l'esito del login
     * 2000: Ok 
     * 4003: Password errata
     * 4004: Username non valido
     */
    @PostMapping(path="/login")
    public @ResponseBody String login(@RequestParam String usr, @RequestParam String pwd){
        Gson gson = new Gson();
        try{
            Utente u = utenteRepository.findByUsername(usr);    
            if(u == null) 
                throw new UtenteNonEsistenteException();
            String hash = u.getPassword();
            // Confronta la password in chiaro con l'hash ottenuto dal Database
            if(BCrypt.checkpw(pwd, hash))
                return gson.toJson(new CodiceRisposta(2000)); // Richiesta eseguita
            else 
                return gson.toJson(new CodiceRisposta(4003)); // Errore di accesso, password non valida
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004)); // Errore di accesso, username non valido
        }
    }
    
    /**
    Registrazione
     * Gestisce la richiesta di registrazione di un nuovo utente.
     * @param u Utente da registrare
     * @return Risposta JSON indicante l'esito della registrazione
     * 2000: Ok
     * 4009: Username già presente
     */
    @PostMapping(path="/signup")
    public @ResponseBody String registrazione(@RequestBody Utente u){
        Gson gson = new Gson();
        try{
            Utente ut = utenteRepository.findByUsername(u.getUsername());
            if(ut == null) 
                throw new UtenteNonEsistenteException();
            return gson.toJson(new CodiceRisposta(4009)); // Utente già esistente
        } 
        catch(UtenteNonEsistenteException ex){
            // Se l'Eccezione viene catturata significa che l'username non è già presente quindi possiamo registrarci
            u.setPassword(BCrypt.hashpw(u.getPassword(), BCrypt.gensalt()));
            u.setRispostaSicurezza(BCrypt.hashpw(u.getRispostaSicurezza(), BCrypt.gensalt()));
            Utente utente = new Utente(u);
            utenteRepository.save(utente);
            return gson.toJson(new CodiceRisposta(2000)); // Corretta registrazione
        }
    }
    
    /**
    Controllo Credenziali
     * Controlla se le credenziali per il cambio della password sono corrette. 
     * Verifica se le risposte alla domanda di sicurezza corrispondono a quelle memorizzate.
     * @param usr Username dell'utente
     * @param dom Domanda di sicurezza
     * @param ris Risposta alla domanda di sicurezza
     * @return Risposta JSON indicante l'esito del recupero password
     * 2000: Ok
     * 4003: Domanda di Sicurezza e/o Risposta di Sicurezza non valida/e
     * 4004: Utente non presente
     */
    @GetMapping(path="/forgot")
    public @ResponseBody String forgot_password(@RequestParam String usr, @RequestParam String dom, @RequestParam String ris){
        Gson gson = new Gson();
        try{
            Utente u = utenteRepository.findByUsername(usr);    
            if(u == null) 
                throw new UtenteNonEsistenteException();
            
            String hash = u.getRispostaSicurezza();
            
            if(dom.equals(u.getDomandaSicurezza()) && BCrypt.checkpw(ris, hash))
                return gson.toJson(new CodiceRisposta(2000));
            else 
                return gson.toJson(new CodiceRisposta(4003)); // Errore di accesso, domanda o risposta non valida            
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004)); // Errore di accesso, username non valido
        }
    }
    
    /**
    Cambio Password
     * Gestisce la richiesta di cambio password dell'utente.
     * @param usr Username dell'utente
     * @param pwd Nuova password dell'utente
     * @return Risposta JSON indicante l'esito del cambio password
     * 2000: Ok
     * 4003: Password uguale alla precedente
     * 4004: Utente non presente
     */
    @PostMapping(path="/cambioPwd")
    public @ResponseBody String change_pwd(@RequestParam String usr, @RequestParam String pwd){
        Gson gson = new Gson();
        try{
            Utente u = utenteRepository.findByUsername(usr);    
            if(u == null) 
                throw new UtenteNonEsistenteException();
            String hash = u.getPassword();
            // Adesso bisogna confrontare la password in chiaro con l'hash ottenuto dal Database
            // L'hash è stato fatto usando l'algoritmo bcrypt
            if(BCrypt.checkpw(pwd, hash)) 
                return gson.toJson(new CodiceRisposta(4003)); // Errore di accesso, password uguale a quella di prima
            else{
                u.setPassword(BCrypt.hashpw(pwd, BCrypt.gensalt()));
                utenteRepository.save(u);
                return gson.toJson(new CodiceRisposta(2000)); // Richiesta eseguita
            }
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004));      // Errore di accesso, username non valido
        }
    }
    
    /**
    Cambio Username
     * Gestisce la richiesta di cambio username dell'utente.
     * @param old_usr Vecchio username dell'utente
     * @param new_usr Nuovo username dell'utente
     * @return Risposta JSON indicante l'esito del cambio username
     * 2000: Ok
     * 4004: Utente non presente
     * 4009: Username già esistente
     */
    @PostMapping(path="/cambioUsr")
    public @ResponseBody String change_usr(@RequestParam String old_usr, @RequestParam String new_usr){
        Gson gson = new Gson();
        try{
            Utente u1 = utenteRepository.findByUsername(old_usr);    
            if(u1 == null) 
                throw new UtenteNonEsistenteException();
            
            Utente u2 = utenteRepository.findByUsername(new_usr);  
            if(u2 == null){
                // Trova tutte le letture associate all'utente
                ArrayList<Lettura> letture = letturaRepository.findByID_Utente(u1);
                
                u2 = new Utente(u1);
                u1.setUsername(new_usr);
                utenteRepository.save(u1);
                
                // Aggiorna l'username nelle letture
                for(Lettura lettura: letture){
                    letturaRepository.delete(lettura);
                    lettura.setUtente(u1);
                    letturaRepository.save(lettura);
                }
                
                utenteRepository.delete(u2);
                
                return gson.toJson(new CodiceRisposta(2000)); // Richiesta eseguita
            }
            else
              return gson.toJson(new CodiceRisposta(4009)); // Username già esistente
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004)); // Errore di accesso, username non valido
        }
    }
    
    /**
    Cambio Credenziali per Recupero Password
     * Gestisce la richiesta di cambio della domanda di sicurezza e della risposta di sicurezza dell'utente.
     * @param usr Username dell'utente
     * @param dom Nuova domanda di sicurezza
     * @param ris Nuova risposta di sicurezza
     * @return Risposta JSON indicante l'esito del cambio domanda e risposta di sicurezza
     * 2000: Ok
     * 4003: Credenziali uguali a prima
     * 4004: Utente non presente
     */
    @PostMapping(path="/cambioSec")
    public @ResponseBody String change_security(@RequestParam String usr, @RequestParam String dom, @RequestParam String ris){
        Gson gson = new Gson();
        try{
            Utente u = utenteRepository.findByUsername(usr);    
            if(u == null) 
                throw new UtenteNonEsistenteException();

            if(BCrypt.checkpw(ris, u.getRispostaSicurezza()) && dom.equals(u.getDomandaSicurezza())) 
                return gson.toJson(new CodiceRisposta(4003)); // Errore di accesso, credenziali uguali a prima
            else{
                u.setDomandaSicurezza(dom);
                u.setRispostaSicurezza(BCrypt.hashpw(ris, BCrypt.gensalt()));
                utenteRepository.save(u);
                return gson.toJson(new CodiceRisposta(2000)); // Richiesta eseguita
            }
        } 
        catch(UtenteNonEsistenteException e){
            return gson.toJson(new CodiceRisposta(4004)); // Errore di accesso, username non valido
        }
    }
    
    /**
    Cambio Modalità Lettura dei Manga
     * Gestisce la richiesta di cambio dello stato di lettura dei manga per l'utente.
     * @param usr Username dell'utente
     * @param manga Nuovo stato di lettura del manga
     */
    @PostMapping(path="/mangaLet")
    public @ResponseBody void change_manga_read(@RequestParam String usr, @RequestParam boolean manga){
        Utente u = utenteRepository.findByUsername(usr);    
        if(u != null){
            u.setManga(manga);
            utenteRepository.save(u);
        }
    }
    
    /**
    Ottiene Modalità Lettura dei Manga
     * Ottiene la modalità di lettura dei manga per l'Utente
     * @param usr Username dell'utente
     * @return il booleano che rappresenta la lettura per manga dell'Utente
     */
    @GetMapping(path="/getManga")
    public @ResponseBody Boolean getManga(@RequestParam String usr){
        Utente u = utenteRepository.findByUsername(usr);    
        if(u != null)
            return u.getManga();        
        return false;
    }
}
