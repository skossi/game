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


/**
* @author     Ottar og �orsteinn. Edit by Hlynur
* @version     1.0a                 Alpha
* @since       2014-10-10        
*/
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

   /**
   * Starts the gameloop by opening components from badlogic pack and sets the orthogonal projection of the camera.
   */
   @Override
   public void create() {
      // create the camera and the SpriteBatch
      camera = new OrthographicCamera();
      camera.setToOrtho(false, 480, 800);
      batch = new SpriteBatch();
      size = 64;
      
      // create Movables
      columns = 7;
      rows = 14;
      Movables = new Movable[columns][rows];
      
	  square = new Texture(Gdx.files.internal("square.png"));
	  triangle = new Texture(Gdx.files.internal("triangle.png"));
	  circle = new Texture(Gdx.files.internal("circle.png"));
	  ex = new Texture(Gdx.files.internal("ex.png"));
	  black = new Texture(Gdx.files.internal("black.png"));
	  selected = new Texture(Gdx.files.internal("selected.png"));
	  prepareMatrix();
   }
   
   /**
   * Creates a new cube on a timed interval. It�s type is randomed. This method is a temporary solution for spawning cubes in debugging mode
   *
   * @return            a new cube of some sort is created and placed in the grid
   */
   private void spawnMovable() {
	  Movable movable;
	  double randomize = Math.random();
	  if (randomize < 1) {
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
      movable.row = available_row;
      movable.x = (size+1)*movable.col;
      movable.y = 900;
      movable.speed = -600;
      movable.width = size;
      movable.height = size;
      movable.isBeingThrusted = false;
      Movables[movable.col][available_row] = movable;
      lastDropTime = TimeUtils.nanoTime();
   }
   
   /**
   * Prepares the Movables matrix by adding a row of immovable blocks below the screen for collision purposes    
   * 
   * @return            one row of immovable blocks are created below the screen
   */
   public void prepareMatrix() {
	   for (int i = 0; i < columns; i++) {
		   Movable movable = new Movable(false);
		   movable.type = createType(movable.typeOne, movable.typeTwo);
		   
	       movable.row = 0;
	       movable.y = -(size+1);
	       movable.speed = 0;
	       movable.width = size;
	       movable.height = size;
	       movable.isBeingThrusted = false;
	       movable.x = (size+1)*i;
		   Movables[i][0] = movable;
	   }
   }
   
   /**
   * Gives a created cube its texture depend on his boolean tree structure    
   * 
   * @param typeOne file Boolean which decides if it is a movable cube or black block
   * @param typeTwo boolean that decides what kind of color the cube is
   * @return            returns Texture corresponding to it�s boolean structure
   */
   private Texture createType(Boolean typeOne, boolean typeTwo) {
		if (typeOne == null) return black;
		return (typeOne ? (typeTwo ? square : circle) : (typeTwo ? triangle : ex));
   }
   
   /**
   * Clears the window and draws all entities. Sends coordinates to methods to find out where player *is touching
   *
   */
   @Override
   public void render() {
      // clear the screen with a dark blue color. The
      // arguments to glClearColor are the red, green
      // blue and alpha component in the range [0,1]
      // of the color to be used to clear the screen.
      Gdx.gl.glClearColor(0.43f, 0.5f, 0.2f, 1);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
      
      dy = Gdx.graphics.getDeltaTime();

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
      for(int i = 0; i < columns; i++) {
    	  for (int j = 0; j < rows; j++) {
    		  Movable m = Movables[i][j];
    		  if (m != null) batch.draw(createType(m.typeOne,m.typeTwo), m.x, m.y);
    	  }
      }
      if(isSelected)batch.draw(selected, selectedX-size/2, selectedY-size/2);
      batch.end();
   }
   
   /**
   *Takes the delta time so we can update each entity to correspond to the input of the gameloop, *time
   * @param dy is the delta time of each frame rendered
   */
   public void update(float dy) {
	   if (isPaused) return;
	   if (TimeUtils.nanoTime() - lastDropTime > 900000000) spawnMovable();
	   
	   int steps = 64;
	   
	   for (int i = 0; i < steps; i++) computeSubStep(dy/steps);
   }
   
   /**
   *Breaks the entities update into smaller steps so it wont render out of bounds.
   * @param dy is the delta time of each frame rendered
   */   
   public void computeSubStep(float dy) {
	   for(Movable[] row : Movables) {
    	  for (Movable m1 : row) {
    		  if(m1 == null || m1.row == 0) continue;
    		  if (m1.speed < 0 && 
				  System.currentTimeMillis() - m1.timeThrusted > 1000) {
				  if (Movables[m1.col][m1.row-1] != null && m1 != Movables[m1.col][m1.row-1] && m1.intersects(Movables[m1.col][m1.row-1])) {
					  Movable m2 = Movables[m1.col][m1.row-1];
					  m1.speed = m2.speed;
					  if(m1.speed>0){
	    				  m1.isBeingThrusted = true;
	    				  m1.timeThrusted = m2.timeThrusted;
	    			  }
	    			  handleMatches(m1);
				  }
    		  }
        	  m1.update(dy);
    	  }
      }
   }
   
   /**
   *Checks to see if the player did indeed move a block to a valid position and to find if he added *three or more together
   * @param m1 A moved Movable block by the user
   */
   public void handleMatches(Movable m1){
	   if (m1.typeOne == null) return;
	   checkRowMatches(m1);
	   checkColMatches(m1);
	   return;
   }
   
   /**
   *Checks out if anyone is linked to the moved Movable in the Column
   * @param m1 A moved Movable block by the user
   */ 
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
   
   /**
   *Checks out if anyone is linked to the moved Movable in the Row. This one is bugged and needs *refactoring
   * @param m1 A moved Movable block by the user
   */
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
	   if(count > 2){
//		   for(int j = index; j < index+count; j++){
//			   Movables[j][row].type = circle;
//		   }
		   shootRows(index, count, row, false);
	   }
	   return;
   }
   
   
   //notkun: shootRows(index, count, row, isBeingThrusted);
   //eftir: búið er að skjóta upp öll kubbum fyrir ofan röð row í dálkum
   //frá index to index+count
   //ef isThrusting er true þá er öllum kubbum sem er verið að skjóta í þeim dálkum
   //skotið alla leið úr borðinu
   public void shootRows(int index, int count, int row, boolean isBeingThrusted){
	   System.out.println("Shooting!");
	   if(isBeingThrusted){
		   //Vantar hér lógík til að skjóta platforminu alla leið upp
	   }
	   for(int j = index; j < index+count; j++){
		   for (int i = row; i < rows; i++){
			   if(Movables[j][i] != null) {
				   if(Movables[j][i].speed < 0) continue;
				   Movables[j][i].speed = 400;
			       Movables[j][i].timeThrusted = System.currentTimeMillis();   
			       Movables[j][i].isBeingThrusted = true;
				   System.out.println("blokk: " + Movables[j][i].row + ", " + Movables[j][i].col + ", " + Movables[j][i].speed);   
			   }
		   }
	   }
   }
   
   /**
   *Finds out if the moved block was indeed the same color is the one moved in it�s direction
   * @param m1 A moved Movable block by the user
   * @param typeOne lets the method know what kind of cube it is.
   * @param typeOne lets the method know what kind of cube it is.
   * @return true if a corresponding block is matched
   */
   public boolean isSameType(Movable m1, Boolean typeOne, boolean typeTwo){
	   if(m1 == null){return false;}
	   return m1.typeOne == typeOne && m1.typeTwo == typeTwo;
   }
   
   /**
   *Passes the x,y coordinates of the screen on to it�s chiled methods to find out if the player is  *indeed clicking at a cube.
   * @param x X-coordinates of the screen
   * @param y Y-coordinates of the screen
   */
   public void findMovable(float x, float y) {
	   int row = (int)(selectedY/size)+1;
	   int col = (int)(selectedX/size);
	   
	   if (row < 0 || row > rows-1 || col < 0 || col > columns-1) return;
	   
	   if (Movables[col][row] != null && Movables[col][row].typeOne != null) {
		   Movable selectedM = new Movable(Movables[col][row]);
		   
		   if (row < 11 && Movables[col][row+1] != null) {
			   Movable targetM = new Movable(Movables[col][row+1]);
			   
			   if (y > selectedY + size && targetM != null && targetM.typeOne != null && targetM.speed == 0) {
				   selectedY += size;
				   swapMovables(selectedM, targetM, selectedM.y, col, row, 1);
				   handleMatches(Movables[col][row]);
				   handleMatches(Movables[col][row+1]);
			   }
		   }
		   
		   if (row > 0 && Movables[col][row-1] != null) {
			   Movable targetM = new Movable(Movables[col][row-1]);
			   
			   if (y < selectedY - size && targetM != null && targetM.typeOne != null && targetM.speed == 0) {
				   selectedY -= size;
				   swapMovables(selectedM, targetM, selectedM.y, col, row, -1);
				   handleMatches(Movables[col][row]);
				   handleMatches(Movables[col][row-1]);
			   }
		   }
	   }
	   
   }
   
   /**
   *Swaps to different Movables if the move of the player was legit
   * @param m1 A moved Movable
   * @param m2 Movable that is going to be swapped
   * @param col the column of the moved cube
   * @param row the row of the moved cube
   * @param add integer that decides if we are swapping upwards or downwards
   */
   public void swapMovables(Movable m1, Movable m2, float y, int col, int row, int add) {
	   Movable temp1 = new Movable(m1);
	   Movable temp2 = new Movable(m2);
	   
	   Movables[col][row] = temp2;
	   Movables[col][row+add] = temp1;
	   
	   Movables[col][row].row = row;
	   Movables[col][row+add].row = row+add;
	   
	   Movables[col][row].y = y;
	   Movables[col][row+add].y = y+size*add;
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