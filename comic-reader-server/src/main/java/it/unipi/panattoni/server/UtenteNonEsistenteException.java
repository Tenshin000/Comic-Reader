package it.unipi.panattoni.server;

/**
 * Eccezione personalizzata per gestire il caso in cui un Utente non esista.
 * Viene lanciata quando si cerca di accedere a un utente che non è presente nel sistema.
 * 
 * @author Francesco Panattoni
 */

public class UtenteNonEsistenteException extends Exception{
    public UtenteNonEsistenteException(){}

    public UtenteNonEsistenteException(String s){
        super(s);
    }
}
