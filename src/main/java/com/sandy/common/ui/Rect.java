package com.sandy.common.ui;

import java.awt.BasicStroke ;
import java.awt.Color ;
import java.awt.Graphics ;
import java.awt.Graphics2D ;
import java.awt.Point ;
import java.awt.Rectangle ;

public class Rect {
	
	protected static final int KNOB_SIZE = 6 ;
	protected static final int NONE = -1, NW = 0, SW = 1, SE = 2, NE = 3 ;
	
	protected Rectangle bounds ;
	protected boolean isSelected ;
	public DrawingCanvas canvas ;

	/**
	 * The constructor that creates a new zero width and height rectangle at the
	 * given position in the canvas.
	 */
	public Rect( Point start, DrawingCanvas dcanvas ) {
		canvas = dcanvas ;
		bounds = new Rectangle( transformToModel( start ) ) ;
	}
	
	public Rectangle getBounds() {
	    return this.bounds ;
	}

	/**
	 * The "primitive" for all resizing/moving/creating operations that affect
	 * the rect bounding box. The current implementation just resets the bounds
	 * variable and triggers a re-draw of the union of the old & new rectangles.
	 * 
	 * This will redraw the shape in new size and place and also "erase" if
	 * bounds are now smaller than before. 
	 */
	protected void setBounds( Rectangle newBounds ) {
		
		Rectangle oldBounds = transformToView( bounds ) ;
		bounds = transformToModel( newBounds ) ;
		updateCanvas( oldBounds.union( newBounds ) ) ;
	}

	/**
	 * The resize operation is called when first creating a rect, as well as
	 * when later resizing by dragging one of its knobs.
	 * 
	 * The two parameters are the points that define the new bounding box. The
	 * anchor point is the location of the mouse-down event during a creation
	 * operation or the opposite corner of the knob being dragged during a
	 * resize operation. The end is the current location of the mouse. If you
	 * create the smallest rectangle which encloses these two points, you will
	 * have the new bounding box. 
	 */
	public void resize( Point anchor, Point end ) {
		
		Rectangle newRect = new Rectangle( anchor ) ;
		newRect.add( end ) ; 
		setBounds( newRect ) ; 
	}

	/**
	 * The translate operation is called when moving a shape by dragging in the
	 * canvas. The two parameters are the delta-x and delta-y to move by.
	 * 
	 * Note that either or both can be negative. Create a new rectangle from our
	 * bounds and translate and then go through the setBounds() primitive to
	 * change it.
	 */
	public void translate( int dx, int dy ) {
		
		Rectangle newRect = new Rectangle( bounds ) ;
		newRect.translate( dx, dy ) ;
		setBounds( newRect ) ;
	}

	/**
	 * Used to change the selected state of the shape which will require
	 * updating the affected area of the canvas to add/remove knobs.
	 */
	public void setSelected( boolean newState ) {
		
		isSelected = newState ;
		updateCanvas( bounds, true ) ;
	}

	/**
	 * The updateCanvas() methods are used when the state has changed in such a
	 * way that it needs to be refreshed in the canvas to properly reflect the
	 * new settings. 
	 */
	protected void updateCanvas( Rectangle areaOfChange, boolean enlargeForKnobs ) {
		
		Rectangle toRedraw = new Rectangle( areaOfChange ) ;
		if( enlargeForKnobs ) {
			toRedraw.grow( KNOB_SIZE / 2, KNOB_SIZE / 2 ) ;
		}
		canvas.repaint( toRedraw ) ;
	}

	protected void updateCanvas( Rectangle areaOfChange ) {
		updateCanvas( areaOfChange, isSelected ) ;
	}
	
	public Rectangle transformToView( Rectangle modelRect ) {
	    double sf = canvas.getScaleFactor() ;
	    return new Rectangle( (int)(modelRect.x * sf), 
            	              (int)(modelRect.y * sf),
            	              (int)(modelRect.width * sf),
            	              (int)(modelRect.height * sf) ) ;
	}

	public Rectangle transformToModel( Rectangle viewRect ) {
	    double sf = canvas.getScaleFactor() ;
	    return new Rectangle( (int)(viewRect.x / sf), 
	            (int)(viewRect.y / sf),
	            (int)(viewRect.width / sf),
	            (int)(viewRect.height / sf) ) ;
	}
	
	public Point transformToView( Point modelPt ) {
	    double sf = canvas.getScaleFactor() ;
	    return new Point( (int)(modelPt.x * sf), 
	                      (int)(modelPt.y * sf) ) ;
	}
	
	public Point transformToModel( Point viewPt ) {
	    double sf = canvas.getScaleFactor() ;
        return new Point( (int)(viewPt.x / sf), 
                          (int)(viewPt.y / sf) ) ;
	}
	
