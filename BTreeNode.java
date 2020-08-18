import java.io.IOException;
import java.io.Serializable;

public class BTreeNode implements Serializable {

	protected TreeObject[] keys; // List of keys 
	private int degree = 0; // Degree
	protected BTreeNode[] pointers; // Pointers to BTreeNodes
	protected int keyCount = 0; // Total keys in node
	private boolean isLeaf; // True if leaf, false if not a leaf
	boolean leaf;
	protected int nodeSize;
	int nodeIdentifier;


	BTreeNode(int degree) throws ClassNotFoundException, IOException {
		this.degree = degree;
		this.keys = new TreeObject[2 * degree - 1]; 
		this.pointers = new BTreeNode[2 * degree];
		this.keyCount = 0;
		this.leaf = true;
		this.nodeSize = BTreeDiskOperations.setNodeSize(this);
		nodeIdentifier = BTreeDiskOperations.getNextNodeIdentifier();
	}

	/**
	 * @return
	 */
	public int getKeysLength() {
		return keys.length;
	}

	/**
	 * @param i
	 * @return
	 */
	public TreeObject getKey(int i) {
		return keys[i];
	}

	/**
	 * @param Index of where you are looking to get the frequency count
	 * @return frequency of the index
	 */
	public int getFrequency(TreeObject object) {
		return object.getFrequency();
	}

	/**
	 * Increases the frequency count for that specific index
	 * @param index of where we are increasing frequency
	 */
	public void increaseFrequency(TreeObject object) {
		object.incrementFrequency();
	}

	/**
	 * @return boolean if the node is a leaf node or not0
	 */
	public boolean isLeaf() {
		return leaf;
	}

	/**
	 * Increments the key count of the node
	 */
	public void incrementKeyCount() {
		keyCount++;
	}

	/**
	 * decrements the key count of the node
	 */
	public void decrementKeyCount() {
		keyCount--;
	}
	
	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}

	/**
	 * Finds the key count in the node
	 * @return amount of keys in the node
	 */
	public int getKeyCount() {
		return keyCount;
	}
	
	public int getNodeIdentifier() {
		return nodeIdentifier;
	}
	
	 public void print() { 
		  
	       // Go through node and put all keys/frequencies on a stack to print from smallest -> largest
	        int i = 0; 
	        for (i = 0; i < this.keyCount; i++) { 
	  
	           // Print the child pointers before node if this is not a leaf node
	            if (this.leaf == false) { 
	                pointers[i].print(); 
	            } 
	            System.out.println(keys[i] + " "); 
	        } 

	        // Print the subtree rooted with last child 
	        if (leaf == false) 
	            pointers[i].print(); 
	    } 
	    
	    // A function to search a key in the subtree rooted with this node. 
	   public BTreeNode search(long k) { // returns NULL if k is not present. 
	  
	        // Find the first key greater than or equal to k 
	        int i = 0; 
	        while (i < keyCount && k > keys[i].key) 
	            i++; 
	  
	        // If the found key is equal to k, return this node 
	        if (keys[i].key == k) 
	            return this; 
	  
	        // If the key is not found here and this is a leaf node 
	        if (leaf == true) 
	            return null; 
	        return pointers[i].search(k); 
	  
	    } 

	    
	    // A function to search a key in the subtree rooted with this node. 
	   public int searchFrequency(long k) { // returns NULL if k is not present. 
		   int retval = 0;
	        // Find the first key greater than or equal to k 
	        int i = 0; 
	        while (i < keyCount && k > keys[i].key) {
	            i++; 
			
			// If the found key is equal to k, return this node 
	        if (keys[i].key == k) 
	            retval =  keys[i].frequency; 
	        }
	        return retval;
	  
	    } 

      	   public String toString() {
		   StringBuilder retVal = new StringBuilder();
		   retVal.append("Node Identifier: " + nodeIdentifier + ":  ");
		   for (int x = 0; x < this.keyCount; x++) {
			   retVal.append("[" + this.keys[x] + "] ");
		   }
		   return retVal.toString();
	   }
	

}
