package hw_one;
import java.io.*;
import java.util.*;

//empty spot denoted by -1
//filepath on my machine: C:\Users\benne\Google Drive\Laptop Sync\UTD\Old\Fall 2022\CS 4365\Homeworks\HW1\src\input.txt


class Optimal_PuzzleState_Tree_Traversal {
	public static void main(String[] args) {
		//set up file input
		Scanner userIn = new Scanner(System.in);
		System.out.print("Please paste the file path:");
		String filePath = userIn.nextLine();
		System.out.println(filePath);
		File inFile = new File(filePath);
		Scanner inputFile;

		//read in the starting state from a given input file- the empty space is represented by *, so when parseInt fails we know the space is supposed to be empty
		try {
			inputFile = new Scanner(inFile);
			int[] input_init = new int[9];
			for(int i = 0; i < 9; i++) {
				try {
					input_init[i] = Integer.parseInt(inputFile.next());
				}
				catch(NumberFormatException e) {
					input_init[i] = -1;
				}
			}

			//initialize requisite variables
			final int[] goal = {7,8,1,6,-1,2,5,4,3};
			Node initialNode = new Node(input_init);
			Node goalNode = new Node(goal);
			int max_depth = 10;
			
			//prompt the user for their desired algorithm
			System.out.println("Please enter the algorithm:\n1- depth-first search\n2- iterative deepening\n3- A* algorithm");
			int algo = userIn.nextInt();
			
			//the most basic depth first search tree implementation- constructs the tree, then searches it up to a maximum depth and prints the resulting path to the goal
			if(algo == 1) {
				try {
					DFStree test1 = new DFStree(initialNode);
					test1.buildDFSTree(goalNode, max_depth);
					LinkedList<Node> test1l = test1.dfs(goalNode, max_depth);
					printBackList(test1l);
					System.out.println("States Enqueued:" + test1.stateEnq);
				}
				catch(NullPointerException e) {
					System.out.println("No goal state found");
				}
			}
			//repeatedly calls DFS starting with a restricted depth and gradually increases it until the goal is found
			else if(algo == 2) {
				try {
					//initialize limit, count of enqueued states, and list of path to goal
					int limit = 0;
					int enq = 0;
					LinkedList <Node> test2l = null;

					//while the goal is not found and the maximum depth has not been reached, build and search a DFStree, incrementing the depth limit by 1 each time
					do {
						DFStree test2 = new DFStree(initialNode);
						limit++;
						test2.buildDFSTree(goalNode, limit);
						test2l = test2.dfs(goalNode, limit);
						enq += test2.stateEnq;
					}while(test2l == null && limit < max_depth);

					printBackList(test2l);
					System.out.println("States Enqueued:" + enq);
				}
				//if depth limit is hit before goal is found, printbacklist will throw a nullpointerexception, indicating lack of path to goal
				catch(NullPointerException e) {
					System.out.println("No Goal State Found");
				}
			}
			//Implementation of A* Search Algorithm
			else if(algo == 3) {
				//prompt user for heuristic selection
				System.out.println("Please select heuristic 1 (Manhattan Distance) or 2 (# of Misplaced) by typing 1 or 2.");
				int heur = userIn.nextInt();

				//if an invalid value is selected for the heuristic, indicate and exit
				if(heur != 1 && heur != 2){
					System.out.println("Please enter a valid value for heuristic selection.");
					userIn.close();
					return;
				}

				//initialize all necessary variables for A* search
				int counter = 0;
				AstarNode astInit = new AstarNode(initialNode, AstarNode.calcDist(initialNode, goalNode, heur));
				PriorityQueue<AstarNode> frontier = new PriorityQueue<>();
				Stack<AstarNode> expanded = new Stack<>();

				//A*'s efficency comes from the use of the distance metric to try to only expand (i.e. generate children for) the nodes 'closest' to the goal
				//start by adding the initial AstarNode to the 'frontier' (Priority Queue, the reason why AstarNode implements comparable)
				frontier.add(astInit);

				//the 'distance' between the goal and itself is zero by definition, so while the goal state is not enqueued (when it is, it will automatically be first in queue)
				while(frontier.peek().distance != 0) {
					//pop the closest node to the goal off the frontier, push it to the stack, and expand it
					AstarNode temp = frontier.remove();
					expanded.push(temp);
					AstarNode[] tempch = AstarNode.aChildrenGen(temp, goalNode, heur);
					//for all of the possible children
					for(int i = 0; i < 4; i++)
						//if the child exists, add it to the frontier
						if(tempch[i] != null) {
							frontier.add(tempch[i]);
							//the stack allows for the tracking of the path itself
							//iterate through the stack, if a given state in the stack reappears at the front of the frontier, there must be a shorter path through that state
							//therefore pop the stack until everything at that point is removed
							for(int j = 0; j < expanded.size() ; j++)
								if(!expanded.elementAt(j).node.isDifferentState(frontier.peek().node))
									for(int k = 0; k < j; k++)
										expanded.pop();								
						}
					counter++;
				}

				//finally, grab the enqueued goal state and push it to the stack
				expanded.push(frontier.remove());
				LinkedList<Node> printList = new LinkedList<>();
				while(expanded.peek().node.isDifferentState(initialNode)) {
					printList.add(expanded.pop().node);
				}
				printList.add(expanded.pop().node);
				printBackList(printList);

				System.out.println("States Enqueued:" + counter);

				/*Old Version using separate function for the two different heuristics
				//Manhattan Distance
				if(heur == 1) {
					int counter = 0;
					AstarNode astInit = new AstarNode(initialNode, AstarNode.manndist(initialNode, goalNode));
					PriorityQueue<AstarNode> frontier = new PriorityQueue<>();
					Stack<AstarNode> expanded = new Stack<>();
					frontier.add(astInit);
					while(frontier.peek().distance != 0) {
						AstarNode temp = frontier.remove();
						expanded.add(temp);
						AstarNode[] tempch = AstarNode.a1ChildrenGen(temp, goalNode);
						for(int i = 0; i < 4; i++)
							if(tempch[i] != null) {
								frontier.add(tempch[i]);
								for(int j = 0; j < expanded.size() ; j++)
									if(!expanded.elementAt(j).node.isDifferentState(frontier.peek().node))
										for(int k = 0; k < j; k++)
											expanded.pop();								
							}
						counter++;
					}
					expanded.push(frontier.remove());
					LinkedList<Node> printList = new LinkedList<>();
					while(expanded.peek().node.isDifferentState(initialNode)) {
						printList.add(expanded.pop().node);
					}
					printList.add(expanded.pop().node);
					printBackList(printList);
					
					System.out.println("States Enqueued:" + counter);
				}
				//Raw number of incorrectly placed nodes
				else if(heur == 2) {
					int counter = 0;
					AstarNode astInit = new AstarNode(initialNode, AstarNode.misplacedist(initialNode, goalNode));
					PriorityQueue<AstarNode> frontier = new PriorityQueue<>();
					Stack<AstarNode> expanded = new Stack<>();
					frontier.add(astInit);
					while(frontier.peek().distance != 0) {
						AstarNode temp = frontier.remove();
						expanded.add(temp);
						AstarNode[] tempch = AstarNode.a2ChildrenGen(temp, goalNode);
						for(int i = 0; i < 4; i++)
							if(tempch[i] != null) {
								frontier.add(tempch[i]);
								for(int j = 0; j < expanded.size() ; j++)
									if(!expanded.elementAt(j).node.isDifferentState(frontier.peek().node))
										for(int k = 0; k < j; k++)
											expanded.pop();
							}
						counter++;
					}
					expanded.push(frontier.remove());
					LinkedList<Node> printList = new LinkedList<>();
					while(expanded.peek().node.isDifferentState(initialNode)) {
						printList.add(expanded.pop().node);
					}
					printList.add(expanded.pop().node);
					printBackList(printList);
					
					System.out.println("States Enqueued: " + counter);
				}
				else
					System.out.println("Not a valid heuristic.");*/
			}
			else
				System.out.println("Error: no algorithm matching that input.");
		} catch (FileNotFoundException e) {
			System.out.println("Error: file not found");
		}
		userIn.close();
	}
	
