package org.tasker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
  protected ArrayList<String> vaults = new ArrayList<String>();
  private Canvas canvas;
  private ColorScheme darkColorScheme = new ColorScheme();
  private ColorScheme lightColorScheme = new ColorScheme();
  private Event event = new Event();
  private Scene scene;
  protected ColorScheme colorScheme;
  protected GraphicsContext gc;
  protected Node nodeToReparent = null;
  protected Node selectedNode = null;
  protected Node targetNode = null;
  protected Stage stage;
  protected State state = State.TREE_SELECTION;
  protected String editorCommand = "gedit";
  protected String workingDirectory = "./";
  protected Tree tree = null;
  protected Vec2 dimensions = new Vec2(1600, 800);
  protected Vec2 globalOffset = new Vec2(0, 0);
  protected Vec2 mouse = new Vec2(0, 0);
  protected Vec2 padding = new Vec2(10, 6);
  protected boolean compact = false;
  protected boolean darkMode = true;
  protected boolean lmbClicked = false;
  protected boolean rmbClicked = false;
  protected boolean showDone = false;
  protected double lineHeight = 40;
  protected double size = 1;
  protected double spaceBetweenNodes = 50;
  private Render render = new Render(this);

  protected void render() {
    render.render();
  }

  protected void handleReparent() {
    if (nodeToReparent == null) {
      return;
    }

    if (targetNode == null) {
      return;
    }

    if (targetNode == nodeToReparent) {
      targetNode = null;
      nodeToReparent = null;
      return;
    }

    if (targetNode.isAncestor(nodeToReparent)) {
      targetNode = null;
      nodeToReparent = null;
      return;
    }

    Node parent = nodeToReparent.parent;
    parent.children.remove(nodeToReparent);
    targetNode.children.add(nodeToReparent);
    nodeToReparent.parent = targetNode;

    nodeToReparent = null;
    targetNode = null;
  }

  protected void setColorScheme() {
    if (darkMode) {
      colorScheme = darkColorScheme;
    } else {
      colorScheme = lightColorScheme;
    }
  }

  protected void zoom() {
    render.render();
    globalOffset.x = -selectedNode.bounds.x;
    globalOffset.y = -selectedNode.bounds.y;
    globalOffset.x += dimensions.x / 4;
    globalOffset.y += dimensions.y / 4;
    render.render();
  }

  protected boolean close() {
    if (tree == null) {
      return true;
    }

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(workingDirectory + "/datastore"));
      writer.write("darkMode=" + darkMode + "\n");
      writer.write("showDone=" + showDone + "\n");
      writer.write("globalOffsetX=" + globalOffset.x + "\n");
      writer.write("globalOffsetY=" + globalOffset.y + "\n");
      if (selectedNode != null) {
        String selectedFQNN = selectedNode.fullyQualifiedName();
        if (selectedFQNN != null) {
          writer.write("selectedNodeFQNN=" + selectedFQNN + "\n");
        }
      }
      if (tree.current != null && tree.current != tree.root) {
        String currentFQNN = tree.current.fullyQualifiedName();
        if (currentFQNN != null) {
          writer.write("currentNodeFQNN=" + currentFQNN + "\n");
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (tree.isModified()) {
      Alert alert = new Alert(AlertType.CONFIRMATION,
          "You have unsaved work. Quit anyway?",
          ButtonType.YES, ButtonType.NO);
      alert.setTitle("Confirmation Dialog");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES) {
        return true;
      }
    } else {
      return true;
    }
    return false;
  }

  protected String[] readLinesFromFile(String filename) {
    String[] lines = new String[0];
    try {
      Path path = Paths.get(filename);
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
      lines = Files.readAllLines(path).toArray(new String[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  private void readDataStore() {
    String lines[] = readLinesFromFile(workingDirectory + "/datastore");
    for (String line : lines) {
      String[] parts = line.split("=");
      if (parts.length == 2) {
        switch (parts[0]) {
          case "darkMode":
            darkMode = Boolean.parseBoolean(parts[1]);
            break;
          case "showDone":
            showDone = Boolean.parseBoolean(parts[1]);
            break;
          case "selectedNodeFQNN":
            selectedNode = tree.findNode(parts[1]);
            break;
          case "globalOffsetX":
            globalOffset.x = Double.parseDouble(parts[1]);
            break;
          case "globalOffsetY":
            globalOffset.y = Double.parseDouble(parts[1]);
            break;
          case "currentNodeFQNN":
            tree.current = tree.findNode(parts[1]);
            break;
          case "editorCommand":
            editorCommand = parts[1];
            break;
        }
      }
    }
  }

  private void vaultDialog() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        TextInputDialog dialog = new TextInputDialog("MyVault");
        dialog.setTitle("New Vault");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              vaults.add(name);
              workingDirectory = name;

              try {
                Files.createDirectories(Paths.get(workingDirectory + "/files"));
              } catch (IOException e) {
                e.printStackTrace();
              }

              try {
                Files.createDirectories(
                    Paths.get(workingDirectory + "/backups"));
              } catch (IOException e) {
                e.printStackTrace();
              }

              try {
                if (!Files.exists(Paths.get(workingDirectory + "/nodes.yml"))) {
                  Files.createFile(Paths.get(workingDirectory + "/nodes.yml"));
                }
              } catch (IOException e) {
                e.printStackTrace();
              }

              try {
                Files.write(Paths.get("/home/sam/.vaults.txt"), vaults);
              } catch (IOException e) {
                e.printStackTrace();
              }

              readVaultsFile();
              render.render();
            }
          });
        });
      }
    });
  }

  private void addListeners(Scene scene, Canvas canvas) {
    scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
      if (state == State.TREE_VIEW) {
        event.keyPressHandler(this, key);
      } else if (state == State.TREE_SELECTION) {
        switch (key.getCode()) {
          case ESCAPE:
            if (close()) {
              System.exit(0);
            }
            break;
          case DIGIT1:
          case DIGIT2:
          case DIGIT3:
          case DIGIT4:
          case DIGIT5:
          case DIGIT6:
          case DIGIT7:
          case DIGIT8:
          case DIGIT9:
            int i = key.getCode().ordinal() - 25;
            workingDirectory = vaults.get(i);
            setup();
            break;
          case N:
            vaultDialog();
            break;
          default:
            break;
        }
        key.consume();
      }
    });

    scene.addEventHandler(MouseEvent.MOUSE_CLICKED, (m) -> {
      mouse.x = m.getX();
      mouse.y = m.getY();

      if (m.getButton().toString() == "PRIMARY") {
        lmbClicked = true;
      }

      if (m.getButton().toString() == "SECONDARY") {
        rmbClicked = true;
      }

      render.render();
    });

    scene.addEventHandler(MouseEvent.MOUSE_MOVED, (m) -> {
      mouse.x = m.getX();
      mouse.y = m.getY();

      render.render();
    });

    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, (m) -> {
      Vec2 prevMouse = new Vec2(mouse.x, mouse.y);

      mouse.x = m.getX();
      mouse.y = m.getY();

      globalOffset.x += mouse.x - prevMouse.x;
      globalOffset.y += mouse.y - prevMouse.y;

      render.render();
    });

    scene.widthProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.x = (double) newVal;
      canvas.setWidth(dimensions.x);
      render.render();
    });

    scene.heightProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.y = (double) newVal;
      canvas.setHeight(dimensions.y);
      render.render();
    });

    stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
      if (close()) {
        System.exit(0);
      } else {
        e.consume();
      }
    });
  }

  private void setup() {
    tree = new Tree();
    tree.readFromYMLFile(workingDirectory + "/nodes.yml");
    readDataStore();
    state = State.TREE_VIEW;
    render.render();
  }

  private void readVaultsFile() {
    if (!Files.exists(Paths.get("/home/sam/.vaults.txt"))) {
      try {
        Files.createFile(Paths.get("/home/sam/.vaults.txt"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    vaults.clear();
    for (String line : readLinesFromFile("/home/sam/.vaults.txt")) {
      vaults.add(line);
    }
  }

  @Override
  public void start(Stage stage) {
    this.stage = stage;
    Parameters params = getParameters();
    List<String> unnamedParams = params.getUnnamed();
    List<String> rawParams = params.getRaw();
    System.out.println("Number of unnamed params: " + unnamedParams.size());
    for (String p : unnamedParams) {
      System.out.println("Unnamed param: " + p);
    }

    System.out.println("Number of raw params: " + rawParams.size());
    for (String p : rawParams) {
      System.out.println("Raw param: " + p);
    }

    darkColorScheme.initializeDark();
    lightColorScheme.initializeLight();

    readVaultsFile();

    canvas = new Canvas(dimensions.x, dimensions.y);
    scene = new Scene(new StackPane(canvas), dimensions.x, dimensions.y);
    stage.setTitle("Tasker");
    gc = canvas.getGraphicsContext2D();

    addListeners(scene, canvas);

    stage.setScene(scene);
    stage.show();

    render.render();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
