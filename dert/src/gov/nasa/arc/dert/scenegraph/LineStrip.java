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

package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.TextureCombineMode;

/**
 * A line object with multiple segments.
 *
 */
public class LineStrip extends Line {

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param vertex
	 * @param normal
	 * @param color
	 * @param coords
	 */
	public LineStrip(final String name, final FloatBuffer vertex, final FloatBuffer normal, final FloatBuffer color,
		final FloatBufferData coords) {
		super(name, vertex, normal, color, coords);
		getMeshData().setIndexMode(IndexMode.LineStrip);
		getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
	}

	/**
	 * Update the elevation when the landscape changes.
	 * 
	 * @param quadTree
	 */
	public synchronized void updateElevation(QuadTree quadTree) {
		FloatBufferData vertexData = _meshData.getVertexCoords();
		FloatBuffer vertex = vertexData.getBuffer();
		int n = vertex.limit();
		Landscape landscape = Landscape.getInstance();
		for (int i = 0; i < n; i += 3) {
			float x = vertex.get(i);
			float y = vertex.get(i + 1);
			double z = landscape.getZ(x, y) + 0.1;
			if (!Double.isNaN(z)) {
				vertex.put(i + 2, (float) z);
			}
		}
		
		_meshData.setVertexCoords(vertexData);
		updateModelBound();
	}

	/**
	 * Determine if this line strip intersects with a quad tree.
	 * 
	 * @param quadTree
	 * @return
	 */
	public boolean intersects(QuadTree quadTree) {
		Vector3[] testPoints = quadTree.getCornerPoints();
		BoundingBox bbox = (BoundingBox) getWorldBound();
		// check for overlap or quadtree is completely inside linestrip bbox
		for (int i = 0; i < testPoints.length; ++i) {
			if (bbox.contains(testPoints[i])) {
				return (true);
			}
		}

		// check if is linestrip completely inside quadtree
		ReadOnlyVector3 center = new Vector3(bbox.getCenter());
		double x = center.getX() - bbox.getXExtent();
		double y = center.getY() - bbox.getYExtent();
		if (quadTree.contains(x, y)) {
			return (true);
		}
		x = center.getX() + bbox.getXExtent();
		y = center.getY() + bbox.getYExtent();
		if (quadTree.contains(x, y)) {
			return (true);
		}
		return (false);
	}
	
	@Override
	public String toString() {
		return(getName());
	}
	
	public void setColor(Color color) {
		ColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);
		setDefaultColor(colorRGBA);
		markDirty(DirtyType.RenderState);
	}

}
