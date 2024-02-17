package org.tasker;

class Rect {
  public int x, y, w, h;

  public boolean contains(Vec2 p) {
    return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
  }

  public Rect(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
}
