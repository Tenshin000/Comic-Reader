package it.unipi.panattoni.server;

/**
 * Eccezione personalizzata per gestire il caso in cui un Capitolo di un Fumetto non esista.
 * Viene lanciata quando si cerca di accedere a un capitolo che non è presente nel sistema.
 * 
 * @author Francesco Panattoni
 */

public class CapitoloNonEsistenteException extends Exception{
    public CapitoloNonEsistenteException(){}

    public CapitoloNonEsistenteException(String s){
        super(s);
    }
}
