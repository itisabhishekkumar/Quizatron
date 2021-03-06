package me.twodee.quizatron.Console.UIComponent;

import com.jfoenix.controls.JFXSlider;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import me.twodee.quizatron.Presentation.Presentation;
import me.twodee.quizatron.Component.UIComponent;
import me.twodee.quizatron.Presentation.View.MediaPresentationView;

import java.io.File;
import java.io.IOException;

import static java.lang.Math.abs;

/**
 * Media Player component for Quizatron
 *
 * @author Dedipyaman Das <2d@twodee.me>
 * @version 1.0.18.1
 * @since 1.0.18.1
 */
public class Player extends UIComponent
{

    private static final String USER_AGENT_STYLESHEET = QuestionConsole.class
            .getResource("/Stylesheets/media.css")
            .toExternalForm();
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private Presentation presentation;
    private String extension;

    @FXML
    private AnchorPane playerNode;
    @FXML
    private Button playBtn;
    @FXML
    private Label currTimeLbl;
    @FXML
    private Label endTimeLbl;
    @FXML
    private JFXSlider timeSlider;
    @FXML
    private Label mediaInfo;
    @FXML
    private Label sourceFileLbl;
    @FXML
    private BorderPane viewPort;
    /**
     * Media player component constructor.
     *
     * @param presentation Presentation state for separate stage
     */
    public Player(Presentation presentation) throws IOException
    {
        this.presentation = presentation;
        FXMLLoader loader = initFXML("player.fxml");
        loader.load();
    }

    /**
     * Initializer to get the FXML components working.
     */
    public void initialize()
    {
        this.getStylesheets().add(USER_AGENT_STYLESHEET);
        timeSlider.setValue(0);
        prepareMediaView();
    }

    /**
     * Prepare the mediaview window with correct dimensions
     * Binds the mediaview's width and height relative to the window size and video ratio
     *
     * @see MediaPresentationView
     * @see Bindings
     */
    private void prepareMediaView()
    {
        if (mediaView == null) {
            mediaView = new MediaView();
        }
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();

        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
    }

    /**
     * Open the file chooser and autoplay the media
     *
     * @param event ActionEvent
     * @throws Exception thrown on failure to open file
     */
    @FXML
    private void chooseMediaFromFile(ActionEvent event) throws Exception
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        File file = fileChooser.showOpenDialog(playerNode.getScene().getWindow());

