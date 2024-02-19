package org.tasker;

import javafx.scene.paint.Color;

class Draw {
  public static void bezier(App app, Vec2 a, Vec2 b, Vec2 c, Vec2 d, Color col,
      double lineWidth) {
    double ax = a.x + app.globalOffset.x;
    double ay = a.y + app.globalOffset.y;
    double bx = b.x + app.globalOffset.x;
    double by = b.y + app.globalOffset.y;
    double cx = c.x + app.globalOffset.x;
    double cy = c.y + app.globalOffset.y;
    double dx = d.x + app.globalOffset.x;
    double dy = d.y + app.globalOffset.y;

    app.gc.setStroke(col);
    app.gc.setLineWidth(lineWidth);
    app.gc.beginPath();
    app.gc.moveTo(ax, ay);
    app.gc.bezierCurveTo(bx, by, cx, cy, dx, dy);
    app.gc.stroke();
    app.gc.setLineWidth(1);
  }

  public static void line(App app, Vec2 a, Vec2 b, Color c) {
    app.gc.setStroke(c);

    double ax = a.x + app.globalOffset.x;
    double ay = a.y + app.globalOffset.y;
    double bx = b.x + app.globalOffset.x;
    double by = b.y + app.globalOffset.y;

    app.gc.strokeLine(ax, ay, bx, by);
  }

  public static void rect(App app, Rect r, Color fg, Color bg) {
    app.gc.setFill(bg);
    app.gc.setStroke(fg);

    double rx = r.x + app.globalOffset.x;
    double ry = r.y + app.globalOffset.y;

    app.gc.fillRect(rx, ry, r.w, r.h);
    app.gc.strokeRect(rx, ry, r.w, r.h);
  }

  public static void text(App app, String s, Vec2 p, Color c) {
    app.gc.setFill(c);

    double px = p.x + app.globalOffset.x;
    double py = p.y + app.globalOffset.y;

    app.gc.fillText(s, px, py);
  }
}