	//old method before realizing that the library implementation of the linked list is doubly linked
	/*public static void printBackList(LinkedList<Node> list) {
		int len = DFStree.listLength(list);
		for(int i = len; i > 0; i--) {
			ListIterator<Node> liter = list.listIterator();
			for(int j = 1; j < i; j++)
				liter.next();
			liter.next().printNode();
			System.out.println();
		}
	}*/

	//new method taking advantage of doubly linked nature of the linkedlist class
	public static void printBackList(LinkedList<Node> list){
		ListIterator<Node> listIter = list.listIterator();
		while(listIter.hasNext())
			listIter.next();
		while(listIter.hasPrevious()){
			listIter.previous().printNode();
			System.out.println();
		}
	}
	
}

//specialized class for use with the A* algorithm- wrapper for node adding a distance metric
class AstarNode extends Node implements Comparable<AstarNode>{
	public int distance;
	public Node node;
	
	//constructor for a node designed for use with the A* algorithm, simply takes in a node and associates a notion of distance from the goal with it
	public AstarNode(Node n, int d) {
		node = n;
		distance = d;
	}
	
	//definition of comparison method for the comparable interface
	public int compareTo(AstarNode n2) {
		if (this.distance == n2.distance)
			return 0;
		else if(this.distance > n2.distance)
			return 1;
		else
			return -1;
	}