        if (file != null) {
            String source = file.toURI().toURL().toExternalForm();
            Media media = new Media(source);

            String fileName = file.getName();

            String fileExtension = getExtension(fileName);
            loadMedia(media, fileExtension);
            playLoadedMedia();
        }
    }

    public String getExtension(String fileName)
    {
        return fileName.substring(fileName.indexOf(".") + 1, fileName.length());
    }

    /**
     * Loads and plays the media source
     *
     * @param media Media - source media to be played
     */
    public void loadMedia(Media media, String extension)
    {
        this.extension = extension;
        initPlayer(media);
        timeSlider.setValue(this.mediaPlayer.getCurrentTime().toSeconds());
        mediaPlayer.setOnReady(this::displayMetaData);
        initTimeSlider();
        initUIControlsBehavior();
    }

    /**
     * Loads the media and instantiates a new media player
     *
     * @param media Media - reusable media component, can be used from anywhere
     */
    private void initPlayer(Media media)
    {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        setMediaPlayer(new MediaPlayer(media));
    }

    /**
     * Fetches and substitutes the placeholders for media metadata
     */
    private void displayMetaData()
    {
        timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
        ObservableMap<String, Object> metaData = mediaPlayer.getMedia().getMetadata();

        String artistName = (String) metaData.get("artist");
        String title = (String) metaData.get("title");
        String album = (String) metaData.get("album");
        String mediaSource = mediaPlayer.getMedia().getSource();

        mediaInfo.setText(title + " - " + artistName + " - " + album);
        try {
            sourceFileLbl.setText(getFileNameFromPath(mediaSource));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        double duration = mediaPlayer.getTotalDuration().toSeconds();
        endTimeLbl.setText(formatTime(duration));
    }

    /**
     * Get the slider running and enable seeking
     * {@link JFXSlider}
     */
    private void initTimeSlider()
    {
        timeSlider.valueProperty()
                  .addListener((observable, oldValue, newValue) ->
                                       sliderSeekBehavior(oldValue, newValue));
        mediaPlayer.currentTimeProperty()
                   .addListener((observable, oldDuration, newDuration) ->
                                        sliderProgressBehavior(oldDuration, newDuration));
        initIndicatorValueProperty();
    }

    /**
     * The slider behavior on dragging/clicking on the JFXSlider to seek
     *
     * @param oldValue Number - before seeking
     * @param newValue Number - after action on slider
     */
    private void sliderSeekBehavior(Number oldValue, Number newValue)
    {
        // Is the change significant enough?
        // Drag was buggy, have to run some tests
        // Affects only the drag it seems
        double tolerance = 1;

        if (mediaPlayer.getTotalDuration().toSeconds() <= 100) {
            tolerance = 0.5;
        }
        if (abs(oldValue.doubleValue() - newValue.doubleValue()) >= tolerance) {
            mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
        }
    }

    /**
     * Behavior of the slider as it progresses
     *
     * @param oldDuration
     * @param newDuration
     */
    private void sliderProgressBehavior(Duration oldDuration, Duration newDuration)
    {
        double newElapsedTime = newDuration.toSeconds();
        double oldElapsedTime = oldDuration.toSeconds();
        double totalDuration = mediaPlayer.getTotalDuration().toSeconds();

        // Making sure it doesn't interfere with the manual seeking
        if (!timeSlider.isValueChanging()) {
            if (newElapsedTime - oldElapsedTime >= 0.1) {
                timeSlider.setValue(newElapsedTime);
            }
            updateTimeLabel(totalDuration, newElapsedTime);
        }
    }

    /**
     * Setting the time elapsed and time left on the appropriate indicators
     */
    private void updateTimeLabel(double totalDuration, double elapsedTime)
    {
        // Get rid of the unnecessary decimal points
        double timeLeft = totalDuration - elapsedTime;

        String elapsedTimeFormatted = formatTime(elapsedTime);
        String remainingTimeFormatted = formatTime(timeLeft);

        currTimeLbl.setText(elapsedTimeFormatted);
        endTimeLbl.setText(remainingTimeFormatted);
    }

    /**
     * Display indicator in HH:MM format
     */
    private void initIndicatorValueProperty()
    {
        timeSlider.setValueFactory(slider ->
                                   Bindings.createStringBinding(
                                           () -> formatTime(slider.getValue()), slider.valueProperty()));
    }

    /**
     * Sets the behavior of the player UI components based on the player state
     */
    private void initUIControlsBehavior()
    {
        mediaPlayer.setOnEndOfMedia(this::stop);

        // Multiline lambdas should be avoided? - Venkat S.
        mediaPlayer.setOnStopped(() -> {
            // Needs to be revisited
            togglePlayPauseBtn(PlayerState.PLAY);
            timeSlider.setValue(0);
        });
        mediaPlayer.setOnPaused(() -> togglePlayPauseBtn(PlayerState.PLAY));
        mediaPlayer.setOnPlaying(() -> togglePlayPauseBtn(PlayerState.PAUSE));
    }

    /**
     * Change the pause button to a loadMedia button and have the appropriate action based on it
     *
     * @param state current state of the player
     * {@link FontAwesomeIconView}
     */
    private void togglePlayPauseBtn(PlayerState state)
    {
        FontAwesomeIconView icon;

        if (state.equals(PlayerState.PLAY)) {
            icon = getIcon(FontAwesomeIcon.PLAY);
            playBtn.setOnAction(this::play);
        }
        else {
            icon = getIcon(FontAwesomeIcon.PAUSE);
            playBtn.setOnAction(this::pause);
        }
        playBtn.setGraphic(icon);
    }

    /**
     * Check if the media player was already there, prepare the presentation and loadMedia the video
     */
    private void playLoadedMedia()
    {
        if (mediaView.getMediaPlayer() != this.mediaPlayer) {
            preparePresentation();
        }
        mediaPlayer.play();
    }

    /**
     * Prepare the external presentation view
     * Get the view controller and embed visual output
     * {@link Presentation}
     */
    private void preparePresentation()
    {
        mediaView.setMediaPlayer(this.mediaPlayer);
        try {
            if (!(presentation.getView() instanceof MediaPresentationView)) {
                presentation.changeView("media-view");
            }

            String format = extension.toLowerCase();

            MediaPresentationView mediaViewController = presentation.getView();

            ImageView fallBackView = getFallBackAudioIcon();
            viewPort.setCenter(fallBackView);
            mediaViewController.showFallBack();

            if (format.equals("mp4") || format.equals("flv")) {
                mediaViewController.embedMediaView(mediaView);
                MediaView mediaViewSec = getMediaViewPort();
                viewPort.setCenter(mediaViewSec);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImageView getFallBackAudioIcon()
    {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/img/audio-icon.gif")));
        imageView.fitWidthProperty().bind(viewPort.widthProperty());
        imageView.fitHeightProperty().bind(viewPort.heightProperty());
        imageView.setPreserveRatio(true);
        return imageView;
    }
    private MediaView getMediaViewPort()
    {
        MediaView mediaViewSec = new MediaView(mediaPlayer);
        mediaViewSec.fitWidthProperty().bind(viewPort.widthProperty());
        mediaViewSec.fitHeightProperty().bind(viewPort.heightProperty());
        mediaViewSec.setPreserveRatio(true);
        return mediaViewSec;
    }
    /**
     * User action to loadMedia the media
     *
     * @param event ActionEvent
     */
    @FXML
    private void play(ActionEvent event)
    {
        playLoadedMedia();
    }

    /**
     * User action event to pause the video
     *
     * @param event
     */
    @FXML
    private void pause(ActionEvent event)
    {
        mediaPlayer.pause();
    }

    /**
     * User action event to stop the media
     */
    public void stop()
    {
        mediaPlayer.stop();
    }

    @FXML
    private void repeat(ActionEvent event)
    {
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }


    public void openPlaylist()
    {

    }

    public void back()
    {

    }

    public void next()
    {

    }

    /**
     * Setter for the class media player
     *
     * @param mediaPlayer MediaPlayer {@link MediaPlayer}
     */
    private void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer;
    }
    // Helper functions

    /**
     * Extracts the filename + extension from the supplied file path
     *
     * @param filePath full file path
     * @return the filename stripped of slashes and everything before
     */
    private String getFileNameFromPath(String filePath) throws Exception
    {
        String cleanPath = java.net.URLDecoder.decode(filePath, "UTF-8");
        return java.nio.file.Paths.get(cleanPath).toFile().getName();
        //return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
    }

    /**
     * Creates a FontAwesomeIconView based on the supplied icon type
     *
     * @param iconType FontAwesome icon to be built
     * @return The final icon with appropriate styles and glyph sizes
     */
    private FontAwesomeIconView getIcon(FontAwesomeIcon iconType)
    {
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setGlyphSize(16);
        icon.setStyleClass("trackBtnIcon");
        return icon;
    }

    /**
     * Formats the time to a MM:SS format as a string
     *
     * @param totalSeconds the time specified in seconds
     * @return the formatted time string
     */
    private String formatTime(double totalSeconds)
    {
        int min = (int) totalSeconds / 60;
        int sec = (int) totalSeconds % 60;

        return String.format("%02d:%02d", min, sec);
    }

    public enum PlayerState
    {
        PLAY, PAUSE
    }
}