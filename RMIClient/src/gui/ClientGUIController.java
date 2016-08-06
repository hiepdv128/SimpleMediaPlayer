/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author hellb
 */
public class ClientGUIController implements Initializable {
    private RemotePlayer remote;
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    
    @FXML
    private Button btPlay;

    @FXML
    private Button btPrev;

    @FXML
    private Button btNext;

    @FXML
    private Label lbNameSong;

    @FXML
    void onPrevClick(ActionEvent event) throws RemoteException {
        remote.prevClick();
        System.out.println("Prev Click!");
    }

    @FXML
    void onPlayClick(ActionEvent event) throws RemoteException {
        remote.playClick();
        System.out.println("Play Click!");
    }

    @FXML
    void onNextClick(ActionEvent event) throws RemoteException {
        remote.nextClick();
        System.out.println("Next Click!");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO get remote object
        try {
            Registry registry = LocateRegistry.getRegistry(HOST,PORT);
            remote = (RemotePlayer) registry.lookup("RemotePlayer");
            
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    try {
                        lbNameSong.setText(remote.getNameSong());
                    } catch (RemoteException ex) {
                        Logger.getLogger(ClientGUIController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
