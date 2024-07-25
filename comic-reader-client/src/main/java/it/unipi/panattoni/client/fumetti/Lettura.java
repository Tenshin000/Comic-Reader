package it.unipi.panattoni.client.fumetti;

import it.unipi.panattoni.client.login.Utente;

import java.io.Serializable;

/**
 * Classe che serve per recuperare informazioni riguardo alla lettura di un fumetto
 * @author Francesco Panattoni
 */

public class Lettura implements Serializable{
    public Utente utente;
    public Fumetto fumetto;
    public Integer capitolo;
    public Integer valutazione;
    
    public Lettura(){}
    
    public Lettura(Utente utente, Fumetto fumetto, Integer capitolo, Integer valutazione){
        if(capitolo < 0)
            throw new IllegalArgumentException();
        
        this.utente = utente;
        this.fumetto = fumetto;
        this.capitolo = capitolo;
        this.valutazione = valutazione;
    }
    
    public Lettura(Utente utente, Fumetto fumetto){
        this(utente, fumetto, 0, null);
    }
}
