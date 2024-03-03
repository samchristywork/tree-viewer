package org.tasker;

import javafx.scene.text.Text;

public class Layout {
  protected static double calculateLayout(App app, Node n) {
    return calculateLayout(app, n, new Vec2(0, 0));
  }

  private static double calculateLayout(App app, Node n, Vec2 offset) {
    n.show = true;

    Text text = new Text(n.label);
    text.setFont(app.render.gc.getFont());
    n.extents.x = text.getLayoutBounds().getWidth();
    n.extents.y = text.getLayoutBounds().getHeight();

    double height = 0;
    double width = n.extents.x + app.render.padding.x * 2 + app.render.spaceBetweenNodes;

    n.bounds.x = offset.x;
    n.bounds.y = offset.y * app.render.lineHeight * app.render.size;
    n.bounds.w = n.extents.x + app.render.padding.x * 2;
    n.bounds.h = n.extents.y + app.render.padding.y * 2;

    if (app.render.compact) {
      n.bounds.y = offset.y * 17;
      n.bounds.h = 16;
      app.render.padding.y = 0;
    }

    if (n == app.nodeToReparent) {
      n.bounds.x = -app.render.globalOffset.x + app.render.mouse.x - n.bounds.w / 2;
      n.bounds.y = -app.render.globalOffset.y + app.render.mouse.y - n.bounds.h / 2;
    }

    if (n.children.size() == 0 && n.links.size() == 0) {
      height = 1;
    } else {
      for (Node child : n.children) {
        if (!app.render.showDone && child.checkAttr("status", "done")) {
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
