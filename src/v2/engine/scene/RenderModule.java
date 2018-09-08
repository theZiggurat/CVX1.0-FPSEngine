package v2.engine.scene;

import lombok.AllArgsConstructor;
import lombok.Getter;
import v2.engine.gldata.VertexBufferObject;
import v2.engine.system.ShaderProgram;

@Getter @AllArgsConstructor
public class RenderModule extends Module {

    /*
        Module with purpose of connecting OpenGL gldata calls to
        scenegraph nodes. Any node with this module will be able to
        attach a shader and VBO model.
     */

    private ShaderProgram shader;
    private VertexBufferObject mesh;

    @Override
    public void render() {

        shader.bind();
        shader.updateUniforms(getParent());
        mesh.render();
        shader.unbind();

    }

    public void cleanup() {

        mesh.cleanup();
        shader.cleanup();

    }

}