	//addition to allow for two different A* Child Generator methods to be merged
	public static int calcDist(Node n, Node goal, int heur) {
		if(heur == 1)
			return manndist(n, goal);
		else
			return misplacedist(n, goal);
	}
	
	//for each position of a value in the current state, find where the value is supposed to be in the goal and calculate the difference; return the sum of all said differences
	public static int manndist(Node n, Node goal) {
		int dist = 0;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++) {
				int check = goal.state[i][j];
				for(int k = 0; k < 3; k++)
					for(int l = 0; l < 3; l++) {
						if(n.state[k][l] == check) {
							dist += Math.abs((k-i)+(l-j));
						}
					}
			}
		return dist;
	}
	
	//for each position of the value in the current node, just determine whether it is misplaced or not and return the number of misplaced nodes
	public static int misplacedist(Node n, Node goal) {
		int dist = 0;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(n.state[i][j] != goal.state[i][j])
					dist++;
		return dist;
	}

	//generate children with distances calculated with heuristic 1: Manhattan Distance
	public static AstarNode[] a1ChildrenGen(AstarNode node, Node goal){
		//leverage the logic of the children generation in the DFStree class to do the majority of the work
		DFStree temp = new DFStree();
		Node[] nodeChildren = temp.childrenGen(node.node);

		/* Old, late-night version of below for loop
		AstarNode child1 = null;
		AstarNode child2 = null;
		AstarNode child3 = null;
		AstarNode child4 = null;

		if(nodeChildren[0] != null)
			child1 = new AstarNode(nodeChildren[0], manndist(nodeChildren[0], goal));
		if(nodeChildren[1] != null)
			child2 = new AstarNode(nodeChildren[1], manndist(nodeChildren[1], goal));
		if(nodeChildren[2] != null)
			child3 = new AstarNode(nodeChildren[2], manndist(nodeChildren[2], goal));
		if(nodeChildren[3] != null)
			child4 = new AstarNode(nodeChildren[3], manndist(nodeChildren[3], goal));
		AstarNode[] ret = new AstarNode[4];
		ret[0] = child1;
		ret[1] = child2;
		ret[2] = child3;
		ret[3] = child4;*/

		//convert children returned from DFS children gen to AstarNodes and return array of converted nodes
		AstarNode[] retArr = {null, null, null, null};
		for(int i = 0; i < 4; i++){
			if(nodeChildren[i] != null)
			retArr[i] = new AstarNode(nodeChildren[i], manndist(nodeChildren[i], goal));
		}
		return retArr;
	}

	//generate children with distances calculated with heuristic 2: Misplaced Values
	public static AstarNode[] a2ChildrenGen(AstarNode node, Node goal){
		DFStree temp = new DFStree();
		Node[] nodeChildren = temp.childrenGen(node.node);
		/*Old, late-night version of below for loop
		AstarNode child1 = null;
		AstarNode child2 = null;
		AstarNode child3 = null;
		AstarNode child4 = null;
		if(nodeChildren[0] != null)
			child1 = new AstarNode(nodeChildren[0], misplacedist(nodeChildren[0], goal));
		if(nodeChildren[1] != null)
			child2 = new AstarNode(nodeChildren[1], misplacedist(nodeChildren[1], goal));
		if(nodeChildren[2] != null)
			child3 = new AstarNode(nodeChildren[2], misplacedist(nodeChildren[2], goal));
		if(nodeChildren[3] != null)
			child4 = new AstarNode(nodeChildren[3], misplacedist(nodeChildren[3], goal));
		AstarNode[] ret = new AstarNode[4];
		ret[0] = child1;
		ret[1] = child2;
		ret[2] = child3;
		ret[3] = child4;*/

		AstarNode[] retArr = {null, null, null, null};
		for(int i = 0; i < 4; i++){
			if(nodeChildren[i] != null)
			retArr[i] = new AstarNode(nodeChildren[i], misplacedist(nodeChildren[i], goal));
		}
		return retArr;
	}

	//added method to clean up how children are generated in A* approach
	public static AstarNode[] aChildrenGen(AstarNode node, Node goal, int heur){
		DFStree temp = new DFStree();
		Node[] nodeChildren = temp.childrenGen(node.node);

		AstarNode[] retArr = {null, null, null, null};
		for(int i = 0; i < 4; i++){
			if(nodeChildren[i] != null)
			retArr[i] = new AstarNode(nodeChildren[i], calcDist(nodeChildren[i], goal, heur));
		}
		return retArr;
	}
}

