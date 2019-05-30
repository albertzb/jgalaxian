// JGSprite.java
//
// Author: J Spencer Seidel
// Description: An abstract Sprite class

package com.spencerseidel;

import java.awt.*;
import com.spencerseidel.*;

public abstract class JGSprite {

  // This is a pointer to an array of faces to display (the app should
  // pass this to us since it's the one that loads images)
  protected Image faces[];
  // Which face we're currently displaying
  protected int currFace;
  // This keeps track of our location (and size) on the screen
  protected Rectangle locRect;
  // This is used in collision detection and may not be the same
  // size as our location rect
  protected Rectangle collisionRect;
  // This rect is used to adjust the amount of the Sprite's image
  // we use in collision detection
  protected Rectangle hotRect;
  // The type field is used in collision detection:
  //     Type 0       : no collision
  //     Type A vs. B : collision
  //     Type A vs. A : no collision
  protected int type;
  // Whether we're currently alive
  protected boolean alive;

  public JGSprite(int x, int y, int w, int h, int spriteType, Image f[]) {
    locRect = new Rectangle(x, y, w, h);
    hotRect = new Rectangle(0, 0, w, h);       // Same size as the Sprite faces is default
    collisionRect = new Rectangle(x, y, w, h); // Same as location
    faces = f;
    currFace = 0;
    alive = true;
    type = spriteType;
  }

  // For simple information
  public int getx() {
    return locRect.x;
  }

  public int gety() {
    return locRect.y;
  }

  public boolean alive() {
    return alive;
  }

  public void setAlive(boolean a) {
    alive = a;
  }

  public int type() {
    return type;
  }

  public Image getCurrFace() {
    if (currFace < 0 || currFace >= faces.length) {
      return null;
    }

    return faces[currFace];
  }

  public Rectangle getCollisionRect() {
    // Update the collision rectangle
    collisionRect.x = locRect.x + hotRect.x;
    collisionRect.y = locRect.y + hotRect.y;
    collisionRect.width = hotRect.width;
    collisionRect.height = hotRect.height;

    return collisionRect;
  }

  // These are all abstract and define the Sprite's behavior in the
  // game
  public abstract void update();                 // Called once an animation frame
  public abstract void died();                   // Called when the Applet sees that "alive" is false
}
