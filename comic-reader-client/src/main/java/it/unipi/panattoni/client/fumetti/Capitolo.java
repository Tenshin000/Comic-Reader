package it.unipi.panattoni.client.fumetti;

import java.io.Serializable;

/**
 * Rappresenta un capitolo di un fumetto.
 * @author Francesco Panattoni
 */

public class Capitolo implements Serializable{
    // Campi
    public Fumetto fumetto;
    public Integer numero;
    public String nome;
    public String descrizione;
    public String formato;
    public Integer pagine;
    
    // Metodi
    public Capitolo(){}
    
    public Capitolo(Fumetto fumetto, Integer numero, String nome, String descrizione, String formato, Integer pagine){
        this.fumetto = fumetto;
        this.numero = numero;
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
        fumetto = c.fumetto;
        numero = c.numero;
        nome = c.nome;
        descrizione = c.descrizione;
        formato = c.formato;
        pagine = c.pagine;
    }
}
