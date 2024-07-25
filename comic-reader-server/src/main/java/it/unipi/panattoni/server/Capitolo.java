package it.unipi.panattoni.server;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Rappresenta un capitolo di un fumetto.
 * Ogni capitolo è identificato univocamente da un numero all'interno del fumetto.
 * 
 * @author Francesco Panattoni
 */

@Entity
@Table(name = "Capitolo", schema = "Capitolo")
public class Capitolo implements Serializable{
    // Campi
    @EmbeddedId
    CapitoloID ID;
    
    @Column(name="Nome")
    private String nome;
    @Column(name="Descrizione")
    private String descrizione;
    @Column(name="Formato")
    private String formato;
    @Column(name="Pagine")
    private Integer pagine;
    
    // Metodi
    public Capitolo(){}
    
    public Capitolo(Fumetto fumetto, Integer numero, String nome, String descrizione, String formato, Integer pagine){
        this.ID = new CapitoloID(fumetto, numero);
        this.nome = nome;
        this.descrizione = descrizione;
        if(formato.equals("jpg") || formato.equals("jpeg") || formato.equals("png"))
            this.formato = formato;
        else
            this.formato = "jpg";
        this.pagine = pagine;
    }
    
    public Capitolo(Fumetto fumetto, Integer numero){
        this(fumetto, numero, "", "", "jpg", 1);
    }
    
    public Capitolo(Fumetto fumetto, Integer numero, Integer pagine){
        this(fumetto, numero, "", "", "jpg", pagine);
    }

    public Capitolo(Capitolo c){
        ID = c.ID;
        nome = c.nome;
        descrizione = c.descrizione;
        formato = c.formato;
        pagine = c.pagine;
    }
    
    public CapitoloID getID(){
        return ID;
    }
    
    public void setID(CapitoloID ID){
        this.ID = ID;
    }
    
    public Fumetto getFumetto(){
        return ID.getFumetto();
    }

    public void setFumetto(Fumetto fumetto){
        ID.setFumetto(fumetto);
    }

    public Integer getNumero(){
        return ID.getNumero();
    }

    public void setNumero(Integer numero){
        ID.setNumero(numero);
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public String getDescrizione(){
        return descrizione;
    }

    public void setDescrizione(String descrizione){
        this.descrizione = descrizione;
    }
    
    public String getFormato(){
        return formato;
    }

    public void setFormato(String formato){
        if(formato.equals("jpg") || formato.equals("jpeg") || formato.equals("png"))
            this.formato = formato;
    }
    
    public Integer getPagine(){
        return pagine;
    }

    public void setPagine(Integer pagine){
        this.pagine = pagine;
    }
}
