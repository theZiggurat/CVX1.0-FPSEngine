package engine.system;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import engine.application.layout.Box;
import engine.utils.ImageLoader;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.ARBMultisample.GL_MULTISAMPLE_ARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    @Getter
    private int height, width;
    @Getter
    private long handle;
    @Getter
    @Setter
    private boolean resized = false;
    @Getter
    private String title;
    private String spec_title;

    private static Window instance;

    public static Window instance() {
        if (instance == null) {
            instance = new Window();
        }
        return instance;
    }

    public Window() {
        this.title = Config.instance().getWindowName();
        this.height = Config.instance().getWindowHeight();
        this.width = Config.instance().getWindowWidth();
        spec_title = title;
    }

    /**
     * Makes necessary GLFW calls to instantiate a system window
     * with openGL context.
     *
     * @throws IllegalStateException Unable to initialize GLFW
     * @throws RuntimeException      Failed to create GLFW window
     */
    public void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GL_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        handle = glfwCreateWindow(width, height, title, 0, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            AppContext.instance().getRoot().forceLayout();
        });

        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                close();
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(handle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2);


        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwShowWindow(handle);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glPolygonMode(GL_FRONT_FACE, GL_FILL);

        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glLineWidth(1);

        //setIcon("res/images/icon.png");

        if(Config.instance().getMultisamples() > 0){
            glEnable(GL_MULTISAMPLE_ARB);
        }
    }

    public void update() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
        glfwSetWindowTitle(handle, spec_title);

        if(lock)
            setCursorPos(lockedcursorPos);

        glfwSetWindowTitle(handle, "" + Core.runtime().getCurrentFPS());

    }

    /**
     * Window poll for Core.loop()
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void close() {
        glfwSetWindowShouldClose(getHandle(), true);
    }

    /**
     * 32 x 32 window icon
     *
     * @param path image path with format "res/image/*"
     */
    public void setIcon(String path) {
        GLFWImage.Buffer images = GLFWImage.malloc(1);
        ByteBuffer buffer = ImageLoader.loadImage(path);

        GLFWImage icon = GLFWImage.malloc();
        icon.set(32, 32, buffer);

        images.put(0, icon);
        glfwSetWindowIcon(handle, images);
    }

    long cursor = 0;

    public void setCursor(int arrow){
        long cursor = glfwCreateStandardCursor(arrow);
        if(this.cursor!=0)glfwDestroyCursor(this.cursor);
        this.cursor = cursor;
        glfwSetCursor(handle, cursor);
    }


    @Getter boolean hidden = false;
    public void hideCursor(boolean hide) {
        this.hidden = hide;
        if (hide)
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        else
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    boolean lock = false;
    @Getter Vector2d lockedcursorPos = new Vector2d(0,0);
    public void lockCursor(boolean lock) {
        this.lock = lock;
        if (lock)
            lockedcursorPos = getCursorPos();
    }

    public void setCursorPos(Vector2d pos){
        glfwSetCursorPos(handle, pos.x, getHeight()-pos.y);
    }

    public Vector2d getCursorPos(){
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(handle, x, y);

        return new Vector2d(x.get(), getHeight()-y.get());
    }

    public Vector2f getCursorPosf(){
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(handle, x, y);

        return new Vector2f((float)x.get()/getWidth(), 1-(float)(y.get()/getHeight()));
    }

    public void setBlending(boolean blending){
        if(blending)
            glEnable(GL_BLEND);
        else
            glDisable(GL_BLEND);
    }

    public void setScissor(Box box) {
        glEnable(GL_SCISSOR_TEST);
        Vector4i pix = box.pixelDimensions(getResolution());
        glScissor(pix.x, pix.y, pix.z + 2, pix.w + 2);
    }

    public void disableScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public Vector2i getResolution(){
        return new Vector2i(width, height);
    }

    public void resizeViewport(Vector2i resolution){
        glViewport(0,0, resolution.x, resolution.y);
    }

    public void resetViewport(){
        glViewport(0,0, getWidth(), getHeight());
    }
}
