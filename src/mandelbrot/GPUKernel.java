
package mandelbrot;
import java.util.Arrays;

import com.amd.aparapi.Kernel;

/**
 * The code in the run() function is converted into OpenCL and runs in paralell on the GPU, in case the GPU can't do it the program will instead use the CPU (multithreaded). 
 * Instead of having a for loop that iterates trough the upscaled pixels on the screen, each run() call gets a value (from getGlobalId() ) between 0 ... 
 * width * upscaledwidth * height * upscaledheight and with this value each call of run only computes the color of one pixel. Since it can be calculated 
 * in the GPU instead of the CPU (and this is mostly doubleing point operations) it can be much faster than calculating on the CPU.
 * 
 * Due to limitations in converting the code to openCL there are several restrictions in place for using objects or other classes
 */

public class GPUKernel extends Kernel{

	private final int totaliterations = 50, escapetreshold = 4;
	private int width, height, upscalingwidth, upscalingheight, imagedetail, redsensitivity, greensensitivity, bluesensitivity;
	private double xcenter, ycenter, magnification;
	private int [] MandelbrotUpscaled;
	private boolean renderjuliaset;
	private double juliavaluereal, juliavalueimaginary;

	public GPUKernel(int w, int h){

		width = w;
		height = h;
		renderjuliaset = false;
		
		imagedetail = 1;

		setAntialiasing(1);
		setMagnification(1);
		setCoordinates(0, 0);
		
		redsensitivity = 1;
		greensensitivity = 155;
		bluesensitivity = 255;

		//Each pixel has 3 ints to store colors
		MandelbrotUpscaled = new int[width * upscalingwidth * height * upscalingheight * 3];
	}

	/**
	 * Calculates the mandelbrot set in the GPU with openCL.
	 */
	@Override
	public void run(){

		/*The value of totaliterations is how many iterations without the value escaping the treshold
		 * will occur until we decide the value will not escape at all. The higher the value the more time
		 * it will take to calculate (especially the black areas which will never escape) but it gives a higher
		 * resolution picture. The totaliterations is 50 here because when we zoom that the precision will
		 * distort the image before the lack of iterations
		 */

		//gets the current y and x positions in the loop
		int xpixel = getGlobalId()%(width * upscalingwidth);
		int ypixel = getGlobalId()/(width * upscalingwidth);
		
		//getGlobalId() is almost like int i in a for loop

		//the relative position in the array
		int z = 3 * (ypixel * width * upscalingwidth + xpixel);
		
		int currentiterations = 0;
		double zreal, zimag, xgrid, ygrid, zrealtemp = 0, zimagtemp = 0;

		//This converts the coordinate in the frame to coordinates in the mandelbrot set		
		xgrid = (xcenter - 2 * (1 / magnification)) + (double) 4 * (1 / magnification) * xpixel / (width * upscalingwidth);
		
		//the (height/width) is to preserve a good picture ratio for example when going fullscreen
		ygrid = ((double) height/width)*(ycenter + 2 * (1 / magnification) - (double) 4 * (1 / magnification) * ypixel / (height * upscalingheight));

		currentiterations = 0; 
		zreal = xgrid;
		zimag = ygrid;

		boolean finished = false;
	
		//image detail increases the more you zoom in
		double newtreshold = (totaliterations + max(1, 10 * imagedetail * log(getMagnification())));
		
		//loops until it hits the a max number of iterations
		while(currentiterations <= newtreshold && finished == false){
			
			// If the value of the pixel in the mandelbrot set has escaped the treshold
			// we know that it belongs to the set and we should color it
			// if it does not escape we color it black
			if(zreal*zreal + zimag*zimag >= escapetreshold){
				
					MandelbrotUpscaled[z] = (int) (redsensitivity * currentiterations/newtreshold);
					MandelbrotUpscaled[z + 1] = (int) (greensensitivity * currentiterations/newtreshold);
					MandelbrotUpscaled[z + 2] = (int) (bluesensitivity * currentiterations/newtreshold);	

				finished = true;
			}
			
			//we calculate a julia set
			if(renderjuliaset){
				zrealtemp = zreal*zreal - zimag*zimag + juliavaluereal;
				zimagtemp = 2*zreal*zimag + juliavalueimaginary;
			}

			else{
				//we calculate a mandelbrot set
				zrealtemp = zreal*zreal - zimag*zimag + xgrid;
				zimagtemp = 2*zreal*zimag + ygrid;
			}

			zreal = zrealtemp;
			zimag = zimagtemp;

			/*
			 * If both zreal and zimag are 0, we can more or less assume that further iterations will not
						bring them above zero, and they are trapped there forever
			 */
			if(zreal == 0 && zimag == 0)finished = true;


			currentiterations++;
		}
	}

	/**
	 * Clears the picture to black color
	 */
	public void erase(){

		Arrays.fill(MandelbrotUpscaled, 0);
	}

