package it.unipi.panattoni.server;

import java.util.ArrayList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Interfaccia per il repository delle letture.
 * Estende CrudRepository per fornire le operazioni CRUD di base sul database.
 * 
 * @author Francesco Panattoni
 */

public interface LetturaRepository extends CrudRepository<Lettura, Integer>{
    
    // Trova una Lettura associata a un determinato Utente e Fumetto
    Lettura findByID_UtenteAndID_Fumetto(Utente utente, Fumetto fumetto);
    
    // Trova una lista di Letture associata a un determinato Utente
    ArrayList<Lettura> findByID_Utente(Utente utente);
    
    // Calcola la valutazione media di un fumetto
    @Query("SELECT AVG(l.valutazione) FROM Lettura l WHERE l.ID.fumetto = :fumetto AND l.valutazione IS NOT NULL")
    Double calcolaMediaFumetto(@Param("fumetto") Fumetto fumetto);
    
    // Trova tutti i Fumetti associati a un determinato Utente
    @Query("SELECT DISTINCT l.ID.fumetto FROM Lettura l WHERE l.ID.utente = :utente")
    ArrayList<Fumetto> findAllFumettiByUtente(@Param("utente") Utente utente);
}
