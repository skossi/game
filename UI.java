package com.blokk.game;

public class UI {
	public float x;
	public float y;
	public float width;
	public float height;
	
	/**
 	*  Constructs a user UI
 	* @param float newX is the UI’s lower-left x co-ordinate
 	* @param float newY is the UI’s upper-left y co-ordinate
 	* @param float newWidth is the UI’s width
 	* @param float newHeight is the UI’s height
 	*/
	public UI(float newX, float newY, float newWidth, float newHeight) {
		x = newX;
		y = newY;		
		width = newWidth;
		height = newHeight;
	}
	
	/**
 	*  Returns an integer based on where the user is touching the UI
 	* @param float touchX is the x co-ordinate of the touch
 	* @param float touchY is the y co-ordinate of the touch
 	*/
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
