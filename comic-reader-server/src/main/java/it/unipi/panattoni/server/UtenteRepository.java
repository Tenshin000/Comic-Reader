package it.unipi.panattoni.server;

import org.springframework.data.repository.CrudRepository;

/**
 * Interfaccia per il repository degli utenti.
 * Estende CrudRepository per fornire le operazioni CRUD di base sul database.
 * 
 * @author Francesco Panattoni
 */

public interface UtenteRepository extends CrudRepository<Utente, Integer>{
    
    // Trova l'Utente dal suo username
    public Utente findByUsername(String user);
}
