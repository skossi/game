package com.blokk.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

public class BlokkGame implements ApplicationListener {
   private Texture dropImage;
   private Texture bucketImage;
   private Sound dropSound;
   private Music rainMusic;
   private SpriteBatch batch;
   private OrthographicCamera camera;
   private Movable[][] Movables;
   private long lastDropTime;
   private float dy;
   private int rows;
   private int columns;
   private boolean isPaused;
   private int size;
   private float selectedX;
   private float selectedY;
   private Texture square;
   private Texture triangle;
   private Texture circle;
   private Texture ex;
   private Texture black;
   private Texture selected;
   private boolean isSelected;

   @Override
   public void create() {
      // create the camera and the SpriteBatch
      camera = new OrthographicCamera();
      camera.setToOrtho(false, 480, 800);
      batch = new SpriteBatch();
      size = 64;
      
      // create Movables
      columns = 7;
      rows = 13;
      Movables = new Movable[columns][rows];
      
	  square = new Texture(Gdx.files.internal("square.png"));
	  triangle = new Texture(Gdx.files.internal("triangle.png"));
	  circle = new Texture(Gdx.files.internal("circle.png"));
	  ex = new Texture(Gdx.files.internal("ex.png"));
	  black = new Texture(Gdx.files.internal("black.png"));
	  selected = new Texture(Gdx.files.internal("selected.png"));
      spawnMovable();
   }
   
   private void spawnMovable() {
	  Movable movable;
	  double randomize = Math.random();
	  if (randomize < 0.8) {
		  movable = new Movable(true);
		  movable.type = createType(movable.typeOne, movable.typeTwo);
	  }
	  else {
		  movable = new Movable(false);
		  movable.type = createType(movable.typeOne, movable.typeTwo);
	  }
	  
	  if (Movables[movable.col][rows-1] != null) return;
	  
      int available_row = 0;
      for (int i = 0; i < rows; i++) {
    	  if (Movables[movable.col][i] == null) {
    		  available_row = i;
    		  break;
    	  }
      }
      Movables[movable.col][available_row] = movable;
      movable.row = available_row;
      movable.x = (size+1)*movable.col;
      movable.y = 800;
      movable.speed = -600;
      movable.width = size;
      movable.height = size;
      movable.isBeingThrusted = false;
      lastDropTime = TimeUtils.nanoTime();
   }
   
   private Texture createType(Boolean typeOne, boolean typeTwo) {
		if (typeOne == null) return black;
		return (typeOne ? (typeTwo ? square : circle) : (typeTwo ? triangle : ex));
   }
   
   @Override
   public void render() {
      // clear the screen with a dark blue color. The
      // arguments to glClearColor are the red, green
      // blue and alpha component in the range [0,1]
      // of the color to be used to clear the screen.
      Gdx.gl.glClearColor(0.43f, 0.5f, 0.2f, 1);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      
      dy = Gdx.graphics.getDeltaTime()/3;

      // tell the camera to update its matrices.
      camera.update();

      // tell the SpriteBatch to render in the
      // coordinate system specified by the camera.
      batch.setProjectionMatrix(camera.combined);
      
      if (Gdx.input.justTouched()) {
    	  Vector3 touchPosOld = new Vector3();
    	  touchPosOld.set(Gdx.input.getX(),Gdx.input.getY(),0);
    	  camera.unproject(touchPosOld);
    	  selectedX = touchPosOld.x;
    	  selectedY = touchPosOld.y;
    	  
    	  int row = (int)(selectedY/size);
   	   	  int column = (int)(selectedX/size);
    	  
    	  selectedX = column*65 + size/2;
    	  selectedY = row*65 + size/2;
    	  isSelected = true;
      }
      
      // process user input
      if(Gdx.input.isTouched()) {
    	 int x = Gdx.input.getX();
    	 int y = Gdx.input.getY();
         Vector3 touchPos = new Vector3();
         touchPos.set(x, y, 0);
         camera.unproject(touchPos);
         
         findMovable(touchPos.x, touchPos.y);
      }
      else isSelected = false;
      
      //if (Gdx.input.justTouched()) isPaused = !isPaused;
      
      update(dy);
      
      batch.begin();
      //batch.draw(bucketImage, bucket.x, bucket.y);
      for(int i = 0; i < columns; i++) {
    	  for (int j = 0; j < rows; j++) {
    		  Movable m = Movables[i][j];
    		  if (m != null && !(m.speed == 0)) batch.draw(createType(m.typeOne,m.typeTwo), m.x, m.y);
    		  else if (m != null && m.speed == 0) batch.draw(createType(m.typeOne,m.typeTwo), i*65, j*65);
    	  }
      }
      if(isSelected)batch.draw(selected, selectedX-size/2, selectedY-size/2);
      batch.end();
   }
   
