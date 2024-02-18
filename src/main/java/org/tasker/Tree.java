package org.tasker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

class Tree {
  public Node root = new Node("root", null);
  public Node current = root;

  public void sort() {
    root.sort();
  }

  public void writeToFile(String filename, String backup) throws IOException {
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
      writer.write(serialize());
      writer.close();
    }

    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(backup));
      writer.write(serialize());
      writer.close();
    }
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
  }

  public Node findNode(String fqnn) {
    for (Node child : root.children) {
      Node n = child.findNode(fqnn);
      if (n != null) {
        return n;
      }
    }

    return null;
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
