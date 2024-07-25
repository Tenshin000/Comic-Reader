package it.unipi.panattoni.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Questa classe rappresenta il punto di ingresso principale dell'applicazione server.
 * Viene utilizzata per avviare l'applicazione Spring Boot.
 * 
 * @author Francesco Panattoni
 */

@SpringBootApplication
@ComponentScan("it.unipi.panattoni.server")
public class ServerApplication{    
    
    public static void main(String[] args){
        System.out.println("Avvio Comic Reader Server");
        
        // Fa Partire il Server
        SpringApplication.run(ServerApplication.class, args);
    }
    
    // Dopo l'avvio del server inserisce i fumetti se non già inseriti
    @Bean
    public CommandLineRunner commandLineRunner(){
        return args -> {
            // Gestisce la creazione dei primi fumetti se non sono già presenti
            GestoreFumetti.gestisci_fumetti_iniziali();
        };
    }
}
