package it.unipi.panattoni.server;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Rappresenta l'identificatore di un capitolo. 
 * È composto dal fumetto e dal numero del capitolo all'interno del fumetto.
 * 
 * @author Francesco Panattoni
 */

@Embeddable
public class CapitoloID implements Serializable{
    // Campi 
    @ManyToOne
    @JoinColumn(name = "Fumetto")
    private Fumetto fumetto;
    
    @Column(name="Numero")
    private Integer numero;
    
    // Metodi
    public CapitoloID(){}
    
    public CapitoloID(Fumetto fumetto, Integer numero){
        this.fumetto = fumetto;
        this.numero = numero;
    }
    
    public Fumetto getFumetto(){
        return fumetto;
    }    

    public void setFumetto(Fumetto fumetto){
        this.fumetto = fumetto;
    }
    
    public Integer getNumero(){
        return numero;
    }

    public void setNumero(Integer numero){
        this.numero = numero;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) 
            return true;
        if(o == null || getClass() != o.getClass()) 
            return false;

        CapitoloID other = (CapitoloID) o;

        // Implementazione specifica della logica di uguaglianza
        if(this.fumetto.getPath().equals(other.fumetto.getPath()) && this.numero.equals(other.numero))
            return true;
        
        return false;
    }

    @Override
    public int hashCode() {
        // Implementazione specifica del calcolo dell'hash
        return Objects.hash(this.fumetto.getPath(), this.numero);
    }
}