//class for implementation of both raw depth-first and iterative deepening searches
class DFStree{
	private Node head;
	private int buildNumber = 0;
	public int stateEnq = 0;
	
	public DFStree() {head = null;}
	public DFStree(Node h) {head = h;}
	
	public LinkedList<Node> dfs(Node goal, int maxDepth){	
		stateEnq = 0;
		return dfsRecur(this.head, goal, maxDepth);
	}
	
	//method for actually performing the depth-first search
	private LinkedList<Node> dfsRecur(Node node, Node goal, int curHeight){
		//if a leaf is found or the limit of the depth of the search is met, return without a path from the head to the goal
		if(node == null || curHeight == 0)
			return null;
		else {
			//if the goal is found along a given path, begin constructing the return list 
			if(!node.isDifferentState(goal)) {
				stateEnq++;
				LinkedList<Node> ret = new LinkedList<>();
				ret.add(node);
				return ret;
			}
			else {
				//each time this function is called, increment the number of states enqueued
				stateEnq++;

				//keep recursing down a node's given children if possible
				LinkedList<Node> list1 = dfsRecur(node.child1, goal, curHeight-1);
				LinkedList<Node> list2 = dfsRecur(node.child2, goal, curHeight-1);
				LinkedList<Node> list3 = dfsRecur(node.child3, goal, curHeight-1);
				LinkedList<Node> list4 = dfsRecur(node.child4, goal, curHeight-1);
				
				//on the return cascade, if a list was returned from the children, append the current node to it
				if(list1 != null)
					list1.add(node);
				if(list2 != null)
					list2.add(node);
				if(list3 != null)
					list3.add(node);
				if(list4 != null)
					list4.add(node);
				
				//calculate the size of the lists, for comparison's sake if the list is null then have it be maximum value
				int link1size = listLength(list1);
				int link2size = listLength(list2);
				int link3size = listLength(list3);
				int link4size = listLength(list4);
				
				//the shortest path to the goal is the best, so find the length of the shortest list and return it (if all lists are null, then return null)
				int minSize = Math.min(link1size, Math.min(link2size, Math.min(link3size, link4size)));
				if(minSize == link1size && minSize != Integer.MAX_VALUE)
					return list1;
				else if (minSize == link2size && minSize != Integer.MAX_VALUE)
					return list2;
				else if (minSize == link3size && minSize != Integer.MAX_VALUE)
					return list3;
				else if(minSize == link4size && minSize != Integer.MAX_VALUE)
					return list4;		
				else
					return null;				
			}
		}
	}
	
	//helper method to find the length of a list with added behavior for null lists for use in comparison above
	public static int listLength(LinkedList<Node> list) {
		if(list == null)
			return Integer.MAX_VALUE;
		ListIterator<Node> listIter = list.listIterator(0);
		int count = 0;
		while(listIter.hasNext()) {
			count++;
			listIter.next();
		}
		return count;
	}
	
	public void buildDFSTree(Node goal_node, int max_depth) {
		//recursively call childrenGen on generated children until
		//one of the child states equals the goal state or 
		//depth limit is reached
		buildNumber = 0;
		recursiveBuild(head, goal_node, max_depth, null);
	}
	
	
	private void recursiveBuild(Node node, Node stop, int height, Node parentn) {
		//properly assign the parent attribute, then check to see if the goal has been found
		node.parent = parentn;
		boolean diffFlag = node.isDifferentState(stop);
				
		//if the maximum depth has not been exceeded and the goal has not been found
		if((height >= 0) && diffFlag) {
			//increment buildNumber (tracks how many times children are built) and generate children, assigning them to the node currently being operated on
			buildNumber++;
			Node[] children = childrenGen(node);
			
			node.child1 = children[0];
			node.child2 = children[1];
			node.child3 = children[2];
			node.child4 = children[3];
			
			//somewhere along the way height was decided to be a better metric for this implementation, so set the height of the current node and decrement
			node.setHeight(height);
			--height;
			
			//for each of the children, if they were generated in the childrenGen method call and they do not cause a cycle, 
			if(node.child1 != null && checkParents(node.child1, node))
				recursiveBuild(node.child1, stop, height, node);
			if(node.child2 != null && checkParents(node.child2, node))
				recursiveBuild(node.child2, stop, height, node);
			if(node.child3 != null && checkParents(node.child3, node))
				recursiveBuild(node.child3, stop, height, node);
			if(node.child4 != null && checkParents(node.child4, node)) 
				recursiveBuild(node.child4, stop, height, node);
		}
	}
	
