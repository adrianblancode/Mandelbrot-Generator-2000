package mandelbrot;

import com.amd.aparapi.Range;

/**
 * This class is responsible for calculating a mandelbot set. It uses the 
 * gpukernel for calculating an an upscaled mandelbrot set and then uses
 * aakernel for antialiasing it. Both the kernels convert java code to openCL
 * and are executed paralell on the GPU or CPU
 */

public class MandelbrotGenerator {

	//Width and height of screen
	private int width, height;

	//stores how long rendering takes
	private int timetorender;

	private boolean GPUenabled, GPUisfunctional;
	
	//kernels which will run in the GPU
	private GPUKernel gkernel;
	private AntialiasingKernel aakernel;
	
	//stores the antialiasing rate
	private int samplesperpixel;
	
	//stores the size of the previous frame
	private int previousize;
	
	public MandelbrotGenerator(int w, int h){

		width = w;
		height = h;
		samplesperpixel = 1;
		previousize = width * height;
		GPUisfunctional = false;
		GPUenabled = false;
		
		gkernel = new GPUKernel(width, height);
		aakernel = new AntialiasingKernel(width, height);
		aakernel.setSource(gkernel.getMandelbrotUpscaled());
	}

	/**
	 * This function will calculate the mandelbrot set.
	 * It will first clear the buffer to black, then it will render the upscaled image with the gpukernel
	 * and then antialias it with the aakernel. It will then collect the time it took to render and if
	 * if the calculations were performed on the GPU or not
	 */
	public void calculate(){
		
		gkernel.erase();

		//Kernels on nvidia GPUs crash if they take longer than 2000ms
		//if the detail or AA gets to high we disable the GPU

		if(GPUisenabled() && (getImageDetail() > 1 || getCurrentAntialiasing() > 4)){
				enableGPU(false);
		}

		//executes kernels on GPU
		gkernel.execute(width * gkernel.getUpscalingWidth() * height * gkernel.getUpscalingHeight());
		aakernel.execute(width * height);

		//stores the results of the rendering
		timetorender = (int) (gkernel.getExecutionTime() + aakernel.getExecutionTime());
		GPUenabled = gkernel.getExecutionMode() == (GPUKernel.EXECUTION_MODE.GPU);
		
		if(!GPUisfunctional && GPUenabled){
			GPUisfunctional = true;
		}
		
	}

	/**
	 * Sets the magnification of the image. 1 is the default and increasing it zooms in.
	 */
	public void setMagnification(double d){

		gkernel.setMagnification(d);
	}

	/**
	 * Gets the magnification of the image
	 */
	public double getMagnification(){

		return gkernel.getMagnification();
	}

	/**
	 * Sets the samples per pixel each pixel will use
	 */
	public void setCurrentAntialiasing(int aa) throws IllegalArgumentException{

		if(aa >=1 && aa <= 16){
			samplesperpixel = aa;

			gkernel.setAntialiasing(aa);
			aakernel.setAntialiasing(aa);
			aakernel.setSource(gkernel.getMandelbrotUpscaled());
		}

		else{
			throw new IllegalArgumentException("Invalid value: " + aa);
		}
	}

	/**
	 * Sets the center coordinates which will be the center of the picture
	 */
	public void setCoordinates(double x, double y){
		gkernel.setCoordinates(x, y);
	}
	
	/**
	 * Converts integer positions within the frame to double positions within the mandelbrot set
	 * @param x
	 * @return
	 */
	public double convertXCoordinate(int x) {
		return (getXCenter() - 2 * (1 / getMagnification())) + (double) 4 * (1 / getMagnification()) * x / width;
	}
	
	/**
	 * Converts integer positions within the frame to double positions within the mandelbrot set
	 * @param y
	 * @return
	 */
	public double convertYCoordinate(int y) {
		return (getYCenter() + 2 * (1 / getMagnification()) - (double) 4 * (1 / getMagnification()) * y / height);
	}

	/**
	 * Returns the X coordinate
	 */
	public double getXCenter(){
		return gkernel.getXCenter();
	}

	/**
	 * Returns the Y coordinate
	 */
	public double getYCenter(){
		return gkernel.getYCenter();
	}
	
