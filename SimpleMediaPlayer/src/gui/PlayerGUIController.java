/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author hellb
 */
public class PlayerGUIController implements Initializable, RemotePlayer {

    private MediaPlayer mediaPlayer;
    private Duration duration;
    private ArrayList<Path> listSongs;
    private ObservableList observableList;
    private boolean atEndMedia = false;
    private double vol = 100;
    private int indexFocus = 0;
    private static final int PORT = 1099;
    private static final String HOST = "localhost";

    @FXML
    private Button btPlay;
    @FXML
    private Button btOpen;
    @FXML
    private Slider slideTime;
    @FXML
    private Slider slideVolume;
    @FXML
    private Label lbTimeline;
    @FXML
    private ListView lvListSong;
    @FXML
    private Label lbNameSong;

    @FXML
    public void onPlayClick(ActionEvent event) {
        playClick();
    }

    @FXML
    public void onPrevClick(ActionEvent event) {
        prevClick();
    }

    @FXML
    public void onNextClick(ActionEvent event) {
        nextClick();
    }

    @FXML
    public void onOpenFile(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File dir = chooser.showDialog(lvListSong.getScene().getWindow());
            Path path = dir.toPath();
            Files.find(path, Integer.MAX_VALUE, (Path t, BasicFileAttributes u)
                    -> t.getFileName().toString().toLowerCase().endsWith(".mp3"))
                    .forEach(listSongs::add);
            observableList = FXCollections.observableArrayList();

            //TODO cast all element to string
            //(file name) and add all this to observableList
            observableList.addAll(listSongs.parallelStream()
                    .map(e -> e.getFileName().toString())
                    .collect(Collectors.toList()));
            lvListSong.setItems(observableList);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //TODO defind remote here
        try {
            RemotePlayer stub = (RemotePlayer) UnicastRemoteObject
                    .exportObject(this, PORT);
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.bind("RemotePlayer", stub);
            System.out.println("Server already!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void playClick() {
        if (listSongs.isEmpty()) {
            System.out.println("List is empty!");
            return;
        }
        if (mediaPlayer == null) {
            playSong(indexFocus);
            return;
        }

        Status status = mediaPlayer.getStatus();
        if (status == Status.UNKNOWN || status == Status.HALTED) {
            // don't do anything
            return;
        }
        if (status == Status.PAUSED
                || status == Status.READY
                || status == Status.STOPPED) {

            if (atEndMedia) {
                mediaPlayer.seek(mediaPlayer.getStartTime());
                atEndMedia = false;
            }
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
    }

    @Override
    public void prevClick() {
        if (indexFocus > 0) {
            playSong(--indexFocus);
        }
    }

    @Override
    public void nextClick() {
        if (indexFocus < listSongs.size() - 1) {
            playSong(++indexFocus);
        }
    }

    @Override
    public String getNameSong() throws RemoteException {
        return (listSongs.get(indexFocus)).getFileName().toString();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listSongs = new ArrayList<>();

        lvListSong.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() > 1) {
                    indexFocus = lvListSong.getSelectionModel()
                            .getSelectedIndex();
                    playSong(indexFocus);
                }
            }
        });
    }
    

    public void playSong(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.stop();
            System.out.println("Stop current!");
        }

        Path song = listSongs.get(index);
        Media media = new Media(song.toUri().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        lbNameSong.setText(song.getFileName().toString());
        configMedia();
        System.out.println("Playing ... "
                + listSongs.get(indexFocus).getFileName());

    }

    private void configMedia() {
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                btPlay.setText("| |");
                duration = mediaPlayer.getMedia().getDuration();
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                playSong(++indexFocus);
            }
        });

        mediaPlayer.setOnPaused(new Runnable() {
            @Override
            public void run() {
                btPlay.setText(">");
            }
        });

        mediaPlayer.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                btPlay.setText("| |");
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateValues();
            }
        });

        slideTime.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (slideTime.isValueChanging()) {
                    mediaPlayer.seek(duration.multiply(slideTime.getValue() / 100));
                    updateValues();
                }
            }
        });

        mediaPlayer.setVolume(vol);
        slideVolume.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (slideVolume.isValueChanging()) {
                    vol = slideVolume.getValue() / 100;
                    mediaPlayer.setVolume(vol);
                    updateValues();
                }
            }
        });
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) elapsed.toSeconds();
        int intDuration = (int) duration.toSeconds();
        int elapsedMinutes = intElapsed / 60;
        int elapsedSecond = intElapsed - elapsedMinutes * 60;
        int durationMinutes = intDuration / 60;
        int durationSecond = intDuration - durationMinutes * 60;
        return String.format("%02d:%02d/%02d:%02d",
                elapsedMinutes, elapsedSecond, durationMinutes, durationSecond);
    }

    private void updateValues() {
        if (slideTime != null && slideVolume != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    Duration duration = mediaPlayer.getMedia().getDuration();
                    lbTimeline.setText(formatTime(currentTime, duration));

                    if (!slideTime.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !slideTime.isValueChanging()) {
                        slideTime.setValue(currentTime.divide(duration)
                                .toMillis() * 100.0);
                    }

                    if (!slideVolume.isValueChanging()) {
                        slideVolume.setValue(mediaPlayer.getVolume() * 100.0);
                    }
                }
            });
        }
    }

}
