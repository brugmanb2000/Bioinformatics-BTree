import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class BTree {

	// Instance Variables
	BTreeNode root = null;
	private int degree;
	private int size;
	private int sequenceLength;
	private int maxKey;
	private int minKey;
	private int maxChildren;
	public boolean hasCache;
	private Cache<BTreeNode> cache;


	/** 
	 * Cache-less constructor for BTree
	 * @param degree for the BTree
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public BTree(int degree) throws IOException, ClassNotFoundException {
		this.degree = degree;
		this.size = 0;
		root = new BTreeNode(degree);
		root.leaf = true;
		root.keyCount = 0;
		hasCache = false;
		BTreeDiskOperations.diskWrite(root, root.nodeIdentifier);
	}



	/**
	 * BTree with Cache
	 * @param degree
	 * @param cacheSize
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public BTree(int degree, int cacheSize) throws IOException, ClassNotFoundException {
		this.degree = degree;
		this.size = 0;
		root = new BTreeNode(degree);
		root.leaf = true;
		root.keyCount = 0;
		hasCache = true;
		cache = new Cache<BTreeNode>(cacheSize);
		BTreeDiskOperations.diskWrite(root, root.nodeIdentifier);
	}

	public BTree(int degree, String bTreeFile, int seqLength ) throws ClassNotFoundException, IOException {
		BTreeDiskOperations.setFileName(bTreeFile);
		this.sequenceLength = seqLength;
		root = new BTreeNode(degree);
		this.degree = degree;
		this.maxKey = (2 * degree)-1;
		this.maxChildren = 2 * degree;
		this.minKey = degree -1;
		hasCache = false;
	}


	public BTree(int degree, String bTreeFile, int seqLength, int cacheSize){
		try {
			BTreeDiskOperations.setFileName(bTreeFile);
			this.sequenceLength = seqLength;
			root = new BTreeNode(degree);
			this.degree = degree;
			this.maxKey = (2 * degree)-1;
			this.maxChildren = 2 * degree;
			this.minKey = degree -1;
			cache = new Cache<BTreeNode>(cacheSize);
			hasCache = true;

		} catch (Exception e) {

		}
	}

	// function to search a key in this tree
	public int searchBTree(BTreeNode root, Long k) {
		int i = 0;
		while((i < root.getKeyCount()) && (k.compareTo(root.getKey(i).key)>0)) {
			i++;
		}
		if((i < root.getKeyCount()) && (k.compareTo(root.getKey(i).key)==0)) 
		{

			return (root.getKey(i).frequency+1);
		}
		else if(root.isLeaf()==true)
		{			
			return -1;//element was not found
		}
		else {

			return searchBTree(root.pointers[i],k);
		}
	}

	// function to search a key in this tree
	public int searchBTreeWithCache(BTree btree, BTreeNode root, Long k) {
		int i = 0;
		BTreeNode cachedNode = btree.cache.searchObject(root);
		if (cachedNode != null) {
			btree.cache.removeObject(cachedNode);
			btree.cache.addObjectFront(cachedNode);
		}

		else {
			btree.cache.addObjectFront(root);
		}
		btree.cache.addObjectFront(root);
		while((i < root.getKeyCount()) && (k.compareTo(root.getKey(i).key)>0)) {
			i++;
		}
		if((i < root.getKeyCount()) && (k.compareTo(root.getKey(i).key)==0)) 
		{

			return (root.getKey(i).frequency+1);
		}
		else if(root.isLeaf()==true)
		{			
			return -1;//element was not found
		}
		else {

			return searchBTree(root.pointers[i],k);
		}
	}

	/**
	 * Step 1 of the insert process. Check if new node is needed.
	 * @param BTree we are adding this into
	 * @param The key we are storing
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void insert(BTree tree, long key) throws IOException, ClassNotFoundException {


		for(int y = 0; y < tree.root.getKeyCount(); y++) {
			if(key == tree.root.keys[y].key) {
				tree.root.keys[y].incrementFrequency();
				//				System.out.println("Frequency updated. Key: " + tree.root.keys[y].getKey() + " Frequency: " + tree.root.keys[y].getFrequency());
				BTreeDiskOperations.diskWrite(tree.root, tree.root.nodeIdentifier);

				// If has cache, check if the cache has object. If so, remove object and re-add to front
				if (hasCache == true) {
					BTreeNode cachedNode = this.cache.searchObject(root);
					if (cachedNode != null) {
						this.cache.removeObject(cachedNode);
						this.cache.addObjectFront(cachedNode);
					}

					else {
						this.cache.addObjectFront(tree.root);
					}
				}
				return;
			}
		}
		BTreeNode r = tree.root;
		// If node is full, split and create a new node
		if (r.getKeyCount() == (2*degree) -1) {
			BTreeNode s = new BTreeNode(degree);
			tree.root = s;
			s.leaf = false;
			s.keyCount = 0;
			s.pointers[0] = r;
			split(s, 0, r);
			nonFullInsert(s, key);
		}
		else {

			// Node isn't full, so we just enter this into the node itself.
			nonFullInsert(r, key);
		}
	}

	/**
	 * Step 2 of the insert process
	 * @param BTree Node we are using
	 * @param key we are storing in the BTree
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void nonFullInsert(BTreeNode x, long key) throws IOException, ClassNotFoundException {

		// Search to see if we need to update frequency
		for(int y = 0; y < x.getKeyCount(); y++) {
			if(key == x.keys[y].key) {
				x.keys[y].incrementFrequency();
				//				System.out.println("Frequency updated. Key: " + x.keys[y].getKey() + " Frequency: " + x.keys[y].getFrequency());

				// If has cache, check if the cache has object. If so, remove object and re-add to front
				if (hasCache == true) {
					BTreeNode cachedNode = this.cache.searchObject(root);
					if (cachedNode != null) {
						this.cache.removeObject(cachedNode);
						this.cache.addObjectFront(cachedNode);
					}

					else {
						this.cache.addObjectFront(x);
					}
				}
				BTreeDiskOperations.diskWrite(x, x.nodeIdentifier);
				return;
			}
		}

		int i = x.keyCount -1;
		if (x.leaf == true) {
			while (i >=0 && key < x.keys[i].getKey()) {
				x.keys[i+1] = x.keys[i];
				i--;
			}
			i++;

			x.keys[i] = new TreeObject(key);
			//			System.out.println("Key inserted: " + key);
			x.keyCount++;;
			BTreeDiskOperations.diskWrite(x, x.getNodeIdentifier());

		} else {
			while (i >= 0 && key < x.keys[i].getKey()) {
				i--;
			}
			i++;

			if (x.pointers[i].keyCount == ((2*degree)-1)) {
				split(x, i, x.pointers[i]);
				if (key > x.getKey(i).getKey()) {
					i++;
				}
			}
			nonFullInsert(x.pointers[i], key);
		}
	}


	/**
	 * Splits the node into two child nodes and a parent node
	 * @param Parent Node: x
	 * @param Index: i 
	 * @param Child Node: xChild
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void split(BTreeNode x, int i, BTreeNode xChild) throws IOException, ClassNotFoundException {
		BTreeNode z = new BTreeNode(degree);
		z.leaf = xChild.leaf;
		z.keyCount = degree - 1;

		for (int j = 0; j < degree-1; j++) {
			z.keys[j] = xChild.keys[j+degree];
		}
		if (xChild.leaf == false) {
			for (int j = 0; j < degree; j++) {
				z.pointers[j] = xChild.pointers[j+degree];
			}
		}

		xChild.keyCount = degree -1;

		for (int b = x.keyCount; b >= i+1; b--) {
			x.pointers[b+1] = x.pointers[b];
		}

		x.pointers[i+1] = z;

		for(int j = x.keyCount -1; j >= i; j--) {
			x.keys[j+1] = x.keys[j];
		}

		x.keys[i] = xChild.keys[degree];
		x.keyCount++;

		BTreeDiskOperations.diskWrite(x, x.nodeIdentifier);
		BTreeDiskOperations.diskWrite(xChild, xChild.nodeIdentifier);
		BTreeDiskOperations.diskWrite(z, z.nodeIdentifier);

		// Add to cache if a cache is used.
		if (hasCache == true) {
			if (this.cache.containsObject(x) == true) {
				this.cache.removeObject(x);
				this.cache.addObjectFront(x);
			} else {
				cache.addObjectFront(x);
			}
			if (this.cache.containsObject(xChild) == true) {
				this.cache.removeObject(xChild);
				this.cache.addObjectFront(xChild);
			} else {
				cache.addObjectFront(xChild);
			}
			if (this.cache.containsObject(z) == true) {
				this.cache.removeObject(z);
				this.cache.addObjectFront(z);
			} else {
				cache.addObjectFront(z);
			}
		}
	}


	public void print() {
		if (root != null) {
			root.print();
		}
		System.out.println();

	}

	public int getRootIdentifier() {
		return this.root.nodeIdentifier;
	}


	public BTreeNode getRoot() {
		return this.root;
	}

  public void printNodes(BTreeNode b) {
		
		if (b == null) {
			return;
		} 
		
		if (b.leaf == true) {
			System.out.println(b.toString());
		}
		
		else {
			for (int a = 0; a < b.pointers.length; a++) {
				printNodes(b.pointers[a]);
			}
		}

	}

}
