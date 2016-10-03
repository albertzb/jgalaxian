// JGEMissileSprite.java
//
// Author: J Spencer Seidel
// Description: The enemy missile

package com.spencerseidel;

import java.awt.*;
import com.spencerseidel.*;

public class JGEMissileSprite extends JGSprite {

	private static final int MOVEMENT_SPEED=(int)(5.0*JGGlob.SCALE);

	public JGEMissileSprite(int x, int y, int w, int h, Image f[]) {
		super(x, y, w, h, JGGlob.EMISSILE_TYPE, f);	
	}

	public void update() {
		locRect.y += MOVEMENT_SPEED;
		if (locRect.y > JGGlob.SCREEN_HEIGHT ) {
			alive = false;
		}
	}
	
	public void died() {
	}
}
