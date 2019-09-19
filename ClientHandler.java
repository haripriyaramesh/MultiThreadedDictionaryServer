import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ClientHandler extends Thread {
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;
	int clientNumber;
	String fileName;

	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String filename, int count) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.fileName = filename;
		this.clientNumber = count;
	}

	@Override
	public void run() {
		String received;
		String toreturn;
		while (true) {
			try {
				// receive the answer from client
				received = dis.readUTF();
				String[] receivedArr = received.split("#");
				String command = receivedArr[0];
				String word = receivedArr[1];
				// write on output stream based on the
				// answer from the client
				switch (command) {

				case "Search":
					JSONObject dataSet1 = readFile();
					toreturn = query(word, dataSet1, dos);
					dos.writeUTF(toreturn);
					break;

				case "Add":
					JSONObject dataSet2 = readFile();
					String[] wordArr = word.split(":");
					word = wordArr[0].trim();
					String meaning = wordArr[1];
					toreturn = query(word, dataSet2, dos);
					if (toreturn.contains("Does not exist")) {
						insert(word, meaning, dataSet2, dos);
						dos.writeUTF(word + " :Added successfully");
					} else
						dos.writeUTF(word + " :Already exists in dictionary");
					break;

				case "Delete":
					JSONObject dataSet3 = readFile();
					toreturn = query(word, dataSet3, dos);
					if (!(toreturn.contains("Does not exist"))) {
						remove(word, dataSet3, dos);
						dos.writeUTF(word + " :Deleted successfully");
					} else
						dos.writeUTF(word + " :Does not exist in Dictionary");

					break;

				default:
					dos.writeUTF("Invalid input");
					break;
				}
			} catch (FileNotFoundException e) {
				try {
					dos.writeUTF("Server Error : dictionary file not found");
				} catch (IOException e1) {
					System.out.println("Error");
				}
				System.out.println("Dictionary file not found!");

			} catch (IOException e) {
				System.out.println("Client " + this.clientNumber + " " + this.s + " sends exit...");
				System.out.println("Closing this connection.");
				try {
					this.s.close();
					break;

				} catch (IOException e1) {
					System.out.println("Exception has occured " + e.getMessage());
				}
				System.out.println("Connection closed");
			} catch (JSONException e) {
				try {
					dos.writeUTF("Server Error : Json exception");
				} catch (IOException e1) {
					System.out.println("Error");
				}
				System.out.println("JSON exception has occurred " + e.getMessage());
			}
		}

		try {
			// closing resources
			this.dis.close();
			this.dos.close();

		} catch (IOException e) {
			System.out.println("error in closing data streams " + e.getMessage());
		}
	}

	/**
	 * This method is the process of query in dictionary.
	 * 
	 * @throws JSONException, JSONException
	 */
	private String query(String word, JSONObject dictData, DataOutputStream output) throws IOException, JSONException {
		String m = "";
		// If the word is in the dictionary, just display the meaning(s) of that word.
		if (dictData.has(word.toLowerCase())) {
			JSONArray w = dictData.getJSONArray(word.toLowerCase());
			m = w.getString(0);
			m = m.replaceAll(";", "\n");
		}
		// If the word is not found, display error message.
		else {
			m = "Does not exist";
		}
		return m;
	}

	/**
	 * This method is for loading the dictionary data format.
	 * @throws JSONException, FileNotFoundException
	 */
	private JSONObject readFile() throws FileNotFoundException, JSONException {
		// "/Users/haripriyaramesh/Desktop/dictionary.json"
		JSONTokener dicRead = new JSONTokener(new FileReader(this.fileName));
		JSONObject dataSet = new JSONObject(dicRead);
		return dataSet;
	}

	/**
	 * This method is the process of addition in dictionary. The synchronized
	 * keyword is for keeping the dictionary in a consistent state.
	 * 
	 * @throws JSONException
	 */
	private synchronized void insert(String word, String meaning, JSONObject dictData, DataOutputStream output)
			throws IOException, JSONException {
		JSONArray newWordMeaning = new JSONArray();
		newWordMeaning.put(meaning);
		dictData.put(word.toLowerCase(), newWordMeaning);
		updateFile(dictData);
	}

	/**
	 * This method is the process of deletion in dictionary. The synchronized
	 * keyword is for keeping the dictionary in a consistent state.
	 */
	private synchronized void remove(String word, JSONObject dictData, DataOutputStream output) throws IOException {
		dictData.remove(word.toLowerCase());
		// Updating the dictionary.
		updateFile(dictData);
	}

	/**
	 * This method is for updating the dictionary data format.
	 */
	private void updateFile(JSONObject dictData) {
		FileWriter outputStream = null;
		try {
			outputStream = new FileWriter(fileName, false);
			outputStream.write(dictData.toString());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			System.out.println("Error writing to the dictionary." + "\n");
		}
	}

}
