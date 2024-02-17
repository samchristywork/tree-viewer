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

  public void sort() { root.sort(); }

  public void serialize(String filename) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
    for (Node child : root.children) {
      child.serialize(writer);
    }
    writer.close();
  }

  public void serialize(String filename, String backup) throws IOException {
    serialize(backup);
    serialize(filename);
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

      if (parts.length == 2) {
        attributes = new HashMap<String, String>();
        String[] attribs = parts[1].split(";");
        for (String attribute : attribs) {
          String[] kv = attribute.split("=");
          attributes.put(kv[0], kv[1]);
        }
      }

      String[] fields = fqnn.split("	");

      for (int i = 0; i < fields.length; i++) {
        String field = fields[i];
        Node child = n.findChild(field);
        if (child == null) {
          Node c = n.addChild(field);
          c.attributes = attributes;
        } else {
          if (i == fields.length - 1) {
            child.attributes = attributes;
          }
        }
        n = child;
      }
    }
  }
}
