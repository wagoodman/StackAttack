package com.wagoodman.stackattack;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.wagoodman.stackattack.MainActivity;


import android.content.res.AssetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class TexFont 
{
	Context mContext;
	final MainActivity game;
	
	AssetManager am;
	int fntTexWidth, fntTexHeight;
	public int fntCellWidth, fntCellHeight;
	int BPP,firstCharOffset,colCount;
	int charWidth[];
	int texID;
	float redVal, greenVal, blueVal;
	int level;
	
	// draw
	private int [] UVarray = new int[4];
	private int glyph, row, col;
	
	
	public float charScale = 1.0f,				// scale the char
							xPos = 0f;	
	private float charWidthModifier = 1.0f;		// scale the char width
	
	// 3D --------------------------------------------------------------

	// Vertex array
	private float vertices[] = {  0.0f,   0.0f,  0.0f,  // 0, Bottom Left
			                      1.0f,   0.0f,  0.0f,  // 1, Bottom Right
			                      1.0f,   1.0f,  0.0f,  // 2, Top Right
			                      0.0f,   1.0f,  0.0f,  // 3, Top Left
			                    };
	// Order of connection for the vertices.
	private short[] indices = { 0, 1, 2, 0, 2, 3 };
	
	// UV co-ords
	private float[] uvs = {0,0, 1,0, 1,1, 0,1};
	
	// Our vertex buffer.
	private FloatBuffer vertexBuffer;

	// Our index buffer.
	private ShortBuffer indexBuffer;
	
	// Texture buffer
	private FloatBuffer texBuffer;
	
	public TexFont(Context context, GL10 gl, int id)
	{

		
		// Get handle on assets
		mContext = context;
		game = (MainActivity) context;
		am = mContext.getAssets();
		
		// Initialize parameters
		redVal = greenVal = blueVal = 1.0f;

		
		// Array to hold character width data
		charWidth = new int[256];

		texID = id;
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
	    // Set texture parameters
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

	    //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	    //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
	    //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
	    //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		
	    // Initialise quad buffers
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
		
		// Texture buffer
		ByteBuffer tbb = ByteBuffer.allocateDirect(4 * 2 * 4);
		tbb.order(ByteOrder.nativeOrder());
		texBuffer = tbb.asFloatBuffer();
		texBuffer.put(uvs);
		texBuffer.position(0);
		
	}
	
	
	public Boolean LoadFont(String fontName, GL10 gl, float horitontalCount) throws IOException
	{
		
		InputStream uStream = am.open(fontName, AssetManager.ACCESS_BUFFER);
		DataInputStream in = new DataInputStream(uStream); // Get around Java's stupid unsigned bytes 
		
		// Check header - should read 0xBF 0xF2 (BFF2)
  		int h0 = in.readUnsignedByte();
  		int h1 = in.readUnsignedByte();
		
		if(h0 != 0xBF || h1 != 0xF2)
		{
			in.close();
			return false;
		}
		
		// Get image dimensions
		fntTexWidth = flipEndian(in.readInt());
		fntTexHeight = flipEndian(in.readInt());
		
		// Get cell dimensions
		fntCellWidth = flipEndian(in.readInt());
		fntCellHeight = flipEndian(in.readInt());	
		
		//Log.e("TexW", String.valueOf(fntTexWidth));
		//Log.e("TexH", String.valueOf(fntTexHeight));
		//Log.e("CelW", String.valueOf(fntCellWidth));
		//Log.e("CelH", String.valueOf(fntCellHeight));
		
		// The height and width values in the crop rect never change
		UVarray[2]=fntCellWidth;
		UVarray[3]=fntCellHeight;
		
		// Pre-calculate column count
		colCount = fntTexWidth / fntCellWidth;
		
		//Log.d(fontName, "fntTexWidth  : " + fntTexWidth);
		//Log.d(fontName, "fntTexHeight  : " + fntTexHeight);
		//Log.d(fontName, "fntCellWidth  : " + fntCellWidth);
		//Log.d(fontName, "fntCellHeight  : " + fntCellHeight);
		//Log.d(fontName, "colCount  : " + colCount);

		
		// Get colour depth
		BPP = in.readUnsignedByte();
				
		// Get base offset
		firstCharOffset = in.readUnsignedByte();
		
		// Read width information
		for(int wLoop = 0; wLoop < 256; ++wLoop)
		{
			charWidth[wLoop] = in.readUnsignedByte();
		}

		// Get bitmap
		int bitLen = (fntTexHeight * fntTexWidth) * (BPP / 8);
		byte bits[] = new byte[bitLen];
		
		in.read(bits, 0, bitLen);
		
		in.close();	
		
		
		// Flip bits and wrap in Bytebuffer for glTexImage2D
		ByteBuffer pix = ByteBuffer.allocate(bits.length);
		
		int lineLen = fntTexWidth * (BPP / 8);
		
		for(int lines = fntTexHeight-1; lines>0 ; --lines)
		{
			pix.put(bits, lines * lineLen, lineLen);
		}
		pix.position(0);
		// Place bitmap in texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		// Place bitmap in texture
		switch(BPP)
		{
		case 8: // Alpha channel info only
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, GL10.GL_ALPHA, fntTexWidth, fntTexHeight, 0, GL10.GL_ALPHA, GL10.GL_UNSIGNED_BYTE, pix);
			break;
			
		case 24: // RGB Texture
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, GL10.GL_RGB, fntTexWidth, fntTexHeight, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE, pix);
			break;
			
		case 32: // RGBA Texture
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, level, GL10.GL_RGBA, fntTexWidth, fntTexHeight, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pix);
			break;
		}
		
		
		// determine scale
		// "1"s should fit on the screen * the width of "1"
		charScale =  game.getWorld().mScreenWidth/(float)(charWidth[(int)"1".charAt(0)]*horitontalCount);
		
		
		//game.text =  "Time!        " + System.currentTimeMillis() + "\n";
		//game.text += "Screen Width:" +   game.getWorld().mScreenWidth + "\n";
		//game.text += "Char Width  :" +   (float)(charWidth[(int)"1".charAt(0)]) + "\n";
		//game.text += "CW x 31     :" +   (float)(charWidth[(int)"1".charAt(0)]*28.236) + "\n";
		//game.text += "charScale   :" +   charScale + "\n";
		//game.textviewHandler.post(game.updateTextView);
		
		
 		return true;
	}
	
	
	// Flip endian-ness of a 32 bit integer value. (Stupid Java, no cake for you)
	int flipEndian(int val)
	{
		return  (val >>> 24) | (val << 24) | ((val << 8) & 0x00FF0000) | ((val >> 8) & 0x0000FF00);
	}
	/*
	// Bodge to get unsigned byte values
	private int getUnsignedByteVal(byte val)
	{
		if(val < 0)
			return 256 + val;
		else
			return val;		
	}
	*/
	public void SetColor(float red, float green, float blue)
	{
		redVal = red;
		greenVal = green;
		blueVal = blue;
	}
	
	
	int tempWidth = 0;
	public int getStringWidth(String text)
	{
		//Log.d("TEXT", String.valueOf(colCount));
		
		tempWidth = 0;
		for(int index = 0 ; index != text.length(); ++index )
		{	
			
			// Add character width to offset for next glyph
			tempWidth += charWidth[(int)text.charAt(index)]*charScale*charWidthModifier;
		}
		return tempWidth;
	}
	
	
	
	
	int idx=0;
	float xOffset = 0;
	int index;
	
	// Print a line of text to screen at specified co-ords
	public void printAt(GL10 gl, String text, float x, float y, float[] rgba, Boolean leftJust, float fontScaleOffset)
	{
		// rely on z-ordering
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		// GL11ETX wont work with lighting on some devices!
		// Disable lighting
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		
		
		// Color
		if (rgba == null)
			gl.glColor4f(redVal, greenVal, blueVal, 1.0f);
		else
			gl.glColor4f(rgba[0], rgba[1], rgba[2], 1.0f);
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		

		// Loop through each character of the string
		for(idx = 0 ; idx != text.length(); ++idx )
		{
			if (leftJust)
				index = idx;
			else
				index = text.length() - idx - 1;
			
			// Calculate glyph position within texture
			glyph = (int)text.charAt(index) - firstCharOffset;
			col = glyph % colCount;
			row = (glyph / colCount) + 1;
			
			// Update the crop rect
			UVarray[0] = col*fntCellWidth;
			UVarray[1] = fntTexHeight - (row*fntCellHeight) - 1;
			
			// Sub character width to offset for next glyph
			if (!leftJust)
				x -= charWidth[glyph + firstCharOffset]*charScale*charWidthModifier*fontScaleOffset;
			
			// Set crop area
			((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray,0);
			// Draw texture
			((GL11Ext) gl).glDrawTexfOES(x,y,World.GLEX11HUD_Z_ORDER,fntCellWidth*charScale*fontScaleOffset,fntCellHeight*charScale*fontScaleOffset);
			
			// Add character width to offset for next glyph
			if (leftJust)
				x += charWidth[glyph + firstCharOffset]*charScale*charWidthModifier*fontScaleOffset;
		}
		/*
		game.text += text + "\n";
		game.text += x + ", " + y + "\n";
		game.text += "charWidthModifier: " + charWidthModifier + "\n";
		game.text += "fontScaleOffset: " + fontScaleOffset + "\n";
		game.text +=  "Screen Width:" +   game.getWorld().mScreenWidth + "\n";
		game.text += "Char Width  :" +   (float)(charWidth[(int)"1".charAt(0)]) + "\n";
		//game.text += "CW x 31     :" +   (float)(charWidth[(int)"1".charAt(0)]*28.236) + "\n";
		game.text += "charScale   :" +   charScale + "\n\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
		// Enable Lighting
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		// re enable depth
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}


	public void printOffsetAt(GL10 gl, String text, float x, float y, float[] basergba, float[] peakrgba, Boolean leftJust, float[] charOffset, int pixXOffset, int pixYOffset, float[] charWidthMod, float[] charHeightMod, float fontScaleOffset)
	{
		// rely on z-ordering
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		// GL11ETX wont work with lighting on some devices!
		// Disable lighting
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		

		if (charOffset == null)
			return;
		
		// reset
		xOffset = 0;
		
		// Setup base color
		
		if (basergba == null)
			gl.glColor4f(redVal, greenVal, blueVal, 1.0f);
		else if (basergba != null && peakrgba == null)
			gl.glColor4f(basergba[0], basergba[1], basergba[2], 1.0f);
		
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		
		
		//game.text = "";
		

		// Loop through each character of the string
		
		
		
		for(idx = 0 ; idx != text.length(); ++idx )
		{
			
			if (leftJust)
				index = idx;
			else
				index = text.length() - idx - 1;
			
			
			//game.text += (charOffset[index]*100) + "\n";
			
			// Calculate glyph position within texture
			glyph = (int)text.charAt(index) - firstCharOffset;
			col = glyph % colCount;
			row = (glyph / colCount) + 1;
			
			// Update the crop rect
			UVarray[0] = col*fntCellWidth;
			UVarray[1] = fntTexHeight - (row*fntCellHeight) - 1;
			
			// Color (peaks)
			if (basergba != null && peakrgba != null)
			{
				gl.glColor4f(
						basergba[0] - (basergba[0]-peakrgba[0])*charOffset[index], 
						basergba[1] - (basergba[1]-peakrgba[1])*charOffset[index], 
						basergba[2] - (basergba[2]-peakrgba[2])*charOffset[index], 
						1.0f);
			}
			
			// Sub character width to offset for next glyph
			if (!leftJust)
				xOffset -= charWidth[glyph + firstCharOffset]*charScale*charWidthModifier*charWidthMod[index]*fontScaleOffset;
			
			
			//game.text += text.charAt(idx) + "    ";
			
			/*
			game.text += String.valueOf(x + xOffset + (charOffset[index]*pixXOffset));
			game.text += "  ,  ";
			game.text += String.valueOf(y + (charOffset[index]*pixYOffset));
			game.text += "    ";
			game.text += String.valueOf(fntCellWidth*charScale*fontScaleOffset);
			game.text += "  ,  ";
			game.text += String.valueOf(fntCellHeight*charScale*charHeightMod[index]*fontScaleOffset);
			*/
			
			//game.text += String.valueOf(charOffset[index]);
			//game.text += "\n";
			//game.textviewHandler.post(game.updateTextView);
			
			// Set crop area
			((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray,0);
			// Draw texture
			((GL11Ext) gl).glDrawTexfOES(x + xOffset + (charOffset[index]*pixXOffset), y + (charOffset[index]*pixYOffset), World.GLEX11HUD_Z_ORDER, fntCellWidth*charScale*fontScaleOffset, fntCellHeight*charScale*charHeightMod[index]*fontScaleOffset);
			
			//game.text = charOffset[index] + "   "+pixXOffset + "\n";
			
			// Add character width to offset for next glyph
			if (leftJust)
				xOffset += charWidth[glyph + firstCharOffset]*charScale*charWidthModifier*charWidthMod[index]*fontScaleOffset;
		}
		
		/*
		game.text =  "Screen Width:" +   game.getWorld().mScreenWidth + "\n";
		game.text += "Char Width  :" +   (float)(charWidth[(int)"1".charAt(0)]) + "\n";
		//game.text += "CW x 31     :" +   (float)(charWidth[(int)"1".charAt(0)]*28.236) + "\n";
		game.text += "charScale   :" +   charScale + "\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		
		//game.textviewHandler.post(game.updateTextView);
		
		// Enable Lighting
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		// re enable
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
	}

	
	float U,V,U1,V1;
	// Calculate UV params
	int RowPitch;
	float ColFactor;
	float RowFactor;
	
	float cellWidth;
	float cellHeight;
	
	//Color[]  clr;
	
	public void print3D(GL10 gl, String text, float[] rgba, float xScale, float yScale)
	{
		/*
		if (clr == null)
		{
			clr = new Color[text.length()];
			Color rnd = Color.NONE;
			for(int index = 0 ; index != text.length(); ++index )
			{
				rnd = Color.pickColorExcept(rnd);
				clr[index] = rnd;
			}
		}
		*/
		gl.glPushMatrix();
		
		// Color
		if (rgba == null)
			gl.glColor4f(redVal, greenVal, blueVal, 1.0f);
		else
			gl.glColor4f(rgba[0], rgba[1], rgba[2], 1.0f);
		
		xPos = 0.0f;

		// Calculate quad size from scaling factors
		cellWidth = fntCellWidth * xScale;
		cellHeight = fntCellHeight * yScale;
		
		// Set the quad buffer
		vertices[3] = vertices[6] = cellWidth;
		vertices[7] = vertices[10] = cellHeight;
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		// Calculate UV params
		RowPitch=(fntTexWidth)/(fntCellWidth);
		ColFactor=((float)(fntCellWidth)/(float)(fntTexWidth+1));
		RowFactor=((float)(fntCellHeight)/(float)(fntTexHeight-1));
		
		// gl temp setup
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		 
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    gl.glActiveTexture(GL10.GL_TEXTURE0);
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
		
	    /*
	    game.text = text + "\n";
	    
	    game.text += fntCellWidth + "\n";
	    game.text += cellWidth + "\n";
	    game.text += fntCellHeight + "\n";
	    game.text += cellHeight + "\n";
	    game.text += "----------------------------\n";
	    */
	    
	    
		// Loop through each character of the string
		for(int index = 0 ; index != text.length(); ++index )
		{
			
			//gl.glColor4f(clr[index].ambient()[0], clr[index].ambient()[1], clr[index].ambient()[2], 1.0f);
			
			// Calculate glyph position within texture
			glyph = (int)text.charAt(index) - firstCharOffset;
			
		    row=(int) (glyph/RowPitch);
		    col=(int) (glyph-row*RowPitch);			
			
			// Set UV area
			U = col*ColFactor;
			V = 1.0f - (row*RowFactor);
			U1 = U+ColFactor;
			V1 = V-RowFactor;
			
			uvs[0] = U;
			uvs[1] = V1;
			
			uvs[2] = U1;
			uvs[3] = V1;
			
			uvs[4] = U1;
			uvs[5] = V;
			
			uvs[6] = U;
			uvs[7] = V;
			
	        texBuffer.put(uvs);
	        texBuffer.position(0);
	        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
			
			// Draw Quad
			gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
		
			// Add character width to offset for next glyph
			xPos = (xScale * charWidth[glyph + firstCharOffset]);
			gl.glTranslatef(xPos,0.0f,0.0f);
			//curX += (int)xPos;
			
			/*
			game.text += "Char: " + text.charAt(index) + " = " + glyph + "\n";
			game.text += "   " + row + ", " + col + "\n";
		    game.text += "   " + U + "\n";
		    game.text += "   " + V + "\n";
		    game.text += "   " + U1 + "\n";
		    game.text += "   " + V1 + "\n";
		    */
			
		}
		
		
		//game.textviewHandler.post( game.updateTextView );
		
		
		// gl tear down
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		gl.glPopMatrix();
	}

	
	
	
}


