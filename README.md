    Thanks !
source form :  https://github.com/learnopengles/Learn-OpenGLES-Tutorials
第一讲 环境
使用EGL ,需要 GLSurfaceView, GLSurfaceView.Renderer
    mGLSurfaceView = new GLSurfaceView(this);
    mGLSurfaceView.setEGLContextClientVersion(2);
    mGLSurfaceView.setRenderer();
    其中，GLSurfaceView.Renderer有三个生命周期的回调
    做初始化               public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    surface 变化时         public void onSurfaceChanged(GL10 glUnused, int width, int height)
    处理每一帧的数据在      public void onDrawFrame(GL10 glUnused)

坐标数据，颜色数据，存储在buffer里进行传输

vertex and fragment shader
    vertex 处理每一个顶点的数据，坐标、颜色等
    fragment 处理每一个像素的数据
  下面这个vertex shader:
    final String vertexShader =
        "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.

      + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
      + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.

      + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.

      + "void main()                    \n"		// The entry point for our vertex shader.
      + "{                              \n"
      + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader.
                                                // It will be interpolated across the triangle.
      + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
      + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
      + "}                              \n";    // normalized screen coordinates.
    定义了2个变量 position and color ,会从传入的buffer(上面定义的坐标颜色buffer)里得到数据
    fragment shader 会从vertex shader里拿到v_Color参数，
    final String fragmentShader =
        "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                                                // precision in the fragment shader.
      + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                                                // triangle per fragment.
      + "void main()                    \n"		// The entry point for our fragment shader.
      + "{                              \n"
      + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.
      + "}                              \n";

下面就是加载shader: vertex and fragment shader
    int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    if (vertexShaderHandle != 0)
    {
        // Pass in the shader source.
        GLES20.glShaderSource(vertexShaderHandle, vertexShader);

        // Compile the shader.
        GLES20.glCompileShader(vertexShaderHandle);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0)
        {
//				String info = GLES20.glGetShaderInfoLog(vertexShaderHandle);
            GLES20.glDeleteShader(vertexShaderHandle);
            vertexShaderHandle = 0;
        }
    }
    新建一个shader,如果success,就会拿到一个id,接着设置shader source,就是上面的vertexShader,
    then we compile it. We can obtain the status from OpenGL and see if it compiled successfully.
在一个工程里Linka vertex and fragment shader
    // Create a program object and store the handle to it.
    int programHandle = GLES20.glCreateProgram();

    if (programHandle != 0)
    {
        // Bind the vertex shader to the program.
        GLES20.glAttachShader(programHandle, vertexShaderHandle);

        // Bind the fragment shader to the program.
        GLES20.glAttachShader(programHandle, fragmentShaderHandle);

        // Bind attributes
        GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

        // Link the two shaders together into a program.
        GLES20.glLinkProgram(programHandle);

        // Get the link status.
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

        // If the link failed, delete the program.
        if (linkStatus[0] == 0)
        {
            GLES20.glDeleteProgram(programHandle);
            programHandle = 0;
        }
    }
linkProgram ,This is what connects the output of the vertex shader with the input of the fragment shader.

在onDrawFrame里，画出图像
    // buffer指向第一个位置
    aTriangleBuffer.position(mPositionOffset);
    // 传入 位置 buffer  mPositionHandle是上面获取到的shader里的参数，like this :
    // mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
    GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer);
    // enable the vertex attribute and move on to the next attribute
    GLES20.glEnableVertexAttribArray(mPositionHandle);
    画图时主要就是传参数了

第二讲 把上一讲中三角形换成方形,另外画了一个 点,模拟光源
    // Pass in the light position in eye space.
    GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

第三讲 在第二讲的基础上，给每一个vertix都增加了光源
第四讲 纹理
    读取raw资源文件(xxx.glsl)工具类：RawResourceReader.readTextFileFromRawResource
    使用纹理:
    // Set the active texture unit to texture unit 0.
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

    // Bind the texture to this unit.
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

    // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
    GLES20.glUniform1i(mTextureUniformHandle, 0);

