// JGScore300Sprite.java
//
// Author: J Spencer Seidel
// Description: A floating, fading "300"

package com.spencerseidel;

import java.awt.*;
import java.lang.Math;
import com.spencerseidel.*;

public class JGScore300Sprite extends JGSprite {
	
	private int cnt;

	public JGScore300Sprite(int x, int y, int w, int h, Image f[]) {
		super(x, y, w, h, 0, f);
		
		cnt=0;
	}

	public void update() {
		if (cnt%3 == 0) {	
			if (++currFace >= faces.length) {
				alive = false;
			}
		}

		cnt++;
	}
	
	public void died() {
	}
}
