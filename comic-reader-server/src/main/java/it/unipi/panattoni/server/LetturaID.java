package it.unipi.panattoni.server;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Classe che rappresenta l'identificatore composto per una lettura.
 * Contiene l'utente e il fumetto letto dall'utente.
 * 
 * @author Francesco Panattoni
 */

@Embeddable
public class LetturaID implements Serializable{
    // Campi 
    @ManyToOne
    @JoinColumn(name = "Utente")
    private Utente utente;
    
    @ManyToOne
    @JoinColumn(name = "Fumetto")
    private Fumetto fumetto;
    
    // Metodi
    public LetturaID(){}
    
    public LetturaID(Utente utente, Fumetto fumetto){
        this.utente = utente;
        this.fumetto = fumetto;
    }
    
    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Fumetto getFumetto() {
        return fumetto;
    }

    public void setFumetto(Fumetto fumetto) {
        this.fumetto = fumetto;
    }
    
    @Override
    public boolean equals(Object o) {
        if(this == o) 
            return true;
        if(o == null || getClass() != o.getClass()) 
            return false;

        LetturaID other = (LetturaID) o;

        // Implementazione specifica della logica di uguaglianza
        if(this.utente.getUsername().equals(other.utente.getUsername()) && this.fumetto.getPath().equals(other.fumetto.getPath()))
            return true;
        
        return false;
    }

    @Override
    public int hashCode() {
        // Implementazione specifica del calcolo dell'hash
        return Objects.hash(this.utente.getUsername(), this.fumetto.getTitolo());
    }
}
