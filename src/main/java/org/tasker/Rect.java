package org.tasker;

public class Rect {
  protected double x, y, w, h;

  protected boolean contains(Vec2 p) {
    return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
  }

  protected boolean intersects(Rect r) {
    return x < r.x + r.w && x + w > r.x && y < r.y + r.h && y + h > r.y;
  }

  protected Rect(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
}
