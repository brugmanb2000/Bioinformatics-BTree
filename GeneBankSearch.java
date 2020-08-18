import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class GeneBankSearch {
	private static int debugLevel = 0;
	private static int cacheIndicator = 0; // <0/1(no/with Cache)>
	private static int cacheSize; // size of cache
	private static BTree btree;
	//	private static String btreeFile;
	//	private static String queryFile;


	public static void main(String[] args) throws ClassNotFoundException, IOException {
		String btreeFile;
		String queryFile;

		// printUsage when the arguments format is not valid (less or more arguments
		// java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> [<cache
		// size>] [<debug level>]
		
		if (args.length < 3 || args.length > 5) {
			printUsage();
			System.exit(1);
		}


		// if the arguments format is valid ( correct amount of arguments
		else {
			// parseIn the cache indicator to see if cache is used
			cacheIndicator = Integer.parseInt(args[0]);
 
			// if there are 3 arguments
			if (args.length == 3) {
				// if cache is used, then
				if (cacheIndicator != 0) {
					// if cacheIndicator is not 0, check if it is 1
					if (cacheIndicator != 1) {
						System.err.println("Type 0 or 1 to indicate if cache is used, other number is not accepted");
						printUsage();
						System.exit(1);
					} else {// if cacheIndicator is not 0, but it is 1, that means cache is used
						System.err.println("Cache is used, so cache size must be specified as well");
						printUsage();
						System.exit(1);

					}

				}

			}
			// the btree file and query file
			btreeFile = args[1];
			queryFile = args[2];

			//if there are 4 arguments
			if(args.length == 4) {
				//if cache !=0, then cache might be used
				if(cacheIndicator != 0) {
					// if cacheIndicator is not 0, check if it is 1
					if (cacheIndicator != 1) {
						System.err.println("Type 0 or 1 to indicate if cache is used, other number is not accepted");
						printUsage();
						System.exit(1);
					}
					else {// if cacheIndicator is not 0, but it is 1, that means cache is used
						//Now cache is used, specify its size
						cacheSize = Integer.parseInt(args[3]);

					}

				}

				//if cacheIndicator is 0, cache is not used
				if(cacheIndicator == 0) {
					debugLevel = Integer.parseInt(args[3]);//parse in debug level, MIGHT BE WRONG
					//check if debugLevel is valid
					if(debugLevel !=0 && debugLevel !=1) {

						System.err.println("Debug level must be 0 or 1");
						printUsage();
						System.exit(1);
					}




				}


			}

			//if there are 5 arguments
			if(args.length == 5) {
				//parse in debuglevel and size
				debugLevel = Integer.parseInt(args[4]);
				cacheSize = Integer.parseInt(args[3]);
				//check if debug level is valid
				if(debugLevel != 0 && debugLevel != 1) {

					System.err.println("Debug level must be 0 or 1.");
					printUsage();
					System.exit(1);

				}




			}

			parseQuery(queryFile,btreeFile);
		}

	}


	/*
	 *  convert query sequences into binary numbers
	 *  takes in query file and btree file as the parameters
	 */
	private static void parseQuery(String QFile, String BFile ) throws ClassNotFoundException, IOException{

		BTreeDiskOperations.setFileName(BFile);
		int degree = BTreeDiskOperations.readMetaData(0);
		int sequence = BTreeDiskOperations.readMetaData(4);
		int rootLength = BTreeDiskOperations.readMetaData(8);
		BTreeNode root = (BTreeNode) BTreeDiskOperations.readRoot(rootLength);
		// Make BTree based on cache
		if (cacheIndicator == 0) {
		btree = new BTree(degree, BFile, sequence);
		} else {
			btree = new BTree(degree, BFile, sequence, cacheSize);
		}
		
		btree.root = root;
		//this string holds binary representation of the query letters
		String binaryRepresentation = "";
		File file = new File(QFile);

		try {
			Scanner fileScan = new Scanner(file);

			while(fileScan.hasNext()) {
				String queryLetter = fileScan.next(); //this variable will hold the letter of query
				for(int i=0; i<queryLetter.length(); i++){


					//convert the query letter into binary representations
					binaryRepresentation += Dna2Bin.dnabin(queryLetter.substring(i, i+1));
				}

				Long binaryLong = Long.parseLong(binaryRepresentation,2);

				if (btree.hasCache == false) {

					int duplicate = btree.searchBTree(btree.root, binaryLong);

					if(duplicate >0) {

						System.out.println(queryLetter.toLowerCase() + ": " + duplicate);
					}
				} else {
					
					int duplicate = btree.searchBTreeWithCache(btree, btree.root, binaryLong);

					if(duplicate >0) {

						System.out.println(queryLetter.toLowerCase() + ": " + duplicate);
					}
				}
				binaryRepresentation = "";// empty the binaryString for next use .

			}




			fileScan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}




	// printUsage
	private static void printUsage() {
		System.err.println("Usage: java GeneBankSearch " + "<0/1(no/with Cache)> <btree file> <query file> "
				+ "[<cache size>] [<debug level>]\n");
		// System.exit(1);
	}
}
