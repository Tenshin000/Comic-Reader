package it.unipi.panattoni.server;

import java.util.ArrayList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Questa interfaccia rappresenta il repository per i capitoli dei fumetti.
 * Estende CrudRepository, fornendo metodi per eseguire operazioni CRUD sui capitoli.
 * 
 * @author Francesco Panattoni
 */

@Repository
public interface CapitoloRepository extends CrudRepository<Capitolo, Integer>{
    
    // Trova un capitolo dato il suo ID.
    public Capitolo findByID(CapitoloID ID);
    
    // Query per cercare fumetti in base a un testo di ricerca, solo se il fumetto è inserito localmente
    @Query(value = "SELECT * FROM Capitolo C WHERE C.Fumetto LIKE %:p% ORDER BY C.Numero", nativeQuery = true)
    public ArrayList<Capitolo> findByPath(@Param("p") String p);
    
    // Query per contare i capitoli di un fumetto
    @Query(value = "SELECT COUNT(*) AS Capitoli FROM Capitolo C WHERE C.Fumetto = :p", nativeQuery = true)
    public Integer countCapitoli(@Param("p") String p);
}
