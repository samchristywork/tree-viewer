package org.tasker;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Render {
  private App app;
  protected ColorScheme darkColorScheme = new ColorScheme();
  protected ColorScheme lightColorScheme = new ColorScheme();
  protected ColorScheme colorScheme;
  protected Vec2 globalOffset = new Vec2(0, 0);
  protected Vec2 padding = new Vec2(10, 6);
  protected boolean compact = false;
  protected boolean darkMode = true;
  protected boolean lmbClicked = false;
  protected boolean rmbClicked = false;
  protected boolean showDone = false;
  protected double lineHeight = 40;
  protected double size = 1;
  protected double spaceBetweenNodes = 50;

  public Render(App app) { this.app = app; }

  private void subtree(Node n) {
    Text text = new Text(n.label);
    text.setFont(app.gc.getFont());

    if (n == app.selectedNode || n.checkAttr("border", "true")) {
      Rect r = n.getSubtreeRect();
      r.x -= padding.x;
      r.y -= padding.y;
      r.w += padding.x * 2;
      r.h += padding.y * 2;
      Draw.rect(app, r, colorScheme.borderColor, colorScheme.borderBackground,
                1);
    }

    for (Node child : n.children) {
      if (!child.show) {
        continue;
      }

      Vec2 a = n.getRightNode();
      Vec2 b = child.getLeftNode();
      Draw.circle(app, a, 3, colorScheme.nodeBorderColor,
                  colorScheme.bezierNodeColor, 1);
      Draw.circle(app, b, 3, colorScheme.nodeBorderColor,
                  colorScheme.bezierNodeColor, 1);
      Draw.bezier(app, a, new Vec2((a.x + b.x) / 2, a.y),
                  new Vec2((a.x + b.x) / 2, b.y), b,
                  colorScheme.bezierCurveColor, 2);

      subtree(child);
    }

    for (int i = 0; i < n.links.size(); i++) {
      String link = n.links.get(i).replace("\t", "→");
      Vec2 a = n.getRightNode();
      Vec2 b = new Vec2(a.x + spaceBetweenNodes,
                        a.y + (i + n.children.size()) * lineHeight);
      Draw.circle(app, a, 3, colorScheme.nodeBorderColor,
                  colorScheme.bezierNodeColor, 1);
      Draw.circle(app, b, 3, colorScheme.nodeBorderColor,
                  colorScheme.bezierNodeColor, 1);
      Draw.bezier(app, a, new Vec2((a.x + b.x) / 2, a.y),
                  new Vec2((a.x + b.x) / 2, b.y), b,
                  colorScheme.bezierCurveColor, 2);
      b.x += padding.x;
      b.y += padding.y / 2;
      Draw.text(app, link, b, colorScheme.textColor);
    }

    n.draw(app);
  }

  private void background() {
    app.gc.clearRect(0, 0, app.dimensions.x, app.dimensions.y);
    app.gc.setFill(colorScheme.backgroundColor);
    app.gc.fillRect(0, 0, app.dimensions.x, app.dimensions.y);
  }

  private void grid(Vec2 span, Vec2 margin, Color c) {
    app.gc.setStroke(c);

    for (double x = globalOffset.x % span.x; x <= app.dimensions.x;
         x += span.x) {
      app.gc.strokeLine(x, 0, x, app.dimensions.y);
    }

    for (double y = globalOffset.y % span.y; y <= app.dimensions.y;
         y += span.y) {
      app.gc.strokeLine(0, y, app.dimensions.x, y);
    }
  }

  private void grids() {
    grid(new Vec2(20, 20), new Vec2(100, 100), colorScheme.gridColor1);
    grid(new Vec2(100, 100), new Vec2(100, 100), colorScheme.gridColor2);
  }

  private void subtree() {
    app.tree.sort();
    subtree(app.tree.root);
    lmbClicked = false;
    rmbClicked = false;
  }

  private void statusText() {
    int fontSize = 16;

    String statusline =
        String.format("Pan=(%.0f, %.0f)", globalOffset.x, globalOffset.y);
    statusline +=
        String.format(" Mouse=(%.0f, %.0f)", app.mouse.x, app.mouse.y);
    statusline += String.format(" Dimensions=(%.0f, %.0f)", app.dimensions.x,
                                app.dimensions.y);

    app.gc.setFill(colorScheme.textColor);
    app.gc.setFont(Font.font("Arial", fontSize));
    app.gc.fillText(statusline, 10, app.dimensions.y - 10);
  }

  private void modifiedIndicator() {
    double fontSize = app.gc.getFont().getSize();

    if (app.tree.isModified()) {
      app.gc.fillText("Modified", app.dimensions.x - 80, 10 + fontSize);
    }
  }

  private void childList() {
    double fontSize = app.gc.getFont().getSize();

    int i = 0;
    for (Node child : app.selectedNode.children) {
      double y = 10 + i * fontSize + fontSize;
      app.gc.setFill(Color.GREY);
      app.gc.fillText("" + (i + 1), 10, y);
      app.gc.setFill(colorScheme.textColor);
      app.gc.fillText("" + child.label, 30, y);
      i++;
    }

    for (String link : app.selectedNode.links) {
      double y = 10 + i * fontSize + fontSize;
      app.gc.setFill(Color.GREY);
      app.gc.fillText("" + (i + 1), 10, y);
      app.gc.setFill(colorScheme.textColor);
      app.gc.fillText("" + link.replace("\t", "→"), 30, y);
      i++;
    }
  }

  private void filePreview() {
    if (app.selectedNode == null) {
      return;
    }

    String[] lines = app.readLinesFromFile(app.workingDirectory + "/files/" +
                                           app.selectedNode.label + ".md");
    double fontSize = app.gc.getFont().getSize();

    if (lines.length != 0) {
      app.gc.setFill(colorScheme.previewBackgroundColor);
      app.gc.fillRect(app.dimensions.x - 320, 0, 320, app.dimensions.y);
    }

    double y = 2 * fontSize;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].replace("[ ]", "☐").replace("[x]", "☑");
      String firstTwo = line.length() > 2 ? line.substring(0, 2) : "";
      if (firstTwo.equals("- ")) {
        line = "• " + line.substring(2);
      }
      y += fontSize;
      app.gc.setFill(colorScheme.textColor);

      if (line.length() > 0 && line.charAt(0) == '#') {
        y += fontSize;
        app.gc.setFont(Font.font("Arial", 24));
        app.gc.fillText(line, app.dimensions.x - 300, y);
        app.gc.setFont(Font.font("Arial", 12));
        y += fontSize / 2;
      } else {
        app.gc.fillText(line, app.dimensions.x - 300, y);
      }
    }
  }

  public void render() {
    if (app.state == State.TREE_SELECTION) {
      app.setColorScheme();
      background();
      grids();
      statusText();

      int i = 1;
      for (String vault : app.vaults) {
        double offset = 10 + i * 16;
        app.gc.setFill(Color.GREY);
        app.gc.fillText("" + i, 10, offset);
        app.gc.setFill(colorScheme.textColor);
        app.gc.fillText(vault, 30, offset);
        i++;
      }

      double offset = 10 + i * 16;
      app.gc.setFill(Color.GREY);
      app.gc.fillText("n", 10, offset);
      app.gc.setFill(colorScheme.textColor);
      app.gc.fillText("New Vault", 30, offset);
    } else if (app.state == State.TREE_VIEW) {
      if (app.selectedNode == null) {
        app.selectedNode = app.tree.root;
      }

      if (app.tree.current.isAncestor(app.selectedNode)) {
        app.tree.current = app.selectedNode;
      }

      app.gc.setFont(Font.font("Arial", 12 * size));
      app.setColorScheme();
      app.handleReparent();
      Layout.calculateLayout(app, app.tree.root);
      background();
      grids();
      subtree();
      statusText();
      modifiedIndicator();
      childList();
      filePreview();
    }
  }
}
