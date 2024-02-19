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

public class App extends Application {
  ColorScheme colorScheme;
  ColorScheme darkColorScheme = new ColorScheme();
  ColorScheme lightColorScheme = new ColorScheme();
  GraphicsContext gc;
  Node nodeToReparent = null;
  Node selectedNode = null;
  Node targetNode = null;
  Tree tree = new Tree();
  Vec2 dimensions = new Vec2(1600, 800);
  Vec2 globalOffset = new Vec2(0, 0);
  Vec2 mouse = new Vec2(0, 0);
  Vec2 padding = new Vec2(10, 6);
  boolean darkMode = false;
  boolean lmbClicked = false;
  boolean rmbClicked = false;
  boolean showDone = false;
  double lineHeight = 40;
  double size = 1;

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
    double width = n.extents.x + padding.x * 2 + 30;

    n.r.x = offset.x;
    n.r.y = offset.y * lineHeight;
    n.r.w = n.extents.x + padding.x * 2;
    n.r.h = n.extents.y + padding.y * 2;

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
      Draw.rect(this, r, colorScheme.borderColor, new Color(0, 0, 0, 0.05));
    }

    if (n.children.size() != 0) {
      for (Node child : n.children) {
        if (!child.show) {
          continue;
        }

        Vec2 a = n.getRightNode();
        Vec2 b = child.getLeftNode();
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

  public void render() {
    gc.setFont(Font.font("Arial", 12 * size));
    handleReparent();

    // Calculate layout
    // Node n = tree.current;
    calculateLayout(tree.root);

    // Set color scheme
    if (darkMode) {
      colorScheme = darkColorScheme;
    } else {
      colorScheme = lightColorScheme;
    }

    // Render background
    gc.clearRect(0, 0, dimensions.x, dimensions.y);
    gc.setFill(colorScheme.backgroundColor);
    gc.fillRect(0, 0, dimensions.x, dimensions.y);

    // Render grid
    Grid.renderGrid(this, new Vec2(20, 20), new Vec2(100, 100),
        colorScheme.gridColor1);
    Grid.renderGrid(this, new Vec2(100, 100), new Vec2(100, 100),
        colorScheme.gridColor2);

    // Render subtree
    tree.sort();
    renderSubtree(tree.root);
    lmbClicked = false;
    rmbClicked = false;

    // Render status text
    gc.setFill(colorScheme.textColor);
    int fontSize = 16;
    gc.setFont(Font.font("Arial", fontSize));

    if (tree.isModified()) {
      gc.fillText("modified", 10, 10 + fontSize);
    }

    // Render list of children
    int i = 1;
    for (Node child : selectedNode.children) {
      double y = 10 + fontSize * 4 + i * fontSize;
      gc.fillText("" + i + " " + child.label, 10, y);
      i++;
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

  @Override
  public void start(Stage stage) {
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

    ColorScheme d = darkColorScheme;
    d.backgroundColor = Color.BLACK;
    d.bezierColor = Color.GREY;
    d.borderColor = new Color(0.5, 0.5, 0.5, 1);
    d.gridColor1 = new Color(0.1, 0.1, 0.1, 1);
    d.gridColor2 = new Color(0.2, 0.2, 0.2, 1);
    d.nodeBackgroundColor = Color.BLACK;
    d.nodeBorderColor = Color.GREY;
    d.nodeCompletedColor = new Color(0.2, 0.4, 0.2, 1);
    d.nodeHoverColor = new Color(0.2, 0.2, 0.2, 1);
    d.nodeReparentColor = new Color(0.12, 0.22, 0.32, 1);
    d.nodeSelectedColor = new Color(0.32, 0.45, 0.5, 1);
    d.textColor = Color.WHITE;

    ColorScheme l = lightColorScheme;
    l.backgroundColor = Color.WHITE;
    l.bezierColor = new Color(0.5, 0.5, 0.7, 0.5);
    l.borderColor = Color.DARKGREY;
    l.gridColor1 = new Color(0.9, 0.9, 0.9, 1);
    l.gridColor2 = new Color(0.8, 0.8, 0.8, 1);
    l.nodeBackgroundColor = Color.WHITE;
    l.nodeBorderColor = Color.DARKGREY;
    l.nodeCompletedColor = new Color(0.8, 0.9, 0.8, 1);
    l.nodeHoverColor = new Color(0.95, 0.95, 0.95, 1);
    l.nodeReparentColor = new Color(0.96, 0.88, 0.72, 1);
    l.nodeSelectedColor = new Color(0.69, 0.85, 0.9, 1);
    l.textColor = Color.BLACK;

    tree.readFromFile("save.tree");

    String lines[] = readLinesFromFile("datastore");

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

    scene.addEventHandler(KeyEvent.KEY_PRESSED,
        (key) -> {
          Event.keyPressHandler(this, key);
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

    stage.setOnCloseRequest((event) -> {
      System.exit(0);
    });

    stage.setScene(scene);
    stage.show();

    render();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