   public void update(float dy) {
	   if (isPaused) return;
	   if (TimeUtils.nanoTime() - lastDropTime > 900000000) spawnMovable();
	   
	   int steps = 64;
	   
	   for (int i = 0; i < steps; i++) computeSubStep(dy/steps);
   }
   
   public void computeSubStep(float dy) {
	   for(Movable[] rows : Movables) {
    	  for (Movable m1 : rows) {
    		  if(m1 == null) continue;
    		  if (m1.speed < 0 && 
    				  System.currentTimeMillis() - m1.timeThrusted > 1000) {
	    		  for(Movable[] rows2 : Movables) {
	    	    	  for (Movable m2 : rows2) {
	    	    		  //tilvikið þegar kubbur rekst á kubb fyrir neðan sig
	    	    		  if (m2 != null && m1 != m2 && m1.intersects(m2)) {
	    	    			  m1.speed = m2.speed;
	    	    			  if(m1.speed>0){
	    	    				  m1.isBeingThrusted = true;
	    	    				  m1.timeThrusted = m2.timeThrusted;
	    	    			  }
	    	    			  handleMatches(m1);
	    	    			  break;
	    	    		  }
//	    	    		  System.out.println("row:" + m1.row);
	    	    		  //hér athugum við fyrir kubba á neðstu línu hvort það séu 3 eða fleiri saman lárétt
	    	    		  //tilvikið að ofan dekkar þetta ekki
	    	    		  if(m1.row == 0 && m1.y <= 20){
	    	    			  m1.speed = 0;
	    	    			  handleMatches(m1);
	    	    			  break;
	    	    		  }
	    	    		  
	    	    	  }
	    	      }
    		  }
        	  m1.update(dy);
    	  }
      }
   }
   
   public void handleMatches(Movable m1){
	   if (m1.typeOne == null) return;
	   checkRowMatches(m1);
	   checkColMatches(m1);
	   return;
   }
   
  public void checkColMatches(Movable m1){
	   Boolean typeOne = m1.typeOne;
	   boolean typeTwo = m1.typeTwo;
	   int count = 0;
	   int col = m1.col;
	   int index = -1;
	   for(int j = 0; j < rows; j++){
		   for(int i = j;  i< rows; i++){
			   if( isSameType(Movables[col][i], typeOne, typeTwo)){
				   if(Movables[col][i].speed == 0){
					   count++;   
				   }
				   
			   } else{
				   break;
			   }
		   }
		   if(count >= 3){
			   index = j;
			   break;
		   }
		   count = 0;
	   }
	   if(count > 1){
		   for(int j = index; j < index+count; j++){
			   Movables[col][j].type = circle;
		   }
	   }
	   return;
  }
   
