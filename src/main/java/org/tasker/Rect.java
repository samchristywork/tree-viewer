package org.tasker;

class Rect {
  public double x, y, w, h;

  public boolean contains(Vec2 p) {
    return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
  }

  public boolean intersects(Rect r) {
    return x < r.x + r.w && x + w > r.x && y < r.y + r.h && y + h > r.y;
  }

  public Rect(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
}
