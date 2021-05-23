package com.efonian.cassandra.util;

import com.efonian.cassandra.misc.ColouredVertex;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.HierholzerEulerianCycle;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jungrapht.visualization.VisualizationImageServer;
import org.jungrapht.visualization.decorators.EdgeShape;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;

import java.awt.*;
import java.awt.image.BufferedImage;

// some methods are outdated
public final class UtilGraph {
    // Source: https://www.baeldung.com/jgrapht (outdated)
    public static <T, G extends DefaultEdge> void generateCompleteGraph(Graph<T, G> graph, int size) {
        CompleteGraphGenerator<T, G> completeGenerator
                = new CompleteGraphGenerator<>(size);
        completeGenerator.generateGraph(graph);
    }
    
//    public static BufferedImage renderString(Graph<String, ? extends DefaultEdge> g) {
//        JGraphXAdapter<String, ? extends DefaultEdge> graphAdapter = new JGraphXAdapter<>(g);
//        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
//        layout.execute(graphAdapter.getDefaultParent());
//        return mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
//    }
    
    public static <T extends DefaultEdge> BufferedImage render(Graph<String, T> graph) {
        return render(graph, new CircleLayoutAlgorithm<>());
    }
    
    public static <T, G extends DefaultEdge> BufferedImage render(Graph<T, G> graph, LayoutAlgorithm<T> layoutAlgorithm) {
        VisualizationImageServer<T, G> vv = VisualizationImageServer.builder(graph)
                .layoutAlgorithm(layoutAlgorithm)
                .layoutSize(new Dimension(900, 900))
                .viewSize(new Dimension(600, 600))
                .build();
        
        vv.getRenderContext().setVertexLabelFunction(Object::toString);
        vv.getRenderContext().setEdgeLabelFunction(Object::toString);
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
        
        return UtilImage.toBufferedImage(vv.getFullImage());
    }
    
    public static <T, G extends DefaultEdge> BufferedImage renderWithColouredVertices(Graph<ColouredVertex<T>, G> graph, LayoutAlgorithm<ColouredVertex<T>> layoutAlgorithm) {
        VisualizationImageServer<ColouredVertex<T>, G> vv = VisualizationImageServer.builder(graph)
                .layoutAlgorithm(layoutAlgorithm)
                .layoutSize(new Dimension(900, 900))
                .viewSize(new Dimension(600, 600))
                .build();
        
        vv.getRenderContext().setVertexLabelFunction(Object::toString);
        vv.getRenderContext().setEdgeLabelFunction(Object::toString);
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
        vv.getRenderContext().setVertexFillPaintFunction(ColouredVertex::getColor);
        
        return UtilImage.toBufferedImage(vv.getFullImage());
    }
    
    public static <T> GraphPath<T, DefaultEdge> shortestPathDijkstra(Graph<T, DefaultEdge> graph, T source, T sink) {
        DijkstraShortestPath<T, DefaultEdge> dijkstraShortestPath
                = new DijkstraShortestPath<>(graph);
        return dijkstraShortestPath.getPath(source, sink);
    }
    
    public static <T> GraphPath<T, DefaultEdge> shortestPathBellmanFord(Graph<T, DefaultEdge> graph, T source, T sink) {
        BellmanFordShortestPath<T, DefaultEdge> bellmanFordShortestPath
                = new BellmanFordShortestPath<>(graph);
        return bellmanFordShortestPath.getPath(source, sink);
    }

//    public static <T> List<GraphPath<T, DefaultEdge>> stronglyConnectedElements(DirectedGraph<T, DefaultEdge> directedGraph) {
//        StrongConnectivityAlgorithm<T, DefaultEdge> scAlg
//                = new KosarajuStrongConnectivityInspector<>(directedGraph);
//        List<DirectedSubgraph<T, DefaultEdge>> stronglyConnectedSubgraphs
//                = scAlg.stronglyConnectedSubgraphs();
//        List<T> stronglyConnectedVertices
//                = new ArrayList<>(stronglyConnectedSubgraphs.get(3)
//                .vertexSet());
//
//        T randomVertex1 = stronglyConnectedVertices.get(0);
//        T randomVertex2 = stronglyConnectedVertices.get(3);
//        AllDirectedPaths<T, DefaultEdge> allDirectedPaths
//                = new AllDirectedPaths<>(directedGraph);
//
//        return allDirectedPaths.getAllPaths(
//                randomVertex1, randomVertex2, false,
//                stronglyConnectedVertices.size());
//    }
    
    public static <T> GraphPath<T, DefaultEdge> eulerianCircuit(Graph<T, DefaultEdge> graph) {
        HierholzerEulerianCycle<T, DefaultEdge> eulerianCycle
                = new HierholzerEulerianCycle<>();
        return eulerianCycle.getEulerianCycle(graph);
    }

//    public static <T> List<T> hamiltonianCircuit(SimpleWeightedGraph<T, DefaultEdge> completeGraph) {
//        return HamiltonianCycle
//                .getApproximateOptimalForCompleteGraph(completeGraph);
//    }
//
//    public static <T> Set<T> findCycles(DirectedGraph<T, DefaultEdge> directedGraph) {
//        CycleDetector<T, DefaultEdge> cycleDetector
//                = new CycleDetector<>(directedGraph);
//
//        return cycleDetector.findCycles();
//    }
}
