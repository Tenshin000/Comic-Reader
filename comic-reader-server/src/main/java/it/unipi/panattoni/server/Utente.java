package it.unipi.panattoni.server;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Classe che rappresenta un Utente nel sistema.
 * Contiene informazioni come username, password, domanda e risposta di sicurezza.
 * 
 * @author Francesco Panattoni
 */

@Entity
@Table(name="Utente", schema="Utente")
public class Utente implements Serializable{
    // Campi
    @Id
    @Column(name="Username")
    private String username;
    
    @Column(name="Password")
    private String password;
    @Column(name="DomandaSicurezza")
    private String domandaSicurezza;
    @Column(name="RispostaSicurezza")
    private String rispostaSicurezza;
    @Column(name="Manga")
    private Boolean manga;
    
    // Metodi
    public Utente(){}
    
    public Utente(String username, String password, String domandaSicurezza, String rispostaSicurezza, boolean manga){
        this.username = username;
        this.password = password;
        this.domandaSicurezza = domandaSicurezza;
        this.rispostaSicurezza = rispostaSicurezza;
        this.manga = manga;
    }
    
    public Utente(String username, String password, String domandaSicurezza, String rispostaSicurezza){
        this(username, password, domandaSicurezza, rispostaSicurezza, false);
    }
    
    public Utente(Utente u){
        this.username = u.username;
        this.password = u.password;
        this.domandaSicurezza = u.domandaSicurezza;
        this.rispostaSicurezza = u.rispostaSicurezza;
        this.manga = u.manga;
    }
  
    public String getUsername(){
        return username;
    }
    
    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }
    
    public String getDomandaSicurezza(){
        return domandaSicurezza;
    }
    
    public void setDomandaSicurezza(String domandaSicurezza){
        this.domandaSicurezza = domandaSicurezza;
    }
    
    public String getRispostaSicurezza(){
        return rispostaSicurezza;
    }
    
    public void setRispostaSicurezza(String rispostaSicurezza){
        this.rispostaSicurezza = rispostaSicurezza;
    }
    
    public boolean getManga(){
        return manga;
    }
    
    public void setManga(boolean manga){
        this.manga = manga;
    }
}
