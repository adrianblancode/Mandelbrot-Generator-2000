package mandelbrot;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This is a canvas object that is in charge of drawing on a frame. It has many functions related to MandelbrotGenerator
 */
class MandelbrotCanvas extends Canvas{

	private BufferedImage Buffer;
	private WritableRaster Raster;
	private Graphics2D gt;
	private MandelbrotGenerator generator;
	private Font systemFont;
	private boolean overlay, rendernotification, savingnotification;
	private int width, height, minAntialiasing, maxAntialiasing;
	
	NumberFormat formatter;
	
	public MandelbrotCanvas(int width, int height){
		this.width = width;
		this.height = height;
		setBackground(Color.black);
		systemFont = new Font("Arial", Font.BOLD ,12);
		Buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Raster = Buffer.getRaster();
		generator = new MandelbrotGenerator(width, height);
		formatter = new DecimalFormat("0.##E0");
		
		setMinAntialiasing(1);
		setMaxAntialiasing(4);
		
		generator.setCurrentAntialiasing(maxAntialiasing);
		generator.setCoordinates(0.0f, 0.0f);
		generator.setImageDetail(1);
		
		overlay = true;
		rendernotification = false;
		savingnotification = false;
	}

	/**
	 * Draws the screen. Uses double buffering.
	 */
	public void paint(Graphics g){
		
		//this is the double buffer
		gt = (Graphics2D) Buffer.getGraphics();
		
		if(hasOverlay()){
			drawOverlay(gt);
		}
		
		g.drawImage(Buffer, 0, 0, null);
		
	}
	
	/**
	 * Draws the information overlay on the screen
	 */
	public void drawOverlay(Graphics gt){
		
		gt.setFont(systemFont);
		gt.setColor(Color.white);
		
		if(generator.getCurrentAntialiasing() > 1){
			gt.drawString(generator.getCurrentAntialiasing() + "x FSAA" , 2, 12*1);
		}
		else{
			gt.drawString("No Antialiasing", 2, 12*1);
		}

		gt.drawString("Time to render: " + Long.toString(generator.getTimetorender()) + "ms", 2, 12*2);

		if(generator.getTimetorender() != 0){
			gt.drawString("FPS: " + (1000/generator.getTimetorender()), 2, 3 * 12);
		}

		gt.drawString(formatter.format(generator.getMagnification()) + " zoom ", 2, 4 * 12);
		
		gt.drawString(generator.getXCenter() + ", " + generator.getYCenter() + "i", 2, 5 * 12);
			
		if(generator.GPUisenabled()){
			gt.drawString("GPU accelerated", 2, 6 * 12);
		}

		else{
			gt.drawString("CPU Multithreaded", 2, 6 * 12);
		}	
		
		if(rendernotification){
			gt.drawString("Rendering...", width/2 - 50, 12);
		}
		
		if(savingnotification){
			gt.drawString("Image Saved", width/2 - 50, 12);
		}
	}
	
	/**
	 * Calculates the mandelbrot set and draws it to the screen
	 */
	public void render(){
		generator.calculate();
		Raster.setPixels(0, 0, width, height, generator.getMandelbrot());
		update(getGraphics());
	}

	//We need to call paint from this function to be able to use double buffering
	@Override
	public void update(Graphics g){
		paint(g);
	}

	/**
	 * Zooms in on the picture to the given coordinates.
	 */
	public void zoomIn(int x, int y){

		rendernotification = true;
		update(getGraphics());
		
		double xtraveldistance = generator.convertXCoordinate(x) - generator.getXCenter();
		double ytraveldistance = generator.convertYCoordinate(y) - generator.getYCenter();
		//int limit = (int) (20 / Math.max(1, Math.log(m.getMagnification())));
		int limit = (int) Math.max(1, (10 - Math.max(0, Math.log(generator.getMagnification() * generator.getImageDetail()))));
		
		generator.setCurrentAntialiasing(minAntialiasing);

		for(int i = 0; i < limit; i++){
			generator.setCoordinates(generator.getXCenter() + ((double)xtraveldistance /limit), generator.getYCenter() + ((double)ytraveldistance /limit));
			generator.setMagnification(generator.getMagnification() * ((double)1 +((double)1/limit)));
			
			if(i == limit -1){
				rendernotification = false;
				generator.setCurrentAntialiasing(maxAntialiasing);
			}
			
			render();
		}

	}

	/**
	 * Zooms out the picture
	 */
	public void zoomOut(int x, int y) {
		rendernotification = true;
		drawOverlay(getGraphics());
		update(getGraphics());
		
		generator.setMagnification(generator.getMagnification() * 0.2f);
		rendernotification = false;
		render();
	}
	
