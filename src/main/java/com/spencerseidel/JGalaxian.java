//
// Author: J Spencer Seidel
// Description: The JGalaxian class controls all aspects of drawing or presenting
//              the game to the player
//

package com.spencerseidel;

import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.lang.Math;
import java.util.*;
import java.applet.*;
import java.io.*;
import javax.imageio.*;
import java.net.URL;
import java.awt.geom.AffineTransform;
import com.spencerseidel.*;
import java.util.prefs.Preferences;

public class JGalaxian extends JPanel implements KeyListener {

  // These will store all the sprite faces.
  private Image sfPlayer[];
  private Image sfPMissile[];
  private Image sfEMissile[];
  private Image sfBadGuy1[];
  private Image sfBadGuy2[];
  private Image sfBadGuy3[];
  private Image sfBadGuy4[];
  private Image sfEx1[];
  private Image sfEx2[];
  private Image sfEx3[];
  private Image sfEx4[];
  private Image sfEx5[];
  private Image sfScore300[];
  private Image extraLife;
  private Image levelFlags[];

  // For stars
  private Point[] stars;
  // These lists manage our Sprites
  ArrayList<ArrayList<JGSprite>> sprites;
  private int currSpriteList;
  JGSprite grid[][];
  // These are our sounds
  private AudioClip wavPlayerFired;
  private AudioClip wavShotBadGuy1;
  private AudioClip wavShotBadGuy2;
  private AudioClip wavShotBigBadGuy;
  private AudioClip wavBadGuyAttacking;
  private AudioClip wavPlayerExplode;
  private AudioClip wavStartGame;
  private AudioClip wavGameOver;
  private AudioClip wavEnemyFired;
  private AudioClip wavFreeGuy;
  private AudioClip wavGetReady;
  private AudioClip wavQuit;
  private AudioClip wavMuzak;
  // This will be our double-buffer system
  private Graphics dbContext;
  private Image db;
  // This keeps track of how to move the bad guy grid
  private int dxGrid;
  // A game counter for delays
  private long gameCount;
  private long gameResume;
  // Set if anything is attacking
  private boolean attacking;
  // Number of lives left
  private int numPlayerLives;
  private int displayNumPlayerLives;
  // Which level
  private int level;
  private double attackFrequency;
  private double enemyFireFrequency;
  private int maxEnemyDescentSpeed;
  private int maxEnemyLateralSpeed;
  private int maxEnemiesAttacking;
  // Score stuff
  private int highScore;
  private int score;
  private int nextFreeGuy;
  // Font stuff
  private Font font;
  private FontMetrics fontMetrics;
  private int fontHeight;
  // Game state
  private int gameState;
  // For storing persistent state data like high score
  private Preferences prefs;

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.addWindowListener(new java.awt.event.WindowAdapter() {
       public void windowClosing(java.awt.event.WindowEvent e) {
         System.exit(0);
       };
     });

    JGalaxian jg = new JGalaxian();
    double scale = JGGlob.DEFAULT_SCALE;
    if (args.length > 0) {
      scale = Double.parseDouble(args[0]);
    }

