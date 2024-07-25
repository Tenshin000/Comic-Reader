package it.unipi.panattoni.server;

/**
 * Eccezione personalizzata per gestire il caso in cui un Fumetto non esista.
 * Viene lanciata quando si cerca di accedere a un fumetto che non è presente nel sistema.
 * 
 * @author Francesco Panattoni
 */

public class FumettoNonEsistenteException extends Exception{
    public FumettoNonEsistenteException(){}

    public FumettoNonEsistenteException(String s){
        super(s);
    }
}