	//from a given node, generates all possible children (i.e. next game states from this point) allowable by the rules of the game
	public Node[] childrenGen(Node n) {
		//initialize necessary varaibles
		Node child_one = null, child_two = null, child_three = null, child_four = null;
		int ind1 = -2, ind2 = -2;
		//find index of empty space
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(n.state[i][j] == -1) {
					ind1 = i;
					ind2 = j;
				}
			}
		}

		//construct possible moves (swaps) based on index location
		//case 1: empty is swappable with position above
		if(ind1 - 1 >= 0) {
			child_one = new Node(n);
			child_one = swap(child_one, ind1, ind2, ind1-1, ind2);
		}
		//case 2: empty is swappable with position below
		if(ind1 + 1 <= 2) {
			child_two = new Node(n);
			child_two = swap(child_two, ind1, ind2, ind1+1, ind2);
		}
		//case 3: empty is swappable with position to the left
		if(ind2 - 1 >= 0) {
			child_three = new Node(n);
			child_three = swap(child_three, ind1, ind2, ind1, ind2-1);
		}
		//case 4: empty is swappable with position to the right
		if(ind2 + 1 <= 2) {
			child_four = new Node(n);
			child_four = swap(child_four, ind1, ind2, ind1, ind2+1);
		}
		
		//once all possible children are constructed, return them as an array of Nodes
		Node[] returnNode = {child_one, child_two, child_three, child_four};
		return returnNode;
	}
	
	//swapping method to aid in programmatic representation of allowable game rules
	private Node swap(Node n, int i, int j, int ii, int jj) {
		int temp = n.state[ii][jj];
		n.state[ii][jj] = n.state[i][j];
		n.state[i][j] = temp;
		
		return n;
	}
	
	//the below methods allow for the detection of cycles in children generation
	private boolean checkParents(Node n, Node nodeParent) {
		return checkParentRecur(n, nodeParent);
	}
	
	//checks that a given node does not match any of its ancestors (thus creating a repeating pattern/cycle)
	private boolean checkParentRecur(Node n, Node nodeParent) {
		boolean diffFlag = n.isDifferentState(nodeParent);
		boolean headFlag = nodeParent.isDifferentState(head);
		//if node is still different and not at the head yet, keep going
		if(diffFlag && headFlag)
			return checkParentRecur(n, nodeParent.parent);
		//if the node is not different from an ancestor, return false
		else if (!diffFlag /*&& headFlag*/)
			return false;
		//if the node is different from all ancestors and the head of the tree is reached, allow further children to be generated in the branch
		else
			return true;
	}
	
	//allow (private) number of states enqueued to be printed out
	public void dispBuildNo() {System.out.println(buildNumber);}
}

class Node{
	//class attributes for a Node
	int[][] state = new int[3][3];
	int height = 0;
	Node child1, child2, child3, child4;
	Node parent;
	
	//default constructor, initialize everything to zero/null where relevant
	public Node() {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				state[i][j] = 0;
		
		child1 = null;
		child2 = null;
		child3 = null;
		child4 = null;		
	}
	
	//constructor to allow single dimensional array to be passed in as a state argument
	public Node(int[] passState) {
		int increment = 0;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++) {
				state[i][j] = passState[increment];
				increment++;
			}
		
		child1 = null;
		child2 = null;
		child3 = null;
		child4 = null;	
	}
	
	//constructor that initializes the new node to be a deep copy of the node passed in
	public Node(Node copy) {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++) 
				this.state[i][j] = copy.state[i][j];
	}
	
	//setter method to indicate the height of a given node
	public void setHeight(int h) {height = h;}
	
	//helper method to allow for the easy printing of the game state in the proper format
	public void printNode() {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++)
				if(this.state[i][j] == -1)
					System.out.print("* ");
				else
					System.out.print(this.state[i][j] + " ");
			System.out.println();
		}
	}
	
	//helper method to allow nodes to be easily compared
	public boolean isDifferentState(Node n2) {
		boolean ret = false;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(this.state[i][j] != n2.state[i][j])
					ret = true;
		return ret;
	}
}