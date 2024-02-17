package org.tasker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

class Event {
  public static final Object[][] keyMap = {
      { "ENTER", "Change the root of the tree to the selected node" },
      { "ADD", "Zoom in" },
      { "SUBTRACT", "Zoom out" },
      { "N", "Add a new node" },
      { "R", "Rename the selected node" },
      { "U", "Show usage" },
      { "S", "Save the tree" },
      { "Q", "Quit" },
      { "ESCAPE", "Quit" },
      { "P", "Insert a new node" },
      { "H", "Move left" },
      { "J", "Move down" },
      { "K", "Move up" },
      { "L", "Move right" },
      { "SPACE", "Go to the root node" },
  };

  public static void usage() {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Help");
    alert.setHeaderText("Tasker");

    String content = "";
    for (Object[] key : keyMap) {
      content += key[0] + ": " + key[1] + "\n";
    }
    alert.setContentText(content);

    alert.showAndWait();
  }

  public static void keyPressHandler(App app, KeyEvent key) {
    KeyCode code = key.getCode();

    switch (code) {
      case ENTER:
        app.tree.current = app.selectedNode;
        app.render();
        break;
      case ADD:
        app.size *= 1.1;
        app.render();
        break;
      case SUBTRACT:
        app.size *= 0.9;
        app.render();
        break;
      case N:
        app.selectedNode.addNode(app);
        break;
      case R:
        app.selectedNode.rename(app);
        break;
      case U:
        usage();
        break;
      case S:
        try {
          String dateTime = LocalDateTime.now().toString();
          app.tree.serialize("save.tree", "backups/" + dateTime + ".tree");
          app.modified = false;
        } catch (IOException e) {
          e.printStackTrace();
        }
        app.render();
        break;
      case Q:
      case ESCAPE:
        if (app.modified) {
          Alert alert = new Alert(AlertType.CONFIRMATION,
              "You have unsaved work. Quit anyway?",
              ButtonType.YES, ButtonType.NO);
          alert.setTitle("Confirmation Dialog");

          Optional<ButtonType> result = alert.showAndWait();
          if (result.isPresent() && result.get() == ButtonType.YES) {
            System.exit(0);
          }
        } else {
          System.exit(0);
        }
        break;
      case P:
        app.selectedNode = app.selectedNode.insert("new");

        app.modified = true;
        app.render();
        break;
      case H:
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            app.selectedNode = app.selectedNode.parent;
          }
        }
        app.render();
        break;
      case L:
        if (app.selectedNode != null) {
          if (app.selectedNode.children.size() > 0) {
            app.selectedNode = app.selectedNode.children.get(0);
          }
        }
        app.render();
        break;
      case J:
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            int index = app.selectedNode.parent.children.indexOf(app.selectedNode);
            if (index < app.selectedNode.parent.children.size() - 1) {
              app.selectedNode = app.selectedNode.parent.children.get(index + 1);
            }
          }
        }
        app.render();
        break;
      case K:
        if (app.selectedNode != null) {
          if (app.selectedNode.parent != null) {
            int index = app.selectedNode.parent.children.indexOf(app.selectedNode);
            if (index > 0) {
              app.selectedNode = app.selectedNode.parent.children.get(index - 1);
            }
          }
        }
        app.render();
        break;
      case SPACE:
        app.tree.current = app.tree.root;
        app.render();
        break;
      default:
        System.out.println("Key Pressed: " + key.getCode());
        break;
    }
  }
}