    jg.scaleScreenElements(scale);
    jg.setPreferredSize(new Dimension(JGGlob.SCREEN_WIDTH, JGGlob.SCREEN_HEIGHT));
    f.getContentPane().add(jg);
    f.pack();
    jg.init();
    f.setVisible(true);
    // TBD: switch to use key bindings
    jg.requestFocusInWindow();
  }

  public void init() {
    gameState = JGGlob.JGSTATE_GAMEOVER;

    // Set up prefrences
    prefs = Preferences.userRoot().node(this.getClass().getName());

    // Make sure we're listening to key events
    // TBD: switch to use key bindings
    addKeyListener(this);

    // Load up all the images and sounds we'll need
    loadMedia();

    // Set up our double-buffering scheme
    db = createImage(JGGlob.SCREEN_WIDTH, JGGlob.SCREEN_HEIGHT);
    dbContext = db.getGraphics();

    // Create our Sprite lists
    sprites = new ArrayList<ArrayList<JGSprite>>();
    sprites.add(new ArrayList<JGSprite>(100));
    sprites.add(new ArrayList<JGSprite>(100));
    currSpriteList = 0;

    grid = new JGSprite[10][6];

    // Set up a font
    font = new Font("SansSerif", Font.BOLD, (int)(14*JGGlob.SCALE));
    dbContext.setFont(font);
    fontMetrics = dbContext.getFontMetrics(font);
    fontHeight = fontMetrics.getHeight();

    // These will be our explosions
    createExImages();

    // Get hiscore
    highScore = prefs.getInt(JGGlob.HIGH_SCORE_PREF, 0);

    stars = new Point[JGGlob.NUM_STARS];
    for (int i=0; i<JGGlob.NUM_STARS; i++) {
      stars[i] = new Point((int)(Math.random()*JGGlob.SCREEN_WIDTH), (int)(Math.random()*JGGlob.SCREEN_HEIGHT));
    }
  }

  public void start() {
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {
    long start, delay;

    // Grab the time
    start = System.currentTimeMillis();

    switch (gameState) {
      case JGGlob.JGSTATE_PLAYING:
        processGame();
      break;

      case JGGlob.JGSTATE_PLAYERDIED:
        processPlayerDied();
      break;

      case JGGlob.JGSTATE_CHANGINGLEVEL1:
      case JGGlob.JGSTATE_CHANGINGLEVEL2:
        processChangingLevel();
      break;

      case JGGlob.JGSTATE_GAMEOVER:
      case JGGlob.JGSTATE_PAUSED:
        updateBackground();
        break;
    }

    // Copy the double buffer to the screen
    g.drawImage(db, 0, 0, this);

    // Figure out how long to delay based on how
    // long it took to draw the frame
    delay = JGGlob.FRAME_DELAY - (System.currentTimeMillis() - start);
    if (delay > 0) {
      try {
        Thread.sleep(delay);
      }
      catch (InterruptedException e){};
    }

    JGGlob.playerFired = false;

    repaint();

    gameCount++;
  }

  // Processes animation and updates game state
  private void processGame() {
    // Animate a frame
    animate();

    if (score > highScore) {
      highScore = score;
      prefs.putInt(JGGlob.HIGH_SCORE_PREF, highScore);
    }

    // Player may have died
    if (!JGGlob.playerAlive) {
      numPlayerLives--;
      gameResume = gameCount + 200;
      gameState = JGGlob.JGSTATE_PLAYERDIED;
    }

    // Woohoo, new level
    if (JGGlob.numBadGuys == 0 && gameResume < 0) {
      gameState = JGGlob.JGSTATE_CHANGINGLEVEL1;

      gameResume = gameCount + 50;
      level++;
      attackFrequency += JGGlob.LEVELINC_ATTACK_FREQUENCY;
      enemyFireFrequency += JGGlob.LEVELINC_ENEMY_FIRE_FREQUENCY;

      // Make things a little more extreme every other level
      if ((level-1)%2==0) {
        if (++maxEnemyDescentSpeed > JGGlob.MAX_ENEMY_DESCENT_SPEED) {
          maxEnemyDescentSpeed = JGGlob.MAX_ENEMY_DESCENT_SPEED;
        }

        if (++maxEnemyLateralSpeed > JGGlob.MAX_ENEMY_LATERAL_SPEED) {
          maxEnemyLateralSpeed = JGGlob.MAX_ENEMY_LATERAL_SPEED;
        }

        maxEnemiesAttacking++;
      }
    }

    // May need to play a player fired sound
    if (JGGlob.playerFired) {
      wavPlayerFired.play();
    }
  }

  // When in player died state
  private void processPlayerDied() {
    if (gameCount > gameResume && !attacking) {
      displayNumPlayerLives--;

      // Add a player Sprite
      sprites.get(currSpriteList).add(new JGPlayerSprite((JGGlob.SCREEN_WIDTH - JGGlob.PLAYER_WIDTH)/2, JGGlob.PLAYER_Y,
                                                            JGGlob.PLAYER_WIDTH, JGGlob.PLAYER_HEIGHT, sfPlayer));
      // Add a missile
      sprites.get(currSpriteList).add(new JGPMissileSprite((JGGlob.SCREEN_WIDTH - JGGlob.PMISSILE_WIDTH)/2 + JGGlob.PMISSILE_XOFFSET, JGGlob.PLAYER_Y,
                                                              JGGlob.PMISSILE_WIDTH, JGGlob.PMISSILE_HEIGHT, sfPMissile));

      JGGlob.loadAnotherMissile = false;
      gameResume = -1;
      JGGlob.playerAlive = true;

      if (numPlayerLives < 0) {
        gameState = JGGlob.JGSTATE_GAMEOVER;
        wavGameOver.play();
      }
      else {
        gameState = JGGlob.JGSTATE_PLAYING;
      }
    }
    else {
      animate();
    }
  }

  // When in changing levels state
  private void processChangingLevel() {
    switch (gameState) {
      case JGGlob.JGSTATE_CHANGINGLEVEL1:
        if (gameCount > gameResume) {
          gameState = JGGlob.JGSTATE_CHANGINGLEVEL2;
          gameResume = gameCount + 150;
          wavGetReady.play();
        }
        else {
          animate();
        }
        break;

      case JGGlob.JGSTATE_CHANGINGLEVEL2:
        if (gameCount > gameResume) {
          gameResume = -1;
          initBadGuyGrid();
          gameState = JGGlob.JGSTATE_PLAYING;
          wavStartGame.play();
        }
        else {
          updateBackground();
        }

        break;
    }
  }

  // Draw the background things like the level and the number
  // of lives left
  private void updateBackground() {

    // Clear the double buffer
    wipeDoubleBuffer();

    int i, x, y;

    // Update the stars
    for (i=0; i<JGGlob.NUM_STARS; i++) {

      x = (int)stars[i].getX();
      y = (int)stars[i].getY();
      if (i%3==0) {
        y+=2;
      }
      else {
        y++;
      }

      if (y > JGGlob.SCREEN_HEIGHT) {
        y=0;
      }
      stars[i].setLocation(x, y);
      dbContext.setColor(new Color((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
      dbContext.fillRect(x, y, (int)(2.0*JGGlob.SCALE), (int)(2.0*JGGlob.SCALE));
    }

    // Extra lives
    x=JGGlob.EXTRALIFE_STARTX;
    for (i=0; i<displayNumPlayerLives; i++) {
      dbContext.drawImage(extraLife, x, JGGlob.SCREEN_HEIGHT - JGGlob.EXTRALIFE_HEIGHT, this);
      x += JGGlob.EXTRALIFE_HORZ_SPACE;
    }

    // Level
    int rem = level%5;
    int div = level/5;
    x=JGGlob.LEVELFLAG_STARTX;
    for (i=0; i<div; i++) {
      dbContext.drawImage(levelFlags[JGGlob.LEVELFLAG_WORTH5], x, JGGlob.SCREEN_HEIGHT - JGGlob.LEVELFLAG_HEIGHT, this);
      x -= JGGlob.LEVELFLAG_HORZ_SPACE;
    }

    for (i=0; i<rem; i++) {
      dbContext.drawImage(levelFlags[JGGlob.LEVELFLAG_NORMAL], x, JGGlob.SCREEN_HEIGHT - JGGlob.LEVELFLAG_HEIGHT, this);
      x -= JGGlob.LEVELFLAG_HORZ_SPACE;
    }

    // Score, High Score
    int highScoreLoc = (JGGlob.SCREEN_WIDTH - fontMetrics.stringWidth("High Score"))/2;

    dbContext.setColor(Color.white);
    dbContext.drawString("Score", 5, fontHeight);
    dbContext.drawString("High Score", highScoreLoc, fontHeight);
    dbContext.setColor(Color.red);
    dbContext.drawString(Integer.toString(score), 5, fontHeight*2);
    dbContext.drawString(Integer.toString(highScore), highScoreLoc, fontHeight*2);

    switch (gameState) {
      case JGGlob.JGSTATE_GAMEOVER:
        messageBox("GAME OVER", true);
        break;
      case JGGlob.JGSTATE_CHANGINGLEVEL2:
        messageBox("GET READY", false);
        break;
      case JGGlob.JGSTATE_PAUSED:
        messageBox("PAUSED...", true);
        break;
    }
  }

  // Processes one frame of animation
  private void animate() {

    // Calculate our next sprite list
    int nextSpriteList = (currSpriteList==0 ? 1 : 0);

    updateBackground();

    // Draw the sprites to the double buffer
    for (JGSprite s : sprites.get(currSpriteList)) {
      s.update();
      dbContext.drawImage(s.getCurrFace(), s.getx(), s.gety(), this);
    }

    // Collision check our Sprites
    ArrayList<Point> explosionPoints = new ArrayList<Point>(100);
    ArrayList<Point> score300 = new ArrayList<Point>(100);

    for (JGSprite s1 : sprites.get(currSpriteList)) {
      // Only consider Sprites that aren't type 0 or dead
      if (s1.type() != 0 && s1.alive()) {
        for (JGSprite s2 : sprites.get(currSpriteList)) {
          // Make sure we're not dealing with the same Sprite, a Sprite with
          // type 0, a dead Sprite, or Sprites of the same type
          boolean differentSigns = (s1.type() > 0 && s2.type() < 0) || (s1.type() < 0 && s2.type() > 0);
          if (s2.type() != 0 && s1 != s2 && differentSigns && s2.alive()) {
            // Check if their collision rectangles are intersecting
            if (s1.getCollisionRect().intersects(s2.getCollisionRect())) {
                int addToScore = 0;

                // Update Sprite states
                switch (getCollisionType(s1.type(), s2.type())) {
                  case JGGlob.COL_PMISSILE_EMISSILE: break;
                  case JGGlob.COL_EMISSILE_PLAYER:
                    s1.setAlive(false);
                    s2.setAlive(false);
                    explosionPoints.add(new Point(s1.getx(), s1.gety()));
                    explosionPoints.add(new Point(s2.getx(), s2.gety()));
                    wavPlayerExplode.play();
                    break;
                  case JGGlob.COL_PLAYER_BADGUY:
                    s1.setAlive(false);
                    s2.setAlive(false);
                    explosionPoints.add(new Point(s2.getx(), s2.gety()));
                    explosionPoints.add(new Point(s1.getx(), s1.gety()));

                    if (s1.type() == JGGlob.HEADBADGUY_TYPE || s2.type() == JGGlob.HEADBADGUY_TYPE) {
                      wavShotBigBadGuy.play();
                    }
                    else {
                      if (Math.random()*100 > 50) {
                        wavShotBadGuy1.play();
                      }
                      else {
                        wavShotBadGuy2.play();
                      }
                    }

                    wavPlayerExplode.play();

                    addToScore = getScoreFromCollision(s1.type(), s2.type());
                  break;

                  case JGGlob.COL_PMISSILE_BADGUY:
                    s1.setAlive(false);
                    s2.setAlive(false);
                    explosionPoints.add(new Point(s1.getx(), s1.gety()));
                    if (s1.type() == JGGlob.HEADBADGUY_TYPE || s2.type() == JGGlob.HEADBADGUY_TYPE) {
                      wavShotBigBadGuy.play();
                    }
                    else {
                      if (Math.random()*100 > 50) {
                        wavShotBadGuy1.play();
                      }
                      else {
                        wavShotBadGuy2.play();
                      }
                    }

                    addToScore = getScoreFromCollision(s1.type(), s2.type());
                    break;
                }

                // Update Score
                score += addToScore;
                if (addToScore == 300) {
                  score300.add(new Point(s1.getx(), s1.gety()));
                }

                // Maybe we get a free guy
                if (score >= nextFreeGuy) {
                  wavFreeGuy.play();
                  nextFreeGuy += JGGlob.FREE_GUY_INTERVAL;
                  numPlayerLives++;
                  displayNumPlayerLives++;
                }
            }
          }
        }
      }
    }

    // Now clear out the dead sprites and update our attacking flag
    sprites.get(nextSpriteList).clear();
    attacking = false;
    int numAttacking = 0;
    for (JGSprite s : sprites.get(currSpriteList)) {
      if (s.alive()) {
        sprites.get(nextSpriteList).add(s);
        if (s instanceof JGBadGuySprite) {
          JGBadGuySprite bg = (JGBadGuySprite)s;
          if (bg.isAttacking()) {
            attacking = true;
            numAttacking++;
          }
        }
      }
      else {
        s.died();
      }
    }

    // Switch sprite lists
    currSpriteList = nextSpriteList;

    // May need to make an explosion
    for (Point p : explosionPoints) {
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 5, 5, sfEx5));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 5, 5, sfEx5));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 4, 4, sfEx4));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 4, 4, sfEx4));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 3, 3, sfEx3));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 3, 3, sfEx3));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 2, 2, sfEx2));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 2, 2, sfEx2));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
      sprites.get(currSpriteList).add(new JGExplosionPieceSprite((int)p.getX(), (int)p.getY(), 1, 1, sfEx1));
    }

    // May need to add a score 300
    for (Point p : score300) {
      sprites.get(currSpriteList).add(new JGScore300Sprite((int)p.getX(), (int)p.getY(), 0, 0, sfScore300));
    }

    // We may need to create another missile
    if (JGGlob.loadAnotherMissile) {
      sprites.get(currSpriteList).add(new JGPMissileSprite(JGGlob.playerx + JGGlob.PMISSILE_XOFFSET, JGGlob.PLAYER_Y,
                                                             JGGlob.PMISSILE_WIDTH, JGGlob.PMISSILE_HEIGHT, sfPMissile));
      // Update the bulletin board
      JGGlob.loadAnotherMissile = false;
    }

    // Update the grid and look for new attackers
    for (int col=0; col<10; col++) {
      for (int row=0; row<6; row++) {
        if (grid[col][row] != null) {
          if (!grid[col][row].alive()) {
            grid[col][row] = null;
          }
          else if (grid[col][row] instanceof JGBadGuySprite) {
            JGBadGuySprite s = (JGBadGuySprite)grid[col][row];

            // Perhaps the enemy should fire
            if (Math.random() < enemyFireFrequency && s.isInChaseMode()) {
              sprites.get(currSpriteList).add(new JGEMissileSprite(grid[col][row].getx() + JGGlob.EMISSILE_XOFFSET,
                                                                     grid[col][row].gety() + JGGlob.EMISSILE_YOFFSET,
                                                                       JGGlob.EMISSILE_WIDTH, JGGlob.EMISSILE_HEIGHT, sfEMissile));
              wavEnemyFired.play();
            }

            boolean doAttack = false;
            boolean dir = (col >= 5 ? JGGlob.BADGUY_ATTACK_RIGHT : JGGlob.BADGUY_ATTACK_LEFT);

            if (JGGlob.numBadGuys < 4 && JGGlob.playerAlive) {
              doAttack = true;
            }
            else {
              // Check if this one can attack
              if (canBadGuyAttack(dir, col, row) && (Math.random() < attackFrequency) && (numAttacking < maxEnemiesAttacking)) {
                doAttack = true;
              }
            }

            if (doAttack) {
              if (!s.isAttacking()) {
                wavBadGuyAttacking.play();

                s.attack(dir);

                // If this sprite is a head bad guy, we need to find wing men
                // for him by choosing any 2 out of 3 possible
                if (s.type() == JGGlob.HEADBADGUY_TYPE) {
                  JGBadGuySprite wingMan1, wingMan2, wingMan3;

                  wingMan1 = (JGBadGuySprite)grid[col+1][4];
                  wingMan2 = (JGBadGuySprite)grid[col][4];
                  wingMan3 = (JGBadGuySprite)grid[col-1][4];

                  int numGoing = 0;

                  // Left side
                  if (col == 3) {
                      if (wingMan3 != null) {
                        numGoing++;
                        wingMan3.attack(dir, s);
                      }

                      if (wingMan2 != null) {
                        numGoing++;
                        wingMan2.attack(dir, s);
                      }

                      if (numGoing < 2 && wingMan1 != null) {
                        numGoing++;
                        wingMan1.attack(dir, s);
                      }
                  }

                  // Right side
                  if (col == 6) {
                      if (wingMan1 != null) {
                        numGoing++;
                        wingMan1.attack(dir, s);
                      }

                      if (wingMan2 != null) {
                        numGoing++;
                        wingMan2.attack(dir, s);
                      }

                      if (numGoing < 2 && wingMan3 != null) {
                        numGoing++;
                        wingMan3.attack(dir, s);
                      }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Update the left edge
    JGGlob.gridLeftEdge += dxGrid;
    if (JGGlob.gridLeftEdge + JGGlob.BADGUY_HORZ_SPACE*10 > JGGlob.SCREEN_WIDTH ||
        JGGlob.gridLeftEdge < 0) {
      JGGlob.gridLeftEdge -= dxGrid;
      dxGrid *= -1;
    }
  }

  // To figure out scoring
  private int getCollisionType(int t1, int t2) {
   if (t1 == JGGlob.PMISSILE_TYPE && isBadGuy(t2) ||
        isBadGuy(t1) && t2 == JGGlob.PMISSILE_TYPE) {
      return JGGlob.COL_PMISSILE_BADGUY;
    }
   else if (t1 == JGGlob.PLAYER_TYPE && isBadGuy(t2) ||
             isBadGuy(t1) && t2 == JGGlob.PLAYER_TYPE) {
      return JGGlob.COL_PLAYER_BADGUY;
    }
   else if (t1 == JGGlob.PLAYER_TYPE && t2 == JGGlob.EMISSILE_TYPE ||
             t1 == JGGlob.EMISSILE_TYPE && t2 == JGGlob.PLAYER_TYPE) {
      return JGGlob.COL_EMISSILE_PLAYER;
    }
   else if (t1 == JGGlob.PMISSILE_TYPE && t2 == JGGlob.EMISSILE_TYPE ||
             t1 == JGGlob.EMISSILE_TYPE && t2 == JGGlob.PMISSILE_TYPE) {
      return JGGlob.COL_PMISSILE_EMISSILE;
    }

    return 0;
  }

  // To determine score
  private int getScoreFromCollision(int t1, int t2) {
    int t;


    if (isBadGuy(t1)) {
      t = t1;
    }
    else {
      t = t2;
    }

    switch (t) {
      case JGGlob.BADGUY1_TYPE: return 20;
      case JGGlob.BADGUY2_TYPE: return 30;
      case JGGlob.BADGUY3_TYPE: return 50;
      case JGGlob.HEADBADGUY_TYPE: return 300;
    }

    return 0;
  }

  // To determine what kind of Sprite we're dealing with
  private boolean isBadGuy(int type) {
    return (type == JGGlob.BADGUY1_TYPE ||
            type == JGGlob.BADGUY2_TYPE ||
            type == JGGlob.BADGUY3_TYPE ||
            type == JGGlob.HEADBADGUY_TYPE);
  }

  // An enemy can attack if he has no neighbor to his right (or left)
  // and no neighbor above him
  private boolean canBadGuyAttack(boolean leftOrRight, int col, int row) {
    boolean horzNeighbor;
    boolean  vertNeighbor;

    // Unless the head bad guys are gone, row 4 can't attack
    if (JGGlob.numHeadBadGuys != 0 && row == 4) {
      return false;
    }

    // If the player is dead, we can't attack
    if (!JGGlob.playerAlive) {
      return false;
    }

    int tmpCol = col + (leftOrRight == JGGlob.BADGUY_ATTACK_RIGHT ? 1 : -1);
    int tmpRow = row + 1;

    if (tmpRow > 5) {
      vertNeighbor = false;
    }
    else {
      if (grid[col][tmpRow] != null) {
        vertNeighbor = true;
      }
      else {
        vertNeighbor = false;
      }
    }

    if (tmpCol < 0) {
      horzNeighbor = false;
    }
    else {
      if (tmpCol > 9) {
        horzNeighbor = false;
      }
      else {
        if (grid[tmpCol][row] != null) {
          horzNeighbor = true;
        }
        else {
          horzNeighbor = false;
        }
      }
    }

    return !(vertNeighbor || horzNeighbor);
  }

  public static BufferedImage scaleImage(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
    BufferedImage dbi = null;
    if(sbi != null) {
        dbi = new BufferedImage(dWidth, dHeight, imageType);
        Graphics2D g = dbi.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
        g.drawRenderedImage(sbi, at);
    }
    return dbi;
  }

  private BufferedImage loadImage(String path) {
    try {
      Class cls = this.getClass();
      ClassLoader cl = cls.getClassLoader();

      BufferedImage bi = ImageIO.read(cl.getResource(path));
      BufferedImage sbi = scaleImage(bi, BufferedImage.TYPE_4BYTE_ABGR, (int)(bi.getWidth()*JGGlob.SCALE), (int)(bi.getHeight()*JGGlob.SCALE), JGGlob.SCALE, JGGlob.SCALE);

      return sbi;
    }
    catch (Exception e) {
      System.err.println("Can't load image " + path + ": " + e.getMessage());
      System.exit(1);
    }

    return null;
  }

  private AudioClip loadSound(String path) {
    try {
      Class cls = this.getClass();
      ClassLoader cl = cls.getClassLoader();
      return Applet.newAudioClip(cl.getResource(path));
    }
    catch(Exception e) {
      System.err.println("Exception: " + e.getMessage());
      System.exit(1);
    }

    return null;
  }

  // Loads all the artwork and sounds
  private void loadMedia() {
    int i;

    // Graphics
    sfPlayer = loadImages(JGGlob.PLAYER_IMAGES_TO_LOAD, JGGlob.PLAYER_IMAGE_BASE);
    sfPMissile = loadImages(JGGlob.PMISSILE_IMAGES_TO_LOAD, JGGlob.PMISSILE_IMAGE_BASE);
    sfEMissile = loadImages(JGGlob.EMISSILE_IMAGES_TO_LOAD, JGGlob.EMISSILE_IMAGE_BASE);
    sfBadGuy1 = loadImages(JGGlob.BADGUY1_IMAGES_TO_LOAD, JGGlob.BADGUY1_IMAGE_BASE);
    sfBadGuy2 = loadImages(JGGlob.BADGUY2_IMAGES_TO_LOAD, JGGlob.BADGUY2_IMAGE_BASE);
    sfBadGuy3 = loadImages(JGGlob.BADGUY3_IMAGES_TO_LOAD, JGGlob.BADGUY3_IMAGE_BASE);
    sfBadGuy4 = loadImages(JGGlob.HEADBADGUY_IMAGES_TO_LOAD, JGGlob.HEADBADGUY_IMAGE_BASE);
    sfScore300 = loadImages(JGGlob.SCORE300_IMAGES_TO_LOAD, JGGlob.SCORE300_IMAGE_BASE);

    // Extra life
    extraLife = loadImage(JGGlob.EXTRALIFE_IMAGE_BASE + JGGlob.IMAGE_EXT);

    // Level flags
    levelFlags = new BufferedImage[2];
    levelFlags[JGGlob.LEVELFLAG_NORMAL] = loadImage(JGGlob.LEVELFLAG_IMAGE_BASE + "0" + JGGlob.IMAGE_EXT);
    levelFlags[JGGlob.LEVELFLAG_WORTH5] = loadImage(JGGlob.LEVELFLAG_IMAGE_BASE + "1" + JGGlob.IMAGE_EXT);

    // Sounds
    wavPlayerFired = loadSound(JGGlob.SOUNDS_BASE + "/playerfired.wav");
    wavShotBadGuy1 = loadSound(JGGlob.SOUNDS_BASE + "/shotbadguy1.wav");
    wavShotBadGuy2 = loadSound(JGGlob.SOUNDS_BASE + "/shotbadguy2.wav");
    wavShotBigBadGuy = loadSound(JGGlob.SOUNDS_BASE + "/shotbigbadguy.wav");
    wavBadGuyAttacking = loadSound(JGGlob.SOUNDS_BASE + "/badguyattacking.wav");
    wavPlayerExplode = loadSound(JGGlob.SOUNDS_BASE + "/playerexplode.wav");
    wavStartGame = loadSound(JGGlob.SOUNDS_BASE + "/startgame.wav");
    wavGameOver = loadSound(JGGlob.SOUNDS_BASE + "/gameover.wav");
    wavEnemyFired = loadSound(JGGlob.SOUNDS_BASE + "/enemyfired.wav");
    wavFreeGuy = loadSound(JGGlob.SOUNDS_BASE + "/freeguy.wav");
    wavGetReady = loadSound(JGGlob.SOUNDS_BASE + "/getready.wav");
    wavQuit = loadSound(JGGlob.SOUNDS_BASE + "/quit.wav");
    wavMuzak = loadSound(JGGlob.SOUNDS_BASE + "/muzak.wav");
  }

  // Create all the explosion images
  private void createExImages() {
    Graphics tmpContext;
    Image img;
    int component;
    int i;

    sfEx5 = new Image[10];
    sfEx4 = new Image[10];
    sfEx3 = new Image[10];
    sfEx2 = new Image[10];
    sfEx1 = new Image[10];

    component = 255;
    for (i=0; i<10; i++) {
      img = createImage((int)(5.0*JGGlob.SCALE), (int)(5.0*JGGlob.SCALE));
      tmpContext = img.getGraphics();
      tmpContext.setColor(new Color(component, 0, 0));
      tmpContext.fillRect(0, 0, (int)(5.0*JGGlob.SCALE), (int)(5.0*JGGlob.SCALE));

      sfEx5[i] = img;

      component -= 25;
      if (component < 0) {
        component = 0;
      }
    }

    component = 255;
    for (i=0; i<10; i++) {
      img = createImage((int)(4.0*JGGlob.SCALE), (int)(4.0*JGGlob.SCALE));
      tmpContext = img.getGraphics();
      tmpContext.setColor(new Color(0, component, 0));
      tmpContext.fillRect(0, 0, (int)(4.0*JGGlob.SCALE), (int)(4.0*JGGlob.SCALE));

      sfEx4[i] = img;

      component -= 25;
      if (component < 0) {
        component = 0;
      }
    }

    component = 255;
    for (i=0; i<10; i++) {
      img = createImage((int)(3.0*JGGlob.SCALE), (int)(3.0*JGGlob.SCALE));
      tmpContext = img.getGraphics();
      tmpContext.setColor(new Color(0, 0, component));
      tmpContext.fillRect(0, 0, (int)(3.0*JGGlob.SCALE), (int)(3.0*JGGlob.SCALE));

      sfEx3[i] = img;

      component -= 25;
      if (component < 0) {
        component = 0;
      }
    }

    component = 255;
    for (i=0; i<10; i++) {
      img = createImage((int)(2.0*JGGlob.SCALE), (int)(2.0*JGGlob.SCALE));
      tmpContext = img.getGraphics();
      tmpContext.setColor(new Color(component, 0, component));
      tmpContext.fillRect(0, 0, (int)(2.0*JGGlob.SCALE), (int)(2.0*JGGlob.SCALE));

      sfEx2[i] = img;

      component -= 25;
      if (component < 0) {
        component = 0;
      }
    }

    component = 255;
    for (i=0; i<10; i++) {
      img = createImage((int)(1.0*JGGlob.SCALE), (int)(1.0*JGGlob.SCALE));
      tmpContext = img.getGraphics();
      tmpContext.setColor(new Color(component, component, component));
      tmpContext.fillRect(0, 0, (int)(1.0*JGGlob.SCALE), (int)(1.0*JGGlob.SCALE));

      sfEx1[i] = img;

      component -= 25;
      if (component < 0) {
        component = 0;
      }
    }
  }

  // Loads a Sprite's faces
  private BufferedImage[] loadImages(int n, String base) {
    BufferedImage sf[] = new BufferedImage[n];
    for (int i=0; i<n; i++) {
      sf[i] = loadImage(base + i + JGGlob.IMAGE_EXT);
    }

    return sf;
  }

  // Wipes our double buffer
  private void wipeDoubleBuffer() {
    dbContext.setColor(Color.black);
    dbContext.fillRect(0, 0, JGGlob.SCREEN_WIDTH, JGGlob.SCREEN_HEIGHT);
  }

  public void messageBox(String s, boolean instructions)
  {
    int l, t, r, b, w, h;

    // Add instructions
    if (instructions) {
      s = s + "\n\nClick window, then:\n<space>: Start\n<p>: Pause\n<q>: Quit\n<left arrow>: left\n<right arrow>: right\n<space>: fire";
    }

    // Figure out how wide
    w = -1;
    for (String line : s.split("\n")) {
      if (fontMetrics.stringWidth(line) > w) {
        w = fontMetrics.stringWidth(line);
      }
    }
    // Figure out how high
    h=fontHeight + JGGlob.FONT_LINE_SPACING;
    for (String line : s.split("\n")) {
      h  += fontHeight + JGGlob.FONT_LINE_SPACING;
    }

    l = (JGGlob.SCREEN_WIDTH - w)/2 - fontMetrics.getMaxAdvance();
    r = l + w + 2*fontMetrics.getMaxAdvance();
    t = (JGGlob.SCREEN_HEIGHT - h)/2;
    b = t + h;

    dbContext.setColor( Color.black );
    dbContext.fillRect( l, t, r-l, b-t );
    dbContext.setColor( Color.white );
    dbContext.drawRect( l, t, r-l-1, b-t-1 );
    dbContext.setColor( Color.red );
    int y = t + fontHeight + JGGlob.FONT_LINE_SPACING;
    for (String line : s.split("\n")) {
      dbContext.drawString(line, (JGGlob.SCREEN_WIDTH-fontMetrics.stringWidth(line))/2, y);
      y  += fontHeight + JGGlob.FONT_LINE_SPACING;
    }
  }

  // Handle key events
  // TBD: switch to use key bindings
  public void keyPressed(KeyEvent e) {
    if (gameState == JGGlob.JGSTATE_GAMEOVER && e.getKeyCode() == KeyEvent.VK_SPACE) {
      initNewGame();
    }
    else {
      switch (e.getKeyCode()) {
        case JGGlob.KEYLEFT1:
        case JGGlob.KEYLEFT2:
          JGGlob.direction=JGGlob.MOVELEFT;
          break;
        case JGGlob.KEYRIGHT1:
        case JGGlob.KEYRIGHT2:
          JGGlob.direction=JGGlob.MOVERIGHT;
          break;
        case JGGlob.KEYFIRE1:
        case JGGlob.KEYFIRE2:
          JGGlob.fire=true;
          if (gameState == JGGlob.JGSTATE_PAUSED) {
            gameState = JGGlob.JGSTATE_PLAYING;
            wavMuzak.stop();
          }
          break;
        case JGGlob.KEYQUIT:
          wavMuzak.stop();
          wavQuit.play();
          try {
            Thread.sleep(1000);
          } catch(Exception ex) {}
          System.exit(0);
          break;
        case JGGlob.KEYPAUSE:
          if (gameState == JGGlob.JGSTATE_PLAYING) {
            gameState = JGGlob.JGSTATE_PAUSED;
            wavMuzak.play();
          }
          else if (gameState == JGGlob.JGSTATE_PAUSED) {
            gameState = JGGlob.JGSTATE_PLAYING;
            wavMuzak.stop();
          }
          break;
      }
    }
  }

  // TBD: switch to use key bindings
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case JGGlob.KEYLEFT1:
      case JGGlob.KEYLEFT2:
        JGGlob.direction=JGGlob.NOMOVE;
        break;
      case JGGlob.KEYRIGHT1:
      case JGGlob.KEYRIGHT2:
        JGGlob.direction=JGGlob.NOMOVE;
        break;
      case JGGlob.KEYFIRE1:
      case JGGlob.KEYFIRE2:
        JGGlob.fire=false;
        break;
      case JGGlob.KEYQUIT:  System.exit(0); break;
    }
  }

  // TBD: switch to use key bindings
  public void keyTyped(KeyEvent e) {
  }

  // Starts a new game
  private void initNewGame() {
    gameState = JGGlob.JGSTATE_PLAYING;

    // Clear out all the sprites
    currSpriteList = 0;
    sprites.get(0).clear();
    sprites.get(1).clear();

    wipeDoubleBuffer();

    // Add a player Sprite
    sprites.get(currSpriteList).add(new JGPlayerSprite((JGGlob.SCREEN_WIDTH - JGGlob.PLAYER_WIDTH)/2, JGGlob.PLAYER_Y,
                                                         JGGlob.PLAYER_WIDTH, JGGlob.PLAYER_HEIGHT, sfPlayer));
    // Add a missile
    sprites.get(currSpriteList).add(new JGPMissileSprite((JGGlob.SCREEN_WIDTH - JGGlob.PMISSILE_WIDTH)/2 + JGGlob.PMISSILE_XOFFSET, JGGlob.PLAYER_Y,
                                                           JGGlob.PMISSILE_WIDTH, JGGlob.PMISSILE_HEIGHT, sfPMissile));

    // Update the bulletin board variables
    JGGlob.loadAnotherMissile = false;
    JGGlob.numBadGuys = 0;
    JGGlob.numHeadBadGuys = 0;

    // Reset enemy downward and lateral speeds
    maxEnemyDescentSpeed = JGGlob.INITIAL_ENEMY_DESCENT_SPEED;
    maxEnemyLateralSpeed = JGGlob.INITIAL_ENEMY_LATERAL_SPEED;

    // Reset the max num of attackers at once
    maxEnemiesAttacking = 3;

    // Add the bad guys
    initBadGuyGrid();

    // Reset our game counter
    gameCount = 0;
    gameResume = -1;

    // Noone is attacking
    attacking = false;

    // Number of lives back to 2
    numPlayerLives = displayNumPlayerLives = 2;

    // Reset level
    level = 1;

    // Zero out score
    score = 0;

    // Zero out free guy
    nextFreeGuy = JGGlob.FREE_GUY_INTERVAL;

    // Reset difficulty
    attackFrequency = JGGlob.INITIAL_ATTACK_FREQUENCY;
    enemyFireFrequency = JGGlob.INITIAL_ENEMY_FIRE_FREQUENCY;

    // Play sounds
    wavStartGame.play();
  }

  private void initBadGuyGrid() {
    JGSprite s;
    int y = JGGlob.BADGUY_VERT_START;
    int x;

    for (int row=0; row<3; row++) {
      x = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2;
      for (int col=0; col<10; col++) {
        s = new JGBadGuySprite(row, col, x, y, JGGlob.BADGUY1_WIDTH, JGGlob.BADGUY1_HEIGHT, sfBadGuy1, JGGlob.BADGUY1_TYPE,
                                 maxEnemyDescentSpeed, maxEnemyLateralSpeed);
        sprites.get(currSpriteList).add(s);
        grid[col][row] = s;
        x += JGGlob.BADGUY_HORZ_SPACE;
        JGGlob.numBadGuys++;
      }
      y -= JGGlob.BADGUY_VERT_SPACE;
    }

    grid[0][3] = null;
    grid[9][3] = null;
    x = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2 + JGGlob.BADGUY_HORZ_SPACE;
    for (int col=1; col<=8; col++) {
      s = new JGBadGuySprite(3, col, x, y, JGGlob.BADGUY1_WIDTH, JGGlob.BADGUY1_HEIGHT, sfBadGuy2, JGGlob.BADGUY2_TYPE,
                               maxEnemyDescentSpeed, maxEnemyLateralSpeed);
      sprites.get(currSpriteList).add(s);
      grid[col][3] = s;
      x += JGGlob.BADGUY_HORZ_SPACE;
      JGGlob.numBadGuys++;
     }

    y -= JGGlob.BADGUY_VERT_SPACE;

    grid[0][4] = null;
    grid[1][4] = null;
    grid[8][4] = null;
    grid[9][4] = null;
    x = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2 + (JGGlob.BADGUY_HORZ_SPACE*2);
    for (int col=2; col<=7; col++) {
      s = new JGBadGuySprite(4, col, x, y, JGGlob.BADGUY1_WIDTH, JGGlob.BADGUY1_HEIGHT, sfBadGuy3, JGGlob.BADGUY3_TYPE,
                               maxEnemyDescentSpeed, maxEnemyLateralSpeed);
      sprites.get(currSpriteList).add(s);
      grid[col][4] = s;
      x += JGGlob.BADGUY_HORZ_SPACE;
      JGGlob.numBadGuys++;
     }

    y -= JGGlob.BADGUY_VERT_SPACE;

    grid[0][5] = null;
    grid[1][5] = null;
    grid[2][5] = null;
    grid[7][5] = null;
    grid[8][5] = null;
    grid[9][5] = null;

    x = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2 + (JGGlob.BADGUY_HORZ_SPACE*3);
    s = new JGBadGuySprite(5, 3, x, y, JGGlob.HEADBADGUY_WIDTH, JGGlob.HEADBADGUY_HEIGHT, sfBadGuy4, JGGlob.HEADBADGUY_TYPE,
                             maxEnemyDescentSpeed, maxEnemyLateralSpeed);
    sprites.get(currSpriteList).add(s);
    grid[3][5] = s;
    x += JGGlob.BADGUY_HORZ_SPACE*3;
    JGGlob.numBadGuys++;
    JGGlob.numHeadBadGuys++;

    x = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2 + (JGGlob.BADGUY_HORZ_SPACE*6);
    s = new JGBadGuySprite(5, 6, x, y, JGGlob.HEADBADGUY_WIDTH, JGGlob.HEADBADGUY_HEIGHT, sfBadGuy4, JGGlob.HEADBADGUY_TYPE,
                             maxEnemyDescentSpeed, maxEnemyLateralSpeed);
    sprites.get(currSpriteList).add(s);
    grid[6][5] = s;
    JGGlob.numBadGuys++;
    JGGlob.numHeadBadGuys++;

    JGGlob.gridLeftEdge = (JGGlob.SCREEN_WIDTH - (JGGlob.BADGUY_HORZ_SPACE*10))/2;
    dxGrid = 1;
  }

  // Yes, this is terrible and a hack. Needs to be reworked.
  //
  public void scaleScreenElements(double scale) {
    JGGlob.SCREEN_WIDTH = (int)(JGGlob.SCREEN_WIDTH/JGGlob.SCALE*scale);
    JGGlob.SCREEN_HEIGHT = (int)(JGGlob.SCREEN_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.FONT_LINE_SPACING = (int)(JGGlob.FONT_LINE_SPACING/JGGlob.SCALE*scale);
    JGGlob.PLAYER_WIDTH = (int)(JGGlob.PLAYER_WIDTH/JGGlob.SCALE*scale);
    JGGlob.PLAYER_HEIGHT = (int)(JGGlob.PLAYER_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.PLAYER_Y = (int)(JGGlob.PLAYER_Y/JGGlob.SCALE*scale);
    JGGlob.PMISSILE_WIDTH = (int)(JGGlob.PMISSILE_WIDTH/JGGlob.SCALE*scale);
    JGGlob.PMISSILE_HEIGHT = (int)(JGGlob.PMISSILE_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.PMISSILE_XOFFSET = (int)(JGGlob.PMISSILE_XOFFSET/JGGlob.SCALE*scale);
    JGGlob.BADGUY1_WIDTH = (int)(JGGlob.BADGUY1_WIDTH/JGGlob.SCALE*scale);
    JGGlob.BADGUY1_HEIGHT = (int)(JGGlob.BADGUY1_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.BADGUY2_WIDTH = (int)(JGGlob.BADGUY2_WIDTH/JGGlob.SCALE*scale);
    JGGlob.BADGUY2_HEIGHT = (int)(JGGlob.BADGUY2_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.BADGUY3_WIDTH = (int)(JGGlob.BADGUY3_WIDTH/JGGlob.SCALE*scale);
    JGGlob.BADGUY3_HEIGHT = (int)(JGGlob.BADGUY3_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.HEADBADGUY_WIDTH = (int)(JGGlob.HEADBADGUY_WIDTH/JGGlob.SCALE*scale);
    JGGlob.HEADBADGUY_HEIGHT = (int)(JGGlob.HEADBADGUY_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.BADGUY_HORZ_SPACE = (int)(JGGlob.BADGUY_HORZ_SPACE/JGGlob.SCALE*scale);
    JGGlob.BADGUY_VERT_SPACE = (int)(JGGlob.BADGUY_VERT_SPACE/JGGlob.SCALE*scale);
    JGGlob.BADGUY_VERT_START = (int)(JGGlob.BADGUY_VERT_START/JGGlob.SCALE*scale);
    JGGlob.EMISSILE_WIDTH = (int)(JGGlob.EMISSILE_WIDTH/JGGlob.SCALE*scale);
    JGGlob.EMISSILE_HEIGHT = (int)(JGGlob.EMISSILE_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.EMISSILE_XOFFSET = (int)(JGGlob.EMISSILE_XOFFSET/JGGlob.SCALE*scale);
    JGGlob.EMISSILE_YOFFSET = (int)(JGGlob.EMISSILE_YOFFSET/JGGlob.SCALE*scale);
    JGGlob.EXTRALIFE_HEIGHT=(int)(JGGlob.EXTRALIFE_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.EXTRALIFE_STARTX=(int)(JGGlob.EXTRALIFE_STARTX/JGGlob.SCALE*scale);
    JGGlob.EXTRALIFE_HORZ_SPACE=(int)(JGGlob.EXTRALIFE_HORZ_SPACE/JGGlob.SCALE*scale);
    JGGlob.LEVELFLAG_HEIGHT=(int)(JGGlob.LEVELFLAG_HEIGHT/JGGlob.SCALE*scale);
    JGGlob.LEVELFLAG_STARTX=(int)(JGGlob.LEVELFLAG_STARTX/JGGlob.SCALE*scale);
    JGGlob.LEVELFLAG_HORZ_SPACE=(int)(JGGlob.LEVELFLAG_HORZ_SPACE/JGGlob.SCALE*scale);
    JGGlob.INITIAL_ENEMY_DESCENT_SPEED=(int)(JGGlob.INITIAL_ENEMY_DESCENT_SPEED/JGGlob.SCALE*scale);
    JGGlob.INITIAL_ENEMY_LATERAL_SPEED=(int)(JGGlob.INITIAL_ENEMY_LATERAL_SPEED/JGGlob.SCALE*scale);
    JGGlob.MAX_ENEMY_DESCENT_SPEED=(int)(JGGlob.MAX_ENEMY_DESCENT_SPEED/JGGlob.SCALE*scale);
    JGGlob.MAX_ENEMY_LATERAL_SPEED=(int)(JGGlob.MAX_ENEMY_LATERAL_SPEED/JGGlob.SCALE*scale);
    JGGlob.SCALE = scale;
  }
}
