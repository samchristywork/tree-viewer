package org.tasker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
  private ArrayList<String> vaults = new ArrayList<String>();
  private Canvas canvas;
  private ColorScheme darkColorScheme = new ColorScheme();
  private ColorScheme lightColorScheme = new ColorScheme();
  private Scene scene;
  protected ColorScheme colorScheme;
  protected GraphicsContext gc;
  protected Node nodeToReparent = null;
  protected Node selectedNode = null;
  protected Node targetNode = null;
  protected Stage stage;
  protected State state = State.TREE_SELECTION;
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

  private void renderSubtree(Node n) {
    Text text = new Text(n.label);
    text.setFont(gc.getFont());

    if (n == selectedNode || n.checkAttr("border", "true")) {
      Rect r = n.getSubtreeRect();
      r.x -= padding.x;
      r.y -= padding.y;
      r.w += padding.x * 2;
      r.h += padding.y * 2;
      Draw.rect(this, r, colorScheme.borderColor, colorScheme.borderBackground,
          1);
    }

    for (Node child : n.children) {
      if (!child.show) {
        continue;
      }

      Vec2 a = n.getRightNode();
      Vec2 b = child.getLeftNode();
      Draw.circle(this, a, 3, colorScheme.nodeBorderColor,
          colorScheme.bezierNodeColor, 1);
      Draw.circle(this, b, 3, colorScheme.nodeBorderColor,
          colorScheme.bezierNodeColor, 1);
      Draw.bezier(this, a, new Vec2((a.x + b.x) / 2, a.y),
          new Vec2((a.x + b.x) / 2, b.y), b,
          colorScheme.bezierCurveColor, 2);

      renderSubtree(child);
    }

    for (int i = 0; i < n.links.size(); i++) {
      String link = n.links.get(i).replace("\t", "→");
      Vec2 a = n.getRightNode();
      Vec2 b = new Vec2(a.x + spaceBetweenNodes,
          a.y + (i + n.children.size()) * lineHeight);
      Draw.circle(this, a, 3, colorScheme.nodeBorderColor,
          colorScheme.bezierNodeColor, 1);
      Draw.circle(this, b, 3, colorScheme.nodeBorderColor,
          colorScheme.bezierNodeColor, 1);
      Draw.bezier(this, a, new Vec2((a.x + b.x) / 2, a.y),
          new Vec2((a.x + b.x) / 2, b.y), b,
          colorScheme.bezierCurveColor, 2);
      b.x += padding.x;
      b.y += padding.y / 2;
      Draw.text(this, link, b, colorScheme.textColor);
    }

    n.draw(this);
  }

  private void handleReparent() {
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

  private void setColorScheme() {
    if (darkMode) {
      colorScheme = darkColorScheme;
    } else {
      colorScheme = lightColorScheme;
    }
  }

  private void renderBackground() {
    gc.clearRect(0, 0, dimensions.x, dimensions.y);
    gc.setFill(colorScheme.backgroundColor);
    gc.fillRect(0, 0, dimensions.x, dimensions.y);
  }

  private void renderGrid() {
    Grid.renderGrid(this, new Vec2(20, 20), new Vec2(100, 100),
        colorScheme.gridColor1);
    Grid.renderGrid(this, new Vec2(100, 100), new Vec2(100, 100),
        colorScheme.gridColor2);
  }

  private void renderSubtree() {
    tree.sort();
    renderSubtree(tree.root);
    lmbClicked = false;
    rmbClicked = false;
  }

  private void renderStatusText() {
    int fontSize = 16;

    String statusline = String.format("Pan=(%.0f, %.0f)", globalOffset.x, globalOffset.y);
    statusline += String.format(" Mouse=(%.0f, %.0f)", mouse.x, mouse.y);
    statusline += String.format(" Dimensions=(%.0f, %.0f)", dimensions.x, dimensions.y);

    gc.setFill(colorScheme.textColor);
    gc.setFont(Font.font("Arial", fontSize));
    gc.fillText(statusline, 10, dimensions.y - 10);
  }

  private void renderModifiedIndicator() {
    double fontSize = gc.getFont().getSize();

    if (tree.isModified()) {
      gc.fillText("Modified", dimensions.x - 80, 10 + fontSize);
    }
  }

  private void renderChildList() {
    double fontSize = gc.getFont().getSize();

    int i = 0;
    for (Node child : selectedNode.children) {
      double y = 10 + i * fontSize + fontSize;
      gc.setFill(Color.GREY);
      gc.fillText("" + (i + 1), 10, y);
      gc.setFill(colorScheme.textColor);
      gc.fillText("" + child.label, 30, y);
      i++;
    }

    for (String link : selectedNode.links) {
      double y = 10 + i * fontSize + fontSize;
      gc.setFill(Color.GREY);
      gc.fillText("" + (i + 1), 10, y);
      gc.setFill(colorScheme.textColor);
      gc.fillText("" + link.replace("\t", "→"), 30, y);
      i++;
    }
  }

  private void renderFilePreview() {
    if (selectedNode == null) {
      return;
    }

    String[] lines = readLinesFromFile(workingDirectory + "/files/" +
        selectedNode.label + ".md");
    double fontSize = gc.getFont().getSize();

    if (lines.length != 0) {
      gc.setFill(colorScheme.previewBackgroundColor);
      gc.fillRect(dimensions.x - 320, 0, 320, dimensions.y);
    }

    double y = 2 * fontSize;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].replace("[ ]", "☐").replace("[x]", "☑");
      y += fontSize;
      gc.setFill(colorScheme.textColor);

      if (line.length() > 0 && line.charAt(0) == '#') {
        y += fontSize;
        gc.setFont(Font.font("Arial", 24));
        gc.fillText(line, dimensions.x - 300, y);
        gc.setFont(Font.font("Arial", 12));
        y += fontSize / 2;
      } else {
        gc.fillText(line, dimensions.x - 300, y);
      }
    }
  }

  public void render() {
    if (state == State.TREE_SELECTION) {
      setColorScheme();
      renderBackground();
      renderGrid();
      renderStatusText();

      int i = 1;
      for (String vault : vaults) {
        double offset = 10 + i * 16;
        gc.setFill(Color.GREY);
        gc.fillText("" + i, 10, offset);
        gc.setFill(colorScheme.textColor);
        gc.fillText(vault, 30, offset);
        i++;
      }

      double offset = 10 + i * 16;
      gc.setFill(Color.GREY);
      gc.fillText("n", 10, offset);
      gc.setFill(colorScheme.textColor);
      gc.fillText("New Vault", 30, offset);
    } else if (state == State.TREE_VIEW) {
      if (selectedNode == null) {
        selectedNode = tree.root;
      }

      if (tree.current.isAncestor(selectedNode)) {
        tree.current = selectedNode;
      }

      gc.setFont(Font.font("Arial", 12 * size));
      setColorScheme();
      handleReparent();
      Layout.calculateLayout(this, tree.root);
      renderBackground();
      renderGrid();
      renderSubtree();
      renderStatusText();
      renderModifiedIndicator();
      renderChildList();
      renderFilePreview();
    }
  }

  private String[] readLinesFromFile(String filename) {
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
              render();
            }
          });
        });
      }
    });
  }

  private void addListeners(Scene scene, Canvas canvas) {
    scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
      if (state == State.TREE_VIEW) {
        Event.keyPressHandler(this, key);
      } else if (state == State.TREE_SELECTION) {
        switch (key.getCode()) {
          case ESCAPE:
            if (Event.close(this)) {
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

      render();
    });

    scene.addEventHandler(MouseEvent.MOUSE_MOVED, (m) -> {
      mouse.x = m.getX();
      mouse.y = m.getY();

      render();
    });

    scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, (m) -> {
      Vec2 prevMouse = new Vec2(mouse.x, mouse.y);

      mouse.x = m.getX();
      mouse.y = m.getY();

      globalOffset.x += mouse.x - prevMouse.x;
      globalOffset.y += mouse.y - prevMouse.y;

      render();
    });

    scene.widthProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.x = (double) newVal;
      canvas.setWidth(dimensions.x);
      render();
    });

    scene.heightProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.y = (double) newVal;
      canvas.setHeight(dimensions.y);
      render();
    });

    stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
      if (Event.close(this)) {
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
    render();
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

    render();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