第五讲 blending modes
    作者又提供了一个工具类 ShapeBuilder.generateCubeData,生成点的数据
        // We turn off the culling of back faces, because if a cube is translucent,
		// then we can now see the back sides of the cube. We should draw them otherwise
		// it might look quite strange. We turn off depth testing for the same reason.
		if (mBlending)
		{
			// No culling of back faces
			GLES20.glDisable(GLES20.GL_CULL_FACE);

			// No depth testing
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);

			// Enable blending
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
		}
		else
		{
			// Cull back faces
			GLES20.glEnable(GLES20.GL_CULL_FACE);

			// Enable depth testing
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);

			// Disable blending
			GLES20.glDisable(GLES20.GL_BLEND);
		}

第六讲 Texture Filtering
    更改Texture Filtering:
    filter = GLES20.GL_NEAREST;
             GLES20.GL_LINEAR;
             GLES20.GL_NEAREST_MIPMAP_NEAREST;
             GLES20.GL_NEAREST_MIPMAP_LINEAR;
             GLES20.GL_LINEAR_MIPMAP_NEAREST;
             GLES20.GL_LINEAR_MIPMAP_LINEAR;
    OpenGL has two parameters that can be set:
             GL_TEXTURE_MIN_FILTER
             GL_TEXTURE_MAG_FILTER
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGrassDataHandle);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, filter);

    支持手势旋转
    Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
    Matrix.rotateM(mCurrentRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f);

第七讲  vertex buffer objects (VBOs)  (GPU-dedicated memory)
 The main difference is that there is an additional step to upload the data into graphics memory,
 and an additional call to bind to this buffer when rendering.
    GlSurfaceView.queueEvent(); // Run on the GL thread

    To upload data to the GPU, we need to follow the same steps in creating a client-side buffer as before:
    first: Transferring data from the Java heap to the native heap is then a matter of a couple calls
        cubePositionsBuffer = ByteBuffer.allocateDirect(cubePositions.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder()).asFloatBuffer(); //
        cubePositionsBuffer.put(cubePositions).position(0);

    Rendering with client-side buffers is straightforward to setup.
    We just need to enable using vertex arrays on that attribute, and pass a pointer to our data:
        // Pass in the position information
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE,
        GLES20.GL_FLOAT, false, 0, mCubePositions);

        mPositionHandle:    The OpenGL index of the position attribute of our shader program.
        POSITION_DATA_SIZE: How many elements (floats) define this attribute.
        GL_FLOAT:           The type of each element.
        false:              Should fixed-point data be normalized? Not applicable since we are using floating-point data.
        0:                  The stride. Set to 0 to mean that the positions should be read sequentially.
        mCubePositions: T   he pointer to our buffer, containing all of the positional data.

    Using separate buffers
        positions = X,Y,Z, X, Y, Z, X, Y, Z, …
        colors = R, G, B, A, R, G, B, A, …
        textureCoordinates = S, T, S, T, S, T, …

    Using a packed buffer
        buffer = X, Y, Z, R, G, B, A, S, T, …

    second:
    // generate as many buffers as we need.
    // This will give us the OpenGL handles for these buffers.
    final int buffers[] = new int[3];
    GLES20.glGenBuffers(3, buffers, 0);

    // Bind to the buffer. Future commands will affect this buffer specifically.
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

    // Transfer data from client memory to the buffer.
    // We can release the client memory after this call.
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubePositionsBuffer.capacity() * BYTES_PER_FLOAT,
        cubePositionsBuffer, GLES20.GL_STATIC_DRAW);

    // IMPORTANT: Unbind from the buffer when we're done with it.
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    glBufferData参数:
        GL_ARRAY_BUFFER: This buffer contains an array of vertex data.
        cubePositionsBuffer.capacity() * BYTES_PER_FLOAT: The number of bytes this buffer should contain.
        cubePositionsBuffer: The source that will be copied to this vertex buffer object.
        GL_STATIC_DRAW: The buffer will not be updated dynamically.

第八讲 index buffer objects  减少冗余数据
    类似第一讲里面,点和颜色数据在一起，复用重复的数据
