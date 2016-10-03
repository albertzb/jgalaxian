// JGExplosionPieceSprite.java
//
// Author: J Spencer Seidel
// Description: A fragment that shoots out during an explosion

package com.spencerseidel;

import java.awt.*;
import java.lang.Math;
import com.spencerseidel.*;

public class JGExplosionPieceSprite extends JGSprite {

	private static double MAX_SPEED = 10.0*JGGlob.SCALE;

	private int dx;
	private int dy;
	private int cnt;

	public JGExplosionPieceSprite(int x, int y, int w, int h, Image f[]) {
		super(x, y, w, h, 0, f);
	
		int s = (int)(Math.random()*(MAX_SPEED-2.0)) + 2;
		int deg = (int)(Math.random()*360.0);

		// Calculate the x and y components of our movement
		double rad = (double)deg*3.14/180.0;

		dy = (int)(Math.cos(rad) * (double)s);
		dx = (int)(Math.sin(rad) * (double)s);

		cnt = 0;
	}

	public void update() {
		if (cnt%3 == 0) {
			if (++currFace >= faces.length) {
				alive = false;
			}
		}

		locRect.x += dx;
		locRect.y += dy;

		if (locRect.x + locRect.width > JGGlob.SCREEN_WIDTH ||
		    locRect.x < 0 ||
				locRect.y + locRect.height > JGGlob.SCREEN_HEIGHT ||
				locRect.y < 0) {
				alive = false;
		}

		cnt++;
	}
	
	public void died() {
	}
}
