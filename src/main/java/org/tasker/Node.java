package org.tasker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class Node {
  ArrayList<Node> children = new ArrayList<Node>();
  Node parent;
  Rect r = new Rect(0, 0, 0, 0); // TODO: Rename this
  String label;
  Vec2 extents = new Vec2(0, 0);
  Vec2 leftNode;
  Vec2 offset = new Vec2(0, 0);
  Vec2 rightNode;
  boolean collapsed = false;
  boolean rightClicked = false;
  double height = 0;
  double width = 0;
  private HashMap<String, String> attributes = new HashMap<String, String>();

  Node(String label, Node parent) {
    this.label = label;
    this.parent = parent;
  }

  Node(String label) {
    this.label = label;
  }

  public void sort() {
    Collections.sort(children, (a, b) -> {
      boolean aDone = a.checkAttr("status", "done");
      boolean bDone = b.checkAttr("status", "done");

      if (aDone && !bDone) {
        return 1;
      } else if (!aDone && bDone) {
        return -1;
      }

      return a.label.compareTo(b.label);
    });

    for (Node child : children) {
      child.sort();
    }
  }

  public boolean checkAttr(String key, String value) {
    return attributes.containsKey(key) && attributes.get(key).equals(value);
  }

  public String fullyQualifiedName() {
    if (parent.parent == null) {
      return label;
    } else {
      return parent.fullyQualifiedName() + "	" + label;
    }
  }

  public void setAttributes(HashMap<String, String> attributes) {
    this.attributes = attributes;
  }

  public Node addChild(String label) {
    Node child = new Node(label, this);
    children.add(child);

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

    for (Node child : n.children) {
      child.parent = n;
    }

    return n;
  }

  public void putAttr(String key, String value) {
    attributes.put(key, value);
  }

  public void removeAttr(String key) {
    attributes.remove(key);
  }

  public void serialize(BufferedWriter writer) throws IOException {
    writer.write(fullyQualifiedName());
    for (String key : attributes.keySet()) {
      writer.write("#" + key + "=" + attributes.get(key) + ";");
    }
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
    } else if (n.attributes.containsKey("status") &&
               n.attributes.get("status").equals("done")) {
      Draw.rect(app, r, Color.GREEN, Color.WHITE);
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