	/**
	 * A demo function showing some of the capabilities of the program
	 */
	public void demo(){
			
		//-0.1 0.651i
		generator.setJuliaValues(-0.1f, 0.651f);
		generator.setRenderJuliaSet(true);
		generator.setCurrentAntialiasing(minAntialiasing);
		
		//double realconstant = (double) (Math.random()*5 * 0.002f);
		//double imaginaryconstant = (double) (Math.random()* 5 * 0.002f);
		
		int prevred = generator.getRedSensitivity();
		int prevblue = generator.getBlueSensitivity();
		int prevgreen = generator.getGreenSensitivity();
		
		generator.setBlueSensitivity(100);
		
		int redval = 30, greenval = 0, blueval = 0;
		
		for(int i = 0; i < 150; i++){

			/*
			 if(generator.getJuliaValueReal() > 0.5f){
				realconstant = -1 * Math.abs(realconstant);
			}
			else if(generator.getJuliaValueReal() < -0.0f){
				realconstant = Math.abs(realconstant);
			}
			
			if(generator.getJuliaValueImaginary() > 1.0f){
				imaginaryconstant = -1 * Math.abs(imaginaryconstant);
			}
			else if(generator.getJuliaValueImaginary() < 0.5f){
				imaginaryconstant = Math.abs(imaginaryconstant);
			}
			*/
			
			generator.setJuliaValues(generator.getJuliaValueReal() + 0.001f, generator.getJuliaValueImaginary() + 0.001f);
			
			if(generator.getRedSensitivity() + redval > 255 || generator.getRedSensitivity() + redval < 0){
				redval = -redval;
			}
			
			if(generator.getGreenSensitivity() + greenval > 255 || generator.getGreenSensitivity() + greenval < 0){
				greenval = -greenval;
			}
			
			if(generator.getBlueSensitivity() + blueval > 255 || generator.getBlueSensitivity() + blueval < 0){
				blueval = -blueval;
			}
			
			generator.setRedSensitivity(generator.getRedSensitivity() + redval);
			generator.setGreenSensitivity(generator.getGreenSensitivity() + greenval);
			generator.setBlueSensitivity(generator.getBlueSensitivity() + blueval);
			
			//System.out.println(generator.getJuliaValueReal() + ", " + generator.getJuliaValueImaginary());
			render();
		}
		
		generator.setRedSensitivity(prevred);
		generator.setBlueSensitivity(prevblue);
		generator.setGreenSensitivity(prevgreen);
		generator.setRenderJuliaSet(false);
		generator.setCurrentAntialiasing(maxAntialiasing);
		render();
	}
	
	/**
	 * This function changes the variables in MandelbrotGenerator, GPUKernel and AntialiasingKernel
	 * to adapt to a new screen resolution.
	 */
	public void changeSize(int w, int h){
		this.width = w;
		this.height = h;
		generator.changeSize(width, height);
		Buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Raster = Buffer.getRaster();
	}
	
	/**
	 * Sets whether the program shall render a text box with information about the image
	 */
	public void setOverlay(boolean b){
		overlay = b;
	}
	
	/**
	 * Returns whether an overlay is being displayed
	 */
	public boolean hasOverlay(){
		return overlay;
	}

	/**
	 * Draws a notification on the screen that notifies that an image has been saved
	 */
	public void drawSavingNotification(boolean b){

		if(b){
			savingnotification = true;	
		}
		else{
			savingnotification = false;
		}

		update(getGraphics());
	}
	
	/**
	 * Gets the current mandelbrotgenerator
	 * @return the generator currently being used
	 */
	public MandelbrotGenerator getGenerator() {
		return generator;
	}
	
	/**
	 * Returns the antialiasing used during zoom
	 */
	public int getMinAntialiasing(){
		return minAntialiasing;
	}
	
	/**
	 * Sets the antialiasing used during zoom. Values must be between 1 and 16
	 */
	public void setMinAntialiasing(int aa) throws IllegalArgumentException{
		
		if(aa >=1 && aa <= 16){
			minAntialiasing = aa;
		}
		else{
			throw new IllegalArgumentException("Invalid value: " + aa);
		}
	}
	
	/**
	 * Returns the maximum antialiasing
	 */
	public int getMaxAntialiasing(){
		return maxAntialiasing;
	}
	
	/**
	 * Sets the maximum antialiasing. Values must be between 1 and 16
	 */
	public void setMaxAntialiasing(int aa) throws IllegalArgumentException{
		
		if(aa >=1 && aa <= 16){
			maxAntialiasing = aa;
		}
		else{
			throw new IllegalArgumentException("Invalid value: " + aa);
		}
	}
}