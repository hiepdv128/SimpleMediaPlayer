package gui;


import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hellb
 */
public interface RemotePlayer extends Remote{
    
    public void playClick() throws RemoteException;

    public void prevClick() throws RemoteException;

    public void nextClick() throws RemoteException;
    
    public String getNameSong() throws RemoteException;
}
