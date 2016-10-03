// JGPlayerSprite.java
//
// Author: J Spencer Seidel
// Description: The player's ship

package com.spencerseidel;

import java.awt.*;
import com.spencerseidel.*;

public class JGPlayerSprite extends JGSprite {

	private static final int MOVEMENT_SPEED=(int)(5.0*JGGlob.SCALE);

	public JGPlayerSprite(int x, int y, int w, int h, Image f[]) {
		super(x, y, w, h, JGGlob.PLAYER_TYPE, f);	

		JGGlob.playerAlive = true;
	}

	public void update() {
		if (JGGlob.direction == JGGlob.MOVELEFT) {
			locRect.x -= MOVEMENT_SPEED;
			if (locRect.x < 0) {
				locRect.x = 0;
			}
		}
		else if (JGGlob.direction == JGGlob.MOVERIGHT) {
			locRect.x += MOVEMENT_SPEED;
			if (locRect.x > (JGGlob.SCREEN_WIDTH - locRect.width)) {
				locRect.x = JGGlob.SCREEN_WIDTH - locRect.width;
			}
		}

		// Update the bulletin board about where we are
		JGGlob.playerx = locRect.x;
	}
	
	public void died() {
		JGGlob.playerAlive = false;
	}
}
