package org.tasker;

import javafx.scene.text.Text;

public class Layout {
  protected static double calculateLayout(App app, Node n) {
    return calculateLayout(app, n, new Vec2(0, 0));
  }

  private static double calculateLayout(App app, Node n, Vec2 offset) {
    n.show = true;

    Text text = new Text(n.label);
    text.setFont(app.gc.getFont());
    n.extents.x = text.getLayoutBounds().getWidth();
    n.extents.y = text.getLayoutBounds().getHeight();

    double height = 0;
    double width = n.extents.x + app.padding.x * 2 + app.spaceBetweenNodes;

    n.bounds.x = offset.x;
    n.bounds.y = offset.y * app.lineHeight * app.size;
    n.bounds.w = n.extents.x + app.padding.x * 2;
    n.bounds.h = n.extents.y + app.padding.y * 2;

    if (app.compact) {
      n.bounds.y = offset.y * 17;
      n.bounds.h = 16;
      app.padding.y = 0;
    }

    if (n == app.nodeToReparent) {
      n.bounds.x = -app.globalOffset.x + app.mouse.x - n.bounds.w / 2;
      n.bounds.y = -app.globalOffset.y + app.mouse.y - n.bounds.h / 2;
    }

    if (n.children.size() == 0 && n.links.size() == 0) {
      height = 1;
    } else {
      for (Node child : n.children) {
        if (!app.showDone && child.checkAttr("status", "done")) {
          child.show = false;
          continue;
        } else if (!child.isAncestor(app.tree.current) &&
            !app.tree.current.isAncestor(child) && app.tree.current != child) {
          child.show = false;
          continue;
        } else {
          child.show = true;
        }

        Vec2 o = new Vec2(offset.x + width, offset.y + height);
        height += calculateLayout(app, child, o);
      }

      for (int i = 0; i < n.links.size(); i++) {
        height += 1;
      }
    }

    return height;
  }
}
