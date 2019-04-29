// JGGlob.java
//
// Author: J Spencer Seidel
// Description: The glob is used to hold all the game state information
//              that many of the Sprites need, like whether there is
//              currently a missile on screen or what key is being
//              pressed. Most of this information could be stored in the
//              app itself, but that can sometimes get too big to
//              read. So, the app owns it, but it's visible to all
//              the sprites, who can update the information as needed. I like
//              to think of it as a community bulletin board. None of the
//              information is mission-critical. Also, it's nice to have
//              all static type information in one place instead of scattered
//              among a bunch of files.

package com.spencerseidel;

import java.awt.event.KeyEvent;

public class JGGlob {

  // Screen and animation info
  public static final double                  DEFAULT_SCALE=1.0;
  public static       double                  SCALE=DEFAULT_SCALE;
  public static       int                     SCREEN_WIDTH = 448; // Scalable
  public static       int                     SCREEN_HEIGHT = 512; // Scalable
  public static final long                    FRAME_DELAY = 20; // Milliseconds
  public static       int                     FONT_LINE_SPACING = 2; // Scalable

  // Keys
  public static final int                     KEYLEFT1 = KeyEvent.VK_LEFT;
  public static final int                     KEYLEFT2 = KeyEvent.VK_J;
  public static final int                     KEYRIGHT1 = KeyEvent.VK_RIGHT;
  public static final int                     KEYRIGHT2 = KeyEvent.VK_K;
  public static final int                     KEYFIRE1 = KeyEvent.VK_SPACE;
  public static final int                     KEYFIRE2 = KeyEvent.VK_A;
  public static final int                     KEYQUIT = KeyEvent.VK_Q;
  public static final int                     KEYPAUSE = KeyEvent.VK_P;

  // Commands
  public static final int                     NOMOVE    = 0;
  public static final int                     MOVELEFT  = 1;
  public static final int                     MOVERIGHT = 2;

  // Type of images we're dealing with
  public static final String                  IMAGE_EXT = ".png";

  // This is static information about each kind of Sprite
  public static final String                  PLAYER_IMAGE_BASE = "art/player";
  public static final int                     PLAYER_IMAGES_TO_LOAD = 1;
  public static       int                     PLAYER_WIDTH = 26; // Scalable
  public static       int                     PLAYER_HEIGHT = 40; // Scalable
  public static       int                     PLAYER_Y = 438; // Scalable
  public static final int                     PLAYER_TYPE = -1;

  public static final String                  PMISSILE_IMAGE_BASE = "art/pmissile";
  public static final int                     PMISSILE_IMAGES_TO_LOAD = 1;
  public static       int                     PMISSILE_WIDTH = 4; // Scalable
  public static       int                     PMISSILE_HEIGHT = 8; // Scalable
  public static       int                     PMISSILE_XOFFSET = 11; // Scalable
  public static final int                     PMISSILE_TYPE = -2;

  public static final String                  BADGUY1_IMAGE_BASE = "art/bg1";
  public static final int                     BADGUY1_IMAGES_TO_LOAD = 20;
  public static       int                     BADGUY1_WIDTH = 28; // Scalable
  public static       int                     BADGUY1_HEIGHT = 28; // Scalable
  public static final int                     BADGUY1_TYPE = 2;

  public static final String                  BADGUY2_IMAGE_BASE = "art/bg2";
  public static final int                     BADGUY2_IMAGES_TO_LOAD = 20;
  public static       int                     BADGUY2_WIDTH = 28; // Scalable
  public static       int                     BADGUY2_HEIGHT = 28; // Scalable
  public static final int                     BADGUY2_TYPE = 3;

  public static final String                  BADGUY3_IMAGE_BASE = "art/bg3";
  public static final int                     BADGUY3_IMAGES_TO_LOAD = 20;
  public static       int                     BADGUY3_WIDTH = 28; // Scalable
  public static       int                     BADGUY3_HEIGHT = 28; // Scalable
  public static final int                     BADGUY3_TYPE = 4;

  public static final String                  HEADBADGUY_IMAGE_BASE = "art/g";
  public static final int                     HEADBADGUY_IMAGES_TO_LOAD = 20;
  public static       int                     HEADBADGUY_WIDTH = 28; // Scalable
  public static       int                     HEADBADGUY_HEIGHT = 28; // Scalable
  public static final int                     HEADBADGUY_TYPE = 5;

