package it.unipi.panattoni.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Classe che rappresenta un fumetto nel sistema.
 * Contiene informazioni come titolo, autori, copertina, data di uscita, sinossi e valutazione.
 * 
 * @author Francesco Panattoni
 */

@Entity
@Table(name="Fumetto", schema="Fumetto")
public class Fumetto implements Serializable{
    // Campi
    @Id
    @Column(name="Path")
    private String path;
    
    @Column(name="Titolo")
    private String titolo;
    @Column(name="Autori")
    private String autori;
    @Column(name="Copertina")
    private String copertina;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @Column(name="DataUscita")
    private Date dataUscita;
    @Column(name="Sinossi")
    private String sinossi;
    @Column(name="Valutazione")
    private Double valutazione;
    @Column(name="Manga")
    private Boolean manga;    
    @Column(name="Locale")
    private Boolean locale;
    
    
    // Metodi
    public Fumetto(){}
    
    public Fumetto(String path, String titolo, String autori, String copertina, Date dataUscita, String sinossi, Double valutazione, Boolean manga, Boolean locale){
        this.path = path;
        this.titolo = titolo;
        this.autori = autori;
        this.copertina = copertina;
        this.dataUscita = dataUscita;
        this.sinossi = sinossi;
        this.valutazione = valutazione;
        this.manga = manga;
        this.locale = locale;
    }
     
    public Fumetto(Fumetto f){
        this.path = f.path;
        this.titolo = f.titolo;
        this.autori = f.autori;
        this.copertina = f.copertina;
        this.dataUscita = f.dataUscita;
        this.sinossi = f.sinossi;
        this.valutazione = f.valutazione;
        this.manga = f.manga;
        this.locale = f.locale;
    }
    
    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }
    
    public String getTitolo(){
        return titolo;
    }

    public void setTitolo(String titolo){
        this.titolo = titolo;
    }

    public String getAutori(){
        return autori;
    }

    public void setAutori(String autori){
        this.autori = autori;
    }

    public String getCopertina(){
        return copertina;
    }

    public void setCopertina(String copertina){
        this.copertina = copertina;
    }

    public Date getDataUscita(){
        return dataUscita;
    }

    public void setDataUscita(Date dataUscita){
        this.dataUscita = dataUscita;
    }

    public String getSinossi(){
        return sinossi;
    }

    public void setSinossi(String sinossi){
        this.sinossi = sinossi;
    }

    public Double getValutazione(){
        return valutazione;
    }

    public void setValutazione(Double valutazione){
        this.valutazione = valutazione;
    }

    public Boolean getManga(){
        return manga;
    }

    public void setManga(Boolean manga){
        this.manga = manga;
    }    
    
    public Boolean getLocale(){
        return locale;
    }

    public void setLocale(Boolean locale){
        this.locale = locale;
    }   
}