   public void checkRowMatches(Movable m1){
	   //Stundum kemur villa thegar kubbur dettur nidur a milli tveggja kubba
	   //thad sem gerist er ad kubburinn er merktur sem ad hann matchist adur en hann
	   //er buinn ad detta nidur
	   Boolean typeOne = m1.typeOne;
	   boolean typeTwo = m1.typeTwo;
	   int count = 0;
	   int row = m1.row;
	   int index = -1;
	   for(int j = 0; j < columns; j++){
		   for(int i = j;  i< columns; i++){
			   if( isSameType(Movables[i][row], typeOne, typeTwo)){
				   if(Movables[i][row].speed == 0){
					   count++;   
				   }
				   
			   } else{
				   break;
			   }
		   }
		   if(count >= 3){
			   index = j;
			   break;
		   }
		   count = 0;
	   }
	   if(count > 1){
//		   for(int j = index; j < index+count; j++){
//			   Movables[j][row].type = circle;
//		   }
		   shootRows(index, count, row, false);
	   }
	   return;
   }
   
   
   //notkun: shootRows(index, count, row, isBeingThrusted);
   //eftir: bÃºiÃ° er aÃ° skjÃ³ta upp Ã¶ll kubbum fyrir ofan rÃ¶Ã° row Ã­ dÃ¡lkum
   //frÃ¡ index to index+count
   //ef isThrusting er true Ã¾Ã¡ er Ã¶llum kubbum sem er veriÃ° aÃ° skjÃ³ta Ã­ Ã¾eim dÃ¡lkum
   //skotiÃ° alla leiÃ° Ãºr borÃ°inu
   public void shootRows(int index, int count, int row, boolean isBeingTrusted){
	   System.out.println("Shooting!");
	   if(isBeingTrusted){
		   //Vantar hÃ©r lÃ³gÃ­k til aÃ° skjÃ³ta platforminu alla leiÃ° upp
	   }
	   for(int j = index; j < index+count; j++){
		   for (int i = row; i < rows; i++){
			   if(Movables[j][i] != null) {
				   if(Movables[j][i].speed != 0) continue;
				   System.out.println("SPEED:" + Movables[j][i].speed);
				   Movables[j][i].speed = 400;
			       Movables[j][i].timeThrusted = System.currentTimeMillis();   
			       Movables[j][i].isBeingThrusted = true;   
			   }
		   }
	   }
   }
   
   public boolean isSameType(Movable m1, Boolean typeOne, boolean typeTwo){
	   if(m1 == null){return false;}
	   return m1.typeOne == typeOne && m1.typeTwo == typeTwo;
   }
   
   public void findMovable(float x, float y) {
	   int row = (int)(selectedY/size);
	   int col = (int)(selectedX/size);
	   
	   if (row < 0 || row > 12 || col < 0 || col > 6) return;
	   
	   if (Movables[col][row] != null && Movables[col][row].typeOne != null) {
		   Movable m1 = new Movable(Movables[col][row]);
		   
		   if (row < 11 && Movables[col][row+1] != null) {
			   Movable m2 = new Movable(Movables[col][row+1]);
			   
			   if (y > selectedY + size && m2 != null && m2.typeOne != null && m2.speed == 0) {
				   selectedY += size;
				   swapMovables(m1, m2, col, row, 1);
				   handleMatches(Movables[col][row]);
				   handleMatches(Movables[col][row+1]);
			   }
		   }
		   
		   if (row > 0 && Movables[col][row-1] != null) {
			   Movable m2 = new Movable(Movables[col][row-1]);
			   
			   if (y < selectedY - size && m2 != null && m2.typeOne != null && m2.speed == 0) {
				   selectedY -= size;
				   swapMovables(m1, m2, col, row, -1);
				   handleMatches(Movables[col][row]);
				   handleMatches(Movables[col][row-1]);
			   }
		   }
	   }
	   
   }
   
//   public boolean canSwap(int column, int row, int add) {
//	   return true;
//   }
//   
   public void swapMovables(Movable m1, Movable m2, int col, int row, int add) {
//	   int m1_col = m1.col;
//	   int m1_row = m1.row;
//	   
//	   int m2_col = m2.col;
//	   int m2_row = m2.row;
	   
	   Movable temp1 = new Movable(m1);
	   Movable temp2 = new Movable(m2);
	   
	   Movables[col][row] = temp2;
	   Movables[col][row+add] = temp1;
   }

   @Override
   public void dispose() {
      // dispose of all the native resources
      dropImage.dispose();
      bucketImage.dispose();
      dropSound.dispose();
      rainMusic.dispose();
      batch.dispose();
   }

   @Override
   public void resize(int width, int height) {
   }

   @Override
   public void pause() {
   }

   @Override
   public void resume() {
   }
}