  public static       int                     BADGUY_HORZ_SPACE = 35; // Scalable
  public static       int                     BADGUY_VERT_SPACE = 24; // Scalable
  public static       int                     BADGUY_VERT_START = 191; // Scalable
  public static final boolean                 BADGUY_ATTACK_RIGHT = true;
  public static final boolean                 BADGUY_ATTACK_LEFT = false;

  public static final String                  EMISSILE_IMAGE_BASE = "art/emissile";
  public static final int                     EMISSILE_IMAGES_TO_LOAD = 1;
  public static       int                     EMISSILE_WIDTH = 4; // Scalable
  public static       int                     EMISSILE_HEIGHT = 8; // Scalable
  public static       int                     EMISSILE_XOFFSET = 14; // Scalable
  public static       int                     EMISSILE_YOFFSET = 24; // Scalable
  public static final int                     EMISSILE_TYPE = 6;

  public static final String                  EXTRALIFE_IMAGE_BASE= "art/extralife0";
  public static       int                     EXTRALIFE_HEIGHT=28; // Scalable
  public static       int                     EXTRALIFE_STARTX=22; // Scalable
  public static       int                     EXTRALIFE_HORZ_SPACE=28; // Scalable

  public static final String                  LEVELFLAG_IMAGE_BASE= "art/levelflag";
  public static       int                     LEVELFLAG_HEIGHT=26; // Scalable
  public static       int                     LEVELFLAG_STARTX=416; // Scalable
  public static       int                     LEVELFLAG_HORZ_SPACE=18; // Scalable
  public static final int                     LEVELFLAG_NORMAL=0;
  public static final int                     LEVELFLAG_WORTH5=1;

  public static final String                  SCORE300_IMAGE_BASE = "art/score300";
  public static final int                     SCORE300_IMAGES_TO_LOAD = 10;

  public static final String                  SOUNDS_BASE = "sounds";

  // Initial attack frequency
  public static final double                 INITIAL_ATTACK_FREQUENCY=0.001;
  public static final double                 LEVELINC_ATTACK_FREQUENCY=0.001;
  // Initial enemy fire frequency
  public static final double                 INITIAL_ENEMY_FIRE_FREQUENCY=0.01;
  public static final double                 LEVELINC_ENEMY_FIRE_FREQUENCY=0.01;
  // Initial enemy descent and lateral speeds
  public static       int                     INITIAL_ENEMY_DESCENT_SPEED=2; // Scalable
  public static       int                     INITIAL_ENEMY_LATERAL_SPEED=2; // Scalable
  // Maximums
  public static       int                     MAX_ENEMY_DESCENT_SPEED=3; // Scalable
  public static       int                     MAX_ENEMY_LATERAL_SPEED=6; // Scalable

  public static final int                     NUM_STARS=25;

  // For scoring and explosion
  public static final int                     COL_PMISSILE_BADGUY=1;
  public static final int                     COL_PLAYER_BADGUY=2;
  public static final int                     COL_EMISSILE_PLAYER=3;
  public static final int                     COL_PMISSILE_EMISSILE=4;

  // Game states
  public static final int                     JGSTATE_GAMEOVER=0;
  public static final int                     JGSTATE_PLAYING=1;
  public static final int                     JGSTATE_CHANGINGLEVEL1=2;
  public static final int                     JGSTATE_CHANGINGLEVEL2=3;
  public static final int                     JGSTATE_PLAYERDIED=4;
  public static final int                     JGSTATE_PAUSED=5;

  // These are for free guys
  public static final int                     FREE_GUY_INTERVAL=4500;

  // These are stateful things that any game object
  // can change as game events happen
  public static int                           direction;
  public static boolean                       fire;
  public static int                           playerx;
  public static int                           numBadGuys;
  public static boolean                       loadAnotherMissile;
  public static boolean                       playerAlive;
  public static int                           gridLeftEdge;
  public static int                           numHeadBadGuys;
  public static boolean                       playerFired;

  // For storing high scores
  public static final String                  HIGH_SCORE_PREF="hiscore";

  void JGGlob() {
  }
}
