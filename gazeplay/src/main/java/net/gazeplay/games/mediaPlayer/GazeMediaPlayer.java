package net.gazeplay.games.mediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class GazeMediaPlayer extends Parent implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;

    private Button[] titre;
    private Button left, playPause, right, fullScreen, addVideo, upArrow, downArrow;
    private BorderPane videoRoot;
    private HBox window, tools;
    private VBox scrollList, videoSide;
    private Text musicTitle;
    private boolean full = false;
    private boolean play = false;
    private MediaFileReader musicList;

    List<EventHandler<Event>> eventTitre;

    @Override
    public void launch() {
        createHandlers();
        createUpDownHandlers();
        createLeftRightHandlers();
    }

    @Override
    public void dispose() {
        this.stopMedia();

    }

    public GazeMediaPlayer(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        eventTitre = new ArrayList<EventHandler<Event>>();

        EventHandler<Event> empty = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

            }
        };
        eventTitre.add(empty);
        eventTitre.add(empty);
        eventTitre.add(empty);

        musicList = new MediaFileReader();

        window = new HBox();
        gameContext.getGazeDeviceManager().addEventFilter(gameContext.getRoot());

        scrollList = new VBox();

        titre = new Button[3];

        upArrow = new Button("^");
        upArrow.setPrefWidth(dimension2D.getWidth() / 5);
        upArrow.setPrefHeight(dimension2D.getHeight() / 7);
        upArrow.setStyle("-fx-background-radius: 5em; ");

        for (int i = 0; i <= 2; i++) {
            titre[i] = new Button();
            titre[i].setPrefWidth(dimension2D.getWidth() / 4);
            titre[i].setPrefHeight(dimension2D.getHeight() / 7);
            putMusic(i, true);
        }

        downArrow = new Button("v");
        downArrow.setPrefWidth(dimension2D.getWidth() / 5);
        downArrow.setPrefHeight(dimension2D.getHeight() / 7);
        downArrow.setStyle("-fx-background-radius: 5em; ");

        scrollList.setSpacing(dimension2D.getHeight() / 30);
        scrollList.setAlignment(Pos.CENTER);
        scrollList.getChildren().addAll(upArrow, titre[0], titre[1], titre[2], downArrow);

        videoSide = new VBox();

        addVideo = new Button("+");
        addVideo.setPrefWidth(dimension2D.getWidth() / 6);
        addVideo.setPrefHeight(dimension2D.getHeight() / 8);

        videoRoot = new BorderPane();

        ImageView video = new ImageView();
        video.resize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

        StackPane videoStack = new StackPane();

        Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
        r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));

        videoStack.getChildren().addAll(r, video);
        video.toFront();

        BorderPane.setAlignment(videoStack, Pos.CENTER);
        videoRoot.setCenter(videoStack);

        musicTitle = new Text();
        musicTitle.setFill(Color.WHITE);

        tools = new HBox();

        left = new Button();
        left.setPrefWidth(dimension2D.getWidth() / 12);
        left.setPrefHeight(dimension2D.getHeight() / 8);
        ImageView leftIv = new ImageView(new Image("data/gazeMediaPlayer/prev.png"));
        leftIv.setPreserveRatio(true);
        leftIv.setFitHeight((90 * left.getHeight()) / 100);
        left.setGraphic(leftIv);
        playPause = new Button();
        playPause.setPrefWidth(dimension2D.getWidth() / 12);
        playPause.setPrefHeight(dimension2D.getHeight() / 8);
        ImageView playPauseIv = new ImageView(new Image("data/gazeMediaPlayer/playPause.png"));
        playPauseIv.setPreserveRatio(true);
        playPauseIv.setFitHeight((90 * playPause.getHeight()) / 100);
        playPause.setGraphic(playPauseIv);
        right = new Button();
        right.setPrefWidth(dimension2D.getWidth() / 12);
        right.setPrefHeight(dimension2D.getHeight() / 8);
        ImageView rightIv = new ImageView(new Image("data/gazeMediaPlayer/next.png"));
        rightIv.setPreserveRatio(true);
        rightIv.setFitHeight((90 * right.getHeight()) / 100);
        right.setGraphic(rightIv);
        fullScreen = new Button();
        fullScreen.setPrefWidth(dimension2D.getWidth() / 12);
        fullScreen.setPrefHeight(dimension2D.getHeight() / 8);
        ImageView ScreenIv = new ImageView(new Image("data/gazeMediaPlayer/fullon.png"));
        ScreenIv.setPreserveRatio(true);
        ScreenIv.setFitHeight((90 * fullScreen.getHeight()) / 100);
        fullScreen.setGraphic(ScreenIv);

        tools.setSpacing(dimension2D.getWidth() / 20);
        tools.setAlignment(Pos.CENTER);
        tools.getChildren().addAll(left, playPause, right, fullScreen);

        videoSide.setSpacing(dimension2D.getHeight() / 30);
        videoSide.setAlignment(Pos.CENTER);
        videoSide.getChildren().addAll(addVideo, videoRoot, musicTitle, tools);

        window.setSpacing(dimension2D.getWidth() / 15);
        window.setAlignment(Pos.CENTER);
        window.getChildren().addAll(scrollList, videoSide);

        window.setLayoutX(dimension2D.getWidth() / 8);
        window.setLayoutY(dimension2D.getHeight() / 12);

        this.gameContext.getChildren().add(window);

    }

    public void createHandlers() {
        EventHandler<Event> eventFull = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                fullScreenCheck();

            }
        };
        fullScreen.addEventFilter(MouseEvent.MOUSE_CLICKED, eventFull);
        fullScreen.addEventFilter(GazeEvent.GAZE_ENTERED, eventFull);

        EventHandler<Event> eventPlayPause = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
                    MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
                    if (play) {
                        mediaView.getMediaPlayer().pause();
                    } else {
                        mediaView.getMediaPlayer().play();
                    }
                    play = !play;
                }
            }
        };

        playPause.addEventFilter(MouseEvent.MOUSE_CLICKED, eventPlayPause);
        playPause.addEventFilter(GazeEvent.GAZE_ENTERED, eventPlayPause);

        EventHandler<Event> eventAddVideo = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                Stage dialog = createDialog(gameContext.getGazePlay().getPrimaryStage());
                dialog.setTitle("new Title");
                dialog.show();
                dialog.toFront();
                dialog.setAlwaysOnTop(true);
            }
        };

        addVideo.addEventFilter(MouseEvent.MOUSE_CLICKED, eventAddVideo);

    }

    public void stopMedia() {
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.getMediaPlayer().stop();
        }
        ((StackPane) videoRoot.getCenter()).getChildren().set(1, new ImageView());
    }

    public void createLeftRightHandlers() {
        EventHandler<Event> eventLeft = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                stopMedia();
                playMusic(true);

            }
        };
        left.addEventFilter(MouseEvent.MOUSE_CLICKED, eventLeft);
        left.addEventFilter(GazeEvent.GAZE_ENTERED, eventLeft);

        EventHandler<Event> eventRight = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                stopMedia();
                playMusic(false);

            }
        };

        right.addEventFilter(MouseEvent.MOUSE_CLICKED, eventRight);
        right.addEventFilter(GazeEvent.GAZE_ENTERED, eventRight);
    }

    private void playMusic(boolean next) {
        MediaFile mf;
        if (next) {
            mf = musicList.nextPlayed();
        } else {
            mf = musicList.prevPlayed();
        }

        if (mf != null && mf.getType().equals("URL")) {

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            String videoUrl = mf.getPath();
            WebView webview = new WebView();
            webview.getEngine().load(videoUrl);
            play = true;

            if (full) {
                BorderPane bp = (BorderPane) videoSide.getParent();
                bp.setLayoutY(0);
                webview.setPrefSize(dimension2D.getWidth(), (7 * dimension2D.getHeight()) / 8); // 360p
            } else {
                webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p
            }

            BorderPane.setAlignment(webview, Pos.CENTER);
            ((StackPane) videoRoot.getCenter()).getChildren().set(1, webview);

        } else if (mf != null && mf.getType().equals("MEDIA")) {
            stopMedia();

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            File file = new File(mf.getPath());
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            MediaView mediaView = new MediaView(player);

            if (full) {
                mediaView.setFitWidth(dimension2D.getWidth());
                mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);

                Rectangle r = new Rectangle(0, 0, (7 * dimension2D.getHeight()) / 8, (7 * dimension2D.getHeight()) / 8);
                r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
                ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

                gameContext.getChildren().clear();
                videoSide.setSpacing(0);
                videoSide.getChildren().remove(addVideo);
                BorderPane bp = new BorderPane();
                bp.setCenter(videoSide);
                /*
                 * double offset = (mediaView.getFitHeight() == 0) ? (7 * dimension2D.getHeight()) / 8 :
                 * mediaView.getFitHeight();
                 * 
                 * double x = (dimension2D.getHeight() - (offset + left.getHeight()));
                 */
                bp.setLayoutY(0);
                gameContext.getChildren().add(bp);
            } else {
                mediaView.setFitHeight(dimension2D.getHeight() / 2);
                mediaView.setFitWidth(dimension2D.getWidth() / 3);
                Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
                r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
                ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);
            }

            BorderPane.setAlignment(mediaView, Pos.CENTER);

            ((StackPane) videoRoot.getCenter()).getChildren().set(1, mediaView);
            player.play();

        }

        musicTitle.setText(mf.getName());

    }

    private void createUpDownHandlers() {

        EventHandler<Event> eventDownArrow = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                EventHandler<Event> temp0 = eventTitre.get(0);
                EventHandler<Event> temp1 = eventTitre.get(1);
                EventHandler<Event> temp2 = eventTitre.get(2);

                titre[0].removeEventFilter(MouseEvent.MOUSE_CLICKED, temp0);
                titre[1].removeEventFilter(MouseEvent.MOUSE_CLICKED, temp1);
                titre[0].removeEventFilter(GazeEvent.GAZE_ENTERED, temp0);
                titre[1].removeEventFilter(GazeEvent.GAZE_ENTERED, temp1);
                eventTitre.set(0, temp1);
                eventTitre.set(1, temp2);

                titre[0].setText(titre[1].getText());
                Node g1 = titre[1].getGraphic();
                titre[1].setGraphic(null);
                titre[0].setGraphic(g1);
                titre[1].setText(titre[2].getText());
                Node g2 = titre[2].getGraphic();
                titre[2].setGraphic(null);
                titre[1].setGraphic(g2);

                titre[0].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre.get(0));
                titre[1].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre.get(1));
                titre[0].addEventFilter(GazeEvent.GAZE_ENTERED, eventTitre.get(0));
                titre[1].addEventFilter(GazeEvent.GAZE_ENTERED, eventTitre.get(1));

                putMusic(2, true);

            }
        };

        downArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, eventDownArrow);
        downArrow.addEventFilter(GazeEvent.GAZE_ENTERED, eventDownArrow);

        EventHandler<Event> eventUpArrow = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                EventHandler<Event> temp0 = eventTitre.get(0);
                EventHandler<Event> temp1 = eventTitre.get(1);
                EventHandler<Event> temp2 = eventTitre.get(2);

                titre[1].removeEventFilter(MouseEvent.MOUSE_CLICKED, temp1);
                titre[2].removeEventFilter(MouseEvent.MOUSE_CLICKED, temp2);
                titre[1].removeEventFilter(GazeEvent.GAZE_ENTERED, temp1);
                titre[2].removeEventFilter(GazeEvent.GAZE_ENTERED, temp2);
                eventTitre.set(1, temp0);
                eventTitre.set(2, temp1);

                titre[2].setText(titre[1].getText());
                Node g1 = titre[1].getGraphic();
                titre[1].setGraphic(null);
                titre[2].setGraphic(g1);
                titre[1].setText(titre[0].getText());
                Node g0 = titre[0].getGraphic();
                titre[0].setGraphic(null);
                titre[1].setGraphic(g0);

                titre[2].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre.get(2));
                titre[1].addEventFilter(MouseEvent.MOUSE_CLICKED, eventTitre.get(1));
                titre[2].addEventFilter(GazeEvent.GAZE_ENTERED, eventTitre.get(2));
                titre[1].addEventFilter(GazeEvent.GAZE_ENTERED, eventTitre.get(1));
                putMusic(0, false);

            }
        };

        upArrow.addEventFilter(MouseEvent.MOUSE_CLICKED, eventUpArrow);
        upArrow.addEventFilter(GazeEvent.GAZE_ENTERED, eventUpArrow);

    }

    private Stage createDialog(Stage primaryStage) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
        });

        VBox choicePane = new VBox();
        choicePane.setSpacing(50);
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        HBox sides = new HBox();
        sides.setSpacing(50);
        sides.setAlignment(Pos.CENTER);

        Text titleText = new Text("Title");

        TextField title = new TextField();
        title.setPromptText("enter the title of the media");
        title.setMaxWidth(primaryStage.getWidth() / 5);

        Button tfi = new Button(gameContext.getGazePlay().getTranslator().translate("ChooseImage"));
        tfi.getStyleClass().add("gameChooserButton");
        tfi.getStyleClass().add("gameVariation");
        tfi.getStyleClass().add("button");
        tfi.setMinHeight(primaryStage.getHeight() / 20);
        tfi.setMinWidth(primaryStage.getWidth() / 10);

        EventHandler<Event> chooseImageHandler = new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                String s = getImage(tfi, dialog);
                if (s != null) {
                    tfi.setText(s);
                }
            }
        };

        tfi.addEventFilter(MouseEvent.MOUSE_CLICKED, chooseImageHandler);

        // URL BLOCK ___
        VBox urlSide = new VBox();
        urlSide.setSpacing(10);

        HBox urlField = new HBox();
        urlField.setAlignment(Pos.CENTER);
        TextField tf = new TextField();
        tf.setPromptText("enter a web URL");
        tf.setMaxWidth(primaryStage.getWidth() / 10);
        urlField.getChildren().add(tf);

        Button buttonURL = new Button("Ok");
        buttonURL.getStyleClass().add("gameChooserButton");
        buttonURL.getStyleClass().add("gameVariation");
        buttonURL.getStyleClass().add("button");
        buttonURL.setMinHeight(primaryStage.getHeight() / 10);
        buttonURL.setMinWidth(primaryStage.getWidth() / 10);

        urlSide.getChildren().addAll(urlField, buttonURL);
        // ___ URL BLOCK

        // PATH BLOCK ___
        VBox pathSide = new VBox();
        pathSide.setSpacing(10);

        Button pathField = new Button("new media");
        pathField.getStyleClass().add("gameChooserButton");
        pathField.getStyleClass().add("gameVariation");
        pathField.getStyleClass().add("button");
        pathField.minHeightProperty().bind(tf.heightProperty());
        pathField.setMinWidth(primaryStage.getWidth() / 10);

        EventHandler<Event> eventNew;
        eventNew = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                String s = getPath(primaryStage);
                pathField.setText(s);
            }
        };

        pathField.addEventHandler(MouseEvent.MOUSE_CLICKED, eventNew);

        Button buttonPath = new Button("Ok");
        buttonPath.getStyleClass().add("gameChooserButton");
        buttonPath.getStyleClass().add("gameVariation");
        buttonPath.getStyleClass().add("button");
        buttonPath.setMinHeight(primaryStage.getHeight() / 10);
        buttonPath.setMinWidth(primaryStage.getWidth() / 10);

        pathSide.getChildren().addAll(pathField, buttonPath);
        // ___ PATH BLOCK

        Text t = new Text();

        EventHandler<Event> eventURL;
        eventURL = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                if (tf.getText() != null && !tf.getText().equals("")) {
                    dialog.close();
                    refresh();
                    String name = title.getText();
                    if (name == null || name.equals("")) {
                        name = "media" + musicList.mediaList.size();
                    }

                    MediaFile mf;
                    if (tfi.getText().equals(gameContext.getGazePlay().getTranslator().translate("ChooseImage"))) {
                        mf = new MediaFile("URL", tf.getText(), name, null);
                    } else {
                        mf = new MediaFile("URL", tf.getText(), name, tfi.getText());
                    }

                    musicList.addMedia(mf);
                    primaryStage.getScene().getRoot().setEffect(null);
                } else {
                    t.setText("Invalid URL !");
                    t.setFill(Color.RED);
                }
            }
        };

        EventHandler<Event> eventPath;
        eventPath = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                if (pathField.getText() != null && !pathField.getText().equals("new media")) {
                    dialog.close();
                    refresh();
                    String name = title.getText();
                    if (name == null || name.equals("")) {
                        name = "media" + musicList.mediaList.size();
                    }
                    MediaFile mf;
                    if (tfi.getText().equals(gameContext.getGazePlay().getTranslator().translate("ChooseImage"))) {
                        mf = new MediaFile("MEDIA", pathField.getText(), name, null);
                    } else {
                        mf = new MediaFile("MEDIA", pathField.getText(), name, tfi.getText());
                    }
                    musicList.addMedia(mf);
                    primaryStage.getScene().getRoot().setEffect(null);
                } else {
                    t.setText("Invalid File !");
                    t.setFill(Color.RED);
                }
            }
        };

        buttonPath.addEventHandler(MouseEvent.MOUSE_CLICKED, eventPath);
        buttonURL.addEventHandler(MouseEvent.MOUSE_CLICKED, eventURL);

        urlSide.setAlignment(Pos.CENTER);
        pathSide.setAlignment(Pos.CENTER);
        sides.getChildren().addAll(urlSide, new Separator(Orientation.VERTICAL), pathSide);

        choicePane.getChildren().addAll(titleText, tfi, title, sides, t);

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        dialog.setScene(scene);

        return dialog;
    }

    private String getPath(Stage primaryStage) {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
        }
        return s;
    }

    public void putMusic(int i, boolean next) {
        MediaFile mf;
        if (next) {
            mf = musicList.next();
        } else {
            mf = musicList.previous();
        }

        EventHandler<Event> event;

        if (mf != null && mf.getType().equals("URL")) {

            titre[i].setText(mf.getName());

            event = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {
                    stopMedia();

                    Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                    String videoUrl = mf.getPath();
                    WebView webview = new WebView();
                    webview.getEngine().load(videoUrl);
                    webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p

                    BorderPane.setAlignment(webview, Pos.CENTER);
                    ((StackPane) videoRoot.getCenter()).getChildren().set(1, webview);
                    play = true;

                    musicList.setPlaying(musicList.mediaList.indexOf(mf));

                    musicTitle.setText(mf.getName());
                }
            };

            titre[i].addEventFilter(MouseEvent.MOUSE_CLICKED, event);
            titre[i].addEventFilter(GazeEvent.GAZE_ENTERED, event);
            eventTitre.set(i, event);

        } else if (mf != null && mf.getType().equals("MEDIA")) {

            titre[i].setText(mf.getName());

            event = new EventHandler<Event>() {
                @Override
                public void handle(Event e) {

                    stopMedia();

                    Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

                    File media = new File(mf.getPath());
                    MediaPlayer player = new MediaPlayer(new Media(media.toURI().toString()));
                    MediaView mediaView = new MediaView(player);
                    mediaView.setFitHeight(dimension2D.getHeight() / 2);
                    mediaView.setFitWidth(dimension2D.getWidth() / 3);

                    BorderPane.setAlignment(mediaView, Pos.CENTER);

                    ((StackPane) videoRoot.getCenter()).getChildren().set(1, mediaView);
                    player.play();
                    play = true;

                    musicList.setPlaying(musicList.mediaList.indexOf(mf));
                    musicTitle.setText(mf.getName());
                }
            };

            titre[i].addEventFilter(MouseEvent.MOUSE_CLICKED, event);
            titre[i].addEventFilter(GazeEvent.GAZE_ENTERED, event);
            eventTitre.set(i, event);
        }

        if (mf != null && mf.getImagepath() != null) {
            File f = new File(mf.getImagepath());
            ImageView iv = new ImageView(new Image(f.toURI().toString()));
            iv.setPreserveRatio(true);
            iv.setFitHeight((90 * titre[i].getHeight()) / 100);
            iv.setFitWidth((90 * titre[i].getHeight()) / 100);
            titre[i].setGraphic(iv);
        } else {
            titre[i].setGraphic(null);
        }

    }

    private void fullScreenCheck() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (!full) {
            enableFullScreen();
        } else {
            disableFullScreen();
        }
        full = !full;
    }

    public void enableFullScreen() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.setFitWidth(dimension2D.getWidth());
            if (mediaView.getMediaPlayer().getMedia().getWidth() != 0) {
                mediaView.setFitHeight((7 * dimension2D.getHeight()) / 8);
            } else {
                mediaView.setFitHeight(0);
            }
            double size = (7 * dimension2D.getHeight()) / 8;
            Rectangle r = new Rectangle(0, 0, size, size);
            r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
            ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

            gameContext.getChildren().clear();
            videoSide.setSpacing(0);
            videoSide.getChildren().remove(addVideo);
            BorderPane bp = new BorderPane();
            bp.setCenter(videoSide);
            // double x = dimension2D.getHeight() - mediaView.getFitHeight() + left.getHeight();
            bp.setLayoutY(0);
            gameContext.getChildren().add(bp);
            BorderPane.setAlignment(mediaView, Pos.CENTER);
        } else if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof WebView) {
            WebView webview = (WebView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            webview.setPrefSize(dimension2D.getWidth(), (7 * dimension2D.getHeight()) / 8); // 360p
            gameContext.getChildren().clear();
            videoSide.setSpacing(0);
            videoSide.getChildren().remove(addVideo);
            BorderPane bp = new BorderPane();
            bp.setCenter(videoSide);
            gameContext.getChildren().add(bp);
        }

    }

    public void disableFullScreen() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof MediaView) {
            MediaView mediaView = (MediaView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            mediaView.setFitHeight(dimension2D.getHeight() / 2);
            mediaView.setFitWidth(dimension2D.getWidth() / 3);

            Rectangle r = new Rectangle(0, 0, dimension2D.getWidth() / 3, dimension2D.getHeight() / 2);
            r.setFill(new ImagePattern(new Image("data/gazeMediaPlayer/gazeMediaPlayer.png")));
            ((StackPane) videoRoot.getCenter()).getChildren().set(0, r);

            gameContext.getChildren().clear();
            videoSide.getChildren().setAll(addVideo, videoRoot, musicTitle, tools);
            videoSide.setSpacing(dimension2D.getHeight() / 30);
            window.getChildren().clear();
            window.getChildren().addAll(scrollList, videoSide);
            gameContext.getChildren().add(window);
        } else if (((StackPane) videoRoot.getCenter()).getChildren().get(1) instanceof WebView) {
            WebView webview = (WebView) ((StackPane) videoRoot.getCenter()).getChildren().get(1);
            webview.setPrefSize(dimension2D.getWidth() / 3, dimension2D.getHeight() / 2); // 360p
            gameContext.getChildren().clear();
            videoSide.setSpacing(dimension2D.getHeight() / 30);
            window.getChildren().clear();
            videoSide.getChildren().setAll(addVideo, videoRoot, musicTitle, tools);
            window.getChildren().addAll(scrollList, videoSide);
            gameContext.getChildren().add(window);
        }
    }

    public void refresh() {
        putMusic(0, true);
        putMusic(1, true);
        putMusic(2, true);
        putMusic(2, false);
        putMusic(1, false);
        putMusic(0, false);

    }

    private String getImage(Button tfi, Stage primaryStage) {
        String s = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.tiff"),
                new ExtensionFilter("PNG Files", "*.png"), new ExtensionFilter("JPeg Files", "*.jpg", "*.jpeg"),
                new ExtensionFilter("GIF Files", "*.gif"), new ExtensionFilter("BMP Files", "*.bmp"),
                new ExtensionFilter("TIFF Files", "*.tiff"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            s = selectedFile.getAbsolutePath();
            ImageView iv;
            try {
                iv = new ImageView(new Image(new FileInputStream(selectedFile)));
                iv.setPreserveRatio(true);
                iv.setFitHeight(primaryStage.getHeight() / 10);
                tfi.setGraphic(iv);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return s;
    }
}
