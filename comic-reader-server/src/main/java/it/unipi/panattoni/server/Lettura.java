package it.unipi.panattoni.server;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Classe che rappresenta i fumetti letti nel sistema. 
 * Contiene informazioni come Utente e Fumetto nella chiave ID, a che capitolo è la valutazione fornita. 
 * 
 * @author Francesco Panattoni
 */

@Entity
@Table(name = "Lettura", schema = "Lettura")
public class Lettura implements Serializable{
    // Campi
    @EmbeddedId
    LetturaID ID;
    
    @Column(name="Capitolo")
    /* 
     Capitolo deve essere maggiore o uguale a 0. Se è 0 significa che è un fumetto che l'utente desidera leggere in 
     futuro.  
    */
    private Integer capitolo;
    
    // Valutazione è il voto che l'utente ha dato al fumetto. Può essere null. 
    @Column(name="Valutazione")
    private Integer valutazione;
    
    // Metodi
    public Lettura(){}
    
    public Lettura(Utente utente, Fumetto fumetto, Integer capitolo, Integer valutazione){
        if(capitolo < 0)
            throw new IllegalArgumentException();
        ID = new LetturaID(utente, fumetto);
        this.capitolo = capitolo;
        this.valutazione = valutazione;
    }
    
    public Lettura(Utente utente, Fumetto fumetto){
        this(utente, fumetto, 0, null);
    }

    public Utente getUtente(){
        return ID.getUtente();
    }

    public void setUtente(Utente utente){
        ID.setUtente(utente);
    }

    public Fumetto getFumetto(){
        return ID.getFumetto();
    }

    public void setFumetto(Fumetto fumetto){
        ID.setFumetto(fumetto);
    }
    
    public Integer getCapitolo(){
        return capitolo;
    }

    public void setCapitolo(Integer capitolo){
        this.capitolo = capitolo;
    }
    
    public Integer getValutazione(){
        return valutazione;
    }
    
    public void setValutazione(Integer valutazione){
        this.valutazione = valutazione;
    }
}