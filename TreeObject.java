import java.io.Serializable;

public class TreeObject implements Serializable {
	protected Long key;  //one data key
	protected int frequency;  //record of duplicates of same data
	
	//Constructor
	public TreeObject(Long input) {
		key = input;
		frequency = 1;
	}

	public int getDuplicates() {
		return this.frequency;
	}
	
	public Long getKey() {
		return this.key;
	}
	
	public void setKey(Long dataKey) {
		this.key = dataKey;
	}
	
	public void incrementFrequency() {
		frequency++;
	}
	
	public int getFrequency() {
		return frequency;
	}
	

	public String toString() {
		String retVal = "";
		String binaryString = Long.toBinaryString(key);
		String paddedZeroes = "";
		for (int i = binaryString.length(); i <= (2 * GeneBankCreateBTree.sequenceLength - 1); i++) {
			paddedZeroes += "0";
		}
		binaryString = paddedZeroes + binaryString;
		while (binaryString.length() > 1) {
		String bin = binaryString.substring(0, 2);
		retVal += Dna2Bin.dnabin(bin);
		binaryString = binaryString.substring(2, binaryString.length());
		}
		return retVal + ": " + frequency;
	}

}
