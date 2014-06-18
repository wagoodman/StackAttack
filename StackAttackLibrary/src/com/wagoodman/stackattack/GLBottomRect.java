package com.wagoodman.stackattack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import android.graphics.Bitmap;
import android.opengl.GLUtils;





class GLBottomRect
{
	//private static final String TAG = "Rect";
	public static final Boolean debug = false;
	
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTextureBuffer;
	private ByteBuffer mIndexBuffer;
	private FloatBuffer mNormalBuffer;
	
	private int indexLen;
	
	protected int mTextureId = -1;

	private float mScaleFactorX = 1.0f;
	private float mScaleFactorY = 1.0f;
	private float mScaleFactorZ = 1.0f;
	
	public GLBottomRect(float scale, float width, float height, float depth )
	{
		init(scale, scale, scale, width, height, depth, 1.0f);
	}

	public GLBottomRect(float scale, float width, float height, float depth, float texScale )
	{
		init(scale, scale, scale, width, height, depth, texScale);
	}
	
	public GLBottomRect(float scaleX, float scaleY, float scaleZ, float width, float height, float depth )
	{
		init(scaleX, scaleY, scaleZ, width, height, depth, 1.0f);
	}

	public GLBottomRect(float scaleX, float scaleY, float scaleZ, float width, float height, float depth, float texScale )
	{
		init(scaleX, scaleY, scaleZ, width, height, depth, texScale);
	}
	
