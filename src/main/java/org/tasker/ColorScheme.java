package org.tasker;

import javafx.scene.paint.Color;

public class ColorScheme {
  protected Color backgroundColor;
  protected Color bezierCurveColor;
  protected Color bezierNodeColor;
  protected Color borderBackground;
  protected Color borderColor;
  protected Color gridColor1;
  protected Color gridColor2;
  protected Color nodeBackgroundColor;
  protected Color nodeBorderColor;
  protected Color nodeCompletedColor;
  protected Color nodeHoverColor;
  protected Color nodeReparentColor;
  protected Color nodeSelectedColor;
  protected Color nodeSelectedCompletedColor;
  protected Color previewBackgroundColor;
  protected Color textColor;

  protected void initializeDark() {
    backgroundColor = Color.BLACK;
    bezierCurveColor = new Color(0.5, 0.5, 0.7, 0.5);
    bezierNodeColor = new Color(0.5, 0.5, 0.7, 1);
    borderBackground = new Color(1, 1, 1, 0.05);
    borderColor = new Color(0.5, 0.5, 0.5, 1);
    gridColor1 = new Color(0.1, 0.1, 0.1, 1);
    gridColor2 = new Color(0.2, 0.2, 0.2, 1);
    nodeBackgroundColor = Color.BLACK;
    nodeBorderColor = Color.GREY;
    nodeCompletedColor = new Color(0.2, 0.4, 0.2, 1);
    nodeHoverColor = new Color(0.2, 0.2, 0.2, 1);
    nodeReparentColor = new Color(0.12, 0.22, 0.32, 1);
    nodeSelectedColor = new Color(0.1, 0.2, 0.3, 1);
    nodeSelectedCompletedColor = new Color(0.1, 0.3, 0.2, 1);
    previewBackgroundColor = new Color(0.2, 0.2, 0.2, 0.8);
    textColor = Color.WHITE;
  }

  protected void initializeLight() {
    backgroundColor = Color.WHITE;
    bezierCurveColor = new Color(0.5, 0.5, 0.7, 0.5);
    bezierNodeColor = new Color(0.5, 0.5, 0.7, 1);
    borderBackground = new Color(0, 0, 0, 0.05);
    borderColor = Color.DARKGREY;
    gridColor1 = new Color(0.9, 0.9, 0.9, 1);
    gridColor2 = new Color(0.8, 0.8, 0.8, 1);
    nodeBackgroundColor = Color.WHITE;
    nodeBorderColor = Color.DARKGREY;
    nodeCompletedColor = new Color(0.8, 0.9, 0.8, 1);
    nodeHoverColor = new Color(0.95, 0.95, 0.95, 1);
    nodeReparentColor = new Color(0.96, 0.88, 0.72, 1);
    nodeSelectedColor = new Color(0.69, 0.85, 0.9, 1);
    nodeSelectedCompletedColor = new Color(0.69, 0.9, 0.85, 1);
    previewBackgroundColor = new Color(0.8, 0.8, 0.8, 0.8);
    textColor = Color.BLACK;
  }
}
