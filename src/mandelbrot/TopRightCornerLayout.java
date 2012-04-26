package mandelbrot;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;

/**
* Layout that puts all components except one in the top right corner of the screen. 
* The single other component i laid out over the entire parent.
**/
public class TopRightCornerLayout implements LayoutManager{

	public static final String TOPRIGHTCORNER = "r"; //For components to be put in the corner.
	public static final String OTHER = "o"; //For the component to be laid out over entire parent.
	ArrayList<Component> cornerComponents = new ArrayList<Component>(); //All components to be put in corner
	Component otherComponent; //Component to be laid out over entire parent.

	@Override
	public void addLayoutComponent(String string, Component comp) {
		if(string.equals(TOPRIGHTCORNER)) {
			cornerComponents.add(comp);
		}
		else {
			otherComponent = comp;
		}
		
	}
	
	@Override
	public void layoutContainer(Container parent) {
		int parentWidth = parent.getWidth();
		for(Component c: parent.getComponents()) {
			if(cornerComponents.contains(c)) {
				Dimension dim = c.getPreferredSize();
				c.setBounds(parentWidth-dim.width, 0,dim.width, dim.height);
				parentWidth -= dim.width;
			}
			else if (c.equals(otherComponent)) {
				parent.setComponentZOrder(otherComponent, parent.getComponentCount()-1);
				c.setBounds(0,0,parent.getWidth(), parent.getHeight());
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(parent.getWidth(), parent.getHeight());
	}

	@Override
	public void removeLayoutComponent(Component arg0) {
		// TODO Auto-generated method stub
		
	}

}