	private void init(float scaleX, float scaleY, float scaleZ, float width, float height, float depth, float texScale)
	{
		
		mScaleFactorX = 1/scaleX;
		mScaleFactorY = 1/scaleY;
		mScaleFactorZ = 1/scaleZ;
		
		//super(context); 
		width *= 2.0f;
		height *= 2.0f;
		depth *= 2.0f;

		float halfWidth = width / 2;
		float halfHeight = height / 2;
		float halfDepth = depth / 2;

		// cube with origin at center of cube

		/*
	    float vertices[] = {
				//Vertices according to faces
	    		-1.0f, -1.0f, 1.0f, //Vertex 0
	    		1.0f, -1.0f, 1.0f,  //v1
	    		-1.0f, 1.0f, 1.0f,  //v2
	    		1.0f, 1.0f, 1.0f,   //v3
	    		
	    		1.0f, -1.0f, 1.0f,	//...
	    		1.0f, -1.0f, -1.0f,    		
	    		1.0f, 1.0f, 1.0f,
	    		1.0f, 1.0f, -1.0f,
	    		
	    		1.0f, -1.0f, -1.0f,
	    		-1.0f, -1.0f, -1.0f,    		
	    		1.0f, 1.0f, -1.0f,
	    		-1.0f, 1.0f, -1.0f,
	    		
	    		-1.0f, -1.0f, -1.0f,
	    		-1.0f, -1.0f, 1.0f,    		
	    		-1.0f, 1.0f, -1.0f,
	    		-1.0f, 1.0f, 1.0f,
	    		
	    		-1.0f, -1.0f, -1.0f,
	    		1.0f, -1.0f, -1.0f,    		
	    		-1.0f, -1.0f, 1.0f,
	    		1.0f, -1.0f, 1.0f,
	    		
	    		-1.0f, 1.0f, 1.0f,
	    		1.0f, 1.0f, 1.0f,    		
	    		-1.0f, 1.0f, -1.0f,
	    		1.0f, 1.0f, -1.0f,
									};
		*/
		
	    float vertices[] = {
				//Vertices according to faces
	    		-halfWidth,	0,	halfDepth, //Vertex 0
	    		halfWidth,	0,	halfDepth,  //v1
	    		-halfWidth,	height,		halfDepth,  //v2
	    		halfWidth,	height, 	halfDepth,   //v3
	    		
	    		halfWidth,	0,	halfDepth,	//...
	    		halfWidth,	0,	-halfDepth,
	    		halfWidth,	height,		halfDepth,
	    		halfWidth,	height,		-halfDepth,
	    		
	    		halfWidth,	0,	-halfDepth,
	    		-halfWidth,	0,	-halfDepth,    		
	    		halfWidth,	height,		-halfDepth,
	    		-halfWidth,	height,		-halfDepth,
	    		
	    		-halfWidth,	0,	-halfDepth,
	    		-halfWidth,	0,	halfDepth,	
	    		-halfWidth,	height,		-halfDepth,
	    		-halfWidth,	height,		halfDepth,
	    		
	    		-halfWidth,	0,	-halfDepth,
	    		halfWidth,	0,	-halfDepth,	
	    		-halfWidth,	0,	halfDepth,
	    		halfWidth,	0,	halfDepth,
	    		
	    		-halfWidth,	height,		halfDepth,
	    		halfWidth,	height,		halfDepth,		
	    		-halfWidth,	height,		-halfDepth,
	    		halfWidth,	height,		-halfDepth,
									};
		
		
		// The initial texture coordinates (u, v) 
	    /*
		float texture[] = {    		
	    		//Mapping coordinates for the vertices
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f, 
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,

	    							};
	     */
	    
		float texture[] = {    		
	    		//Mapping coordinates for the vertices
	    		0.0f,				0.0f,				//Front
	    		0.0f,				width*texScale,
	    		height*texScale,	0.0f,
	    		height*texScale,	width*texScale, 
	    		
	    		0.0f,				0.0f,				//Right
	    		0.0f,				depth*texScale,
	    		height*texScale,	0.0f,
	    		height*texScale,	depth*texScale, 

	    		0.0f,				0.0f,				//Back
	    		0.0f,				width*texScale,
	    		height*texScale,	0.0f,
	    		height*texScale,	width*texScale, 
	    		
	    		0.0f,				0.0f,				//Left
	    		0.0f,				depth*texScale,
	    		height*texScale,	0.0f,
	    		height*texScale,	depth*texScale, 
	    		
	    		0.0f,				0.0f,				//Top
	    		0.0f,				width*texScale,
	    		depth*texScale,		0.0f,
	    		depth*texScale,		width*texScale, 
	    		
	    		0.0f,				0.0f,				//Bottom
	    		0.0f,				width*texScale,
	    		depth*texScale,		0.0f,
	    		depth*texScale,		width*texScale, 

	    							};
	    /*
		gl.glNormal3f(0, 0, 1);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		gl.glNormal3f(0, 0, -1);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);

		gl.glColor4f(1, 1, 1, 1);
		gl.glNormal3f(-1, 0, 0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
		gl.glNormal3f(1, 0, 0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);

		gl.glColor4f(1, 1, 1, 1);
		gl.glNormal3f(0, 1, 0);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
		gl.glNormal3f(0, -1, 0);*/
		
		
		float normals[] = {
				// Normals
				0.0f, 0.0f, 1.0f, 						
				0.0f, 0.0f, 1.0f, 
				0.0f, 0.0f, 1.0f, 
				0.0f, 0.0f, 1.0f, 
				
				0.0f, 0.0f, -1.0f, 
				0.0f, 0.0f, -1.0f, 
				0.0f, 0.0f, -1.0f, 
				0.0f, 0.0f, -1.0f, 
				
				-1.0f, 0.0f, 0.0f, 						
				-1.0f, 0.0f, 0.0f, 
				-1.0f, 0.0f, 0.0f,  
				-1.0f, 0.0f, 0.0f, 
				
				1.0f, 0.0f, 0.0f, 
				1.0f, 0.0f, 0.0f, 
				1.0f, 0.0f, 0.0f, 
				1.0f, 0.0f, 0.0f, 
				
				0.0f, 1.0f, 0.0f, 						
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
									};
		
		
		// The initial indices definition
		byte indices[] = {
				//Faces definition
	    		0,1,3, 0,3,2, 			//Face front
	    		4,5,7, 4,7,6, 			//Face right
	    		8,9,11, 8,11,10, 		//... 
	    		12,13,15, 12,15,14, 	
	    		16,17,19, 16,19,18, 	
	    		20,21,23, 20,23,22, 	
									};

		// Buffers to be passed to gl*Pointer() functions must be
		// direct, i.e., they must be placed on the native heap
		// where the garbage collector cannot move them.
		//
		// Buffers with multi-byte data types (e.g., short, int,
		// float) must have their byte order set to native order
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuf.asFloatBuffer();
		mTextureBuffer.put(texture);
		mTextureBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mNormalBuffer = byteBuf.asFloatBuffer();
		mNormalBuffer.put(normals);
		mNormalBuffer.position(0);
		
		//
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
		indexLen = indices.length;
		
	}

	
	
