package com.wagoodman.stackattack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;


class GLCube
{
	//private static final String TAG = "Rect";
	public static final Boolean debug = false;
	
	private FloatBuffer mVertexBuffer;
	private ByteBuffer mIndexBuffer;
	private FloatBuffer mNormalBuffer;
	
	private float mWidth, mHeight, mDepth;
	
	private int[] mTextureIds = new int[BlockValue.values().length]; 
	private FloatBuffer[] mTextureBuffers = new FloatBuffer[BlockValue.values().length]; 
	
	private int indexLen;
	
	//protected int mTextureId = -1;
	protected Boolean mShouldLoadTexture = false;

	private float mScaleFactor = 1.0f;
	
	public GLCube(float scale, float width, float height, float depth )
	{
		init(scale, width, height, depth, 1.0f);
	}

	public GLCube(float scale, float width, float height, float depth, float texScale )
	{
		init(scale, width, height, depth, texScale);
	}
	

	private void init(float scale, float width, float height, float depth, float texScale)
	{
		
		mScaleFactor = 1/scale;
		
		mWidth = width;
		mHeight = height;
		mDepth = depth;
		
		//super(context); 
		width *= 2.0f;
		height *= 2.0f;
		depth *= 2.0f;

		float halfWidth = (mWidth)*mScaleFactor;
		float halfHeight = (mHeight)*mScaleFactor;
		float halfDepth = (mDepth)*mScaleFactor;

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
	    		-halfWidth,	-halfHeight,	halfDepth, //Vertex 0
	    		halfWidth,	-halfHeight,	halfDepth,  //v1
	    		-halfWidth,	halfHeight,		halfDepth,  //v2
	    		halfWidth,	halfHeight, 	halfDepth,   //v3
	    		
	    		halfWidth,	-halfHeight,	halfDepth,	//...
	    		halfWidth,	-halfHeight,	-halfDepth,
	    		halfWidth,	halfHeight,		halfDepth,
	    		halfWidth,	halfHeight,		-halfDepth,
	    		
	    		halfWidth,	-halfHeight,	-halfDepth,
	    		-halfWidth,	-halfHeight,	-halfDepth,    		
	    		halfWidth,	halfHeight,		-halfDepth,
	    		-halfWidth,	halfHeight,		-halfDepth,
	    		
	    		-halfWidth,	-halfHeight,	-halfDepth,
	    		-halfWidth,	-halfHeight,	halfDepth,	
	    		-halfWidth,	halfHeight,		-halfDepth,
	    		-halfWidth,	halfHeight,		halfDepth,
	    		
	    		-halfWidth,	-halfHeight,	-halfDepth,
	    		halfWidth,	-halfHeight,	-halfDepth,	
	    		-halfWidth,	-halfHeight,	halfDepth,
	    		halfWidth,	-halfHeight,	halfDepth,
	    		
	    		-halfWidth,	halfHeight,		halfDepth,
	    		halfWidth,	halfHeight,		halfDepth,		
	    		-halfWidth,	halfHeight,		-halfDepth,
	    		halfWidth,	halfHeight,		-halfDepth,
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
		for (int idx=0; idx < mTextureBuffers.length; idx++)
		{
			float texture[] = getTexCoords(BlockValue.values()[idx].mTexScale);
			byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
			byteBuf.order(ByteOrder.nativeOrder());
			FloatBuffer textureBuffer = byteBuf.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);
			
			mTextureBuffers[idx] = textureBuffer;
		}

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

	public void initGLTextures(GL10 gl, int texCount) 
	{
		//Generate x texture pointers...
		gl.glGenTextures(texCount, mTextureIds, 0);
	}
	
	
	private float[] getTexCoords(float texScale)
	{

		float width = mWidth * 2f;
		float height = mHeight * 2f;
		float depth = mDepth * 2f;
		
		return new float[] {    		
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
    		
    		0.0f,				0.0f,				//Top?
    		0.0f,				width*texScale,
    		depth*texScale,		0.0f,
    		depth*texScale,		width*texScale, 
    		
    		0.0f,				0.0f,				//Bottom?
    		0.0f,				width*texScale,
    		depth*texScale,		0.0f,
    		depth*texScale,		width*texScale, 

    							};
		
	}
	
	/**
	 * Load the textures
	 * 
	 * @param gl - The GL Context
	 * @param context - The Activity context
	 */
	public void loadGLTexture(GL10 gl, Bitmap bitmap, Integer textureIdx) 
	{
		textureId = mTextureIds[textureIdx];

		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		
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
	
		
	int textureId;
	public void draw(GL10 gl, Integer textureIdx)
	{
		
		//gl.glScalef(mScaleFactor, mScaleFactor, mScaleFactor);
		
		
		
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
		
		// use texture
		if (textureIdx != null)
		{
			// get texId
			textureId = mTextureIds[textureIdx];
			
			gl.glEnable(GL10.GL_TEXTURE_2D);
			
			// Enable the texture state
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Point to our buffers
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffers[textureIdx]);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
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
		if (textureIdx != null)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		
	} // end draw

}
