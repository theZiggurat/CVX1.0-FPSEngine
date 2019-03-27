package v2.engine.event;

import v2.engine.scene.Node;
import v2.engine.scene.RenderType;

import java.util.ArrayList;
import java.util.stream.Stream;

public class SelectionManager {

    private ArrayList<Node> selected;

    public SelectionManager(){
        selected = new ArrayList<>();
    }

    public void addSelection(Node node){
        node.setSelected(true);
        selected.add(node);
    }

    public void clear(){
        selected.forEach(e -> e.setSelected(false));
        selected.clear();
    }

    public Stream<Node> stream(){
        return selected.stream();
    }

    public void renderSelected(RenderType type){
        stream().forEach(e -> e.render(type));
    }

    public void renderSelected(RenderType type, Node.Condition condition){
        stream().forEach(e -> {if(condition.isvalid(e))e.render(type, condition);});
    }
}