	/**
	 * Load the textures
	 * 
	 * @param gl - The GL Context
	 * @param context - The Activity context
	 */
	public void loadGLTexture(GL10 gl, Bitmap bitmap) 
	{

		//Generate one texture pointer...
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureId = textures[0];
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
		
		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		
		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		//Clean up
		bitmap.recycle();
	}
	
	
	
	
	
	public void draw(GL10 gl)
	{
		
		
		gl.glScalef(mScaleFactorX, mScaleFactorY, mScaleFactorZ);

		gl.glDisable(GL10.GL_BLEND);
		
		
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
		
		// rely on z-ordering
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		// use texture
		if (mTextureId != -1 && mTextureBuffer != null)
		{
			gl.glEnable(GL10.GL_TEXTURE_2D);
			
			// Enable the texture state
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Point to our buffers
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
		}
		// don't use texture
		else
		{
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
				
		// Draw Verticies
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);


		gl.glDrawElements(GL10.GL_TRIANGLES, indexLen, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		// if using textures
		if (mTextureId != -1 && mTextureBuffer != null)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		
	} // end draw

}






/*
class GLBottomRect
{
	//private static final String TAG = "Rect";
	public static final Boolean debug = false;
	
	
	protected FloatBuffer mVertexBuffer;
	private ByteBuffer mIndicesBuffer;
	protected FloatBuffer mTextureBuffer;
	private int mNumberOfIndices;
	
	protected int mTextureId = -1;
	protected Boolean mShouldLoadTexture = false;

	// The bitmap we want to load as a texture.
	private Bitmap mBitmap; // New variable.
	

	public void loadBitmap(Bitmap bitmap)
	{ // New function.
		this.mBitmap = bitmap;
		mShouldLoadTexture = true;
	}

	private void loadGLTexture(GL10 gl)
	{ // New function
		// Generate one texture pointer...
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureId = textures[0];

		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

		// Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		// Use the Android GLUtils to specify a two-dimensional texture image
		// from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
	}
	
	public GLBottomRect( float dist, float w, float h, float d )
	{
		//super(context); 
		
		//dist = 0.3f;
		
		int width = (int) (dist*w);
		int height = (int) (dist*h);
		int depth = (int) (dist*d);
		
		int halfWidth = width / 2;
		int halfDepth = depth / 2;

		// cube with origin at center of cube

		
//		float vertices[] = {
//				// FRONT
//				-halfWidth, 0, halfDepth, 
//				halfWidth, 0, halfDepth, 
//				-halfWidth, height, halfDepth, 
//				halfWidth, height, halfDepth,
//				// BACK
//				-halfWidth, 0, -halfDepth, 
//				-halfWidth, height, -halfDepth, 
//				halfWidth, 0, -halfDepth, 
//				halfWidth, height, -halfDepth,
//				// LEFT
//				-halfWidth, 0, halfDepth, 
//				-halfWidth, height, halfDepth, 
//				-halfWidth, 0, -halfDepth, 
//				-halfWidth, height, -halfDepth,
//				// RIGHT
//				halfWidth, 0, -halfDepth, 
//				halfWidth, height, -halfDepth, 
//				halfWidth, 0, halfDepth, 
//				halfWidth, height, halfDepth,
//				// TOP
//				-halfWidth, height, halfDepth, 
//				halfWidth, height, halfDepth, 
//				-halfWidth, height, -halfDepth, 
//				halfWidth, height, -halfDepth,
//				// BOTTOM
//				-halfWidth, 0, halfDepth, 
//				-halfWidth, 0, -halfDepth, 
//				halfWidth, 0, halfDepth, 
//				halfWidth, 0, -halfDepth, };
		
		
		float vertices[] = {
				//Vertices according to faces
	    		-halfWidth,	0f,		halfDepth, //Vertex 0
	    		halfWidth,	0f,		halfDepth,  //v1
	    		-halfWidth,	height,	halfDepth,  //v2
	    		halfWidth,	height,	halfDepth,   //v3
	    		
	    		halfWidth,	0f,		halfDepth,	//...
	    		halfWidth,	0f,		-halfDepth,    		
	    		halfWidth,	height,	halfDepth,
	    		halfWidth,	height,	-halfDepth,
	    		
	    		halfWidth,	0f,		-halfDepth,
	    		-halfWidth,	0f,		-halfDepth,    		
	    		halfWidth,	height,	-halfDepth,
	    		-halfWidth,	height,	-halfDepth,
	    		
	    		-halfWidth,	0f,		-halfDepth,
	    		-halfWidth,	0f,		halfDepth,    		
	    		-halfWidth,	height,	-halfDepth,
	    		-halfWidth,	height,	halfDepth,
	    		
	    		-halfWidth,	0f,		-halfDepth,
	    		halfWidth,	0f,		-halfDepth,    		
	    		-halfWidth,	0f,		halfDepth,
	    		halfWidth,	0f,		halfDepth,
	    		
	    		-halfWidth,	height,	halfDepth,
	    		halfWidth,	height,	halfDepth,    		
	    		-halfWidth, height,	-halfDepth,
	    		halfWidth,	height,	-halfDepth,
				};


	    byte indices[] = {
				//Faces definition
	    		0,1,3, 0,3,2, 			//Face front
	    		4,5,7, 4,7,6, 			//Face right
	    		8,9,11, 8,11,10, 		//... 
	    		12,13,15, 12,15,14, 	
	    		16,17,19, 16,19,18, 	
	    		20,21,23, 20,23,22, 	
									};
		

	    float texCoords[] = {    		
	    		//Mapping coordinates for the vertices
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f, 
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		
	    		0.0f, 0.0f,
	    		0.0f, 1.0f,
	    		1.0f, 0.0f,
	    		1.0f, 1.0f,
	    		};

		// Buffers to be passed to gl*Pointer() functions must be
		// direct, i.e., they must be placed on the native heap
		// where the garbage collector cannot move them.
		//
		// Buffers with multi-byte data types (e.g., short, int,
		// float) must have their byte order set to native order
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		mIndicesBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndicesBuffer.put(indices);
		mIndicesBuffer.position(0);
		mNumberOfIndices = indices.length;
		
		ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTextureBuffer = tbb.asFloatBuffer();
		mTextureBuffer.put(texCoords);
		mTextureBuffer.position(0);

	}

	public void draw(GL10 gl)
	{
		
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
		
		//Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		//Enable the vertex and texture state
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
		
		// initlize (done once)
		if (mShouldLoadTexture)
		{
			loadGLTexture(gl);
			mShouldLoadTexture = false;
		}
		
		// use texture
		if (mTextureId != -1 && mTextureBuffer != null)
		{
			gl.glEnable(GL10.GL_TEXTURE_2D);
			// Enable the texture state
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Point to our buffers
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
		}
		// don't use texture
		else
		{
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		
		//Draw the vertices as triangles, based on the Index Buffer information
		gl.glDrawElements(GL10.GL_TRIANGLES, mNumberOfIndices, GL10.GL_UNSIGNED_BYTE, mIndicesBuffer);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		
		if (mTextureId != -1 && mTextureBuffer != null)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}
		
	} // end draw

}
*/