	/**
	 * Returns the number of samples per pixel
	 */
	public int getCurrentAntialiasing(){
		return samplesperpixel;
	}
	
	/**
	 * Returns the time it took to render the mandelbrot set
	 */
	public int getTimetorender(){
		return timetorender;
	}
	
	/**
	 * Returns true if the calculations were performed on the GPU, false if performed on the CPU
	 */
	public boolean GPUisenabled(){
		return GPUenabled;
	}
	
	/**
	 * Returns whether the GPU is functional in the program
	 */
	public boolean GPUisfunctional(){
		return GPUisfunctional;
	}
	
	/**
	 * If true, then it sets the kernels to use the GPU only
	 */
	public void enableGPU(boolean b){
		
		if(b == true){
			gkernel.setExecutionMode(GPUKernel.EXECUTION_MODE.GPU);
			aakernel.setExecutionMode(GPUKernel.EXECUTION_MODE.GPU);
		}
		
		else{
			gkernel.setExecutionMode(GPUKernel.EXECUTION_MODE.JTP);
			aakernel.setExecutionMode(GPUKernel.EXECUTION_MODE.JTP);
		}
	}
	
	/**
	 * returns the antialiased mandelbrot picture
	 */
	public int [] getMandelbrot(){
		return aakernel.getMandelbrotAntialiased();
	}
	
	/**
	 * Sets whether the program will render a julia set or not
	 * @param b will render a julia set if true, a mandelbrot set if false
	 */
	public void setRenderJuliaSet(boolean b){
		gkernel.setRenderJuliaSet(b);
	}
	
	/**
	 * Returns whether the program will render a julia set
	 * @return
	 */
	public boolean getRenderJuliaSet(){
		return gkernel.getRenderJuliaSet();
	}
	
	/**
	 * Sets the julia value of the set
	 * @param f The julia value to be used.
	 * @return
	 */
	public void setJuliaValues(double real, double imag){
		gkernel.setJuliaValues(real, imag);
	}
	
	/**
	 * Returns the real julia value of the set
	 * @return
	 */
	public double getJuliaValueReal(){
		return gkernel.getJuliaValueReal();
	}
	
	/**
	 * Returns the imaginary julia value of the set
	 * @return
	 */
	public double getJuliaValueImaginary(){
		return gkernel.getJuliaValueImaginary();
	}
	
	/**
	 * Modifies the variables so they will work if the screen changes size
	 */
	public void changeSize(int w, int h){
		this.width = w;
		this.height = h;
		
		gkernel.changeSize(width, height);
		aakernel.changeSize(width, height);
		aakernel.setSource(gkernel.getMandelbrotUpscaled());
		}
	
	/**
	 * Sets the size of the total allowed iterations. Higher values will render a picture with more detail
	 */
	public void setImageDetail(int i){
		gkernel.setImageDetail(i);
	}
	
	/**
	 * Gets the image quality
	 */
	public int getImageDetail(){
		return gkernel.getImageDetail();
	}
	
	/**
	 * Sets the red color of the picture. Values must be from 0 to 255;
	 */
	public void setRedSensitivity(int r){
		gkernel.setRedSensitivity(r);
	}
	
	/**
	 * Sets the green color of the picture. Values must be from 0 to 255;
	 */
	public void setGreenSensitivity(int g){
		gkernel.setGreenSensitivity(g);
	}
	
	/**
	 * Sets the blue color of the picture. Values must be from 0 to 255;
	 */
	public void setBlueSensitivity(int b){
		gkernel.setBlueSensitivity(b);
	}
	
	/**
	 * Sets how much red the picture will have (values from 0 to 255)
	 */
	public int getRedSensitivity(){
		return gkernel.getRedSensitivity();
	}
	
	/**
	 * Sets how much green the picture will have (values from 0 to 255)
	 */
	public int getGreenSensitivity(){
		return gkernel.getGreenSensitivity();
	}
	
	/**
	 * Sets how much blue the picture will have (values from 0 to 255)
	 */
	public int getBlueSensitivity(){
		return gkernel.getBlueSensitivity();
	}
	

}

