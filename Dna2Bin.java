/*
 * A utility class to convert DNA code to Binary or Binary to DNA
 * 
 */

public class Dna2Bin {
	String f;

	public Dna2Bin(String f) {
		this.f = f;
	}

	public static String dnabin(String f) {
		String h = "";
		switch (f) {
		case "a":
			h = "00";
			break;
		case "c":
			h = "01";
			break;
		case "g":
			h = "10";
			break;
		case "t":
			h = "11";
			break;
		case "A":
			h = "00";
			break;
		case "C":
			h = "01";
			break;
		case "G":
			h = "10";
			break;
		case "T":
			h = "11";
			break;
		case "00":
			h = "a";
			break;
		case "01":
			h = "c";
			break;
		case "10":
			h = "g";
			break;
		case "11":
			h = "t";
			break;
		default:
			h = "Error, an illegal character string was entered.";
		}
		
		return h;
	}

}
