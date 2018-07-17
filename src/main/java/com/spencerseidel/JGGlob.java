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
	public static final double                  SCALE=1.5;
	public static final int                     SCREEN_WIDTH = (int)(448*SCALE);
	public static final int                     SCREEN_HEIGHT = (int)(512*SCALE);
	public static final long                    FRAME_DELAY = 20;                    // Milliseconds
	public static final int											FONT_LINE_SPACING = (int)(2*SCALE);

	// Keys
	public static final int                     KEYLEFT1 = KeyEvent.VK_LEFT;
	public static final int                     KEYLEFT2 = KeyEvent.VK_J;
	public static final int                     KEYRIGHT1 = KeyEvent.VK_RIGHT;
	public static final int                     KEYRIGHT2 = KeyEvent.VK_K;
	public static final int                     KEYFIRE1 = KeyEvent.VK_SPACE;
	public static final int                     KEYFIRE2 = KeyEvent.VK_A;
	public static final int                     KEYQUIT = KeyEvent.VK_Q;

	// Commands
	public static final int                     NOMOVE    = 0;
	public static final int                     MOVELEFT  = 1;
	public static final int                     MOVERIGHT = 2;

	// Type of images we're dealing with
	public static final String                  IMAGE_EXT = ".png";

	// This is static information about each kind of Sprite
	public static final String                  PLAYER_IMAGE_BASE = "art/player";
	public static final int                     PLAYER_IMAGES_TO_LOAD = 1;
	public static final int                     PLAYER_WIDTH = (int)(26.0*SCALE);
	public static final int                     PLAYER_HEIGHT = (int)(40.0*SCALE);
	public static final int                     PLAYER_Y = (int)(438.0*SCALE);
	public static final int                     PLAYER_TYPE = -1;

	public static final String                  PMISSILE_IMAGE_BASE = "art/pmissile";
	public static final int                     PMISSILE_IMAGES_TO_LOAD = 1;
	public static final int                     PMISSILE_WIDTH = (int)(4.0*SCALE);
	public static final int                     PMISSILE_HEIGHT = (int)(8.0*SCALE);
	public static final int                     PMISSILE_XOFFSET = (int)(11.0*SCALE);
	public static final int                     PMISSILE_TYPE = -2;

	public static final String                  BADGUY1_IMAGE_BASE = "art/bg1";
	public static final int                     BADGUY1_IMAGES_TO_LOAD = 20;
	public static final int                     BADGUY1_WIDTH = (int)(28.0*SCALE);
	public static final int                     BADGUY1_HEIGHT = (int)(28.0*SCALE);
	public static final int                     BADGUY1_TYPE = 2;

	public static final String                  BADGUY2_IMAGE_BASE = "art/bg2";
	public static final int                     BADGUY2_IMAGES_TO_LOAD = 20;
	public static final int                     BADGUY2_WIDTH = (int)(28.0*SCALE);
	public static final int                     BADGUY2_HEIGHT = (int)(28.0*SCALE);
	public static final int                     BADGUY2_TYPE = 3;

	public static final String                  BADGUY3_IMAGE_BASE = "art/bg3";
	public static final int                     BADGUY3_IMAGES_TO_LOAD = 20;
	public static final int                     BADGUY3_WIDTH = (int)(28.0*SCALE);
	public static final int                     BADGUY3_HEIGHT = (int)(28.0*SCALE);
	public static final int                     BADGUY3_TYPE = 4;

	public static final String                  HEADBADGUY_IMAGE_BASE = "art/g";
	public static final int                     HEADBADGUY_IMAGES_TO_LOAD = 20;
	public static final int                     HEADBADGUY_WIDTH = (int)(28.0*SCALE);
	public static final int                     HEADBADGUY_HEIGHT = (int)(28.0*SCALE);
	public static final int                     HEADBADGUY_TYPE = 5;

	public static final int                     BADGUY_HORZ_SPACE = (int)(35.0*SCALE);
	public static final int                     BADGUY_VERT_SPACE = (int)(24.0*SCALE);
	public static final int                     BADGUY_VERT_START = (int)(191.0*SCALE);
	public static final boolean                 BADGUY_ATTACK_RIGHT = true;
	public static final boolean                 BADGUY_ATTACK_LEFT = false;

	public static final String                  EMISSILE_IMAGE_BASE = "art/emissile";
	public static final int                     EMISSILE_IMAGES_TO_LOAD = 1;
	public static final int                     EMISSILE_WIDTH = (int)(4.0*SCALE);
	public static final int                     EMISSILE_HEIGHT = (int)(8.0*SCALE);
	public static final int                     EMISSILE_XOFFSET = (int)(14.0*SCALE);
	public static final int                     EMISSILE_YOFFSET = (int)(24.0*SCALE);
	public static final int                     EMISSILE_TYPE = 6;

	public static final String                  EXTRALIFE_IMAGE_BASE= "art/extralife0";
	public static final int                     EXTRALIFE_HEIGHT=(int)(28.0*SCALE);
	public static final int                     EXTRALIFE_STARTX=(int)(22.0*SCALE);
	public static final int                     EXTRALIFE_HORZ_SPACE=(int)(28.0*SCALE);

	public static final String                  GAMEOVER_IMAGE_BASE= "art/gameover";
	public static final int                     GAMEOVER_WIDTH=(int)(200.0*SCALE);
	public static final int                     GAMEOVER_HEIGHT=(int)(300.0*SCALE);

	public static final String                  GETREADY_IMAGE_BASE= "art/getready";
	public static final int                     GETREADY_WIDTH=(int)(200.0*SCALE);
	public static final int                     GETREADY_HEIGHT=(int)(50.0*SCALE);

	public static final String                  LEVELFLAG_IMAGE_BASE= "art/levelflag";
	public static final int                     LEVELFLAG_HEIGHT=(int)(26.0*SCALE);
	public static final int                     LEVELFLAG_STARTX=(int)(416.0*SCALE);
	public static final int                     LEVELFLAG_HORZ_SPACE=(int)(18.0*SCALE);
	public static final int                     LEVELFLAG_NORMAL=0;
	public static final int                     LEVELFLAG_WORTH5=1;

	public static final String                  SCORE300_IMAGE_BASE = "art/score300";
	public static final int                     SCORE300_IMAGES_TO_LOAD = 10;

	public static final String									SOUNDS_BASE = "sounds";

	// Initial attack frequency
	public static final double 								INITIAL_ATTACK_FREQUENCY=0.001;
	public static final double 								LEVELINC_ATTACK_FREQUENCY=0.001;
	// Initial enemy fire frequency
	public static final double 								INITIAL_ENEMY_FIRE_FREQUENCY=0.01;
	public static final double 								LEVELINC_ENEMY_FIRE_FREQUENCY=0.01;
	// Initial enemy descent and lateral speeds
	public static final int 										INITIAL_ENEMY_DESCENT_SPEED=(int)(2.0*SCALE);
	public static final int 										INITIAL_ENEMY_LATERAL_SPEED=(int)(2.0*SCALE);
	// Maximums
	public static final int 										MAX_ENEMY_DESCENT_SPEED=(int)(3.0*SCALE);
	public static final int 										MAX_ENEMY_LATERAL_SPEED=(int)(6.0*SCALE);

	public static final int 										NUM_STARS=25;

	// For scoring and explosion
	public static final int 										COL_PMISSILE_BADGUY=1;
	public static final int 										COL_PLAYER_BADGUY=2;
	public static final int 										COL_EMISSILE_PLAYER=3;
	public static final int 										COL_PMISSILE_EMISSILE=4;

	// Game states
	public static final int 										JGSTATE_GAMEOVER=0;
	public static final int 										JGSTATE_PLAYING=1;
	public static final int 										JGSTATE_CHANGINGLEVEL1=2;
	public static final int 										JGSTATE_CHANGINGLEVEL2=3;
	public static final int 										JGSTATE_PLAYERDIED=4;

	// These are for free guys
	public static final int 										FREE_GUY_INTERVAL=4500;

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

	void JGGlob() {
	}
}
