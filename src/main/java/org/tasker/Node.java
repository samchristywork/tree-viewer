package org.tasker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

class Node {
  String label;
  ArrayList<Node> children = new ArrayList<Node>();
  Node parent;
  HashMap<String, String> attributes;
  boolean collapsed = false;

  Node(String label, Node parent) {
    this.label = label;
    this.parent = parent;
  }

  Node(String label) {
    this.label = label;
  }

  private String fullyQualifiedName() {
    if (parent.parent == null) {
      return label;
    } else {
      return parent.fullyQualifiedName() + "	" + label;
    }
  }

  public Node addChild(String label) {
    Node child = new Node(label, this);
    children.add(child);

    Collections.sort(children, (a, b) -> a.label.compareTo(b.label));

    return child;
  }

  public Node findChild(String label) {
    for (Node child : children) {
      if (child.label.equals(label)) {
        return child;
      }
    }
    return null;
  }

  public Node insert(String label) {
    Node n = new Node(this.label, this);
    n.children = this.children;
    children = new ArrayList<Node>();
    children.add(n);
    this.label = label;

    Collections.sort(children, (a, b) -> a.label.compareTo(b.label));

    return n;
  }

  public void serialize(BufferedWriter writer) throws IOException {
    writer.write(fullyQualifiedName());
    writer.write("\n");

    for (Node child : children) {
      child.serialize(writer);
    }
  }

  public void addNode(App app) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        TextInputDialog dialog = new TextInputDialog(label);
        dialog.setTitle("New Node");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              addChild(name);
              app.modified = true;
              app.render();
            }
          });
        });
      }
    });
  }

  public void rename(App app) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        TextInputDialog dialog = new TextInputDialog(label);
        dialog.setTitle("Rename Node");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              label = name;
              app.modified = true;
              app.render();
            }
          });
        });
      }
    });
  }

  public void draw(App app, Node n, Vec2 offset) {
    Text text = new Text(n.label);
    text.setFont(app.gc.getFont());
    Vec2 extents = new Vec2(text.getLayoutBounds().getWidth(),
        text.getLayoutBounds().getHeight());

    Rect r = new Rect(
        offset.x - app.padding.x, offset.y * app.lineHeight - app.padding.y,
        extents.x + app.padding.x * 2, extents.y + app.padding.y * 2);

    if (n == app.selectedNode) {
      Draw.rect(app, r, Color.BLACK, Color.LIGHTGREEN);
    } else if (n.attributes != null && n.attributes.containsKey("foo")) {
      Draw.rect(app, r, Color.RED, Color.RED);
    } else {
      Draw.rect(app, r, Color.BLACK, Color.WHITE);
    }
    if (r.contains(new Vec2(app.mouse.x - app.globalOffset.x,
        app.mouse.y - app.globalOffset.y))) {
      Draw.rect(app, r, Color.BLACK, Color.LIGHTGRAY);

      if (app.lmbClicked) {
        System.out.println(n.label);
        app.selectedNode = n;
      }

      if (app.rmbClicked) {
        addNode(app);
      }
    }

    Draw.text(app, label,
        new Vec2(offset.x, offset.y * app.lineHeight + 3 * extents.y / 4),
        Color.BLACK);
  }
}
