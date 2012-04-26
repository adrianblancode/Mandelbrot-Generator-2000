package mandelbrot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;

/**
 * A frame object for that uses MandelbrotCanvas
 */
public class MandelbrotFrame extends JFrame implements MouseListener,
		ActionListener {

	protected MandelbrotCanvas canvas;
	protected boolean fullscreen;
	protected int width, height;
	protected Calendar cal;
	protected SimpleDateFormat sdf;
	protected Date date;
	protected JPanel buttonsPanel;
	protected JButton closeButton, fullScreenButton, minimizeButton,
			saveButton, settingsButton;
	protected ImageIcon settingsIcon;
	private File savedPicturesPath = new File("");

	public MandelbrotFrame(int width, int height) {
		this(new Dimension(width, height));
	}

	public MandelbrotFrame(Dimension d) {

		width = (int) d.getWidth();
		height = (int) d.getHeight();

		fullscreen = false;
		this.setResizable(false);

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
				0, false);

		Action escapeAction = new AbstractAction() {
			// close the frame when the user presses escape
			@Override
			public void actionPerformed(ActionEvent e) {
				// this.dispose();
				System.exit(0);
			}
		};

		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(escapeKeyStroke, "ESCAPE");
		this.getRootPane().getActionMap().put("ESCAPE", escapeAction);

		canvas = new MandelbrotCanvas(width, height);
		canvas.addMouseListener(this);
		if (!OSValidator.isMac()) {
			// Layout does not work with Mac. Uses default BorderLayout on mac
			// instead
			setLayout(new TopRightCornerLayout());
		}

		Toolkit tk = Toolkit.getDefaultToolkit();

		// Initializes icons for the buttons.
		ImageIcon programicon;
		ImageIcon crossicon;
		ImageIcon screenicon;
		ImageIcon minimizeicon;
		ImageIcon saveIcon;

		 //Different try/catch in case of single images missing
		 //this enables us to include the resources in the jar file
		 try{
		 programicon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/mandelbrot.png")));
		 }
		 catch(Exception e) {
		 programicon = new ImageIcon();
		 }
		 try {
		 crossicon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/cross.png")));
		 closeButton = new JButton(crossicon);
		 }
		 catch(Exception e) {
		 closeButton = new JButton("Close");
		 }
		 try {
		 screenicon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/screen.png")));
		 fullScreenButton = new JButton(screenicon);
		 }
		 catch(Exception e) {
		 fullScreenButton = new JButton("Fullscreen");
		 }
		 try {
		 minimizeicon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/minimize.png")));
		 minimizeButton = new JButton(minimizeicon);
		 }
		 catch(Exception e) {
		 minimizeButton = new JButton("Minimize");
		 }
		 try {
		 saveIcon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/floppy.png")));
		 saveButton = new JButton(saveIcon);
		 }
		 catch(Exception e) {
		 saveButton = new JButton("Save picture");
		 }
		 try {
		 settingsIcon = new
		 ImageIcon(tk.getImage(getClass().getResource("resources/settings.png")));
		 settingsButton = new JButton(settingsIcon);
		 }
		 catch(Exception e) {
		 settingsIcon = new ImageIcon();
		 settingsButton = new JButton("Settings");
		 }

		setIconImage(programicon.getImage());
		setTitle("Mandelbrot Generator 2000");

		// Initializes all buttons for the UI
		
		closeButton.addActionListener(this);
		fullScreenButton.addActionListener(this);
		minimizeButton.addActionListener(this);
		saveButton.addActionListener(this);
		settingsButton.addActionListener(this);

		Dimension buttonSize = new Dimension(16, 16);
		closeButton.setPreferredSize(buttonSize);
		fullScreenButton.setPreferredSize(buttonSize);
		minimizeButton.setPreferredSize(buttonSize);
		saveButton.setPreferredSize(buttonSize);
		settingsButton.setPreferredSize(buttonSize);

		buttonsPanel = new JPanel(); // Default FlowLayout

		buttonsPanel.add(settingsButton);
		buttonsPanel.add(saveButton);
		buttonsPanel.add(minimizeButton);
		buttonsPanel.add(fullScreenButton);
		buttonsPanel.add(closeButton);

		buttonsPanel.setBounds(buttonsPanel.getX(), buttonsPanel.getY(),
				buttonsPanel.getWidth(), buttonSize.height);
		buttonsPanel.setBackground(canvas.getBackground());

		//different code for mac
		if (OSValidator.isMac()) {
			add(BorderLayout.CENTER, canvas);
			add(BorderLayout.NORTH, buttonsPanel);
		} else {
			add(TopRightCornerLayout.TOPRIGHTCORNER, buttonsPanel);
			this.add(TopRightCornerLayout.OTHER, canvas);
		}

		this.setSize(new Dimension(width, height + 22));
		this.setVisible(true);
		canvas.render();
		canvas.update(canvas.getGraphics());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		//zooms in 
		if (me.getButton() == MouseEvent.BUTTON1) {
			canvas.zoomIn(me.getX(), me.getY());
		
		//demo function
		} else if (me.getButton() == MouseEvent.BUTTON2) {
			// setFullscreen(!isFullscreen());
			canvas.demo();
			
		//zooms out
		} else if (OSValidator.isMac() && me.getButton() == MouseEvent.BUTTON2 ||me.getButton() == MouseEvent.BUTTON3) {
			canvas.zoomOut(me.getX(), me.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Required by MouseListener
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Required by MouseListener
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// Required by MouseListener

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// Required by MouseListener

	}

	/**
	 * Sets the frame and all related objects to or from fullscreen
	 */
	public void setFullscreen(boolean b) {
		dispose();
		setUndecorated(b);
		if (b) {
			if (OSValidator.isMac()) {
				GraphicsDevice gd = GraphicsEnvironment
						.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				gd.setFullScreenWindow(this);
				canvas.changeSize(gd.getFullScreenWindow().getWidth(), gd
						.getFullScreenWindow().getHeight());
			} else {
				Dimension screenSize = Toolkit.getDefaultToolkit()
						.getScreenSize();
				setBounds(0, 0, screenSize.width, screenSize.height);
				canvas.changeSize(screenSize.width, screenSize.height);
				setVisible(true);
			}
		} else {
			setBounds(0, 0, width, height);
			canvas.changeSize(width, height);
			setVisible(true);
		}
		fullscreen = b;
		repaint();
		canvas.render();
	}

	/**
	 * Returns whether the program is in fullscreen or not
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		//sets the frame to fullscreen
		if (event.getSource().equals(fullScreenButton)) {
			setFullscreen(!isFullscreen());
		}
		//exits the frame
		if (event.getSource().equals(closeButton)) {
			if (JOptionPane.showConfirmDialog(this,
					"Do you really want to quit?", "Cancel?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				System.exit(0);
			}
		}
		//minimizes the grame
		if (event.getSource().equals(minimizeButton)) {
			if (isFullscreen() && OSValidator.isMac()) {
				setFullscreen(false);
			}
			this.setState(Frame.ICONIFIED);
		}
		//saves the image
		if (event.getSource().equals(saveButton)) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(savedPicturesPath);
			int userChoice = fileChooser.showSaveDialog(this);
			savedPicturesPath = fileChooser.getCurrentDirectory();
			if (userChoice == JFileChooser.APPROVE_OPTION) {
				BufferedImage image = new BufferedImage(canvas.getWidth(),
						canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				canvas.paint(g);

				date = new Date();
				// System.out.println(sdf.format(date));
				File file = fileChooser.getSelectedFile();
				String fileName = file.getName();
				String fileType = null;
				if (fileName.lastIndexOf('.') > 0) {
					fileType = fileName
							.substring(fileName.lastIndexOf('.') + 1)
							.toUpperCase();
					System.out.println(fileType);
				} else {
					file = new File(fileChooser.getSelectedFile() + ".png");
					System.out.println(file.getName());
					fileType = "PNG";
				}
				// File f = new File("saved pictures/" + sdf.format(date) +
				// ".png");
				try {
					ImageIO.write(image, fileType, file);
					canvas.drawSavingNotification(true);
					canvas.drawSavingNotification(false);
				} catch (Exception e) {
					System.out.println("Failure in writing to file: " + e);
				}
			}
		}
		//opens the settings frame
		if (event.getSource().equals(settingsButton)) {
			new SettingsFrame();
		}
	}
	
	/**
	 * This is the frame that handles all the settings in the mandelbrot picture
	 */
	class SettingsFrame extends JFrame implements ActionListener {

		private int width;
		private int height;
		private JPanel settingsPanel;
		private JCheckBox informationOverlay, GPUcheckbox, useJuliaSet;
		private JLabel antialiasingLabel, antialiasingDuringZoomLabel,
				detailLevelLabel, redText, blueText, greenText, juliaRealValue, juliaImValue,
				foregroundcolorLabel;
		private JSlider antialiasing, antialiasingDuringZoom, detailLevel;
		private JFormattedTextField redSetValue, blueSetValue, greenSetValue,
				juliaSetImaginaryValue, juliaSetRealValue;
		private JButton closeButton, refreshButton;
		private MandelbrotGenerator generator = canvas.getGenerator();

		public SettingsFrame() {
			// Initiates the panel for settings
			settingsPanel = new JPanel();
			settingsPanel.setLayout(new BoxLayout(settingsPanel,
					BoxLayout.Y_AXIS));

			setIconImage(settingsIcon.getImage());
			setTitle("Settings");

			// Initiates components for settings
			informationOverlay = new JCheckBox("Information Overlay");
			informationOverlay.setSelected(canvas.hasOverlay());
			settingsPanel.add(informationOverlay);

			antialiasingDuringZoomLabel = new JLabel(
					"Minimum antialiasing (used during zoom)");
			settingsPanel.add(antialiasingDuringZoomLabel);

			antialiasingDuringZoom = new JSlider();
			antialiasingDuringZoom.setMaximum(16);
			antialiasingDuringZoom.setPaintTicks(true);
			antialiasingDuringZoom.setMajorTickSpacing(2);
			antialiasingDuringZoom.setSnapToTicks(true);
			antialiasingDuringZoom.setPaintLabels(true);
			antialiasingDuringZoom.setValue(canvas.getMinAntialiasing());
			settingsPanel.add(antialiasingDuringZoom);

			antialiasingLabel = new JLabel(
					"Maximum antialiasing (used during rendering)");
			settingsPanel.add(antialiasingLabel);

			antialiasing = new JSlider();
			antialiasing.setMaximum(16);
			antialiasing.setPaintTicks(true);
			antialiasing.setMajorTickSpacing(2);
			antialiasing.setSnapToTicks(true);
			antialiasing.setPaintLabels(true);
			antialiasing.setValue(canvas.getMaxAntialiasing());
			settingsPanel.add(antialiasing);

			detailLevelLabel = new JLabel("Detail level");
			settingsPanel.add(detailLevelLabel);
			detailLevel = new JSlider();
			detailLevel.setMaximum(10);
			detailLevel.setPaintTicks(true);
			detailLevel.setMajorTickSpacing(1);
			detailLevel.setSnapToTicks(true);
			detailLevel.setPaintLabels(true);
			detailLevel.setValue(generator.getImageDetail());
			settingsPanel.add(detailLevel);

			foregroundcolorLabel = new JLabel("Set colors [0-255]");
			settingsPanel.add(foregroundcolorLabel);
			JPanel colorPanel = new JPanel();
			redText = new JLabel("Red");
			colorPanel.add(redText);
			redSetValue = new JFormattedTextField(generator
					.getRedSensitivity());
			redSetValue.setColumns(3);
			colorPanel.add(redSetValue);
			greenText = new JLabel("Green");
			colorPanel.add(greenText);
			greenSetValue = new JFormattedTextField(generator
					.getGreenSensitivity());
			greenSetValue.setColumns(3);
			colorPanel.add(greenSetValue);
			blueText = new JLabel("Blue");
			colorPanel.add(blueText);
			blueSetValue = new JFormattedTextField(generator
					.getBlueSensitivity());
			blueSetValue.setColumns(3);
			colorPanel.add(blueSetValue);
			settingsPanel.add(colorPanel);
			
			//To make sure that doubles show correctly
			DecimalFormat decimalFormat = new DecimalFormat("#.######");
			useJuliaSet = new JCheckBox("Render Julia set");
			useJuliaSet.setSelected(generator.getRenderJuliaSet());
			useJuliaSet.addActionListener(this);
			settingsPanel.add(useJuliaSet);
			juliaRealValue = new JLabel("Julia real value");
			JPanel juliaRealSetPanel = new JPanel(); //Default flowlayout
			juliaRealSetPanel.add(juliaRealValue);
			juliaSetRealValue = new JFormattedTextField(decimalFormat);//new JFormattedTextField();
			juliaSetRealValue.setValue(generator.getJuliaValueReal());
			juliaSetRealValue.setColumns(6);
			juliaSetRealValue.setEnabled(useJuliaSet.isSelected());
			settingsPanel.add(juliaSetRealValue);
			juliaRealSetPanel.add(juliaSetRealValue);
			settingsPanel.add(juliaRealSetPanel);
			juliaImValue = new JLabel("Julia imaginary value");
			JPanel juliaImSetPanel = new JPanel(); //Default flow layout
			juliaImSetPanel.add(juliaImValue);
			juliaSetImaginaryValue = new JFormattedTextField(decimalFormat);
			juliaSetImaginaryValue.setValue(generator.getJuliaValueImaginary());
			juliaSetImaginaryValue.setColumns(6);
			juliaSetImaginaryValue.setEnabled(useJuliaSet.isSelected());
			juliaImSetPanel.add(juliaSetImaginaryValue);
			settingsPanel.add(juliaImSetPanel);

			GPUcheckbox = new JCheckBox("Enable GPU");
			GPUcheckbox.setEnabled(generator.GPUisfunctional());
			GPUcheckbox.setSelected(generator.GPUisenabled());
			settingsPanel.add(GPUcheckbox);

			JPanel settingsButtonPanel = new JPanel();
			closeButton = new JButton("Refresh and close");
			closeButton.addActionListener(this);
			refreshButton = new JButton("Refresh");
			refreshButton.addActionListener(this);
			settingsButtonPanel.add(refreshButton);
			settingsButtonPanel.add(closeButton);
			settingsPanel.add(settingsButtonPanel);

			// Sets the size of the settingsframe
			// Calculated by the width and height of settingsPanels components
			int width = 50; // To get some width should components have the
							// wrong settings
			int height = 50; // To get some height should components have the
								// wrong settings.
			for (Component comp : settingsPanel.getComponents()) {
				width = Math.max(width, comp.getPreferredSize().width);
				height += comp.getPreferredSize().height;
			}
			setSize(width, height);
			add(settingsPanel);
			setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			//renders julia set
			if(event.getSource().equals(useJuliaSet)) {
				juliaSetImaginaryValue.setEnabled(useJuliaSet.isSelected());
				juliaSetRealValue.setEnabled(useJuliaSet.isSelected());
			}
			else {
				//sets the antialiasing
				canvas.setOverlay(informationOverlay.isSelected());
				canvas.setMinAntialiasing(Math.max(1,
						antialiasingDuringZoom.getValue()));
				canvas.setMaxAntialiasing(Math.max(1, antialiasing.getValue()));
				generator.setCurrentAntialiasing(Math.max(1,
						antialiasing.getValue()));
	
				// We know these are Integers due to it being a FormattedTextField
				Integer redValue = (Integer) redSetValue.getValue();
				Integer blueValue = (Integer) blueSetValue.getValue();
				Integer greenValue = (Integer) greenSetValue.getValue();
	
				generator.setImageDetail(detailLevel.getValue());
	
				//changes colors
				if (redValue <= 255 && redValue >= 0) {
					generator.setRedSensitivity(redValue);
				} else {
					redSetValue.setValue(generator.getRedSensitivity());
				}
				if (blueValue <= 255 && blueValue >= 0) {
					generator.setBlueSensitivity(blueValue);
				} else {
					blueSetValue.setValue(generator.getBlueSensitivity());
				}
				if (greenValue <= 255 && greenValue >= 0) {
					generator.setGreenSensitivity(greenValue);
				} else {
					greenSetValue.setValue(generator.getGreenSensitivity());
				}
				
				//settings for julia sets
				generator.setRenderJuliaSet(useJuliaSet.isSelected());
				if(generator.getRenderJuliaSet()) {
					try{
						generator.setJuliaValues((Double)juliaSetRealValue.getValue(), (Double)juliaSetImaginaryValue.getValue());
					}
					catch(Exception e) {
						System.err.println("Textfield returned wrong valuetype, trying to parse from string");
						//Known bug in JFormattedTextField causes the field to return a long during certain conditions
						generator.setJuliaValues(Double.valueOf(juliaSetRealValue.getValue().toString()), 
								Double.valueOf(juliaSetImaginaryValue.getValue().toString()));
					}
				}
	
				// Cannot call setCPUOnly with same value as it currently is
				// Therefore the double check
				if (!generator.GPUisenabled() && GPUcheckbox.isSelected()) {
					generator.enableGPU(true);
	
					// detail is lowered to avoid GPU timeouts
					if (generator.getImageDetail() > 1) {
						generator.setImageDetail(1);
					}
	
				} else if (generator.GPUisenabled() && !GPUcheckbox.isSelected()) {
	
					generator.enableGPU(false);
				}
	
				canvas.render();
				if (event.getSource().equals(closeButton)) {
					dispose();
				}
			}
		}
	}
}
