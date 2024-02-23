package org.tasker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class Event {
  private static String sep = "→";

  private static Object bindings[][] = {
      { "0", KeyCode.DIGIT0, "Go to root" },
      { "1-9", null, "Jump to the nth child of the current node" },
      { "Add", KeyCode.ADD, "Zoom in" },
      { "Subtract", KeyCode.SUBTRACT, "Zoom out" },
      { "Enter", KeyCode.ENTER, "Set current node" },
      { "Escape", KeyCode.ESCAPE, "Exit" },
      { "Forward Slash", KeyCode.SLASH, "Find node dialog" },
      { "Space", KeyCode.SPACE, "Select event dialog" },
      { "Tab", KeyCode.TAB, "Show/hide completed nodes" },
      { "A", KeyCode.A, "Select random node" },
      { "C", KeyCode.C, "View list of changes to the tree" },
      { "H", KeyCode.H, "Move left" },
      { "J", KeyCode.J, "Move down" },
      { "K", KeyCode.K, "Move up" },
      { "L", KeyCode.L, "Move right" },
      { "M", KeyCode.M, "Mark a node as done" },
      { "N", KeyCode.N, "Add a new node" },
      { "O", KeyCode.O, "Open node file" },
      { "P", KeyCode.P, "Add a parent node" },
      { "Q", KeyCode.Q, "Exit" },
      { "R", KeyCode.R, "Rename the selected node" },
      { "S", KeyCode.S, "Save the tree" },
      { "T", KeyCode.T, "Toggle compact mode" },
      { "U", KeyCode.U, "Show usage" },
      { "X", KeyCode.X, "Delete the selected node" },
      { "Z", KeyCode.Z, "Pan to selected node" },
      { "", null, "Test" },
      { "", null, "Toggle dark mode" },
  };

  private static void handleEvent(App app, String event) {
    System.out.println("Event: " + event);

    switch (event) {
      case "Add a new node":
        app.selectedNode.addNode(app);
        break;
      case "Delete the selected node":
        Node parent = app.selectedNode.parent;
        app.tree.deleteNode(app.selectedNode);
        app.selectedNode = parent;
        zoom(app);
        break;
      case "Exit":
        if (close(app)) {
          System.exit(0);
        }
        break;
      case "Find node dialog":
        ArrayList<String> nodeArray = app.tree.current.getFQNNs();
        String[] nodes = new String[nodeArray.size()];
        nodes = nodeArray.toArray(nodes);
        String fqnn = showDialog(nodes, "Find Node");
        Node n = app.tree.findNode(fqnn);
        if (n != null) {
          app.selectedNode = n;
        }
        zoom(app);
        break;
      case "Go to root":
        app.selectedNode = app.tree.root;
        zoom(app);
        break;
      case "Add a parent node":
        app.selectedNode = app.selectedNode.insert("new");
        app.selectedNode = app.selectedNode.parent;
        app.render();
        zoom(app);
        break;
      case "Mark a node as done":
        if (app.selectedNode.checkAttr("status", "done")) {
          app.selectedNode.removeAttr("status");
        } else {
          app.selectedNode.putAttr("status", "done");
        }
        app.render();
        zoom(app);
        break;
      case "Move left":
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            app.selectedNode = app.selectedNode.parent;
          }
        }
        zoom(app);
        break;
      case "Move down":
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            int index = app.selectedNode.parent.children.indexOf(app.selectedNode);
            if (index < app.selectedNode.parent.children.size() - 1) {
              app.selectedNode = app.selectedNode.parent.children.get(index + 1);
            } else {
              app.selectedNode = app.selectedNode.parent.children.get(0);
            }
          }
        }
        zoom(app);
        break;
      case "Move up":
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            int index = app.selectedNode.parent.children.indexOf(app.selectedNode);
            if (index > 0) {
              app.selectedNode = app.selectedNode.parent.children.get(index - 1);
            } else {
              app.selectedNode = app.selectedNode.parent.children.get(
                  app.selectedNode.parent.children.size() - 1);
            }
          }
        }
        zoom(app);
        break;
      case "Move right":
        if (app.selectedNode != null) {
          if (app.selectedNode.children.size() > 0) {
            app.selectedNode = app.selectedNode.children.get(0);
          }
        }
        zoom(app);
        break;
      case "Open node file":
        app.selectedNode.openFile(app);
        break;
      case "Pan to selected node":
        zoom(app);
        break;
      case "Rename the selected node":
        app.selectedNode.rename(app);
        break;
      case "Save the tree":
        try {
          String dateTime = LocalDateTime.now().toString();
          app.tree.writeToFile(app.workingDirectory + "/save.tree",
              app.workingDirectory + "/backups/" + dateTime +
                  ".tree");
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case "Select event dialog":
        String[] commands = new String[bindings.length];
        for (int i = 0; i < bindings.length; i++) {
          commands[i] = (String) bindings[i][2];
        }
        String command = showDialog(commands, "Run Command");
        handleEvent(app, command);
        break;
      case "Select random node":
        app.selectedNode = app.tree.randomNode();
        zoom(app);
        break;
      case "Set current node":
        app.tree.current = app.selectedNode;
        zoom(app);
        break;
      case "Show usage":
        usage();
        break;
      case "Test":
        app.tree.test();
        break;
      case "Toggle compact mode":
        app.compact = !app.compact;
        break;
      case "Toggle dark mode":
        app.darkMode = !app.darkMode;
        break;
      case "Show/hide completed nodes":
        app.showDone = !app.showDone;
        break;
      case "View list of changes to the tree":
        app.tree.viewChanges();
        break;
      case "Zoom in":
        app.size *= 1.1;
        break;
      case "Zoom out":
        app.size *= 0.9;
        break;
      default:
        System.out.println("Unknown event: " + event);
        break;
    }

    app.render();
  }

  protected static void zoom(App app) {
    app.render();
    app.globalOffset.x = -app.selectedNode.bounds.x;
    app.globalOffset.y = -app.selectedNode.bounds.y;
    app.globalOffset.x += app.dimensions.x / 4;
    app.globalOffset.y += app.dimensions.y / 4;
  }

  protected static boolean close(App app) {
    if (app.tree == null) {
      return true;
    }

    try {
      BufferedWriter writer = new BufferedWriter(
          new FileWriter(app.workingDirectory + "/datastore"));
      writer.write("darkMode=" + app.darkMode + "\n");
      writer.write("showDone=" + app.showDone + "\n");
      writer.write("globalOffsetX=" + app.globalOffset.x + "\n");
      writer.write("globalOffsetY=" + app.globalOffset.y + "\n");
      if (app.selectedNode != null) {
        String selectedFQNN = app.selectedNode.fullyQualifiedName();
        if (selectedFQNN != null) {
          writer.write("selectedNodeFQNN=" + selectedFQNN + "\n");
        }
      }
      if (app.tree.current != null && app.tree.current != app.tree.root) {
        String currentFQNN = app.tree.current.fullyQualifiedName();
        if (currentFQNN != null) {
          writer.write("currentNodeFQNN=" + currentFQNN + "\n");
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (app.tree.isModified()) {
      Alert alert = new Alert(AlertType.CONFIRMATION,
          "You have unsaved work. Quit anyway?",
          ButtonType.YES, ButtonType.NO);
      alert.setTitle("Confirmation Dialog");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES) {
        return true;
      }
    } else {
      return true;
    }
    return false;
  }

  private static boolean caseInsensitiveCharMatch(char a, char b) {
    return Character.toLowerCase(a) == Character.toLowerCase(b);
  }

  private static boolean fuzzyMatchChoices(String choice, String newV) {
    if (newV.length() == 0) {
      return true;
    }

    int j = 0;
    for (int i = 0; i < choice.length(); i++) {
      if (j == newV.length()) {
        return true;
      }
      if (caseInsensitiveCharMatch(choice.charAt(i), newV.charAt(j))) {
        j++;
      }
    }
    if (j == newV.length()) {
      return true;
    }

    return false;
  }

  private static boolean exactMatchChoices(String choice, String newV) {
    return choice.toLowerCase().contains(newV.toLowerCase());
  }

  private static void updateChoices(TextField textField, VBox availableChoices,
      String[] choices, String newV) {
    ArrayList<Label> exactMatches = new ArrayList<>();
    ArrayList<Label> fuzzyMatches = new ArrayList<>();

    availableChoices.getChildren().clear();
    for (String choice : choices) {
      if (choice != null) {
        String c = choice.replace("\t", sep);
        if (exactMatchChoices(c, newV)) {
          exactMatches.add(new Label(c));
        } else if (fuzzyMatchChoices(c, newV)) {
          fuzzyMatches.add(new Label(c));
        }
      }
    }

    for (Label label : exactMatches) {
      label.setStyle("-fx-font-weight: bold;");
      availableChoices.getChildren().add(label);
    }
    for (Label label : fuzzyMatches) {
      availableChoices.getChildren().add(label);
    }
  }

  private static String showDialog(String[] choices, String title) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle(title);

    TextField textField = new TextField();
    VBox availableChoices = new VBox();

    textField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> o, String oldV,
          String newV) {
        updateChoices(textField, availableChoices, choices, newV);
      }
    });

    updateChoices(textField, availableChoices, choices, "");

    textField.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.UP) {
        ObservableList<javafx.scene.Node> children = availableChoices.getChildren();
        javafx.scene.Node topChoice = children.get(0);
        children.remove(0);
        children.add(topChoice);
      } else if (e.getCode() == KeyCode.DOWN) {
        ObservableList<javafx.scene.Node> children = availableChoices.getChildren();
        javafx.scene.Node bottomChoice = children.get(children.size() - 1);
        children.remove(children.size() - 1);
        children.add(0, bottomChoice);
      }
    });

    textField.onActionProperty().set(e -> {
      ObservableList<javafx.scene.Node> children = availableChoices.getChildren();
      if (children.size() > 0) {
        textField.setText(((Label) children.get(0)).getText());
      } else {
        textField.setText("");
      }
      dialog.close();
    });

    VBox vBox = new VBox(10, textField, availableChoices);
    vBox.setPadding(new Insets(20, 20, 20, 20));
    dialog.getDialogPane().setContent(vBox);

    ButtonType buttonTypeOk = new ButtonType("OK");
    dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
    dialog.setOnShown(e -> textField.requestFocus());
    dialog.showAndWait();

    return textField.getText().replace(sep, "\t");
  }

  public static void usage() {
    String content = "";
    for (Object[] key : bindings) {
      if (key[0] != "") {
        content += key[0] + ": " + key[2] + "\n";
      }
    }

    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Help");
    alert.setHeaderText("Tasker");
    alert.setContentText(content);
    alert.showAndWait();
  }

  protected static void keyPressHandler(App app, KeyEvent key) {
    if (app.tree == null) {
      return;
    }

    KeyCode code = key.getCode();

    switch (code) {
      case DIGIT1:
      case DIGIT2:
      case DIGIT3:
      case DIGIT4:
      case DIGIT5:
      case DIGIT6:
      case DIGIT7:
      case DIGIT8:
      case DIGIT9:
        if (app.selectedNode == null) {
          break;
        }
        int index = code.ordinal() - 25;
        if (app.selectedNode.children.size() > index) {
          app.selectedNode = app.selectedNode.children.get(index);
        } else if (app.selectedNode.children.size() > 0) {
          app.selectedNode = app.selectedNode.children.get(app.selectedNode.children.size() - 1);
        }
        zoom(app);
        app.render();
        break;
      default:
        break;
    }

    boolean handled = false;
    for (Object[] binding : bindings) {
      if (code == (KeyCode) binding[1]) {
        handleEvent(app, (String) binding[2]);
        handled = true;
      }
    }

    if (!handled) {
      System.out.println("Key Pressed: " + key.getCode());
    }
  }
}
