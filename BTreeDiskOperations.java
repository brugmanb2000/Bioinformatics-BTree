import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;

public class BTreeDiskOperations implements Serializable {

	private static String bTreeFilename;
	private static int metaData = 4 + 4 + 1 + 4; // 3 ints and a boolean in the BTreeNode  
	private static int degree;
	private static int nodeSize; // Calculated upon finding the degree
	private static ArrayList<Integer> nodeIdentifiers = new ArrayList<Integer>();
	private static int identifier = 0;
	private static int sequenceLength = 0;
	private static int rootNodeIdentifier;

	public static void setFileName(String filename) {
		bTreeFilename = filename;
	}

	public static void setDegree(int treeDegree) {
		degree = treeDegree;

	}

	public static void setSequenceLength(int length) {
		sequenceLength = length;
	}

	public static void setRootNodeIdentifier(int identifier) {
		rootNodeIdentifier = identifier;
	}

	public static int setNodeSize (BTreeNode b) throws IOException, ClassNotFoundException {
		byte[] nodeTest=BTreeDiskOperations.objectToBytes(b);
		BTreeNode test = (BTreeNode) BTreeDiskOperations.bytesToObject(nodeTest);
		// System.out.println(nodeTest.length);
		return nodeTest.length;
	}

	private static byte[] objectToBytes(Object object) throws IOException {	
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		ObjectOutput output = new ObjectOutputStream(byteArray);
		output.writeObject(object);
		byte[] data = byteArray.toByteArray();
		return data;
	}

	private static Object bytesToObject(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream input = new ByteArrayInputStream(data);
		ObjectInput object = new ObjectInputStream(input);
		return object.readObject();
	} 

	public static int getNextNodeIdentifier() {
		identifier++;
		nodeIdentifiers.add(identifier);
		return identifier;
	}



	public static void diskWrite(BTreeNode node, int nodeIdentifier) throws IOException {

		// Get an instance variable for future operations
		byte[] nodeData;
		int position = (nodeIdentifier * nodeSize) + metaData;

		// Try creating a Random Access File and writing data
		RandomAccessFile file = new RandomAccessFile(bTreeFilename, "rw");

		// If node is empty, write what the node size should be
		if (node == null) {
			nodeData = new byte[nodeSize];

			// If node isn't empty, write the node to bytes
		} else {
			nodeData = objectToBytes(node);	
		}

		// Find open position for data to be written
		file.seek(position);

		// Write and close file
		file.write(nodeData);
		file.close();
	}

	public static BTreeNode diskRead(int nodeIdentifier) throws FileNotFoundException,IOException,ClassNotFoundException {

		// Find node position
		int position = (nodeIdentifier * nodeSize) + metaData;

		// Open file as read-only
		RandomAccessFile file = new RandomAccessFile(bTreeFilename, "r");

		// Create a place to read data as long as a BTree Node
		byte[] nodeBuffer = new byte[nodeSize];

		// Find position and read
		file.seek(position);
		file.read(nodeBuffer);

		// Convert Bytes to Node
		BTreeNode node = (BTreeNode) bytesToObject(nodeBuffer);

		// Close the stream and return node
		file.close();
		return node;

	}

	public static int readMetaData(int offset){
		int retVal = 0;
		try {
			RandomAccessFile file = new RandomAccessFile(bTreeFilename, "rw");		
			file.seek(offset);
			retVal = file.readInt();

			file.close();

		} catch (Exception e) {
			System.out.println("Error reading meta data. Error: " + e);
		}
		return retVal;

	}

	public static BTreeNode readRoot(int length) throws ClassNotFoundException, IOException {
		BTreeNode retVal;
		try { 
			RandomAccessFile file = new RandomAccessFile("rootfile", "rw");
			file.seek(0);
			byte[] rootNode = new byte[length];
			file.readFully(rootNode, 0, rootNode.length);
			retVal = (BTreeNode) BTreeDiskOperations.bytesToObject(rootNode);
			file.close();
			return retVal;
		} catch (Exception e) {
			System.out.println("Error reading in root node. Error: " + e.getMessage());
		}
		return null;
	}

	public static void writeMetaData(BTree tree) {
		try {
			RandomAccessFile file = new RandomAccessFile(bTreeFilename, "rw");

			// Write Meta Data
			file.seek(0);
			
			//4 bytes
			file.writeInt(degree); // Write Degree
			
			// bytes
			file.writeInt(sequenceLength); // Write NodeSize
			//			file.writeInt(rootNodeIdentifier); // Offset for Meta Data
			
			
			byte[] rootBytes = BTreeDiskOperations.objectToBytes(tree.root);
			
			// 4 bytes
			file.writeInt(rootBytes.length);
			file.close();
			
			RandomAccessFile rootFile = new RandomAccessFile("rootfile", "rw");
			rootFile.seek(0);
			rootFile.write(rootBytes);

			// Close the stream and return node
			rootFile.close();


		} catch (Exception e) {
			System.out.println("Error writing meta data. Error: " + e);
		}
	}


	// Takes the degree + metadata and increases degree until metadata + degree calculations are just under 4096
	public static int calculateBestDegree() {
		System.out.println("Best degree needs to be found. Searching for best degree..");
		int totalSize = 0;

		// Search for best degree
		while (totalSize < 4096) {
			totalSize = metaData + ((2 * degree+1) * 4) /* pointer values */ + 4 + ((2 * degree - 1) * (20)); // 8 (object header) + 12 (Tree Object Long and Int)
			degree++;
		}

		nodeSize = ((2*degree)*4) + ((2* degree -1)*20);

		// Set the total size
		degree += -2;
		totalSize = metaData + ((2 * degree) * 4) + 4 + ((2 * degree - 1) * (20));
		System.out.println("Best Degree Found: " + degree);
		return degree;
	}
}


