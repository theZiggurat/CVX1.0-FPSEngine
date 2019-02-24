package v2.engine.gldata.vbo;

import org.joml.Vector3f;
import v2.engine.utils.AssimpLoader;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Meshs {

    /** TODO:
     *         Rework into primitive generator
     */

    private final static String path = "res/models/primitives/";

    public final static Mesh3D sphere = AssimpLoader.loadMeshGroup(path + "sphere.obj").get(0);
    public final static Mesh3D quad = AssimpLoader.loadMeshGroup(path + "quad.obj").get(0);
    public final static Mesh3D cube = AssimpLoader.loadMeshGroup(path + "cube.obj").get(0);
    public final static Mesh3D dome = AssimpLoader.loadMeshGroup(path + "dome.obj").get(0);
    public static Mesh3DLine line = new Mesh3DLine();

    static{
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);
        indices.add(1);

        ArrayList<Vector3f> positions = new ArrayList<>();
        positions.add(new Vector3f(0f,0f,0f));
        positions.add(new Vector3f(1f,1f,1f));

        line.setIndices(indices);
        line.setPositions(positions);
        line.bind();

    }


}