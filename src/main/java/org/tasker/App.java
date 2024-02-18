package org.tasker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

class ColorScheme {
  Color backgroundColor;
  Color bezierColor;
  Color borderColor;
  Color gridColor1;
  Color gridColor2;
  Color jumpTextColor;
  Color nodeBackgroundColor;
  Color nodeBorderColor;
  Color nodeCompletedColor;
  Color nodeHoverColor;
  Color nodeReparentColor;
  Color nodeSelectedColor;
  Color textColor;
}

public class App extends Application {
  ColorScheme colorScheme;
  ColorScheme darkColorScheme = new ColorScheme();
  ColorScheme lightColorScheme = new ColorScheme();
  GraphicsContext gc;
  Node selectedNode = null;
  Tree tree = new Tree();
  Vec2 dimensions = new Vec2(1600, 800);
  Vec2 globalOffset = new Vec2(100, 100);
  Vec2 mouse = new Vec2(0, 0);
  Vec2 padding = new Vec2(10, 6);
  boolean lmbClicked = false;
  boolean modified = false;
  boolean rmbClicked = false;
  double lineHeight = 40;
  double size = 1;

  private Vec2 renderSubtree(Node n, Vec2 offset) {
    double height = 0;

    Text text = new Text(n.label);
    text.setFont(gc.getFont());
    Vec2 extents = new Vec2(text.getLayoutBounds().getWidth(),
        text.getLayoutBounds().getHeight());

    double width = extents.x + padding.x * 2 + 30;

    Rect r = new Rect(offset.x - padding.x, offset.y * lineHeight - padding.y,
        extents.x + padding.x * 2, extents.y + padding.y * 2);

    double maxSubtreeWidth = 0;
    if (n.children.size() == 0) {
      height = 1;
    } else {
      for (Node child : n.children) {
        Vec2 a = new Vec2(offset.x + extents.x + padding.x,
            offset.y * lineHeight + extents.y / 2);
        Vec2 b = new Vec2((offset.x + width) - padding.x,
            (offset.y + height) * lineHeight + extents.y / 2);
        Draw.bezier(this, a, new Vec2((a.x + b.x) / 2, a.y),
            new Vec2((a.x + b.x) / 2, b.y), b, Color.BLACK);

        Vec2 e = renderSubtree(child, new Vec2(offset.x + width, offset.y + height));
        height += e.y;
        maxSubtreeWidth = Math.max(maxSubtreeWidth, e.x);
      }
    }

    if (n == selectedNode || n.label.equals("org")) {
      int foo = 5;
      Draw.rect(this,
          new Rect(r.x - foo, r.y - foo,
              maxSubtreeWidth + width - 30 + foo * 2,
              (height - 1) * lineHeight + extents.y + padding.y * 2 +
                  foo * 2),
          Color.GREY, Color.TRANSPARENT);
    }

    n.draw(this, n, offset);

    return new Vec2(width + maxSubtreeWidth, height);
  }

  public void render() {
    gc.clearRect(0, 0, dimensions.x, dimensions.y);

    // Calculate layout
    Node n = tree.current;
    calculateLayout(n);

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

    Node n = tree.current;
    renderSubtree(n, new Vec2(0, 0));
    lmbClicked = false;
    rmbClicked = false;

    // Render status text
    gc.setFill(colorScheme.textColor);
    int fontSize = 16;
    gc.setFont(Font.font("Arial", fontSize));

    if (modified) {
      gc.fillText("modified", 10, 10 + fontSize);
    }
  }

  @Override
  public void start(Stage stage) {

    // TODO: Move to a config file
    darkColorScheme.backgroundColor = Color.BLACK;
    darkColorScheme.bezierColor = Color.GREY;
    darkColorScheme.borderColor = new Color(0.5, 0.5, 0.5, 1);
    darkColorScheme.gridColor1 = new Color(0.1, 0.1, 0.1, 1);
    darkColorScheme.gridColor2 = new Color(0.2, 0.2, 0.2, 1);
    darkColorScheme.jumpTextColor = Color.RED;
    darkColorScheme.nodeBackgroundColor = Color.BLACK;
    darkColorScheme.nodeBorderColor = Color.GREY;
    darkColorScheme.nodeCompletedColor = new Color(0.2, 0.4, 0.2, 1);
    darkColorScheme.nodeHoverColor = new Color(0.2, 0.2, 0.2, 1);
    darkColorScheme.nodeReparentColor = new Color(0.12, 0.22, 0.32, 1);
    darkColorScheme.nodeSelectedColor = new Color(0.32, 0.45, 0.5, 1);
    darkColorScheme.textColor = Color.WHITE;
    lightColorScheme.backgroundColor = Color.WHITE;
    lightColorScheme.bezierColor = Color.GREY;
    lightColorScheme.borderColor = Color.DARKGREY;
    lightColorScheme.gridColor1 = new Color(0.9, 0.9, 0.9, 1);
    lightColorScheme.gridColor2 = new Color(0.8, 0.8, 0.8, 1);
    lightColorScheme.jumpTextColor = Color.DARKRED;
    lightColorScheme.nodeBackgroundColor = Color.WHITE;
    lightColorScheme.nodeBorderColor = Color.DARKGREY;
    lightColorScheme.nodeCompletedColor = new Color(0.6, 0.8, 0.6, 1);
    lightColorScheme.nodeHoverColor = new Color(0.95, 0.95, 0.95, 1);
    lightColorScheme.nodeReparentColor = new Color(0.96, 0.88, 0.72, 1);
    lightColorScheme.nodeSelectedColor = new Color(0.68, 0.85, 0.9, 1);
    lightColorScheme.textColor = Color.BLACK;

    tree.readFromFile("save.tree");
    selectedNode = tree.current;

    Canvas canvas = new Canvas(dimensions.x, dimensions.y);
    gc = canvas.getGraphicsContext2D();

    Scene scene = new Scene(new StackPane(canvas), dimensions.x, dimensions.y);

    stage.setTitle("Tasker");

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
    launch();
  }
}
