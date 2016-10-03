// JGBadGuySprite.java
//
// Author: J Spencer Seidel
// Description: The player's ship

package com.spencerseidel;

import java.awt.*;
import com.spencerseidel.*;

public class JGBadGuySprite extends JGSprite {

	private static final int ATTACK_NOT_ATTACKING=0;
	private static final int ATTACK_BEGIN=1;
	private static final int ATTACK_CHASE=2;
	private static final int ATTACK_DESCEND=3;
	private static final int ATTACK_HOME=4;
	private static final int ATTACK_AS_SLAVE=5;
	
	private static final int ATTACK_SPIN_SPEED=3;

	// We'll use a counter to determine when to switch faces
	private int cnt;
	// This array is used to figure out which face to display
	private int facePattern[];	
	// This index keeps track of which face to display
	private int facePatternIdx;
	// This keeps track of which direction we're moving
	private int dy;
	private int dx;
	private int maxDescentSpeed;
	private int maxLateralSpeed;
	// Keeps track of this Sprite's place in the grid
	private int row, col;
	// Keeps track of our attack state
	private int attackState;
	private boolean attackDirection;
	// When in slave mode, we have a master
	private JGBadGuySprite master;

	public JGBadGuySprite(int r, int c, int x, int y, int w, int h, Image f[], int type, int maxDescent, int maxLateral) {
		super(x, y, w, h, type, f);	

		facePattern = new int[4];
		facePattern[0] = 0;
		facePattern[1] = 1;
		facePattern[2] = 0;
		facePattern[3] = 2;
		facePatternIdx = c%4;

		row = r;
		col = c;

		attackState = ATTACK_NOT_ATTACKING;
		master = null;

		maxDescentSpeed = maxDescent;
		maxLateralSpeed = maxLateral;
	}

	public void update() {
		switch (attackState) {
			case ATTACK_BEGIN: attackBegin(); break;
			case ATTACK_CHASE: attackChase(); break;
			case ATTACK_DESCEND: attackDescend(); break;
			case ATTACK_HOME: attackHome(); break;
			case ATTACK_AS_SLAVE: getMastersState(); break;
			default: updateNotAttacking(); break;
		}
	}

	// For the master/slave relationship
	public int getCurrFaceIdx() {
		return currFace;
	}
	
	// This gets the master's state
	private void getMastersState() {
	
		// if our master died, we're on our own
		if (!master.alive() || locRect.y > JGGlob.SCREEN_HEIGHT) {
			attackState = ATTACK_CHASE;
			dy = maxDescentSpeed;
			return;
		}
	
		int masterx = master.getx();	
		int mastery = master.gety();
		currFace = master.getCurrFaceIdx();

		locRect.x = masterx;
		if (col < master.getCol()) {
			locRect.x -= JGGlob.BADGUY_HORZ_SPACE;	
		}
		else if (col > master.getCol()) {
			locRect.x += JGGlob.BADGUY_HORZ_SPACE;	
		}

		locRect.y = mastery + JGGlob.BADGUY_HORZ_SPACE;
	}

	// This is the "up and over" manuver
	private void attackBegin() {
		int deltaFace = (attackDirection == JGGlob.BADGUY_ATTACK_LEFT ? -1 : 1);
		int targetFace = (attackDirection == JGGlob.BADGUY_ATTACK_LEFT ? 3 : 18);

		dx = (attackDirection == JGGlob.BADGUY_ATTACK_LEFT ? (int)(-2.0*JGGlob.SCALE) : (int)(2.0*JGGlob.SCALE));
		
		locRect.x += dx;
		locRect.y += dy;

		dy += (int)(JGGlob.SCALE);
		if (dy > maxDescentSpeed) {
			dy = maxDescentSpeed;	
		}

		if (cnt%ATTACK_SPIN_SPEED == 0) {
			currFace += deltaFace;	
		}

		if (currFace == targetFace) {
			attackState = ATTACK_CHASE;
			currFace = 19;
		}
	
		cnt++;
	}

