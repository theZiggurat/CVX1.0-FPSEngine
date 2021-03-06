package engine.application.element.panel;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4i;
import engine.application.element.Element;
import engine.glapi.TextureObject;
import engine.glapi.vbo.Meshs;
import engine.application.layout.Box;
import engine.system.Config;
import engine.glapi.Shader;
import engine.system.Window;
import engine.utils.Color;

import static org.lwjgl.opengl.GL11.*;

public class Panel extends Element {

    private static PanelShader instance;
    private static PanelShader instance(){
        if (instance == null)
            instance = new PanelShader();
        return instance;
    }

    // defines color of panel in UI
    @Setter @Getter private Color color;

    @Setter @Getter private TextureObject imageBuffer;
    @Setter @Getter private boolean isImage = false;

    @Setter private boolean drawBorder;
    @Setter private Color borderColor;
    @Getter @Setter private int borderSize;

    @Getter @Setter private boolean scissor = true;

    // defines corner rounding for each corner in pixels
    // x: top-left, y: top-right, z: bottom-left, w: bottom-right
    @Setter @Getter private Vector4i rounding;

    public Panel(Color color, Vector4i rounding) {
        super();
        relativeBox = new Box(0,0,1,1);
        absoluteBox = new Box(0,0,1,1);
        this.color = color;
        this.rounding = rounding;

        drawBorder = false;
        borderColor = new Color(0x202020);
        borderSize = 0;
    }

    public Panel(){
        this(new Color(0x3f3f3f), new Vector4i(0,0,0,0));
    }

    @Override
    public void render(){

        glPolygonMode(GL_FRONT, GL_FILL);
        instance().bind();
        instance().updateUniforms(this);
        if (isScissor()) {
            Window.instance().setScissor(getAbsoluteBox());
            Meshs.posquad.render();
            super.render();
            Window.instance().disableScissor();
        } else {
            Meshs.posquad.render();
            super.render();
        }
        instance().unbind();

    }

    @Override
    public void cleanup(){
        super.cleanup();
    }

    private static class PanelShader extends Shader {

        public PanelShader(){
            super();

            createVertexShader("shaders/gui/panel_vs.glsl");
            createFragmentShader("shaders/gui/panel_fs.glsl");
            link();

            addUniform("box.x");
            addUniform("box.y");
            addUniform("box.width");
            addUniform("box.height");

            addUniform("resolution");
            addUniform("rounding");

            addUniform("color");
            addUniform("texture");
            addUniform("isTexture");
            addUniform("isDepth");
            addUniform("multisamples");

            addUniform("borderSize");
            addUniform("borderColor");
            addUniform("border");
        }

        public void updateUniforms(Panel e){

            Box box = e.getAbsoluteBox();

            setUniform("box.x", box.getX());
            setUniform("box.y", box.getY());
            setUniform("box.width", box.getWidth());
            setUniform("box.height", box.getHeight());

            setUniform("resolution", e.getAbsoluteBox().resolution());
            setUniform("rounding", e.getRounding());


            if(e.isImage){
                activeTexture(e.getImageBuffer(), 0);
                setUniform("texture", 0);
                setUniform("isTexture", 1);
                setUniform("isDepth", e.getImageBuffer().isDepth()? 1 : 0);
                setUniform("multisamples", e.getImageBuffer().isMultisample() ? Config.instance().getMultisamples() : 0);
            }
            else {
                setUniform("isTexture", 0);
                setUniform("color", e.getColor());
            }

            setUniform("border", e.drawBorder ? 1 : 0);
            setUniform("borderSize", e.borderSize);
            setUniform("borderColor", e.borderColor);
        }
    }

    public void setRounding(Vector4i rounding) {
        setRounding(rounding.x, rounding.y, rounding.z, rounding.w);
    }

    public void setRounding(int rounding){
        this.rounding.x = rounding;
        this.rounding.y = rounding;
        this.rounding.z = rounding;
        this.rounding.w = rounding;
    }

    public void setRounding(int r1, int r2, int r3, int r4){
        this.rounding.x = r1;
        this.rounding.y = r2;
        this.rounding.z = r3;
        this.rounding.w = r4;
    }
}
