package org.tasker;

import javafx.scene.paint.Color;

public class ColorScheme {
  protected Color background;
  protected Color bezierCurve;
  protected Color bezierNode;
  protected Color borderBackground;
  protected Color border;
  protected Color grid1;
  protected Color grid2;
  protected Color heading;
  protected Color modified;
  protected Color nodeBackground;
  protected Color nodeBorder;
  protected Color nodeCompleted;
  protected Color nodeHover;
  protected Color nodeReparent;
  protected Color nodeSelected;
  protected Color previewBackground;
  protected Color text;

  protected void initializeDark() {
    background = Color.BLACK;
    bezierCurve = new Color(0.5, 0.5, 0.7, 0.5);
    bezierNode = new Color(0.5, 0.5, 0.7, 1);
    borderBackground = new Color(1, 1, 1, 0.05);
    border = new Color(0.5, 0.5, 0.5, 1);
    grid1 = new Color(0.1, 0.1, 0.1, 1);
    grid2 = new Color(0.2, 0.2, 0.2, 1);
    heading = new Color(0.5, 0.5, 0.7, 1);
    modified = new Color(0.5, 0.5, 0.7, 1);
    nodeBackground = Color.BLACK;
    nodeBorder = Color.GREY;
    nodeCompleted = new Color(0.2, 0.4, 0.2, 1);
    nodeHover = new Color(0.2, 0.2, 0.2, 1);
    nodeReparent = new Color(0.12, 0.22, 0.32, 1);
    nodeSelected = new Color(0.1, 0.2, 0.3, 1);
    previewBackground = new Color(0.2, 0.2, 0.2, 0.8);
    text = Color.WHITE;
  }

  protected void initializeLight() {
    background = Color.WHITE;
    bezierCurve = new Color(0.5, 0.5, 0.7, 0.5);
    bezierNode = new Color(0.5, 0.5, 0.7, 1);
    borderBackground = new Color(0, 0, 0, 0.05);
    border = Color.DARKGREY;
    grid1 = new Color(0.9, 0.9, 0.9, 1);
    grid2 = new Color(0.8, 0.8, 0.8, 1);
    heading = new Color(0.5, 0.5, 0.7, 1);
    modified = new Color(0.5, 0.5, 0.7, 1);
    nodeBackground = Color.WHITE;
    nodeBorder = Color.DARKGREY;
    nodeCompleted = new Color(0.8, 0.9, 0.8, 1);
    nodeHover = new Color(0.95, 0.95, 0.95, 1);
    nodeReparent = new Color(0.96, 0.88, 0.72, 1);
    nodeSelected = new Color(0.69, 0.85, 0.9, 1);
    previewBackground = new Color(0.8, 0.8, 0.8, 0.8);
    text = Color.BLACK;
  }
}
