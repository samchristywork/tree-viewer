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

  private static void renderChildList(GraphicsContext gc, Node selectedNode, ColorScheme colorScheme) {
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

  private static void renderFilePreview(GraphicsContext gc, Node selectedNode, String workingDirectory, ColorScheme colorScheme, Vec2 dimensions) {
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
      String firstTwo = line.length() > 2 ? line.substring(0, 2) : "";
      if (firstTwo.equals("- ")) {
        line = "• " + line.substring(2);
      }
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

  protected static void render(State state, GraphicsContext gc, Tree tree,
      Mouse m, Node selectedNode, Vec2 globalOffset, Vec2 dimensions,
      String workingDirectory, ColorScheme colorScheme, String[] vaults, double size) {
    if (state == State.TREE_SELECTION) {
      //setColorScheme();
      renderBackground(gc, dimensions, colorScheme);
      renderGrid(gc, globalOffset, dimensions, colorScheme);
      renderStatusText(gc, globalOffset, dimensions, m, colorScheme);

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
      //setColorScheme();
      //handleReparent();
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

  private static void renderSubtree(Node n) {
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
}