	/**
	 * Sets how many samples per pixel the antialiasing will use. Does not work with below 1 as input.
	 * For best results, use an input number that has a natural square root, such as 4, 9, or 16
	 * The purpose of this function is to try to divide samplesperpixel into two reasonably equal
	 * factors, so that the upscaling can be even on both sides.
	 */
	public void setAntialiasing(int samplesperpixel){

		if(samplesperpixel <1){
			System.out.println("Error: samples per pixel must be 1 or greater");
		}

		if(samplesperpixel % (int)Math.sqrt(samplesperpixel) == 0){
			this.upscalingheight = (int)Math.sqrt(samplesperpixel);
			this.upscalingwidth = samplesperpixel / (int)Math.sqrt(samplesperpixel);
		}

		//Tries to count the factors and put good proportions on the antialiasing
		else{
			for (int i = 1; i < samplesperpixel; i++)
			{
				if(samplesperpixel % i == 0){
				upscalingwidth = samplesperpixel / i;
				upscalingheight = samplesperpixel / upscalingwidth;
				}
			}
			
		} 

		MandelbrotUpscaled = new int[width * upscalingwidth * height * upscalingheight * 3];

	}

	/**
	 * Sets the magnification
	 */
	public void setMagnification(double d){

		this.magnification = d;
	}

	/**
	 * Sets the magnification
	 */
	public double getMagnification(){

		return (double) magnification;
	}

	/**
	 * Sets the center coordinates which will be the center of the picture
	 */
	public void setCoordinates(double x, double y){

		xcenter = x;
		ycenter = y;
	}

	/**
	 * Returns the X coordinate
	 */
	public double getXCenter(){

		return (double) xcenter;
	}

	/**
	 * Returns the Y coordinate
	 */
	public double getYCenter(){

		return (double) ycenter;
	}

	/**
	 * Returns the array where the calculated and non-antialiased mandelbrot set is stored
	 */
	public int [] getMandelbrotUpscaled(){
		return MandelbrotUpscaled;
	}

	/**
	 * Sets whether the program will render a julia set or not
	 * @param b
	 */
	public void setRenderJuliaSet(boolean b){
		renderjuliaset = b;
	}

	/**
	 * Returns whether the program will render a julia set
	 * @return
	 */
	public boolean getRenderJuliaSet(){
		return renderjuliaset;
	}

	/**
	 * Sets the julia values of the set
	 * @param f
	 * @return
	 */
	public void setJuliaValues(double real, double im){
		juliavaluereal = real;
		juliavalueimaginary = im;
	}

	/**
	 * Returns the real julia value of the set
	 * @return
	 */
	public double getJuliaValueReal(){
		return juliavaluereal;
	}

	/**
	 * Returns the imaginary julia value of the set
	 * @return
	 */
	public double getJuliaValueImaginary(){
		return juliavalueimaginary;
	}

	/**
	 * Changes variables to adapt to a new screen resolution
	 */
	public void changeSize(int w, int h){
		this.width = w;
		this.height = h;
		MandelbrotUpscaled = new int[width * upscalingwidth * height * upscalingheight * 3];
	}
	
	/**
	 * Sets the size of the total allowed iterations. Higher values will render a picture with more detail
	 */
	public void setImageDetail(int i){
		if(i >= 0 && i <= 10){
			imagedetail = i;
		}
		else{
			System.out.println("Image quality must be between 0 and 10");
		}
	}
	
	/**
	 * Gets the image quality
	 */
	public int getImageDetail(){
		return imagedetail;
	}
	
	/**
	 * Returns how much the height is being upscaled by
	 */
	public int getUpscalingHeight(){
		return upscalingheight;
	}
	
	/**
	 * Returns how much the width is being upscaled by
	 */
	public int getUpscalingWidth(){
		return upscalingwidth;
	}
	
	
	/**
	 * Sets the sensitivity of the colors. Must be between 1 and 16 
	 */
	public void setRGBsensitivity(int r, int b, int g){
		
		setRedSensitivity(r);
		setBlueSensitivity(b);
		setGreenSensitivity(g);
	}
	
	/**
	 * Sets the red color of the picture. Values must be from 0 to 255;
	 */
	public void setRedSensitivity(int r) throws IllegalArgumentException{
		
		if(r >= 0 && r <= 255){
			redsensitivity = r;
		}
		else{
			
			throw new IllegalArgumentException("Invalid value: " + r);
		}
	}
		
	/**
	 * Sets the green color of the picture. Values must be from 0 to 255;
	 */
	public void setGreenSensitivity(int g) throws IllegalArgumentException{
		if(g >= 0 && g <= 255){
			greensensitivity = g;
		}
		else{
			
			throw new IllegalArgumentException("Invalid value: " + g);
		}
	}
	
	/**
   	 * Sets the blue color of the picture. Values must be from 0 to 255;
	 */
	public void setBlueSensitivity(int b) throws IllegalArgumentException{
		if(b >= 0 && b <= 255){
			bluesensitivity = b;
		}
		else{
			
			throw new IllegalArgumentException("Invalid value: " + b);
		}
		
	}
	
	/**
	 * Sets how much red the picture will have (values from 0 to 255)
	 */
	public int getRedSensitivity(){
		return redsensitivity;
	}
	
	/**
	 * Sets how much green the picture will have (values from 0 to 255)
	 */
	public int getGreenSensitivity(){
		return greensensitivity;
	}
	
	/**
	 * Sets how much blue the picture will have (values from 0 to 255)
	 */
	public int getBlueSensitivity(){
		return bluesensitivity;
	}
	
}


