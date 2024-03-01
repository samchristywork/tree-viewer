package org.tasker;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class Mouse {
  public boolean lmbClicked = false;
  public boolean rmbClicked = false;
  public Vec2 pos = new Vec2(0, 0);
}

public class Render {
  private static String[] readLinesFromFile(String filename) {
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

  private static void renderBackground(GraphicsContext gc, Vec2 dimensions,
      ColorScheme colorScheme) {
    gc.clearRect(0, 0, dimensions.x, dimensions.y);
    gc.setFill(colorScheme.backgroundColor);
    gc.fillRect(0, 0, dimensions.x, dimensions.y);
  }

  private static void renderGrid(GraphicsContext gc, Vec2 globalOffset,
      Vec2 dimensions, ColorScheme colorScheme) {
    Grid.renderGrid(gc, new Vec2(20, 20), new Vec2(100, 100),
        globalOffset, dimensions, colorScheme.gridColor1);
    Grid.renderGrid(gc, new Vec2(100, 100), new Vec2(100, 100),
        globalOffset, dimensions, colorScheme.gridColor2);
  }

  private static void renderTree(Tree tree, Mouse m) {
    tree.sort();
    renderSubtree(tree.root);
    m.lmbClicked = false;
    m.rmbClicked = false;
  }

  private static void renderStatusText(GraphicsContext gc, Vec2 globalOffset,
      Vec2 dimensions, Mouse mouse, ColorScheme colorScheme) {
    int fontSize = 16;

    String statusline = String.format("Pan=(%.0f, %.0f)", globalOffset.x, globalOffset.y);
    statusline += String.format(" Mouse=(%.0f, %.0f)", mouse.pos.x, mouse.pos.y);
    statusline += String.format(" Dimensions=(%.0f, %.0f)", dimensions.x, dimensions.y);

    gc.setFill(colorScheme.textColor);
    gc.setFont(Font.font("Arial", fontSize));
    gc.fillText(statusline, 10, dimensions.y - 10);
  }

  private static void renderModifiedIndicator(GraphicsContext gc, Tree tree, Vec2 dimensions) {
    double fontSize = gc.getFont().getSize();

    if (tree.isModified()) {
      gc.fillText("Modified", dimensions.x - 80, 10 + fontSize);
    }
  }
}
