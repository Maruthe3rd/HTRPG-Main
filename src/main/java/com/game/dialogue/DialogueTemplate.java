package com.game.dialogue;

import java.util.List;
import java.util.Map;

public class DialogueTemplate {
    private String sceneId;
    private String startNodeId;
    private Map<String, DialogueNode> nodes;

    public String getSceneId() { return sceneId; }
    public void setSceneId(String sceneId) { this.sceneId = sceneId; }

    public String getStartNodeId() { return startNodeId; }
    public void setStartNodeId(String startNodeId) { this.startNodeId = startNodeId; }

    public Map<String, DialogueNode> getNodes() { return nodes; }
    public void setNodes(Map<String, DialogueNode> nodes) { this.nodes = nodes; }
}

