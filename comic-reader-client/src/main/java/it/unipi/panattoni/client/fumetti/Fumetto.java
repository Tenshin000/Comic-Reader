package it.unipi.panattoni.client.fumetti;

import java.io.Serializable;

/**
 * Classe che rappresenta un fumetto nel sistema.
 * @author Francesco Panattoni
 */

public class Fumetto implements Serializable{
    // Campi
    public String path;
    public String titolo;
    public String autori;
    public String copertina;
    public String dataUscita;
    public String sinossi;
    public Double valutazione;
    public Boolean manga;    
    public Boolean locale;
    
    // Metodi
    public Fumetto(){}
    
    public Fumetto(String path, String titolo, String autori, String copertina, String dataUscita, String sinossi, Double valutazione, Boolean manga, Boolean locale){
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
}
