// JGPMissileSprite.java
//
// Author: J Spencer Seidel
// Description: The player's missile

package com.spencerseidel;

import java.awt.*;
import com.spencerseidel.*;

public class JGPMissileSprite extends JGSprite {

	private static final int MOVEMENT_SPEED=(int)(10*JGGlob.SCALE);

	private boolean missileFired;

	public JGPMissileSprite(int x, int y, int w, int h, Image f[]) {
		super(x, y, w, h, 0, f);	
		missileFired = false;
	}

	public void update() {
		if (JGGlob.fire == true && missileFired == false) {
			missileFired = true;
			type = JGGlob.PMISSILE_TYPE;
			
			// This is for the benefit of the applet, which will
			// reset it after the frame is animated
			JGGlob.playerFired = true;
		}

		if (missileFired) {
			locRect.y -= MOVEMENT_SPEED;
			if (locRect.y < 0) {
				alive = false;
			}
		}
		else {
			// We need the player's x so we can follow it
			locRect.x = JGGlob.playerx + JGGlob.PMISSILE_XOFFSET;
		}

		// We must die if the player dies
		if (JGGlob.playerAlive == false) {
			alive = false;
		}
	}
	
	public void died() {
		if (JGGlob.playerAlive) {	
			JGGlob.loadAnotherMissile = true;
		}
		else {
			JGGlob.loadAnotherMissile = false;
		}
	}
}
