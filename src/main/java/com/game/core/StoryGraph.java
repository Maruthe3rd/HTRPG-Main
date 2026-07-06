package com.game.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.dialogue.DialogueChoice;
import com.game.dialogue.DialogueNode;
import com.game.dialogue.DialogueTemplate;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the static branch graph for a single character by parsing its story
 * JSON and following every choice edge (within a file and across
 * {@code targetStoryFile} chapters, including a minigame's tiered outcomes).
 * Nodes are keyed globally as {@code storyFile#nodeId}, the same keys the
 * database uses for explored nodes, so the timeline map can overlay progress.
 */
public final class StoryGraph {

    public static final class Node {
        public final String key;
        public final String storyFile;
        public final String nodeId;
        public final String speaker;
        public final String text;
        public final boolean ending;
        public final String endingKey;
        public final List<String> targets = new ArrayList<>();
        public int depth;

        Node(String key, String storyFile, String nodeId, String speaker,
             String text, boolean ending, String endingKey) {
            this.key = key;
            this.storyFile = storyFile;
            this.nodeId = nodeId;
            this.speaker = speaker;
            this.text = text;
            this.ending = ending;
            this.endingKey = endingKey;
        }
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, DialogueTemplate> templateCache = new HashMap<>();
    private final String rootKey;

    private StoryGraph(String rootKey) {
        this.rootKey = rootKey;
    }

    public Collection<Node> nodes() { return nodes.values(); }
    public Node node(String key)    { return nodes.get(key); }
    public String rootKey()         { return rootKey; }
    public boolean isEmpty()        { return nodes.isEmpty(); }

    public static String key(String storyFile, String nodeId) {
        return storyFile + "#" + nodeId;
    }

    public static StoryGraph forCharacter(GameCharacter character) {
        String storyFile = character.prologueStoryFile();
        StoryGraph graph = new StoryGraph(key(storyFile, startNodeOf(storyFile)));
        DialogueTemplate template = load(storyFile);
        if (template != null && template.getStartNodeId() != null) {
            graph.buildFrom(storyFile, template.getStartNodeId());
            graph.computeDepths();
        }
        return graph;
    }

    private static String startNodeOf(String storyFile) {
        DialogueTemplate t = load(storyFile);
        return (t != null) ? t.getStartNodeId() : null;
    }

    private DialogueTemplate template(String storyFile) {
        return templateCache.computeIfAbsent(storyFile, StoryGraph::load);
    }

    private void buildFrom(String storyFile, String startNode) {
        Deque<String[]> queue = new ArrayDeque<>();
        queue.add(new String[]{storyFile, startNode});

        while (!queue.isEmpty()) {
            String[] cur = queue.poll();
            String sf = cur[0], nid = cur[1];
            String k = key(sf, nid);
            if (nodes.containsKey(k)) continue;

            DialogueTemplate t = template(sf);
            DialogueNode dn = (t != null && t.getNodes() != null) ? t.getNodes().get(nid) : null;
            if (dn == null) continue; // dangling reference — just leave it out of the map

            boolean ending = "SET_FLAG_AND_END".equals(dn.getTriggerAction());
            Node node = new Node(k, sf, nid, dn.getSpeaker(), dn.getText(),
                    ending, ending ? dn.getActionParameter() : null);
            nodes.put(k, node);

            if (ending || dn.getChoices() == null) continue;

            for (DialogueChoice ch : dn.getChoices()) {
                String destFile = (ch.getTargetStoryFile() != null) ? ch.getTargetStoryFile() : sf;
                String[] targets = {
                        ch.getTargetNodeId(),
                        ch.getTargetNodeIdHigh(),
                        ch.getTargetNodeIdMedium(),
                        ch.getTargetNodeIdLow()
                };
                for (String target : targets) {
                    if (target == null) continue;
                    String tk = key(destFile, target);
                    if (!node.targets.contains(tk)) node.targets.add(tk);
                    queue.add(new String[]{destFile, target});
                }
            }
        }
    }

    /** Shortest-path depth from the root, used as the column index in the map layout. */
    private void computeDepths() {
        if (!nodes.containsKey(rootKey)) return;
        Map<String, Integer> dist = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();
        dist.put(rootKey, 0);
        q.add(rootKey);
        while (!q.isEmpty()) {
            String k = q.poll();
            Node n = nodes.get(k);
            n.depth = dist.get(k);
            for (String t : n.targets) {
                if (nodes.containsKey(t) && !dist.containsKey(t)) {
                    dist.put(t, dist.get(k) + 1);
                    q.add(t);
                }
            }
        }
    }

    private static DialogueTemplate load(String storyFile) {
        try (InputStream in = StoryGraph.class.getResourceAsStream(storyFile)) {
            if (in == null) return null;
            return MAPPER.readValue(in, DialogueTemplate.class);
        } catch (Exception e) {
            return null;
        }
    }
}
