package org.tasker;

import javafx.scene.paint.Color;

class Grid {
  public static void renderGrid(App app, Vec2 span, Vec2 margin, Color c) {
    app.gc.setStroke(c);

    for (double x = app.globalOffset.x % span.x; x <= app.dimensions.x; x += span.x) {
      app.gc.strokeLine(x, 0, x, app.dimensions.y);
    }

    for (double y = app.globalOffset.y % span.y; y <= app.dimensions.y; y += span.y) {
      app.gc.strokeLine(0, y, app.dimensions.x, y);
    }
  }
}
