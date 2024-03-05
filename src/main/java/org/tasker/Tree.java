package org.tasker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Tree {
  protected Node root = new Node("root", null, false);
  protected Node current = root;
  private String state = "";
  private String fullyQualifiedState = "";

  private String decodeString(String s) {
    try {
      String decodedString = java.net.URLDecoder.decode(s, "UTF-8");
      return decodedString;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 is not supported", e);
    }
  }

  protected void sort() {
    root.sort();
  }

  protected boolean isModified() {
    if (serializeYML().equals(state)) {
      return false;
    }

    return true;
  }

  private static void changesDialog(ArrayList<String> addedLines,
      ArrayList<String> removedLines) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Changes");

    VBox added = new VBox();
    VBox removed = new VBox();

    String sep = "→";
    for (String line : addedLines) {
      String l = line.replace("\t", sep);
      added.getChildren().add(new Label(l));
    }

    for (String line : removedLines) {
      String l = line.replace("\t", sep);
      removed.getChildren().add(new Label(l));
    }

    if (addedLines.size() == 0) {
      added.getChildren().add(new Label("None"));
    }

    if (removedLines.size() == 0) {
      removed.getChildren().add(new Label("None"));
    }

    VBox vBox = new VBox(10, new Label("Added:"), added, new Label("Removed:"),
        removed);
    vBox.setPadding(new Insets(20, 20, 20, 20));
    vBox.getChildren().get(0).setStyle("-fx-font-weight: bold;");
    vBox.getChildren().get(2).setStyle("-fx-font-weight: bold;");

    dialog.getDialogPane().setContent(vBox);

    ButtonType buttonTypeOk = new ButtonType("OK");
    dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
    dialog.showAndWait();
  }

  protected void viewChanges() {
    String s = serialize();
    String[] lines = s.split("\n");
    ArrayList<String> added = new ArrayList<String>();
    ArrayList<String> removed = new ArrayList<String>();

    for (String line : lines) {
      if (fullyQualifiedState.indexOf(line) == -1) {
        added.add(line);
      }
    }

    lines = fullyQualifiedState.split("\n");
    for (String line : lines) {
      if (s.indexOf(line) == -1) {
        removed.add(line);
      }
    }

    changesDialog(added, removed);
  }

  protected void writeToYAMLFile(String filename, String backup)
      throws IOException {
    String s = serializeYML();

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

    state = serializeYML();
    fullyQualifiedState = serialize();
  }

  private String serializeYML() {
    String s = "";
    for (Node child : root.children) {
      s += child.serializeYML(0);
    }
    return s;
  }

  private String serialize() {
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

  protected void readFromYMLFile(String filename) {
    String[] lines = readLinesFromFile(filename);

    Node n = root;
    int depth = 0;
    for (String line : lines) {
      String[] parts = line.split(":");
      String attributes = "";
      if (parts.length == 2) {
        attributes = parts[1];
      }
      String label = decodeString(parts[0]).strip().replace("\"", "");
      int d = (line.length() - line.stripLeading().length()) / 2;
      if (d == depth) {
        n.children.add(new Node(label, n, attributes, false));
      } else if (d == depth + 1) {
        n = n.children.get(n.children.size() - 1);
        n.children.add(new Node(label, n, attributes, false));
        depth++;
      } else if (d < depth) {
        for (int i = 0; i < depth - d; i++) {
          n = n.parent;
        }
        if (n == null) {
          n = root;
        }
        n.children.add(new Node(label, n, attributes, false));
        depth = d;
      }
    }

    state = serializeYML();
    fullyQualifiedState = serialize();
  }

  protected Node randomNode() {
    ArrayList<Node> nodes = root.getNodes();
    int n = nodes.size();
    int i = (int) (Math.random() * n);
    return nodes.get(i);
  }

  protected Node findNode(String fqnn) {
    String fqnnParts[] = fqnn.split("	");
    for (Node child : root.children) {
      Node n = child.findNode(fqnnParts);
      if (n != null) {
        return n;
      }
    }

    return null;
  }

  protected int countNodes() {
    return root.countNodes();
  }

  protected void deleteNode(Node n) {
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

  protected void test() {
    Node n = root;
    System.out.println("Testing tree");
    test(n);
  }
}
