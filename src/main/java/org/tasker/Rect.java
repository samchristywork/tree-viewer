package org.tasker;

class Rect {
  public double x, y, w, h;

  public boolean contains(Vec2 p) {
    return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
  }

  public Rect(double x, double y, double w, double h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
}
