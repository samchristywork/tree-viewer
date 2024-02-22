package org.tasker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Tree {
  protected Node root = new Node("root", null);
  protected Node current = root;
  private String state = "";

  protected void sort() { root.sort(); }

  protected boolean isModified() {
    if (serialize().equals(state)) {
      return false;
    }

    return true;
  }

  public void writeToFile(String filename, String backup) throws IOException {
    String s = serialize();

    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
      writer.write(s);
      writer.close();
    }

    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
      writer.write(s);
      writer.close();
    }

    state = s;
  }

  public String[] getFQNNs() {
    ArrayList<String> fqnns = root.getFQNNs();
    return fqnns.toArray(new String[0]);
  }

  public String serialize() {
    String s = "";
    for (Node child : root.children) {
      s += child.serialize();
    }
    return s;
  }

  private String[] readLinesFromFile(String filename) {
    String[] lines = new String[0];
    try {
      Path path = Paths.get(filename);
      lines = Files.readAllLines(path).toArray(new String[0]);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  public void readFromFile(String filename) {
    String[] lines = readLinesFromFile(filename);

    for (String line : lines) {
      Node n = root;

      String parts[] = line.split("#");
      String fqnn = parts[0];

      HashMap<String, String> attributes = new HashMap<String, String>();

      // Parse attributes
      if (parts.length == 2) {
        attributes = new HashMap<String, String>();
        String[] attribs = parts[1].split(";");
        for (String attribute : attribs) {
          String[] kv = attribute.split("=");
          attributes.put(kv[0], kv[1]);
        }
      }

      // Parse fully qualified node name
      String[] fields = fqnn.split("	");

      for (int i = 0; i < fields.length; i++) {
        String field = fields[i];
        Node child = n.findChild(field);
        if (child == null) {
          Node c = n.addChild(field);
          c.setAttributes(attributes);
        } else {
          if (i == fields.length - 1) {
            child.setAttributes(attributes);
          }
        }
        n = child;
      }
    }

    state = serialize();
  }

  public Node randomNode() {
    ArrayList<Node> nodes = root.getNodes();
    int n = nodes.size();
    int i = (int) (Math.random() * n);
    return nodes.get(i);
  }

  public Node findNode(String fqnn) {
    String fqnnParts[] = fqnn.split("	");
    for (Node child : root.children) {
      Node n = child.findNode(fqnnParts);
      if (n != null) {
        return n;
      }
    }

    return null;
  }

  public void deleteNode(Node n) {
    if (n.parent != null) {
      n.parent.children.remove(n);

      for (Node child : n.children) {
        child.parent = n.parent;
        n.parent.children.add(child);
      }
    }
  }

  private void test(Node n) {
    for (Node child : n.children) {
      if (child.parent != n) {
        System.out.println("Parent of " + child.label + " is not " + n.label);
      }
      test(child);
    }
  }

  public void test() {
    Node n = root;
    System.out.println("Testing tree");
    test(n);
  }
}
