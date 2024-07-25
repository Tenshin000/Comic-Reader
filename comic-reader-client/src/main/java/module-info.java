module it.unipi.panattoni.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.desktop;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires com.google.gson;

    exports it.unipi.panattoni.client;
    exports it.unipi.panattoni.client.account;
    exports it.unipi.panattoni.client.fumetti;
    exports it.unipi.panattoni.client.login;    
    
    opens it.unipi.panattoni.client to javafx.fxml;
    opens it.unipi.panattoni.client.account to javafx.fxml;
    opens it.unipi.panattoni.client.fumetti to javafx.fxml;
    opens it.unipi.panattoni.client.login to javafx.fxml;    
}
