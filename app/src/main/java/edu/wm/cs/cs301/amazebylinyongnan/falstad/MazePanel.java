package edu.wm.cs.cs301.amazebylinyongnan.falstad;

//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import generation.Seg;

import java.io.Serializable;
import edu.wm.cs.cs301.amazebylinyongnan.falstad.MazeController;
import edu.wm.cs.cs301.amazebylinyongnan.ui.ManualPlayActivity;
import edu.wm.cs.cs301.amazebylinyongnan.ui.AutoPlayActivity;
import android.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;



/**
 * Add functionality for double buffering to an AWT Panel class.
 * Used for drawing a maze.
 * 
 * @author pk
 *
 */
public class MazePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* Panel operates a double buffer see
	 * http://www.codeproject.com/Articles/2136/Double-buffer-in-standard-Java-AWT
	 * for details
	 */

	private Canvas canvas;
	private Paint color = new Paint();
	private ManualPlayActivity manualPlayActivity;
	private AutoPlayActivity autoPlayActivity;
	private int setColor;
	private boolean manual = true;


	//private Image bufferImage ;
	private MazeController controller;
		
	//firstPersonDrawer redraw method
	//private Graphics2D gc;
	
	//firstPersonDrawer BoundingBoxIsVisible method
	//private Point p;

	
	//font
	//final Font largeBannerFont = new Font("TimesRoman", Font.BOLD, 48);
	//final Font smallBannerFont = new Font("TimesRoman", Font.BOLD, 16);
	
	//seg
	private Seg seg;
	//private Color color;

    private Bitmap sky;
    private Bitmap ground;
    private Bitmap wall;



	//---------------------New methods for Android App---------------------------

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

    public Canvas getCanvas(){
        return canvas;
    }

	public void setManualPlayActivity(ManualPlayActivity activity){
		manualPlayActivity = activity;
        sky = manualPlayActivity.getSkyBMP();
        ground = manualPlayActivity.getGroundBMP();
        wall = manualPlayActivity.getWallBMP();
	}

    public ManualPlayActivity getManualPlayActivity(){
        return manualPlayActivity;
    }

	public void setAutoPlayActivity(AutoPlayActivity activity){
		autoPlayActivity = activity;
        sky = autoPlayActivity.getSkyBMP();
        ground = autoPlayActivity.getGroundBMP();
        wall = autoPlayActivity.getWallBMP();
	}

    public AutoPlayActivity getAutoPlayActivity(){
        return autoPlayActivity;
    }

	public void update(){
		if (manual == true){
			manualPlayActivity.runOnUiThread(new Runnable(){public void run(){
				manualPlayActivity.updateGraphics();
			}});
		}else{
			autoPlayActivity.runOnUiThread((new Runnable(){public void run(){
				autoPlayActivity.updateGraphics();
			}}));
		}
	}



	//---------------------------------------------------------------------------

	/**
	 * Constructor. Object is not focusable.
	 */
	public MazePanel() {
		super() ;
	}
	
	public MazePanel(MazeController controller){
		super();
		this.controller = controller;
		//this.setFocusable(false);
	}
	
	public MazePanel(Seg seg){
		super();
		this.seg = seg;
		//this.setFocusable(false);
	}



	/*
	public void update(Graphics g) {
		paint(g) ;
	}
	public void update() {
		paint(getGraphics()) ;
	}
	*/

	/**
	 * Draws the buffer image to the given graphics object.
	 * This method is called when this panel should redraw itself.
	 */
    /*
	public void paint(Graphics g) {
		if (null == g) {
			System.out.println("MazePanel.paint: no graphics object, skipping drawImage operation") ;
		}
		else {
			g.drawImage(bufferImage,0,0,null) ;	
		}
	}

	public void initBufferImage() {
		bufferImage = createImage(Constants.VIEW_WIDTH, Constants.VIEW_HEIGHT);
		if (null == bufferImage)
		{
			System.out.println("Error: creation of buffered image failed, presumedly container not displayable");
		}
	}
	*/
	/**
	 * Obtains a graphics object that can be used for drawing. 
	 * Multiple calls to the method will return the same graphics object 
	 * such that drawing operations can be performed in a piecemeal manner 
	 * and accumulate. To make the drawing visible on screen, one
	 * needs to trigger a call of the paint method, which happens 
	 * when calling the update method. 
	 * @return graphics object to draw on
	 */
    /*
	public Graphics getBufferGraphics() {
		if (null == bufferImage)
			initBufferImage() ;
		if (null == bufferImage)
			return null ;
		return bufferImage.getGraphics() ;
	}
    */

	/**
	 * This is created to support the FirstPersonDrawer redraw method.
	 */
    /*
	public void setSimpleGraphics() {
		gc = (Graphics2D) getBufferGraphics();
		
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}
	*/


	public void setColor(String color) {
		if(color == "red"){
			setColor = Color.RED;
		}
		if(color == "blue"){
			setColor = Color.BLUE;
		}
		if(color == "gray"){
			setColor = Color.GRAY;
		}
		if(color == "black"){
			setColor = Color.BLACK;
		}
		if(color == "white"){
			setColor = Color.WHITE;
		}
		if(color == "orange"){
			setColor = (0xFFFF8800);
		}
		if (color == "yellow"){
			setColor = Color.YELLOW;
		}
		if (color == "darkGray") {
			setColor = Color.DKGRAY;
		}
        if(color == "green"){
            setColor = Color.GREEN;
        }

		this.color.setColor(setColor);
	}

    public Paint getColor(){
        return color;
    }

	/**
	 * This method is created for setting the color with color object as parameter.
	 * @param color
	 */
	public void gcSetColorWithColor(Paint color){
		this.color = color;

		/*
		//gc.setColor(color);
		gc = (Graphics2D) this.getBufferGraphics();
		gc.setColor(color);
		*/
	}
	


	/**
	 * This method is created for seg class to set the color with r,g,b three integers.
	 * @param r
	 * @param g
	 * @param b
	 */
	public void setSegColor(int r, int g, int b) {
		//color = new Color(r,g,b);
		int setColor = (255)<<24|((r)<<16)|((g)<<8)|(b);
		color.setColor(setColor);
	}
	
	/**
	 * This method is created for seg class to get the color object.
	 * @return
	 */
	public Paint getSegColor(){
		return color;
	}
	
	/**
	 * This method is created for seg class the set the color with only one integer value as parameter.
	 * @param i
	 */
	public void segSetColorWithOneParameter(int i) {
		color.setColor(i);
        //color = new Color(i);
	}
	
	/**
	 * This method sets the graphics.
	 */
    /*
	public void setGC() {
		gc = (Graphics2D) getBufferGraphics();
	}
	*/
	/**
	 * This is the getter method for graphic object.
	 * @return
	 */
    /*
	public Graphics getGC(){
		return gc;
	}
    */

    public void setManual(boolean manual){
        this.manual = manual;
    }


	//-----------------------Draw Line, Oval, Rect & Polygon--------------------------
	public void drawLine(int x1, int y1, int x2, int y2){
        canvas.drawLine(x1, y1, x2, y2, color);
    }

    public void fillOval(int x, int y, int width, int height){
        canvas.drawOval(new RectF(x,y,x+width,y+height),color);
    }

    public void fillRect(int x, int y, int width, int height){
        if (color.getColor() == Color.BLACK){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            canvas.drawBitmap(sky, null, new Rect(x, y, x+width, y+height), null);
        }

        else if (color.getColor() == Color.DKGRAY){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            canvas.drawBitmap(ground, null, new Rect(x, y, x+width, y+height), null);
        }

        else if (color.getColor() == Color.GREEN){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            canvas.drawBitmap(wall, null, new Rect(x, y, x+width, y+height), null);
        }

        else{
            canvas.drawRect(new Rect(x, y, x+width, y+height), color);
        }    }
    /*
    public void drawString(String str, int x, int y){
        canvas.drawText(str,x,y,color);
    }
    */

    public void fillPolygon(int[] xps, int[] yps, int j){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final Path p = new Path();
        p.moveTo(xps[0], yps[0]);
        for (int i = 0; i < j; i++){
            p.lineTo(xps[i], yps[i]);
        }
        p.lineTo(xps[0], yps[0]);

        BitmapShader shader = new BitmapShader(wall, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);


        Paint segColor = new Paint();
        //segColor.setColor(setColor);
        //segColor.setStyle(Paint.Style.FILL);
        //canvas.drawPath(p, segColor);
        segColor.setShader(shader);
        canvas.drawPath(p, segColor);


    }
	//----------------------------------------------------------------
	
	

}