	// Adjust our lateral speed and angle to try and sweep the player
	private void attackChase() {
		int d = (locRect.x < JGGlob.playerx ? (int)(1.0*JGGlob.SCALE) : (int)(-1.0*JGGlob.SCALE));

		if (cnt%7 == 0)
		{
			dx += d;
			if (dx > maxLateralSpeed) {
				dx = maxLateralSpeed;
			}
			else if (dx < -maxLateralSpeed) {
				dx = -maxLateralSpeed;
			}
		}

		// Adjust the face based on what percentage we are at max
		//
		
		if (dx==0) {
			currFace = 19;	
		}
		else if (dx > 0) {
			// 4.0 is the number of available faces
			int faceIdx = (int)(4.0*(double)(dx)/(double)(maxLateralSpeed));
			// face '18' is the least angled	moving right, '14' is most angled
			currFace = 18 - faceIdx;
			if (currFace < 15) {
				currFace = 15;
			}
		}
		else {
			// 4.0 is the number of available faces
			int faceIdx = (int)(4.0*(double)(dx)/(double)(maxLateralSpeed));
			// face '3' is the least angled	moving left face, '6' is most angled
			currFace = 3 + -faceIdx;
			if (currFace > 6 ) {
				currFace = 6;
			}
		}

		locRect.x += dx;
		locRect.y += dy;

		if (locRect.y > (int)(400.0*JGGlob.SCALE)) {
			attackState = ATTACK_DESCEND;	
		}
		
		cnt++;
	}

	// We've made a swoop, now just continue downward
	private void attackDescend() {
	
		locRect.x += dx;
		locRect.y += dy;

		if (locRect.y > JGGlob.SCREEN_HEIGHT) {
			locRect.y = -locRect.height;
			locRect.x = JGGlob.gridLeftEdge + (JGGlob.BADGUY_HORZ_SPACE*col);
			attackState = ATTACK_HOME;
			currFace = 19;
			dy = (int)(2.0*JGGlob.SCALE);
		}
		
		cnt++;
	}

	private void attackHome() {
		int origy = JGGlob.BADGUY_VERT_START - JGGlob.BADGUY_VERT_SPACE*row;
		locRect.y += dy;

		if (origy - locRect.y < 11) {
			if (--currFace < 11) {
				locRect.y = origy;
				facePatternIdx = col%4;
				currFace = facePattern[facePatternIdx];
				attackState = ATTACK_NOT_ATTACKING;
			}
		}

		locRect.x = JGGlob.gridLeftEdge + (JGGlob.BADGUY_HORZ_SPACE*col);	

		cnt++;
			
	}

	private void updateNotAttacking() {
		// Update our face
		if (cnt%15 == 0) {
			facePatternIdx++;
			if (facePatternIdx > 3) {
				facePatternIdx = 0;
			}

			currFace = facePattern[facePatternIdx];
		}

		cnt++;

		locRect.x = JGGlob.gridLeftEdge + (JGGlob.BADGUY_HORZ_SPACE*col);
	}

	public void attack(boolean direction) {
		attackState = ATTACK_BEGIN;	
		attackDirection = direction;	
		currFace = (attackDirection == JGGlob.BADGUY_ATTACK_LEFT ? 10 : 11);
		dy = (int)(-5.0*JGGlob.SCALE);
	}

	// Slave mode
	public void attack(boolean direction, JGBadGuySprite myMaster) {
		attackState = ATTACK_AS_SLAVE;
		master = myMaster;	
		attackDirection = direction;	
		currFace = (attackDirection == JGGlob.BADGUY_ATTACK_LEFT ? 10 : 11);
		dy = (int)(-5.0*JGGlob.SCALE);
	}

	public boolean isAttacking() {
		return (attackState != ATTACK_NOT_ATTACKING);	
	}
	
	public boolean isInChaseMode() {
		return (attackState == ATTACK_CHASE);	
	}

	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}

	public void died() {
		JGGlob.numBadGuys--;
	
		if (type == JGGlob.HEADBADGUY_TYPE) {
			JGGlob.numHeadBadGuys--;
		}
	}
}
