package com.learnopengles.android.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Loading in the texture from an image file
 */
public class TextureHelper
{
	public static int loadTexture(final Context context, final int resourceId)
	{
		// We first need to ask OpenGL to create a new handle for us. This handle serves as a unique identifier,
		// and we use it whenever we want to refer to the same texture in OpenGL.
		// The OpenGL method can be used to generate multiple handles at the same time; here we generate just one.
		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);
		
		if (textureHandle[0] != 0)
		{
			// Once we have a texture handle, we use it to load the texture.
			// First, we need to get the texture in a format that OpenGL will understand.
			// We can’t just feed it raw data from a PNG or JPG, because it won’t understand that.
			// The first step that we need to do is to decode the image file into an Android Bitmap object
			// By default, Android applies pre-scaling to bitmaps depending on the resolution of your device and
			// which resource folder you placed the image in. We don’t want Android to scale our bitmap at all,
			// so to be sure, we set inScaled to false.
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;	// No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

			// We then bind to the texture and set a couple of parameters. Binding to a texture tells
			// OpenGL that subsequent OpenGL calls should affect this texture. We set the default
			// filters to GL_NEAREST, which is the quickest and also the roughest form of filtering.
			// All it does is pick the nearest texel at each point in the screen,
			// which can lead to graphical artifacts and aliasing.
			// GL_TEXTURE_MIN_FILTER
			// 		This tells OpenGL what type of filtering to apply when drawing the texture smaller than the original size in pixels.
			// GL_TEXTURE_MAG_FILTER
			// 		This tells OpenGL what type of filtering to apply when magnifying the texture beyond its original size in pixels.

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Android has a very useful utility to load bitmaps directly into OpenGL. Once you’ve read in a resource into a Bitmap object
			// public static void texImage2D (int target, int level, Bitmap bitmap, int border)
			// We want a regular 2D bitmap, so we pass in GL_TEXTURE_2D as the first parameter.
			// The second parameter is for mip-mapping, and lets you specify the image to use at each level.
			// We’re not using mip-mapping here so we’ll put 0 which is the default level. We pass in
			// the bitmap, and we’re not using the border so we pass in 0.
			// We then call recycle() on the original bitmap, which is an important step to
			// free up memory. The texture has been loaded into OpenGL, so we don’t need to
			// keep a copy of it lying around. Yes, Android apps run under a Dalvik VM that
			// performs garbage collection, but Bitmap objects contain data that resides in
			// native memory and they take a few cycles to be garbage collected if you don’t
			// recycle them explicitly. This means that you could actually crash with an out of memory
			// error if you forget to do this, even if you no longer hold any references to the bitmap.

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();						
		}
		
		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}
		
		return textureHandle[0];
	}
}
