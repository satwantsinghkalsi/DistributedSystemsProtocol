import java.util.LinkedList;
import java.util.Queue;

public class ConvergeCast {
	
	static int[] parent;
	
	public static int getParent(int id) {
		return parent[id];
	}
	
	static void createSpnningTree(int[][] adjMatrix){
		boolean[] visited = new boolean[adjMatrix.length];
		parent = new int[adjMatrix.length];
		int i=0;
		Queue<Integer> queue = new LinkedList<>();
		queue.add(0);
		parent[0] = 0;
		visited[0] = true;
		while(!queue.isEmpty()){
			int u = queue.remove();
			i=0;
			while(i<adjMatrix[u].length){
				if(adjMatrix[u][i] == 1 && visited[i] == false){
					queue.add(i);
					ConvergeCast.parent[i] = u;
					visited[i] = true;
				}
				i++;
			}
		}
	}

	
}
