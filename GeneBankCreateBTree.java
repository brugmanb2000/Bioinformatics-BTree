import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class GeneBankCreateBTree {

	// Make values static
	static int format;;
	static int degree = 0;
	static String filename = "";
	public static int sequenceLength = 0;
	static int cacheSize = 0;
	static int debugLevel = 0;
	static boolean cache = false;
	static BTree tree;
	static String argsMessage = "GeneBankCreateBTree requires 4 - 6 arguments of the format: \n"
			+ "java GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file>\n"
			+ "// <sequence length> [<cache size>] [<debug level>] [cache size and debug \n" + "are optional]";

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		// Argument input:
		// java GeneBankCreateBTree 0L1<0/1(no/with Cache)> 1L2<degree> 2L3<gbk file>
		// 3l4<sequence length> [4L5<cache size>] [5L6<debug level>] index/Length
		// if args.length = 0 run static variables for now.
		
			if (args.length < 1 | args.length > 6) {
				System.out.println(argsMessage);
				System.exit(0);
			} else {
				if (Integer.parseInt(args[0]) == 1) {
					cache = true;
				}
				try {
					degree = Integer.parseInt(args[1]);

				} catch (NumberFormatException e) {
					System.out.println(argsMessage);
					System.out.println(e);
					System.exit(0);
				}
				filename = args[2];
				sequenceLength = Integer.parseInt(args[3]);
				// Check for size argument
				if (args.length >= 5) {
					if (cache = true) {
						if (Integer.parseInt(args[4]) > 0) {
							try {
								cacheSize = Integer.parseInt(args[4]);
							} catch (NumberFormatException e) {
								System.out.println(argsMessage);
								System.exit(0);
							}
						} else {
							System.out.println("Cache was specified without a cache size");
						}
					} else {
						try {
							debugLevel = Integer.parseInt(args[4]);
						} catch (NumberFormatException e) {
							System.out.println(argsMessage);
							System.exit(0);
						}
					}
				}
				if (args.length == 6) {
					try {
						debugLevel = Integer.parseInt(args[5]);
					} catch (NumberFormatException e) {
						System.out.println(argsMessage);
						System.exit(0);
					}
				}
			}
			
		// Check for 0 degree being entered
		if (degree == 0) {
			degree = BTreeDiskOperations.calculateBestDegree();
		}



		// Create BTree File/Dump File

		String bTreeFile = (filename + ".btree.data." + sequenceLength + "." + degree);
		BTreeDiskOperations.setFileName(bTreeFile);
		BTreeDiskOperations.setDegree(degree);
		BTreeDiskOperations.setSequenceLength(sequenceLength);

		if (cache == true) {
			tree = new BTree(degree, cacheSize);
		} else {
			tree = new BTree(degree);
		}


		// Run Program
		System.out.println("Reading file..");
		readFile(filename);
		System.out.println("File parsed.");
		System.out.println("Writing Meta Data..");
		tree.print();

		// Write Meta Data
		BTreeDiskOperations.setRootNodeIdentifier(tree.getRootIdentifier());
		BTreeDiskOperations.writeMetaData(tree);
		System.out.println("Meta Data written.");
		// Write Root
		BTreeNode root = tree.root;
		BTreeDiskOperations.diskWrite(root, root.nodeIdentifier);

		// Print dump file if debug level 1
		if (debugLevel == 1) {
			System.out.println("Writing debug file..");
			String dumpFile = (filename + ".btree.dump." + sequenceLength);

			// Store original stdout stream
			PrintStream out = System.out;

			// Set stdout to print to the dumpFile.txt file
			PrintStream outStream = new PrintStream(new FileOutputStream(dumpFile));
			System.setOut(outStream);

			// Print BTree
			tree.print();

			// Set stdout back to the console
			System.setOut(out);
			System.out.println("Debug file written.");
		}

	}


	/**
	 * This method will read a file and add all correct substrings into the BTree
	 * 
	 * @param String with file name
	 * @throws ClassNotFoundException 
	 */
	static void readFile(String filename) throws ClassNotFoundException {
		File file = new File(filename);
		try {
			boolean originFound = false;
			String line = null;
			Scanner scanner = new Scanner(file);

			// Loop through file until end of file
			while (scanner.hasNextLine()) {

				// Loop through file until the word "ORIGIN" is found. Once found, boolean sets
				// to true and we enter the 2nd while loop

				if (originFound == false) {
					if (scanner.hasNext() == false) {
						break;
					}
				}

				if (originFound == false) {
					line = scanner.next();
					if (line.toUpperCase().equals("ORIGIN")) {
						originFound = true;
						scanner.useDelimiter("n|\\n");
					}
				}

				// Loop through lines and collect different strings
				if (originFound == true) {
					if (scanner.hasNext() == false) {
						break;
					}

					String evaluate = removeWS(scanner.next());

					if (evaluate.length() >= 2) {
						String test = evaluate.substring(0, 2);
						if (test.equals("//")) {
							originFound = false;
							scanner.useDelimiter("\\s");
						} else {
							// System.out.println("Original Line: " + (evaluate));
							evaluateLine(evaluate);
						}

					}

				}
			}
			// Close Scanner
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File cannot be found. Please try again.");
		}
	}

	/**
	 * Helper function for ReadFile to evaluate a line and create a DNA Strand Reads
	 * through line and evaluates whether or not to parse the string as a DNA
	 * sequence
	 * 
	 * @param line
	 * @throws ClassNotFoundException 
	 */
	static void evaluateLine(String line) throws ClassNotFoundException {

		// Current list
		char array[] = line.toLowerCase().toCharArray();

		// String of DNA Strands
		char list[] = new char[sequenceLength];

		// Create DNA Strands
		if (array.length >= sequenceLength) {

			for (int y = 0; y < (array.length) - sequenceLength; y++) {
				for (int x = 0; x < sequenceLength; x++) {
					list[x] = array[y + x];
				}
				convertArray(list);
			}
		}
	}

	/**
	 * Method parses the DNA Strand and converts it to base-2 conversion This method
	 * will parse through a char list and create DNA Strands out of the correct
	 * variables
	 * 
	 * @param char[]
	 * @throws ClassNotFoundException 
	 */
	static void convertArray(char[] list) throws ClassNotFoundException {

		// Instance variables
		boolean returnValue = true;
		StringBuilder dnaStrand = new StringBuilder();
		StringBuilder characterValues = new StringBuilder();
		for (int x = 0; x < list.length; x++) {

			// Parse through DNA Strand and convert to correct number. If an incorrect
			// character is found, ignore posting that strand.
			if (list[x] == 'a') {
				dnaStrand.append("00");
				characterValues.append('a');
			} else if (list[x] == 't') {
				dnaStrand.append("11");
				characterValues.append('t');
			} else if (list[x] == 'c') {
				dnaStrand.append("01");
				characterValues.append('c');
			} else if (list[x] == 'g') {
				dnaStrand.append("10");
				characterValues.append('g');
			} else {
				returnValue = false;
			}

		}

		// Post only if valid characters were found
		if (returnValue == true) {
			String dnaCompleted = dnaStrand.toString();
			// System.out.println("DNA Count: " + dnaCount);
			// System.out.println("----------");

			// If DNA strand is unique, place in B Tree, else update frequency for that DNA
			// String

			long dnaToLong = Long.parseLong(dnaCompleted, 2);
			// System.out.println(dnaToLong);
			// System.out.println(String.valueOf(dnaToLong));

			try {
				tree.insert(tree, dnaToLong);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**     
	 * Removes whitespace in a line
	 * 
	 * @param string
	 * @return string without whitespace
	 */
	private static String removeWS(String string) {
		return string.replaceAll("\\s", "");
	}


}