package com.blokk.game;

public class UI {
	public float x;
	public float y;
	public float width;
	public float height;
	
	public UI(float newX, float newY, float newWidth, float newHeight) {
		x = newX;
		y = newY;		
		width = newWidth;
		height = newHeight;
	}
	
	public int isTouched(float touchX, float touchY) {
		float section = width/4;
		if (touchY > y && touchY < y + height) {
			if (touchX > 0 && touchX < section) {
				System.out.println("PAUSE");
				return 1; // pause
			}
			else if (touchX >= section && touchX < 2*section) return 2;
			else if (touchX >= 2*section && touchX < 3*section) return 3;
			else return 4;
		}
		return -1;
	}
}
