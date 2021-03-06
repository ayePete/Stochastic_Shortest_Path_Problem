/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.util.ArrayList;
import java.util.Random;
import com.rits.cloning.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javafx.util.Pair;


/**
 *
 * @author Peter
 */

class Data implements Comparable<Data> {
	public final int index;
	public final int priority;
 
 
	public Data(int index, int priority) {
		this.index = index;
		this.priority = priority;
	} 
 
 
	@Override 
	public int compareTo(Data other) {
		return Integer.valueOf(priority).compareTo(other.priority);
	} 
	 
	public boolean equals(Data other) {
		return priority == other.priority;
	} 
 
 
	// also implement equals() and hashCode() 
} 

public class Main {
    
    public static final int GRAPHSIZE = 5;
    public static final int EDGE_NO = 8;
    public static final int STARTNODE = 3;
    public static final int ENDNODE = 2;
    public static final int DP = 4;
    public static Cloner cloner = new Cloner();

    public static double c1 = 1.48;
    public static double c2 = 0.5;
    public static Random rand = new Random(500);
    public static ArrayList<Particle> swarm;
    public static ArrayList<Double> gbest;
    static int MAX_VERTS = 50000;
    public static Particle dummyParticle;
    public static ArrayList<ArrayList<ArrayList<Pair<Double, Integer>>>> GRAPH; // = new int[][]{
//        {0, 2, 4, 0, 0, 0, 0},
//        {2, 0, 0, 8, 3, 0, 0},
//        {4, 0, 0, 6, 0, 9, 0},
//        {0, 8, 6, 0, 5, 0, 10},
//        {0, 3, 0, 5, 0, 0, 7},
//        {0, 0, 9, 0, 0, 0, 2},
//        {0, 0, 0, 10, 7, 2, 0}
//    };

    public static void main(String[] args) {
        //GRAPH = randGraph();
        dummyParticle = new Particle();
        long startTime = System.currentTimeMillis();
        psoSPP();
        long endTime = System.currentTimeMillis();
        double totalTime = (endTime-startTime)/1000.0;
        double averageTime = totalTime/50.0;
        System.out.println("\nTotal time: " + totalTime);
        System.out.println("Average time: " + averageTime);
        
//        System.out.println("\nDijkstra: ");
//        startTime = System.currentTimeMillis();
//        System.out.println(dijkstra(GRAPH, STARTNODE, ENDNODE));
//        endTime = System.currentTimeMillis();
//        totalTime = (endTime-startTime)/1000.0;
//        averageTime = totalTime/50.0;
//        System.out.println("Total time: " + totalTime);
//        System.out.println("Average time: " + averageTime);
    }
    
