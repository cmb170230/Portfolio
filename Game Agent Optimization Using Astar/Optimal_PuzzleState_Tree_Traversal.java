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
			int[] goal = {7,8,1,6,-1,2,5,4,3};
			Node initialNode = new Node(input_init);
			Node goalNode = new Node(goal);
			int max_depth = 10;
			
			System.out.println("Please enter the algorithm:\n1- depth-first search\n2- iterative deepening\n3- A* algorithm");
			int algo = userIn.nextInt();
			//System.out.println(algo);
			
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
			else if(algo == 2) {
				try {
				int limit = 0;
				int enq = 0;
				LinkedList <Node> test2l = null;
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
				catch(NullPointerException e) {
					System.out.println("No Goal State Found");
				}
			}
			else if(algo == 3) {
				System.out.println("Please select hueristic 1 or 2 by typing 1 or 2.");
				int huer = userIn.nextInt();
				//Manhattan Distance
				if(huer == 1) {
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
				else if(huer == 2) {
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
					System.out.println("Not a valid hueristic.");
			}
			else
				System.out.println("Error: no algorithm matching that input.");
		} catch (FileNotFoundException e) {
			System.out.println("Error: file not found");
		}
		userIn.close();
	}
	
	public static void printBackList(LinkedList<Node> list) {
		int len = DFStree.listLength(list);
		for(int i = len; i > 0; i--) {
			ListIterator<Node> liter = list.listIterator();
			for(int j = 1; j < i; j++)
				liter.next();
			liter.next().printNode();
			System.out.println();
		}
	}
	
}

class AstarNode implements Comparable<AstarNode>{
	public int distance;
	public Node node;
	
	public AstarNode(Node n, int d) {
		node = n;
		distance = d;
		}
	
	public int compareTo(AstarNode n2) {
		if (this.distance == n2.distance)
			return 0;
		else if(this.distance > n2.distance)
			return 1;
		else
			return -1;
	}
	
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
	
	public static int misplacedist(Node n, Node goal) {
		int dist = 0;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(n.state[i][j] != goal.state[i][j])
					dist++;
		return dist;
	}
	
	public static AstarNode[] a1ChildrenGen(AstarNode node, Node goal){
		DFStree temp = new DFStree();
		Node[] nodeChildren = temp.childrenGen(node.node);
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
		ret[3] = child4;
		return ret;
	}
	public static AstarNode[] a2ChildrenGen(AstarNode node, Node goal){
		DFStree temp = new DFStree();
		Node[] nodeChildren = temp.childrenGen(node.node);
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
		ret[3] = child4;
		return ret;
	}
}

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
	
	private LinkedList<Node> dfsRecur(Node node, Node goal, int curHeight){
		if(node == null || curHeight == 0)
			return null;
		else {
			if(!node.isDifferentState(goal)) {
				stateEnq++;
				LinkedList<Node> ret = new LinkedList<>();
				ret.add(node);
				return ret;
			}
			else {
				stateEnq++;
				LinkedList<Node> list1 = dfsRecur(node.child1, goal, curHeight-1);
				LinkedList<Node> list2 = dfsRecur(node.child2, goal, curHeight-1);
				LinkedList<Node> list3 = dfsRecur(node.child3, goal, curHeight-1);
				LinkedList<Node> list4 = dfsRecur(node.child4, goal, curHeight-1);
				
				if(list1 != null)
					list1.add(node);
				if(list2 != null)
					list2.add(node);
				if(list3 != null)
					list3.add(node);
				if(list4 != null)
					list4.add(node);
				
				int link1size = listLength(list1);
				int link2size = listLength(list2);
				int link3size = listLength(list3);
				int link4size = listLength(list4);
				
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
	
	private void recursiveBuild(Node node, Node stop, int depth, Node parentn) {
		node.parent = parentn;
		boolean diffFlag = node.isDifferentState(stop);
				
		if((depth >= 0) && diffFlag /*&& !initFlag*/) {
			buildNumber++;
			Node[] children = childrenGen(node);
			
			node.child1 = children[0];
			node.child2 = children[1];
			node.child3 = children[2];
			node.child4 = children[3];
			
			node.setDepth(depth);
			--depth;
			
			if(node.child1 != null && checkParents(node.child1, node))
				recursiveBuild(node.child1, stop, depth, node);
			if(node.child2 != null && checkParents(node.child2, node))
				recursiveBuild(node.child2, stop, depth, node);
			if(node.child3 != null && checkParents(node.child3, node))
				recursiveBuild(node.child3, stop, depth, node);
			if(node.child4 != null && checkParents(node.child4, node)) 
				recursiveBuild(node.child4, stop, depth, node);
		}
	}
	
	public Node[] childrenGen(Node n) {
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
		
			
		Node[] returnNode = {child_one, child_two, child_three, child_four};
		
	return returnNode;
	}
	
	private Node swap(Node n, int i, int j, int ii, int jj) {
		int temp = n.state[ii][jj];
		n.state[ii][jj] = n.state[i][j];
		n.state[i][j] = temp;
		
		return n;
	}
	
	private boolean checkParents(Node n, Node nodeParent) {
		return checkParentRecur(n, nodeParent);
	}
	
	private boolean checkParentRecur(Node n, Node nodeParent) {
		boolean diffFlag = n.isDifferentState(nodeParent);
		boolean headFlag = nodeParent.isDifferentState(head);
		if(diffFlag && headFlag)
			return checkParentRecur(n, nodeParent.parent);
		else if (!diffFlag && !headFlag)
			return false;
		else
			return true;
	}
	
	public void dispBuildNo() {System.out.println(buildNumber);}
	
	
	//public int getEnqueued() {return buildNumber;}
}

class Node{
	int[][] state = new int[3][3];
	int depth = 0;
	Node child1, child2, child3, child4;
	Node parent;
	
	public Node() {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				state[i][j] = 0;
		
		child1 = null;
		child2 = null;
		child3 = null;
		child4 = null;		
	}
	
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
	
	//deep copy
	public Node(Node copy) {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++) 
				this.state[i][j] = copy.state[i][j];
		}
	
	public void setDepth(int d) {depth = d;}
	
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
	
	public boolean isDifferentState(Node n2) {
		boolean ret = false;
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 3; j++)
				if(this.state[i][j] != n2.state[i][j])
					ret = true;
		return ret;
	}
}

//search tree for shortest path













