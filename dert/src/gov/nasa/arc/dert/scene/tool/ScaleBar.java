/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brian Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.ScaleBarState;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a class for a 3D map scale.
 */
public class ScaleBar extends FigureMarker implements Tool {

	public static final Icon icon = Icons.getImageIcon("scale_16.png");

	// Defaults
	public static Color defaultColor = Color.white;
	public static int defaultCellCount = 4;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultAutoLabel = true;
	public static double defaultAzimuth = 0;
	public static double defaultTilt = 0;

	// Label
	private boolean autoLabel;

	// Map Element state
	private ScaleBarState state;

	// Dimensions
	private int cellCount;
	private double radius;
	
	private double xOff, yOff;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ScaleBar(ScaleBarState state) {
		super(state.name, state.location, state.size, state.zOff, state.color, state.labelVisible, false, state.locked);
		this.state = state;
		cellCount = state.cellCount;
		radius = state.radius;
		autoLabel = state.autoLabel;
		setAzimuth(state.azimuth);
		setTilt(state.tilt);
		setVisible(state.visible);
		this.state = state;
		
		contents.detachChild(surfaceNormalArrow);
		surfaceNormalArrow = null;
		contents.setScale(1);

		buildRod();
		
		state.setMapElement(this);
	}
	
	public String getLabel() {
		return(label.getText());
	}
	

	@Override
	protected void scaleShape(double scale) {
		// do nothing;
	}
	
	@Override
	public void setShape(ShapeType shapeType, boolean force) {
		buildRod();
	}
	
	private void buildRod() {
		if (shape != null)
			contents.detachChild(shape);
		shape = Shape.createShape("_geometry", ShapeType.rod, cellCount, (float)radius, (float)size*cellCount);
		
		TextureState tState = new TextureState();
		tState.setTexture(getTexture());
		tState.setEnabled(true);
		shape.getGeometry().setRenderState(tState);
		shape.getGeometry().getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
		shape.getSceneHints().setCastsShadows(false);
		contents.attachChild(shape);
		
		label.setTranslation(new Vector3(0, 2.5*radius, 0));
		if (autoLabel)
			label.setText(getName()+" = "+String.format(Landscape.stringFormat, (size*cellCount)).trim());

		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
		
		SpatialUtil.setPickHost(shape, this);
	}

	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		super.setInMotion(inMotion, pickPosition);
		if (inMotion) {
			ReadOnlyVector3 trans = getTranslation();
			xOff = pickPosition.getX()-trans.getX();
			yOff = pickPosition.getY()-trans.getY();
		} else {
			xOff = 0;
			yOff = 0;
		}
	}

	@Override
	public void setLocation(double x, double y, double z, boolean doEdit) {
		super.setLocation(x - xOff, y - yOff, z, doEdit);
	}
	
	public int getCellCount() {
		return(cellCount);
	}
	
	public void setCellCount(int cells) {
		if (cellCount == cells)
			return;
		cellCount = cells;
		buildRod();
	}
	
	public void setAutoLabel(boolean autoLabel) {
		this.autoLabel = autoLabel;
	}
	
	public boolean isAutoLabel() {
		return(autoLabel);
	}

	/**
	 * Get the MapElement state
	 */
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the map element type
	 */
	public Type getType() {
		return (Type.Scale);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}
	
	public double getCellRadius() {
		return(radius);
	}
	
	public void setCellRadius(double radius) {
		if (this.radius == radius)
			return;
		this.radius = radius;
		buildRod();
	}

	/**
	 * Get the color map texture
	 * 
	 * @return
	 */
	private Texture2D getTexture() {
		Texture2D texture = null;
		texture = new Texture2D();
		texture.setWrap(Texture.WrapMode.Clamp);
		texture.setTextureStoreFormat(TextureStoreFormat.RGBA8);
		texture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
		texture.setMagnificationFilter(Texture.MagnificationFilter.NearestNeighbor);
		texture.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.NearestNeighborNoMipMaps));
		texture.setApply(Texture2D.ApplyMode.Modulate);
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(cellCount*4*4*4);
		int[] col = new int[2];
		Color wcolor = Color.WHITE;
		col[0] = MathUtil.bytes2Int((byte)wcolor.getAlpha(), (byte)wcolor.getBlue(),
			(byte)wcolor.getGreen(), (byte)wcolor.getRed());
		Color bcolor = Color.BLACK;
		col[1] = MathUtil.bytes2Int((byte)bcolor.getAlpha(), (byte)bcolor.getBlue(),
			(byte)bcolor.getGreen(), (byte)bcolor.getRed());
		for (int j=0; j<4; ++j)
			for (int i = 0; i < cellCount; ++i)
				for (int k=0; k<4; ++k)
					buffer.putInt(col[i%2]);		
		buffer.limit(cellCount*4*4*4);
		buffer.rewind();
		
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(buffer);
		Image image = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, cellCount*4, 4, list, null);
		texture.setImage(image);
		return (texture);
	}

	/**
	 * Get the point and distance to seek to
	 */
	public double getSeekPointAndDistance(Vector3 point) {
		BoundingVolume bv = getWorldBound();
		point.set(bv.getCenter());
		return (getRadius() * 1.5);
	}

	@Override
	public String toString() {
		return (getName());
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		// nothing here
	}

	/**
	 * Update size depending on camera location.
	 */
	@Override
	public void update(BasicCamera camera) {
		// nothing here
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Scale.defaultColor", defaultColor, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Scale.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultAutoLabel = StringUtil.getBooleanValue(properties, "MapElement.Scale.defaultAutoLabel", defaultAutoLabel, false);
		defaultCellCount = StringUtil.getIntegerValue(properties, "MapElement.Scale.defaultCellCount", true, defaultCellCount, false);
		defaultAzimuth = StringUtil.getDoubleValue(properties, "MapElement.Scale.defaultAzimuth", false,
			defaultAzimuth, false);
		defaultTilt = StringUtil.getDoubleValue(properties, "MapElement.Scale.defaultTilt", false, defaultTilt, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Scale.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Scale.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.Scale.defaultAutoLabel", Boolean.toString(defaultAutoLabel));
		properties.setProperty("MapElement.Scale.defaultCellCount", Integer.toString(defaultCellCount));
		properties.setProperty("MapElement.Scale.defaultAzimuth", Double.toString(defaultAzimuth));
		properties.setProperty("MapElement.Scale.defaultTilt", Double.toString(defaultTilt));
	}
	
}
