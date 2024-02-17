package org.tasker;

import javafx.scene.paint.Color;

class Grid {
  public static void renderGrid(App app, Vec2 span, Vec2 margin, Color c) {
    Vec2 start = new Vec2(-margin.x, -margin.y);
    Vec2 end = new Vec2(app.dimensions.x + margin.x, app.dimensions.y + margin.y);

    for (double x = start.x; x <= end.x; x += span.x) {
      Draw.line(app, new Vec2(x, start.y), new Vec2(x, end.y), c);
    }

    for (double y = start.y; y <= end.y; y += span.y) {
      Draw.line(app, new Vec2(start.x, y), new Vec2(end.x, y), c);
    }
  }
}
