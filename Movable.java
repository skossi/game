package com.blokk.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Movable {
	public Boolean typeOne;
	public boolean typeTwo;
	public int col;
	public int row;
	public Texture type;
	public float speed;
	public float x;
	public float y;
	public float width;
	public float height;
	public boolean isMovable;
	public long timeThrusted;
	public boolean isBeingThrusted;
	
	public Movable(boolean isMovable) {
		col = randomizeSlot();
		
		if (true) {
			typeOne = randomizeType();;
			typeTwo = true;
//			type = createType();
		}
		else {
			typeOne = null;
//			type = black;
		}
	}
	
	public Movable(Movable m) {
		typeOne = m.typeOne;
		typeTwo = m.typeTwo;
		col = m.col;
		row = m.row;
		type = m.type;
		speed = m.speed;
		x = m.x;
		y = m.y;
		width = m.width;
		height = m.height;
		isMovable = m.isMovable;
	}
	
	public void update(float dy) {	
		
		if(System.currentTimeMillis() - timeThrusted > 1000 && isBeingThrusted){
			isBeingThrusted = false;
			speed = -600;
			//breytum týpum svo þetta fari ekki að haga sér illa
			//semsagt skjótast upp aftur og aftur
			typeOne = null;
//			typeTwo = randomizeType();
			
		}
		y += speed*dy;
		
		return;
	}
	
	public boolean intersects(Movable m) {
		if (x < (m.x + m.width) && (x + width) > m.x && (y + height) > m.y && y < (m.y + m.height)) return true;
		return false;
	}
	
//	private Texture createType() {
//		1 = false;
//		
//		if (typeOne && typeTwo) return square;
//		else if (!typeOne && typeTwo) return triangle;
//		else if (typeOne && !typeTwo) return circle;
//		else return ex;
//	}
	
	private boolean randomizeType() {
		boolean type = false;
		
		if (Math.random() < 0.5) type = true;
		
		return type;
	}
	
//	private Boolean randomizeType_B() {
//		Boolean type = false;
//		
//		if (Math.random() < 0.5) type = true;
//		
//		return type;
//	}
	
	private int randomizeSlot() {
		return MathUtils.random(0, 6);
	}
}