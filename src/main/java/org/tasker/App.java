package org.tasker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {
  private ColorScheme darkColorScheme = new ColorScheme();
  private ColorScheme lightColorScheme = new ColorScheme();
  private double lineHeight = 40;
  private double spaceBetweenNodes = 50;
  protected ColorScheme colorScheme;
  protected GraphicsContext gc;
  protected Node nodeToReparent = null;
  protected Node selectedNode = null;
  protected Node targetNode = null;
  protected Stage stage;
  protected Tree tree = new Tree();
  protected String workingDirectory = "./";
  protected Vec2 dimensions = new Vec2(1600, 800);
  protected Vec2 globalOffset = new Vec2(0, 0);
  protected Vec2 mouse = new Vec2(0, 0);
  protected Vec2 padding = new Vec2(10, 6);
  protected boolean compact = false;
  protected boolean darkMode = false;
  protected boolean lmbClicked = false;
  protected boolean rmbClicked = false;
  protected boolean showDone = false;
  protected double size = 1;

  private double calculateLayout(Node n) {
    return calculateLayout(n, new Vec2(0, 0));
  }

  private double calculateLayout(Node n, Vec2 offset) {
    n.show = true;

    Text text = new Text(n.label);
    text.setFont(gc.getFont());
    n.extents.x = text.getLayoutBounds().getWidth();
    n.extents.y = text.getLayoutBounds().getHeight();

    double height = 0;
    double width = n.extents.x + padding.x * 2 + spaceBetweenNodes;

    n.bounds.x = offset.x;
    n.bounds.y = offset.y * lineHeight * size;
    n.bounds.w = n.extents.x + padding.x * 2;
    n.bounds.h = n.extents.y + padding.y * 2;

    if (compact) {
      n.bounds.y = offset.y * 17;
      n.bounds.h = 16;
      padding.y = 0;
    }

    if (n == nodeToReparent) {
      n.bounds.x = -globalOffset.x + mouse.x - n.bounds.w / 2;
      n.bounds.y = -globalOffset.y + mouse.y - n.bounds.h / 2;
    }

    if (n.children.size() == 0) {
      height = 1;
    } else {
      for (Node child : n.children) {
        if (!showDone && child.checkAttr("status", "done")) {
          child.show = false;
          continue;
        } else if (!child.isAncestor(tree.current) &&
                   !tree.current.isAncestor(child) && tree.current != child) {
          child.show = false;
          continue;
        } else {
          child.show = true;
        }

        Vec2 o = new Vec2(offset.x + width, offset.y + height);
        height += calculateLayout(child, o);
      }
    }

    return height;
  }

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

    if (n.children.size() != 0) {
      for (Node child : n.children) {
        if (!child.show) {
          continue;
        }

        Vec2 a = n.getRightNode();
        Vec2 b = child.getLeftNode();
        Draw.circle(this, a, 3, colorScheme.nodeBorderColor,
                    colorScheme.bezierColor, 1);
        Draw.circle(this, b, 3, colorScheme.nodeBorderColor,
                    colorScheme.bezierColor, 1);
        Draw.bezier(this, a, new Vec2((a.x + b.x) / 2, a.y),
                    new Vec2((a.x + b.x) / 2, b.y), b, colorScheme.bezierColor,
                    2);

        renderSubtree(child);
      }
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
  }

  public void render() {
    if (tree.current.isAncestor(selectedNode)) {
      tree.current = selectedNode;
    }

    gc.setFont(Font.font("Arial", 12 * size));
    setColorScheme();
    handleReparent();
    calculateLayout(tree.root);
    renderBackground();
    renderGrid();
    renderSubtree();
    renderStatusText();
    renderModifiedIndicator();
    renderChildList();
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

  private void addListeners(Scene scene, Canvas canvas) {
    scene.widthProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.x = (double)newVal;
      canvas.setWidth(dimensions.x);
      render();
    });

    scene.heightProperty().addListener((obs, oldVal, newVal) -> {
      dimensions.y = (double)newVal;
      canvas.setHeight(dimensions.y);
      render();
    });

    scene.addEventHandler(KeyEvent.KEY_PRESSED,
                          (key) -> { Event.keyPressHandler(this, key); });

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

    stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
      if (Event.close(this)) {
        System.exit(0);
      } else {
        e.consume();
      }
    });
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

    tree.readFromFile("save.tree");

    readDataStore();

    if (getParameters().getRaw().size() > 0) {
      System.out.println("args: " + getParameters().getRaw());
      String firstArg = getParameters().getRaw().get(0);
      if (firstArg != null) {
        selectedNode = tree.findNode(firstArg);
      }
    }

    if (selectedNode == null) {
      selectedNode = tree.root;
    }

    Canvas canvas = new Canvas(dimensions.x, dimensions.y);
    Scene scene = new Scene(new StackPane(canvas), dimensions.x, dimensions.y);
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
