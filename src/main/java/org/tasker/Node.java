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

  public String serialize() {
    String s = fullyQualifiedName();

    for (String key : attributes.keySet()) {
      s += "#" + key + "=" + attributes.get(key) + ";";
    }
    s += "\n";

    for (Node child : children) {
      s += child.serialize();
    }

    return s;
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

  public boolean isAncestor(Node node) {
    if (parent == node) {
      return true;
    }
    if (parent == null) {
      return false;
    }
    return parent.isAncestor(node);
  }

  // TODO: Inefficient
  public Node findNode(String fqnn) {
    if (fullyQualifiedName().equals(fqnn)) {
      return this;
    }
    for (Node child : children) {
      Node n = child.findNode(fqnn);
      if (n != null) {
        return n;
      }
    }
    return null;
  }

  private void drawText(App app, Vec2 offset, Vec2 extents) {
    Draw.text(app, label,
        new Vec2(offset.x, offset.y * app.lineHeight + 3 * extents.y / 4),
        app.colorScheme.textColor);
  }

  private void drawRect(App app, Node n, Vec2 offset, Vec2 extents, Rect r) {
    if (r.contains(new Vec2(app.mouse.x - app.globalOffset.x,
        app.mouse.y - app.globalOffset.y))) {
      Draw.rect(app, r, app.colorScheme.nodeBorderColor,
          app.colorScheme.nodeHoverColor);

      if (app.lmbClicked) {
        app.selectedNode = n;
      }

      if (app.rmbClicked) {
        app.rmbClicked = false;
        if (app.nodeToReparent == null) {
          app.nodeToReparent = n;
        } else {
          app.targetNode = n;
        }
      }
    } else if (n == app.nodeToReparent) {
      Draw.rect(app, r, app.colorScheme.nodeBorderColor,
          app.colorScheme.nodeReparentColor);
    } else if (n == app.selectedNode) {
      Draw.rect(app, r, app.colorScheme.nodeBorderColor,
          app.colorScheme.nodeSelectedColor);
    } else if (n.checkAttr("status", "done")) {
      Draw.rect(app, r, app.colorScheme.nodeBorderColor,
          app.colorScheme.nodeCompletedColor);
    } else {
      Draw.rect(app, r, app.colorScheme.nodeBorderColor,
          app.colorScheme.nodeBackgroundColor);
    }
  }

  public Vec2 getRightNode() {
    return new Vec2(r.x + r.w, r.y + r.h / 2);
  }

  public Vec2 getLeftNode() {
    return new Vec2(r.x, r.y + r.h / 2);
  }

  public Rect getSubtreeRect() {
    double x = r.x;
    double y = r.y;
    double w = r.w;
    double h = r.h;

    for (Node child : children) {
      Rect r = child.getSubtreeRect();
      if (r.x < x) {
        x = r.x;
      }
      if (r.y < y) {
        y = r.y;
      }
      if (r.x + r.w > x + w) {
        w = r.x + r.w - x;
      }
      if (r.y + r.h > y + h) {
        h = r.y + r.h - y;
      }
    }

    return new Rect(x, y, w, h);
  }

  public void draw(App app) {
    if (app.jumpMode) {
      if (app.jumpModeSelection == app.jumpModeIndex) {
        app.selectedNode = this;
        app.jumpModeSelection = 0;
        app.jumpMode = false;
      }
    }

    drawRect(app, this, offset, extents, r);
    drawText(app, offset, extents);

    if (app.jumpMode) {
      app.gc.setFont(Font.font("Arial", 9 * app.size));
      String s = "" + app.jumpModeIndex;
      app.jumpModeIndex++;
      Vec2 p = new Vec2(offset.x - app.padding.x + 2,
          offset.y * app.lineHeight + extents.y / 2 -
              app.padding.y + 2);
      Color c = app.colorScheme.jumpTextColor;
      Draw.text(app, s, p, c);
      app.gc.setFont(Font.font("Arial", 12 * app.size));
    }
  }
}