    static void psoSPP(){
        int accuracy = 0;
        double r1, r2;
        ArrayList<String> gBests = new ArrayList<>();
        GRAPH = randGraph();
        for (int k = 0; k < 40; k++) {
            init();
            for (int i = 0; i < 50; i++) {
                for (Particle p : swarm) {
                    r1 = round(rand.nextDouble(), DP);
                    r2 = round(rand.nextDouble(), DP);

                    // Get differences between X and pBest and X and gBest
                    ArrayList<Double[]> gDiff = p.subtractPosition(gbest);
                    ArrayList<Double[]> pDiff = p.subtractPosition(p.getPBest());

                    // Get magnitude of each difference
                    int pDiffMagnitude = (int) round(c1 * r1 * pDiff.size(), 0);
                    int gDiffMagnitude = (int) round(c2 * r2 * gDiff.size(), 0);

                    // Generate new velocity
                    Double[] newPosition = new Double[GRAPHSIZE];
                    Arrays.fill(newPosition, -1.0);
                    for (int j = 0; j < gDiffMagnitude; j++) {
                        int index = rand.nextInt(gDiff.size());
                        Double[] diffVal = gDiff.get(index);
                        newPosition[diffVal[0].intValue()] = diffVal[1];
                        gDiff.remove(index);
                        if(gDiff.isEmpty())
                            break;
                    }
                    for (int j = 0; j < pDiffMagnitude; j++) {
                        int index = rand.nextInt(pDiff.size());
                        Double[] diffVal = pDiff.get(index);
                        int pDiffIndex = diffVal[0].intValue();
                        if (newPosition[pDiffIndex] == -1.0) {
                            newPosition[pDiffIndex] = diffVal[1];
                        }
                        pDiff.remove(index);
                        if(pDiff.isEmpty())
                            break;
                    }
                    ArrayList<Integer> emptyIndices = new ArrayList<>();
                    for (int j = 0; j < newPosition.length; j++) {
                        if (newPosition[j] < 0) {
                            emptyIndices.add(j);
                        }
                    }
                    
                    int prevVel = rand.nextInt(GRAPHSIZE) + 1;
                    int velCount = 0;
                    for (Integer j : emptyIndices) {
                        newPosition[j] = p.getVelocity().get(j);
                        velCount++;
                        if(emptyIndices.size() == GRAPHSIZE && velCount >= prevVel)
                            break;
                    }
                    for (int j = 0; j < newPosition.length; j++){
                        if(newPosition[j] < 0){
                            newPosition[j] = p.getPosition().get(j);
                        }
                    }
                    
                    double prevFitness = p.getPBestFitness();
                    ArrayList<Double> newPositionList = new ArrayList<>(Arrays.asList(newPosition));
                    p.setPosition(newPositionList);
                    p.generateVelocity();
                    if (p.getFitness() < prevFitness) {
                        p.updatePBest();
                    }
                }
                computeGBest();
            }
            Stack<Integer> bestPath = dummyParticle.decodePath(gbest);
            System.out.println("bestPat: " + bestPath);
            double pso = dummyParticle.getPathCost(bestPath);
            String output = "Path: " + bestPath + " Fitness: " + pso;
            System.out.println("PSO: " + pso);
            //int dijkstra = dijkstra(GRAPH, STARTNODE, ENDNODE);
                    
            //System.out.println("Dijkstra: " + dijkstra);
//            if(dijkstra == pso)
//                accuracy++;
            gBests.add(output);
        }
//        for(String out: gBests){
//            System.out.println(out);
//        }
           //System.out.println("Error count: " + errorCount);  
           System.out.println("\nAccuracy: " + (accuracy));
    }

    public static void printVelocity(ArrayList<Double[]> v) {
        System.out.print("[");
        for (int i = 0; i < v.size() - 1; i++) {
            System.out.print(v.get(i)[0] + "(" + v.get(i)[1] + "), ");
        }
        System.out.println(v.get(v.size() - 1)[0] + "(" + v.get(v.size() - 1)[1] + ")]");
    }

    private static void init() {
        GRAPH = randGraph();
        System.out.println(GRAPH);
        //System.out.println(Arrays.deepToString(GRAPH));
        swarm = new ArrayList<>();
        for (int i = 0; i < GRAPHSIZE/2; i++) {
            Particle p = new Particle(GRAPHSIZE);
            swarm.add(p);
        }
        computeGBest();
    }
    
    static double minFitness;

    private static void computeGBest() {
        boolean improvement = false;
        minFitness = gbest == null ? Double.MAX_VALUE : dummyParticle.getPathCost(dummyParticle.decodePath(gbest));
        int minIndex = 0;
        for (int i = 0; i < swarm.size(); i++) {
            Particle p = swarm.get(i);
            if (p.getFitness() < minFitness) {
                minFitness = p.getFitness();
                minIndex = i;
                improvement = true;
            }
        }
        if (improvement) {
            gbest = cloner.deepClone(swarm.get(minIndex).getPosition());
        }
        gbest = memeticSearch(gbest);
    }