	/**
	 * When the DrawingCanvas needs a shape to draw itself, it sends a draw
	 * message, passing the graphics context and the current region being
	 * redrawn. If the shape intersects with that region, it must draw itself
	 * doing whatever it takes to properly represent itself in the canvas
	 *( colors, location, size, knobs, etc. ) by messaging the Graphics object.
	 */
	public void draw( Graphics g, Rectangle clipRect ) {
		
	    double scaleFactor = canvas.getScaleFactor() ;
	    
		if( !bounds.intersects( clipRect ) )
			return ;

		g.setColor( Color.red ) ;
		((Graphics2D)g).setStroke(new BasicStroke(3));
		
		Rectangle viewBounds = transformToView( bounds ) ;
		g.drawRect( viewBounds.x, viewBounds.y, viewBounds.width, viewBounds.height ) ;
		
		if( isSelected ) { 
			// if selected, draw the resizing knobs along the 4 corners
			Rectangle[] knobs = getKnobRects() ;
			for( int i = 0 ; i < knobs.length ; i++ ) {
				g.fillRect( (int)(knobs[i].x*scaleFactor), 
				            (int)(knobs[i].y*scaleFactor), 
						    knobs[i].width, knobs[i].height ) ;
			}
		}
	}

	/**
	 * When the DrawingCanvas needs to determine which shape is under the mouse,
	 * it asks the shape to determine if a point is "inside".
	 * 
	 * This method should returns true if the given point is inside the region
	 * for this shape. For a rectangle, any point within the bounding box is
	 * inside the shape.
	 */
	public boolean inside( Point pt ) {
		return bounds.contains( transformToModel( pt ) ) ;
	}

	protected Rectangle[] getKnobRects() {
		
		Rectangle[] knobs = new Rectangle[4] ;
		
		knobs[NW] = new Rectangle( bounds.x - KNOB_SIZE / 2, 
				                   bounds.y - KNOB_SIZE / 2, 
				                   KNOB_SIZE, KNOB_SIZE ) ;
		
		knobs[SW] = new Rectangle( bounds.x - KNOB_SIZE / 2, 
				                   bounds.y + bounds.height - KNOB_SIZE / 2, 
				                   KNOB_SIZE, KNOB_SIZE ) ;
		
		knobs[SE] = new Rectangle( bounds.x + bounds.width - KNOB_SIZE / 2,
				                   bounds.y + bounds.height - KNOB_SIZE / 2, 
				                   KNOB_SIZE, KNOB_SIZE ) ;
		
		knobs[NE] = new Rectangle( bounds.x + bounds.width - KNOB_SIZE / 2,
				                   bounds.y - KNOB_SIZE / 2, 
				                   KNOB_SIZE, KNOB_SIZE ) ;
		return knobs ;
	}

	/**
	 * Helper method to determine if a point is within one of the resize corner
	 * knobs. If not selected, we have no resize knobs, so it can't have been a
	 * click on one. Otherwise, we calculate the knob rects and then check
	 * whether the point falls in one of them. The return value is one of NW,
	 * NE, SW, SE constants depending on which knob is found, or NONE if the
	 * click doesn't fall within any knob.
	 */
	protected int getKnobContainingPoint( Point pt ) {
		
		if( !isSelected ) {
			return NONE ;
		}

		Rectangle[] knobs = getKnobRects() ;
		for( int i = 0 ; i < knobs.length ; i++ ) {
			if( knobs[i].contains( pt ) ) {
				return i ;
			}
		}
		return NONE ;
	}

	/**
	 * Method used by DrawingCanvas to determine if a mouse click is starting a
	 * resize event. In order for it to be a resize, the click must have been
	 * within one of the knob rects( checked by the helper method
	 * getKnobContainingPoint ) and if so, we return the "anchor" i.e. the knob
	 * opposite this corner that will remain fixed as the user drags the
	 * resizing knob of the other corner around. During the drag actions of a
	 * resize, that fixed anchor point and the current mouse point will be
	 * passed to the resize method, which will reset the bounds in response to
	 * the movement. If the mouseLocation wasn't a click in a knob and thus not
	 * the beginning of a resize event, null is returned.
	 */
	public Point getAnchorForResize( Point mouseLocation ) {
		
		int whichKnob = getKnobContainingPoint( mouseLocation ) ;

		if( whichKnob == NONE )
			return null ;
		
		switch( whichKnob ) {
			case NW:
				return new Point( bounds.x + bounds.width, 
						          bounds.y + bounds.height ) ;
			case NE:
				return new Point( bounds.x, 
						          bounds.y + bounds.height ) ;
			case SW:
				return new Point( bounds.x + bounds.width, 
						          bounds.y ) ;
			case SE:
				return new Point( bounds.x, bounds.y ) ;
		}
		return null ;
	}

	public Object clone() {
		return this ;
	}
}
