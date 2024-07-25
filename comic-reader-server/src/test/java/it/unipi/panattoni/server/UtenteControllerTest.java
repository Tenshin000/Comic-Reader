package it.unipi.panattoni.server;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test per la classe UtenteController
 * @author Francesco Panattoni
 */

@SpringBootTest
public class UtenteControllerTest{
    // Campi
    @Autowired
    private UtenteController uc;
    @Autowired
    private UtenteRepository ur;
    
    // Metodi  
    @Test
    public void test_signup_ok(){
        System.out.println("Test per la registrazione corretta");
        Gson gson = new Gson();
        
        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        
        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = signup(u);
        
        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_signup_not(){
        System.out.println("Test per la registrazione con username già presente");
        Gson gson = new Gson();
        
        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);
        
        String aspettativa = gson.toJson(new CodiceRisposta(4009));
        String realta = uc.registrazione(u);
        
        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_login_ok(){
        System.out.println("Test per il login con credenziali giuste");
        Gson gson = new Gson();
        
        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);
        
        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = uc.login(username, password);
        
        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_login_not(){
        System.out.println("Test per il login con credenziali errata");
        Gson gson = new Gson();
        
        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);
        
        String aspettativa = gson.toJson(new CodiceRisposta(4003));
        String realta = uc.login(username, "Fumetti0_0");
        
        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_forgot_password_ok() {
        System.out.println("Test per il recupero password con credenziali corrette");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = uc.forgot_password(username, dom, ris);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }

    @Test
    public void test_forgot_password_not() {
        System.out.println("Test per il recupero password con credenziali errate");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(4003));
        String realta = uc.forgot_password(username, dom, "Cody");

        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_change_pwd_ok() {
        System.out.println("Test per il cambio password con password diversa dalla precedente");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String nuovaPassword = "NuovaPassword123";
        
        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = uc.change_pwd(username, nuovaPassword);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }

    @Test
    public void test_change_pwd_not() {
        System.out.println("Test per il cambio password con password uguale alla precedente");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(4003));
        String realta = uc.change_pwd(username, password);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_change_usr_ok() {
        System.out.println("Test per il cambio username con username non esistente");
        Gson gson = new Gson();

        String oldUsername = "Pana";
        String newUsername = "NuovoPana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";
        boolean manga = false;
        
        Utente u = new Utente(oldUsername, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = uc.change_usr(oldUsername, newUsername);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }

    @Test
    public void test_change_usr_not() {
        System.out.println("Test per il cambio username con username già esistente");
        Gson gson = new Gson();

        String oldUsername = "Pana";
        String newUsername = "NuovoPana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";

        Utente u1 = new Utente(oldUsername, password, dom, ris, false);
        signup(u1);

        Utente u2 = new Utente(newUsername, password, dom, ris, true);
        signup(u2);

        String aspettativa = gson.toJson(new CodiceRisposta(4009));
        String realta = uc.change_usr(oldUsername, newUsername);

        remove_utente(u1);
        remove_utente(u2);
        assert aspettativa.equals(realta);
    }

    @Test
    public void test_change_security_ok() {
        System.out.println("Test per il cambio credenziali di sicurezza con credenziali diverse");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        boolean manga = false;
        
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";        
        String newdom= "In quale città sei nato?";
        String newris = "Pescia";

        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(2000));
        String realta = uc.change_security(username, newdom, newris);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }

    @Test
    public void test_change_security_not() {
        System.out.println("Test per il cambio credenziali di sicurezza con credenziali uguali");
        Gson gson = new Gson();

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";  
        boolean manga = false;

        Utente u = new Utente(username, password, dom, ris, manga);
        signup(u);

        String aspettativa = gson.toJson(new CodiceRisposta(4003));
        String realta = uc.change_security(username, dom, ris);

        remove_utente(u);
        assert aspettativa.equals(realta);
    }
    
    @Test
    public void test_manga(){
        System.out.println("Test per il cambio del flag manga");

        String username = "Pana";
        String password = "Fumetti000";
        String dom = "Qual è il nome del tuo primo animale domestico?";
        String ris = "Buddy";  
        
        Utente u = new Utente(username, password, dom, ris, false);
        signup(u);
        
        uc.change_manga_read(username, true);
        u = ur.findByUsername(username);
                
        assert u.getManga() == true;
        assert uc.getManga(username) == true;
        
        remove_utente(u);
    }
    
    private void remove_utente(Utente u){
        ur.delete(u);
    }
    
    private String signup(Utente u){        
        return uc.registrazione(u);
    }
}