    public static double round(double d, int numbersAfterDecimalPoint) {
        double n = Math.pow(10, numbersAfterDecimalPoint);
        double d2 = d * n;
        long lon = (long) d2;
        lon = ((long) (d2 + 0.5) > lon) ? lon + 1 : lon;
        return (lon) / n;
    }
    
    public static ArrayList<ArrayList<ArrayList<Pair<Double, Integer>>>> randGraph() {
        ArrayList<ArrayList<ArrayList<Pair<Double, Integer>>>> g = new ArrayList<>(GRAPHSIZE);
        for (int i = 0; i < GRAPHSIZE ; i++) {
            ArrayList<ArrayList<Pair<Double, Integer>>> row = new ArrayList<>();
            for (int j = 0; j < GRAPHSIZE; j++) {
                ArrayList<Pair<Double, Integer>> pairList = new ArrayList<>();
                pairList.add(new Pair(-1.0, 1));
                row.add(pairList);
            }
            g.add(row);
        }
        int edgeCount = 0;
        int counter = 0;
        int cost = 0;
        for (int i = 0; i < GRAPHSIZE; i++) {
            int j = rand.nextInt(GRAPHSIZE);
            if(i == j)
                continue;
            int distSize = 1 + rand.nextInt(5);
            ArrayList<Double> distribution = generateDistribution(distSize, 100);
            ArrayList<Pair<Double, Integer>> costProb = new ArrayList<>();
            for (int k = 0; k < distribution.size(); k++) {
                costProb.add(new Pair<>(distribution.get(k), 10 + rand.nextInt(1000)));
            }
            g.get(i).set(j, costProb);
            g.get(j).set(i, costProb);
            counter++;
            if (counter == EDGE_NO) {
                break;
            }
        }
        for (;;) {
            int i = rand.nextInt(GRAPHSIZE);
            int j = rand.nextInt(GRAPHSIZE);
            if (i == j) {
                continue;
            }
            if (g.get(i).get(j).get(0).getKey() < 0.0) {
                int distSize = 1 + rand.nextInt(5);
                ArrayList<Double> distribution = generateDistribution(distSize, 100);
                ArrayList<Pair<Double, Integer>> costProb = new ArrayList<>();
                for (int k = 0; k < distribution.size(); k++){
                    costProb.add(new Pair<>(distribution.get(k), 10 + rand.nextInt(1000)));
                }
                g.get(i).set(j, costProb);
                g.get(j).set(i, costProb);
                counter++;
            }
            if (counter == EDGE_NO) {
                break;
            }
        }
        return g;
    }
    
