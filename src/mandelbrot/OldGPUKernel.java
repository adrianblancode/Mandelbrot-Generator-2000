package mandelbrot;
import java.util.Arrays;

import com.amd.aparapi.Kernel;

/**
 * The code in the run() function is converted into OpenCL and runs in paralell on the GPU, in case the GPU can't do it the program will instead use the CPU (multithreaded). 
 * Instead of having a for loop that iterates trough the upscaled pixels on the screen, each run() call gets a value (from getGlobalId() ) between 0 ... 
 * width * upscaledwidth * height * upscaledheight and with this value each call of run only computes the color of one pixel. Since it can be calculated 
 * in the GPU instead of the CPU (and this is mostly floating point operations) it can be much faster than calculating on the CPU.
 * 
 * Due to limitations in converting the code to openCL there are several restrictions in place for using objects or other classes
 */

public class OldGPUKernel extends Kernel{

	private final int totaliterations = 30, escapetreshold = 4;
	private int width, height, upscalingwidth, upscalingheight;
	private float xcenter, ycenter, magnification, juliavaluereal, juliavalueimaginary;
	private int [] MandelbrotUpscaled;
	private boolean renderjuliaset;
	
	public OldGPUKernel(int w, int h){
	
		width = w;
		height = h;
		renderjuliaset = false;
		
		setAntialiasing(1);
		setMagnification(1);
		setCoordinates(0, 0);
		
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

		//Almost like int i, in a for loop
		int ping = getGlobalId();
		
		int currentiterations = 0;
		float zreal, zimag, xgrid, ygrid, zrealtemp = 0, zimagtemp = 0;

		int ypixel = ping/(width * upscalingwidth);
		int xpixel = ping%(width * upscalingwidth);

		/* Koordinaterna i grafikuppritning matchar inte koordinaterna i ett matematiskt koordinatsystem, och d�rf�r m�ste
		 * vi konvertera dem f�rst. Inom grafikuppritning �r 0,0 inte vid origo utan vid det �vre v�nstra h�rner, 
		 * d�rf�r kommer v�ra startkoordinater vara -2 i det reella planet (v�xande) samt 2 i det imagin�ra planet (minskande)
		 * D�refter r�knar vi med den totala distansen i grafen (4) g�nger den aktuella pixeln genom det totala
		 * antalet pixlar i den ledden f�r att "konvertera" positionen i grafiken till motsvarande position
		 * inom en matematiskt graf.
		 * 
		 * xpixel samt ypixel �r grafikkordinater
		 * ygrid samt xgrid �r motsvarande koordinater i ett matematiskt koordinatsystem
		 */

		xgrid = (xcenter - 2 * (1 / magnification)) + (float) 4 * (1 / magnification) * xpixel / (width * upscalingwidth);
		ygrid = (ycenter + 2 * (1 / magnification)) - (float) 4 * (1 / magnification) * ypixel / (height * upscalingheight);

		currentiterations = 0;
		zreal = xgrid;
		zimag = ygrid;

		boolean finished = false;
		
		//the logarithm of the magnification will act weird if it has a value under 1
		int newtreshold = (int) (totaliterations + max(1, 30 * log(magnification)));

		while(currentiterations <= newtreshold && finished == false){
			
			/* If the value of the pixel in the mandelbrot set has escaped the treshold
			 * we know that it belongs to the set and we should color it
			 */
			if(zreal*zreal + zimag*zimag >= escapetreshold){

				int z = 3 * (ypixel * width * upscalingwidth + xpixel);

				if(z + 2 < MandelbrotUpscaled.length){
					MandelbrotUpscaled[z] = (int) (0 * currentiterations/newtreshold);
					MandelbrotUpscaled[z + 1] = (int) (155 * currentiterations/newtreshold);
					MandelbrotUpscaled[z + 2] = (int) (255 * currentiterations/newtreshold);
				}

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

		/* The rest of the numbers will be lazily coded to simply scale the width by that much, this should generally 
		 * only happen when someone tries to input a prime or some other number with disproportionate factors such as 10(5 * 2). 
		 * The antialising will become very smooth on the x-axis but incredibly uneven on the y-axis so this is not recommended,
		 * however as long as you don't try to input weird numbers such as primes this should happen.
		 */

		else{
			this.upscalingwidth = samplesperpixel;
			this.upscalingheight = 1;
		} 

		MandelbrotUpscaled = new int[width * upscalingwidth * height * upscalingheight * 3];

	}

	/**
	 * Sets the magnification
	 */
	public void setMagnification(float d){
		
		this.magnification = d;
	}
	
	/**
	 * Sets the magnification
	 */
	public float getMagnification(){
		
		return magnification;
	}
	
	/**
	 * Sets the center coordinates which will be the center of the picture
	 */
	public void setCoordinates(float x, float y){

		xcenter = x;
		ycenter = y;
	}

	/**
	 * Returns the X coordinate
	 */
	public float getXCenter(){

		return xcenter;
	}

	/**
	 * Returns the Y coordinate
	 */
	public float getYCenter(){

		return ycenter;
	}
	
	/**
	 * gets the scalingwidth of the picture.
	 * @return <code>upscalingwidth</code>
	 */
	public int getUpscalingwidth() {
		return upscalingwidth;
	}
	
	/**
	 * Get tge scalingheight of the picture.
	 * @return <code>upscalingheight</code>
	 */
	public int getUpscalingheight() {
		return upscalingheight;
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
	public void setJuliaValues(float real, float im){
		juliavaluereal = real;
		juliavalueimaginary = im;
	}
	
	/**
	 * Returns the real julia value of the set
	 * @return
	 */
	public float getJuliaValueReal(){
		return juliavaluereal;
	}
	
	/**
	 * Returns the imaginary julia value of the set
	 * @return
	 */
	public float getJuliaValueImaginary(){
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
}