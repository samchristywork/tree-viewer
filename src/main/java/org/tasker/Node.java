package org.tasker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  Vec2 rightNode;
  boolean collapsed = false;
  boolean rightClicked = false;
  boolean show = true;
  private HashMap<String, String> attributes = new HashMap<String, String>();

  Node(String label, Node parent) {
    this.label = label;
    this.parent = parent;
  }

  Node(String label) {
    this.label = label;
  }

  public ArrayList<String> getFQNNs() {
    ArrayList<String> fqnns = new ArrayList<String>();
    fqnns.add(fullyQualifiedName());
    for (Node child : children) {
      ArrayList<String> childFQNNs = child.getFQNNs();
      for (String fqnn : childFQNNs) {
        fqnns.add(fqnn);
      }
    }
    return fqnns;
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
    if (parent == null) {
      return null;
    } else {
      if (parent.parent == null) {
        return label;
      } else {
        return parent.fullyQualifiedName() + "	" + label;
      }
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

              app.render();
            }
          });
        });
      }
    });
  }

  public void openFile(App app) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        String filename = attributes.get("file");
        if (filename == null) {
          filename = "files/" + label + ".md";
        }

        TextInputDialog dialog = new TextInputDialog(filename);
        dialog.setTitle("Open File");
        dialog.setContentText("Name:");
        dialog.showAndWait().ifPresent(name -> {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              String filename = name;

              Path path = Paths.get(filename);
              if (!Files.exists(path)) {
                try {
                  Files.createFile(path);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }

              attributes.put("file", filename);

              try {
                Runtime.getRuntime().exec("st -e nvim " + filename);
              } catch (Exception e) {
                e.printStackTrace();
              }

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
  public Node findNode(String[] fqnn) {
    if (fqnn.length == 0) {
      return null;
    }

    if (fqnn.length == 1) {
      if (label.equals(fqnn[0])) {
        return this;
      }
    }

    for (Node child : children) {
      String[] fqnnTail = new String[fqnn.length - 1];
      for (int i = 1; i < fqnn.length; i++) {
        fqnnTail[i - 1] = fqnn[i];
      }
      Node n = child.findNode(fqnnTail);
      if (n != null) {
        return n;
      }
    }

    return null;
  }

  private void drawText(App app, Vec2 extents) {
    double x = r.x + app.padding.x;
    double y = r.y + 3 * extents.y / 4 + app.padding.y;
    Draw.text(app, label, new Vec2(x, y), app.colorScheme.textColor);
  }

  private void drawRect(App app, Node n, Vec2 offset, Vec2 extents, Rect r) {
    Vec2 mousePos = new Vec2();
    mousePos.x = app.mouse.x - app.globalOffset.x;
    mousePos.y = app.mouse.y - app.globalOffset.y;
    if (r.contains(mousePos)) {
      ColorScheme cs = app.colorScheme;
      Draw.rect(app, r, cs.nodeBorderColor, cs.nodeHoverColor);

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
      if (!child.show) {
        continue;
      }

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
    drawRect(app, this, new Vec2(r.x, r.y), extents, r);
    drawText(app, extents);
  }
}