    static int dijkstra(int[][] G, int i, int j){
		//Get the number of vertices in G 
		int n = G.length;
		 
		/* ... Your code here ... */ 
		int[] distance = new int[G.length];
		PriorityQueue<Data> PQ = new PriorityQueue<>();
		boolean[] inTree = new boolean[G.length];
		 
		for (int index = 0; index < G.length; index++) {
			if (index == i) {
				distance[index] = 0;
			} 
			else { 
				distance[index] = Integer.MAX_VALUE;
                                
				PQ.add(new Data(index, distance[index]));
				inTree[index] = true; 
			} 
                        
		} 
		 
		for (int index = 0; index < G.length; index++) { // for each edge (v,z) do
			if (G[i][index] != 0) { // There is an edge
				if (distance[i] + G[i][index] < distance[index]) { // if D[v] + w((v,z)) < D[z] then 
					int oldIndex = distance[index];
					distance[index] = distance[i] + G[i][index]; // D[z] ← D[v] + w((v,z))  
                                        Data t = new Data(index, oldIndex);
					PQ.remove(new Data(index, oldIndex));
					PQ.add(new Data(index, distance[index])); // update PQ wrt D[z] 
				} 
			} 
		} 
		 
			 
		while (PQ.peek() != null) { // If PQ isn't empty
			Data vertex = PQ.poll(); // RemoveMin
			for (int index = 0; index < G.length; index++) { // for each edge (u,z) with z ∈ PQ do
				if (G[vertex.index][index] != 0 && inTree[index] == true) { // z ∈ PQ
					if (distance[vertex.index] + G[vertex.index][index] < distance[index]) { // if D[v] + w((v,z)) < D[z] then 
						int oldIndex = distance[index];
						distance[index] = distance[vertex.index] + G[vertex.index][index]; // D[z] ← D[v] + w((v,z)) 
						PQ.remove(new Data(index, oldIndex));
						PQ.add(new Data(index, distance[index])); // update PQ wrt D[z] 
					} 
				} 
			 
			} 
		} 
		if (distance[j] == Integer.MAX_VALUE || distance[j] < 0) {
			return -1; 
		} 
		else { 
			return distance[j];
		} 
    }
    private static ArrayList<Double> memeticSearch(ArrayList<Double> prevVal) {
        double min = minFitness;
        double gamma = 0.5;
        int prob = 0;
        ArrayList<Double> result = cloner.deepClone(prevVal);
        ArrayList<Double> prospective = cloner.deepClone(prevVal);
        ArrayList<Double> z = new ArrayList<>();
        for (int i = 0; i < GRAPHSIZE; i++) {
            z.add(rand.nextDouble() * 3);
        }
        //System.out.println("Previous: " + prevVal);
        
        for (int i = 0; i < 10; i++) {
            prevVal = (ArrayList) prospective.clone();
            int index = rand.nextInt(GRAPHSIZE);
            for (int j = 0; j < GRAPHSIZE; j++) {
                prob = rand.nextInt(2);
                //System.out.println(index);
                if(prob == 0)
                    gamma = gamma * -1;
                double newVal = prevVal.get(j) + gamma * z.get(j);
                if(newVal > 3.0)
                    newVal = 3.0;
                prospective.set(j, newVal);
            }
            //System.out.println(prospective);
            double fitness = dummyParticle.getPathCost(dummyParticle.decodePath(prospective));
            //System.out.println("Min: " + min + " MemFitness: " + fitness);
            if (fitness > min) {
                for (int j = 0; j < GRAPHSIZE; j++) {
                    z.set(j, rand.nextDouble() * 3);
                }
                gamma -= 0.01;
            }
            else if(fitness < min){
                min = fitness;
                System.out.println("Successfull memetic");
                result = cloner.deepClone(prospective);
            }
        }
        return result;
    }
    
    public static ArrayList<Double> generateDistribution(int size, int max){
        Set<Double> set = new TreeSet<>();
        set.add(0.0);
        while (set.size() < size) {
            set.add(Double.valueOf(1 + rand.nextInt(max)));
        }
        set.add(Double.valueOf(max));
        ArrayList<Double> output = new ArrayList<>();
        output.addAll(set);
        for (int i = 1; i < output.size(); i++) {
            double diff = output.get(i) - output.get(i-1);
            output.set(i-1, diff);
        }
        output.remove(output.size()-1);
        for (int i = 0; i < output.size(); i++) {
            double quotient = round(output.get(i)/max, 2);
            output.set(i, quotient);
        }
        return output;
    }
}


    
//    public static void dijkstra(){
//        Integer[][] distanceGraph = new Integer[GRAPHSIZE][GRAPHSIZE];
//        int start = STARTNODE;
//        int end = ENDNODE;
//        int cummCost = 0;
//        int currNode = start;
//        int nextNode = currNode;
//        int minTotalCost = 0;
//        while (true){
//            int minPartialCost = Integer.MAX_VALUE;
//            for (int i = 0; i < distanceGraph[nextNode].length; i++) {
//                int partialCost = minTotalCost + GRAPH[currNode][i];
//                if(partialCost < minPartialCost){
//                    minPartialCost = partialCost;
//                    nextNode = i;
//                }
//            }
//            minTotalCost = minPartialCost;
//            if (nextNode == end)
//                break;
//            currNode = nextNode;
//        }
//    }
