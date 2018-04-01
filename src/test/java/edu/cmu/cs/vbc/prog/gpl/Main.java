package edu.cmu.cs.vbc.prog.gpl;

import edu.cmu.cs.varex.annotation.VConditional;

import java.io.IOException;

/**
 * @author chupanw
 */
public class Main {


    public static void splStart___(){

    }

    public static void splPrint___(String s)
    {
        System.out.println(s);
    }

    public static void main( String[] args ) throws IOException {
        new Main();
        splStart___();
        mainBody___(args);
        splEnd___();
    }

    public static void splEnd___()
    {
    }

    public static void mainBody___(String[] args) throws IOException {
        // Step 1: create graph object
        Graph g = new  Graph();

        //TODO: cheating
        String args0 = "random4-gpl-benchmark.txt";
        String args1 = "v0";

        // Step 2: sets up the benchmark file to read
            splPrint___("[" + args0 + "]");
            g.runBenchmark( args0 );

            // Step 3: reads number of vertices, number of edges
            // and weights
            int num_vertices = 0;
            int num_edges = 0;
            int dummy = 0;

            num_vertices = g.readNumber();
            num_edges = g.readNumber();
            dummy = g.readNumber();
            dummy = g.readNumber();
            dummy = g.readNumber();

            // Step 4: reserves space for vertices, edges and weights
            Vertex V[] = new  Vertex[num_vertices];
            int weights[] = new int[num_edges];
            int startVertices[] = new int[num_edges];
            int endVertices[] = new int[num_edges];

            // Step 5: creates the vertices objects
            int i=0;
            for ( i=0; i<num_vertices; i++ ) {
                V[i] = new Vertex().assignName( "v"+i );
                g.addVertex( V[i] );
            }

            // Step 6:        reads the edges
            for( i=0; i<num_edges; i++ )
            {
                startVertices[i] = g.readNumber();
                endVertices[i] = g.readNumber();
            }

            // Step 7: reads the weights
            for( i=0; i<num_edges; i++ )
            {
                weights[i] = g.readNumber();
            }

            // Stops the benchmark reading
            g.stopBenchmark();

            // Step 8: Adds the edges
            for ( i=0; i<num_edges; i++ )
            {
                g.addAnEdge( V[startVertices[i]], V[endVertices[i]],weights[i] );
            }

            // Executes the selected features
        if (valid()) {
            Graph.startProfile();
            splPrint___("arg1: {" + args1 + "}");
            Vertex rootVertex = g.findsVertex(args1.trim());
            g.run(rootVertex);
            Graph.stopProfile();
            splPrint___("******************************************");
            splPrint___("<BASE___ graph>");
            g.display();
            Graph.resumeProfile();
        }

            // End profiling
            Graph.endProfile();
    }


    public static boolean GPL = true;
    public static boolean MainGpl = true;
    public static boolean Alg = true;
    public static boolean Gtp = true;

    @VConditional
    public static boolean Src =true;
    @VConditional
    public static boolean SrcProg =true;
    @VConditional
    public static boolean Src2 =true;
    @VConditional
    public static boolean BASE =true;
    @VConditional
    public static boolean DIRECTED =true;
    @VConditional
    public static boolean UNDIRECTED= !DIRECTED;
    @VConditional
    public static boolean WEIGHTED=true;
    @VConditional
    public static boolean SEARCH= true;
    @VConditional
    public static boolean BFS= false;
    @VConditional
    public static boolean DFS=true;
    @VConditional
    public static boolean NUMBER=true;
    @VConditional
    public static boolean CONNECTED= false;
    @VConditional
    public static boolean STRONGLYCONNECTED=true;
    @VConditional
    public static boolean TRANSPOSE=true;
    @VConditional
    public static boolean CYCLE=true;
    @VConditional
    public static boolean MSTPRIM= false;
    @VConditional
    public static boolean MSTKRUSKAL= false;
    @VConditional
    public static boolean SHORTEST=true;

    public static boolean valid() {
        return GPL  &&  (!GPL  ||  MainGpl)  &&  (!MainGpl  ||  GPL)  &&  (!MainGpl  ||  Alg)  &&  (!MainGpl  ||  Gtp)  &&  (!MainGpl  ||  BASE)  &&  (!Alg  ||  MainGpl)  &&  (!Src  ||  MainGpl)  &&  (!WEIGHTED  ||  MainGpl)  &&  (!Gtp  ||  MainGpl)  &&  (!BASE  ||  MainGpl)  &&  (!Alg  ||  NUMBER  ||  CONNECTED  ||  TRANSPOSE  ||  STRONGLYCONNECTED  ||  CYCLE  ||  MSTPRIM  ||  MSTKRUSKAL  ||  SHORTEST)  &&  (!NUMBER  ||  Alg)  &&  (!CONNECTED  ||  Alg)  &&  (!TRANSPOSE  ||  Alg)  &&  (!STRONGLYCONNECTED  ||  Alg)  &&  (!CYCLE  ||  Alg)  &&  (!MSTPRIM  ||  Alg)  &&  (!MSTKRUSKAL  ||  Alg)  &&  (!SHORTEST  ||  Alg)  &&  (!Src  ||  SrcProg)  &&  (!SrcProg  ||  Src)  &&  (!SrcProg  ||  Src2)  &&  (!SrcProg  ||  SEARCH)  &&  (!Src2  ||  SrcProg)  &&  (!SEARCH  ||  SrcProg)  &&  (!Src2  ||  BFS  ||  DFS)  &&  (!BFS  ||  Src2)  &&  (!DFS  ||  Src2)  &&  (!BFS  ||  !DFS)  &&  (!Gtp  ||  DIRECTED  ||  UNDIRECTED)  &&  (!DIRECTED  ||  Gtp)  &&  (!UNDIRECTED  ||  Gtp)  &&  (!DIRECTED  ||  !UNDIRECTED)  &&  (!NUMBER  ||  Src)  &&  (!CONNECTED  ||  UNDIRECTED)  &&  (!CONNECTED  ||  Src)  &&  (!STRONGLYCONNECTED  ||  DIRECTED)  &&  (!STRONGLYCONNECTED  ||  DFS)  &&  (!CYCLE  ||  DFS)  &&  (!MSTKRUSKAL  ||  UNDIRECTED)  &&  (!MSTKRUSKAL  ||  WEIGHTED)  &&  (!MSTPRIM  ||  UNDIRECTED)  &&  (!MSTPRIM  ||  WEIGHTED)  &&  (!MSTKRUSKAL  ||  !MSTKRUSKAL  ||  !MSTPRIM)  &&  (!SHORTEST  ||  DIRECTED)  &&  (!SHORTEST  ||  WEIGHTED)  &&  (!STRONGLYCONNECTED  ||  TRANSPOSE);
    }
}
