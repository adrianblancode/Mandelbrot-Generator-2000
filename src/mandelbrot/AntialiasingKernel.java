package mandelbrot;
import com.amd.aparapi.Kernel;

/**
 * The code in the run() function is converted into OpenCL and runs in paralell on the GPU, in case the GPU can't do it the program will instead use the CPU (multithreaded). 
 * Instead of having a for loop that iterates trough the upscaled pixels on the screen, each run() call gets a value (from getGlobalId() ) between 0 ... 
 * WIDTH * upscaledwidth * HEIGHT * upscaledheight and with this value each call of run only computes the color of one pixel. Since it can be calculated 
 * in the GPU instead of the CPU (and this is mostly floating point operations) it can be much faster than calculating on the CPU.
 * 
 * Due to limitations in converting the code to openCL there are several restrictions in place for using objects or other classes
 */

public class AntialiasingKernel extends Kernel{

	private int [] MandelbrotAntialiased, MandelbrotUpscaled;
	private int width, height;
	private int upscalingwidth, upscalingheight, samplesperpixel;
	
	public AntialiasingKernel(int w, int h){
		
		width = w;
		height = h;
				
		setAntialiasing(1);
		MandelbrotAntialiased = new int[width * height * 3];
	}
	
	/*
	 * We paralell compute the antialiasing in the GPU by having one tread on each chunk.
	 * This function takes the upscaled mandelbrot image, and downscales it and applies antialiasing.
	 * It will take pixels in chunks with size on the value of samplesperpixel and then take
	 * the average of the colors and apply it to a pixel. Eg if the samples per pixel is 16 and 
	 * the first chunk of 4x4 pixels in the upscaled image has the average color C, 
	 * then the pixel 0,0 in the antialiased picture will have the color C
	 */
	@Override public void run(){
		   
	      int ping = getGlobalId();

	      int ypixel = (ping / width) * upscalingheight ;
	      int xpixel = (ping * upscalingwidth)%(width * upscalingwidth);

	      int z1, z2;
	      float tempred = 0, tempgreen = 0, tempblue = 0;

	      //calculates the chunks
	      for(int ychunk = 0; ychunk < upscalingheight; ychunk++){
	    	  for(int xchunk = 0; xchunk < upscalingwidth; xchunk++){

	    		  z1 = 3 * (ypixel * width * upscalingwidth + xpixel) + 3 * (ychunk * width * upscalingwidth + xchunk);

	    		  if(z1 + 2 < MandelbrotUpscaled.length){
	    			  //stores the average colors of the chunks
	    			  tempred += (float) MandelbrotUpscaled[z1]/(samplesperpixel);
	    			  tempgreen += (float) MandelbrotUpscaled[z1 + 1]/(samplesperpixel);
	    			  tempblue += (float) MandelbrotUpscaled[z1 + 2]/(samplesperpixel);
	    		  }
	    	  }
	      }

	      //Takes the average of the colors and applies it to the downscaled pixel

	      z2 = 3 * ping;

	      MandelbrotAntialiased[z2] = (int) tempred;
	      MandelbrotAntialiased[z2 + 1] = (int) tempgreen;
	      MandelbrotAntialiased[z2 + 2] = (int) tempblue;


	   }
	
	/**
	 * Sets how many samples per pixel the antialiasing will use. Does not work with below 1 as input.
	 * For best results, use an input number that has a natural square root, such as 4, 9, or 16
	 * The purpose of this function is to try to divide samplesperpixel into two reasonably equal
	 * factors, so that the upscaling can be even on both sides.
	 */
	public void setAntialiasing(int samplesperpixel){

		this.samplesperpixel = samplesperpixel;

		if(samplesperpixel < 1){
			System.out.println("Error: samples per pixel must be 1 or greater");
		}

		else if(samplesperpixel % (int)Math.sqrt(samplesperpixel) == 0){
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
	}
	
	/**
	 * Updates the location of the array with the upscaled mandelbrot set
	 */
	public void setSource(int [] mu){
		MandelbrotUpscaled = mu;
	}
	
	/**
	 * Returns the array where the calculated and antialiased mandelbrot set is stored
	 */
	public int [] getMandelbrotAntialiased(){
		return MandelbrotAntialiased;
	}
	
	/**
	 * Changes variables to adapt to a new screen resolution
	 */
	public void changeSize(int w, int h){
		this.width = w;
		this.height = h;
		MandelbrotAntialiased = new int[width * height * 3];
	}
}
