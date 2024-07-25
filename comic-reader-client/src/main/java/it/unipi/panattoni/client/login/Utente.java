package it.unipi.panattoni.client.login;

import java.io.Serializable;

/**
 * Classe che rappresenta un Utente nel sistema.
 * @author Francesco Panattoni
 */

public class Utente implements Serializable{
    // Campi
    public String username;
    public String password;
    public String domandaSicurezza;
    public String rispostaSicurezza;
    public Boolean manga; 
    
    // Metodi
    public Utente(){}
    
    public Utente(String username, String password, String domandaSicurezza, String rispostaSicurezza, Boolean manga){
        this.username = username;
        this.password = password;
        this.domandaSicurezza = domandaSicurezza;
        this.rispostaSicurezza = rispostaSicurezza;
        this.manga = manga;
    }
    
    public Utente(String username, String password, String domandaSicurezza, String rispostaSicurezza){
        this(username, password, domandaSicurezza, rispostaSicurezza, false);
    }
}
