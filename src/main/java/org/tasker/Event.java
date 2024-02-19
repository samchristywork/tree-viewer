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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

class Event {
  static final Object[][] keyMap = {
      { "0", "Jump to the root node" },
      { "1-9", "Jump to the nth child of the current node" },
      { "ADD", "Zoom in" },
      { "SUBTRACT", "Zoom out" },
      { "ENTER", "Change the root of the tree" },
      { "ESCAPE", "Quit" },
      { "SPACE", "Open the command menu" },
      { "TAB", "Show/hide finished nodes" },
      { "F", "Open the find node dialog" },
      { "H", "Move left" },
      { "J", "Move down" },
      { "K", "Move up" },
      { "L", "Move right" },
      { "M", "Mark a node as done" },
      { "N", "Add a new node" },
      { "O", "Open node file" },
      { "P", "Insert a new node" },
      { "Q", "Quit" },
      { "R", "Rename the selected node" },
      { "S", "Save the tree" },
      { "U", "Show usage" },
      { "Z", "Center the view on the selected node" },
  };

  static String commands[] = {
      "Exit",
      "Go to root",
      "Toggle dark mode",
      "Test",
  };

  static boolean caseInsensitiveCharMatch(char a, char b) {
    return Character.toLowerCase(a) == Character.toLowerCase(b);
  }

  static boolean filterChoices(String choice, String newV) {
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

  static void updateChoices(TextField textField, VBox availableChoices,
      String[] choices, String newV) {
    availableChoices.getChildren().clear();
    for (String choice : choices) {
      if (choice != null) {
        String c = choice.replace("\t", "→");
        if (filterChoices(c, newV)) {
          availableChoices.getChildren().add(new Label(c));
        }
      }
    }
  }

  static String showDialog(String[] choices) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Text Update Window");

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

    return textField.getText().replace("→", "\t");
  }

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

    if (code.isDigitKey()) {
      app.jumpModeSelection *= 10;
      app.jumpModeSelection += code.ordinal() - 24;
      app.render();
      return;
    }

    switch (code) {
      case ADD:
        app.size *= 1.1;
        app.render();
        break;
      case BACK_SPACE:
        app.jumpModeSelection /= 10;
        app.render();
        break;
      case ENTER:
        app.tree.current = app.selectedNode;
        app.render();
        break;
      case SPACE:
        app.tree.current = app.tree.root;
        app.render();
        break;
      case SUBTRACT:
        app.size *= 0.9;
        app.render();
        break;
      case TAB:
        app.showDone = !app.showDone;
        app.render();
        break;
      case D:
        app.darkMode = !app.darkMode;
        app.render();
        break;
      case F:
        app.jumpMode = !app.jumpMode;
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
      case L:
        if (app.selectedNode != null) {
          if (app.selectedNode.children.size() > 0) {
            app.selectedNode = app.selectedNode.children.get(0);
          }
        }
        app.render();
        break;
      case M:
        if (app.selectedNode.checkAttr("status", "done")) {
          app.selectedNode.removeAttr("status");
        } else {
          app.selectedNode.putAttr("status", "done");
        }

        app.modified = true;
        app.render();
        break;
      case N:
        app.selectedNode.addNode(app);
        break;
      case P:
        app.selectedNode = app.selectedNode.insert("new");

        app.modified = true;
        app.render();
        break;
      case R:
        app.selectedNode.rename(app);
        break;
      case S:
        try {
          String dateTime = LocalDateTime.now().toString();
          app.tree.writeToFile("save.tree", "backups/" + dateTime + ".tree");
          app.modified = false;
        } catch (IOException e) {
          e.printStackTrace();
        }
        app.render();
        break;
      case T:
        app.tree.test();
        break;
      case U:
        usage();
        break;
      case Z:
        app.globalOffset.x = -app.selectedNode.r.x;
        app.globalOffset.y = -app.selectedNode.r.y;
        app.globalOffset.x += app.dimensions.x / 2;
        app.globalOffset.y += app.dimensions.y / 2;
        app.globalOffset.x -= app.selectedNode.r.w / 2;
        app.globalOffset.y -= app.selectedNode.r.h / 2;
        app.render();
        break;
      case Q:
      case ESCAPE:
        try {
          BufferedWriter writer = new BufferedWriter(new FileWriter("datastore"));
          String fqnn = app.selectedNode.fullyQualifiedName();
          writer.write("darkMode=" + app.darkMode + "\n");
          writer.write("showDone=" + app.showDone + "\n");
          writer.write("selectedNodeFQNN=" + fqnn + "\n");
          writer.write("globalOffsetX=" + app.globalOffset.x + "\n");
          writer.write("globalOffsetY=" + app.globalOffset.y + "\n");
          writer.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

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
      default:
        System.out.println("Key Pressed: " + key.getCode());
        break;
    }
  }
}
