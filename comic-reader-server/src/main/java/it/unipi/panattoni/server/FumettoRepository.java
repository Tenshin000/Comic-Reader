package it.unipi.panattoni.server;

import java.util.ArrayList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Interfaccia per il repository dei fumetti.
 * Estende CrudRepository per fornire le operazioni CRUD di base sul database.
 * 
 * @author Francesco Panattoni
 */

@Repository
public interface FumettoRepository extends CrudRepository<Fumetto, Integer>{
    
    // Trova il Fumetto dal suo Path.
    public Fumetto findByPath(String path);
    
    // Trova il Fumetto dalla sua Copertina.
    public Fumetto findByCopertina(String copertina);
    
    // Query per trovare il numero di capitoli che ha un Fumetto
    @Query(value = "SELECT COUNT(*) AS Capitoli FROM Fumetto F INNER JOIN Capitolo C ON F.Path = C.Fumetto WHERE F.Path = :path", nativeQuery = true)
    public Integer countChapters(@Param("path") String path);
    
    // Query per cercare fumetti in base a un testo di ricerca
    @Query(value = "SELECT * FROM Fumetto F WHERE F.titolo LIKE %:testo% OR F.autori LIKE %:testo% LIMIT 15", nativeQuery = true)
    public ArrayList<Fumetto> findByString(@Param("testo") String testo);
    
    // Query per cercare fumetti in base a un testo di ricerca, solo se il fumetto è inserito localmente
    @Query(value = "SELECT * FROM Fumetto F WHERE F.titolo LIKE %:testo% AND F.Locale IS TRUE LIMIT 15", nativeQuery = true)
    public ArrayList<Fumetto> findByLocal(@Param("testo") String testo);
}
