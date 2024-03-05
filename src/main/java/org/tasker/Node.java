package org.tasker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;

public class Node {
  private HashMap<String, String> attributes = new HashMap<String, String>();
  protected ArrayList<Node> children = new ArrayList<Node>();
  protected ArrayList<String> links = new ArrayList<String>();
  protected Node parent;
  protected Rect bounds = new Rect(0, 0, 0, 0);
  protected String label;
  protected Vec2 extents = new Vec2(0, 0);
  protected boolean show = true;

  protected Node(String label, Node parent) {
    this.label = label;
    this.parent = parent;
  }

  protected Node(String label, Node parent, String attributes) {
    this.label = label;
    this.parent = parent;

    if (attributes.equals("")) {
      return;
    }

    String[] attrs = attributes.split(";");
    for (String attr : attrs) {
      String[] kv = attr.split("=");
      this.attributes.put(decodeString(kv[0]), decodeString(kv[1]));
    }
  }

  protected Node(String label) {
    this.label = label;
  }

  private String encodeString(String s) {
    try {
      String encodedString = URLEncoder.encode(s, "UTF-8");
      return encodedString;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is not supported", e);
    }
  }

  private String decodeString(String s) {
    try {
      String decodedString = java.net.URLDecoder.decode(s, "UTF-8");
      return decodedString;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is not supported", e);
    }
  }

  protected ArrayList<String> getFQNNs() {
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

  protected void sort() {
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

  protected boolean checkAttr(String key, String value) {
    return attributes.containsKey(key) && attributes.get(key).equals(value);
  }

  protected String fullyQualifiedName() {
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

  protected void setAttributes(HashMap<String, String> attributes) {
    this.attributes = attributes;
  }

  protected Node addChild(String label) {
    Node child = new Node(label, this);
    children.add(child);

    return child;
  }

  protected ArrayList<Node> getNodes() {
    ArrayList<Node> nodes = new ArrayList<Node>();
    nodes.add(this);
    for (Node child : children) {
      ArrayList<Node> childNodes = child.getNodes();
      for (Node n : childNodes) {
        nodes.add(n);
      }
    }
    return nodes;
  }

  protected Node findChild(String label) {
    for (Node child : children) {
      if (child.label.equals(label)) {
        return child;
      }
    }
    return null;
  }

  protected Node insert(String label) {
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

  protected void putAttr(String key, String value) {
    attributes.put(key, value);
  }

  protected void removeAttr(String key) {
    attributes.remove(key);
  }

  protected String serialize() {
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

  protected String serializeYML(int depth) {
    String s = "";
    for (int i = 0; i < depth; i++) {
      s += "  ";
    }
    s += "\"" + encodeString(label) + "\":";

    for (String key : attributes.keySet()) {
      s += encodeString(key) + "=" + encodeString(attributes.get(key)) + ";";
    }

    s += "\n";

    for (Node child : children) {
      s += child.serializeYML(depth + 1);
    }

    return s;
  }

  protected void addLink(Node n) {
    links.add(n.fullyQualifiedName());
  }

  protected void addNode(App app) {
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
              app.zoom();
            }
          });
        });
      }
    });
  }

  protected void rename(App app) {
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
              app.zoom();
            }
          });
        });
      }
    });
  }

  protected void openFile(App app) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        String filename = app.workingDirectory + "/files/" + label + ".md";
        try {
          ArrayList<String> cmd = new ArrayList<String>();
          for (String c : app.editorCommand.split(" ")) {
            cmd.add(c);
          }
          cmd.add(filename);
          Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        } catch (Exception e) {
          e.printStackTrace();
        }

        app.render();
      }
    });
  }

  protected boolean isAncestor(Node node) {
    if (parent == node) {
      return true;
    }
    if (parent == null) {
      return false;
    }
    return parent.isAncestor(node);
  }

  protected Node findNode(String[] fqnn) {
    if (fqnn.length == 0) {
      return null;
    }

    if (fqnn.length == 1) {
      if (label.equals(fqnn[0])) {
        return this;
      }
    }

    for (Node child : children) {
      if (label.equals(fqnn[0])) {
        String[] fqnnTail = new String[fqnn.length - 1];
        for (int i = 1; i < fqnn.length; i++) {
          fqnnTail[i - 1] = fqnn[i];
        }
        Node n = child.findNode(fqnnTail);
        if (n != null) {
          return n;
        }
      }
    }

    return null;
  }

  private void drawText(App app, Vec2 extents) {
    double x = bounds.x + app.render.padding.x;
    double y = bounds.y + 3 * extents.y / 4 + app.render.padding.y;
    Draw.text(app, label, new Vec2(x, y), app.render.colorScheme.textColor);
  }

  private Color combine(ArrayList<Color> colors) {
    double r = 0;
    double g = 0;
    double b = 0;
    double a = 0;

    for (Color c : colors) {
      r += c.getRed();
      g += c.getGreen();
      b += c.getBlue();
      a += c.getOpacity();
    }

    r /= colors.size();
    g /= colors.size();
    b /= colors.size();
    a /= colors.size();

    return new Color(r, g, b, a);
  }

  private void drawRect(App app, Node n, Vec2 offset, Vec2 extents, Rect r) {
    Vec2 mousePos = new Vec2();
    mousePos.x = app.render.mouse.x - app.render.globalOffset.x;
    mousePos.y = app.render.mouse.y - app.render.globalOffset.y;
    if (r.contains(mousePos)) {
      ColorScheme cs = app.render.colorScheme;
      Draw.rect(app, r, cs.nodeBorderColor, cs.nodeHoverColor, 1);

      if (app.render.mouse.lmbClicked) {
        app.selectedNode = n;
      }

      if (app.render.mouse.rmbClicked) {
        app.render.mouse.rmbClicked = false;
        if (app.nodeToReparent == null) {
          app.nodeToReparent = n;
        } else {
          app.targetNode = n;
        }
      }
    } else if (n == app.nodeToReparent) {
      Draw.rect(app, r, app.render.colorScheme.nodeBorderColor,
          app.render.colorScheme.nodeReparentColor, 1);
    } else if (n == app.selectedNode && n.checkAttr("status", "done")) {
      Draw.rect(app, r, app.render.colorScheme.nodeBorderColor,
          app.render.colorScheme.nodeSelectedCompletedColor, 1);
    } else if (n == app.selectedNode) {
      Draw.rect(app, r, app.render.colorScheme.nodeBorderColor,
          app.render.colorScheme.nodeSelectedColor, 1);
    } else if (n.checkAttr("status", "done")) {
      Draw.rect(app, r, app.render.colorScheme.nodeBorderColor,
          app.render.colorScheme.nodeCompletedColor, 1);
    } else {
      Draw.rect(app, r, app.render.colorScheme.nodeBorderColor,
          app.render.colorScheme.nodeBackgroundColor, 1);
    }
  }

  protected Vec2 getRightNode() {
    return new Vec2(bounds.x + bounds.w, bounds.y + bounds.h / 2);
  }

  protected Vec2 getLeftNode() {
    return new Vec2(bounds.x, bounds.y + bounds.h / 2);
  }

  protected Rect getSubtreeRect() {
    double x = bounds.x;
    double y = bounds.y;
    double w = bounds.w;
    double h = bounds.h;

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

  protected void draw(App app) {
    drawRect(app, this, new Vec2(bounds.x, bounds.y), extents, bounds);
    drawText(app, extents);
  }
}
