import java.awt.EventQueue;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class DictionaryClient {

	private JFrame frmDictionaryClient;
	private JTextField inputField;
	private DataInputStream dis;
	private DataOutputStream dos;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					int portNum = Integer.parseInt(args[1]);
					String serverAddress = args[0];
					InetAddress ip = InetAddress.getByName(serverAddress);
					Socket s = new Socket(ip, portNum);
					System.out.println("connecting with server");
					DictionaryClient window = new DictionaryClient(s);
					window.frmDictionaryClient.setVisible(true);
				} 
				catch(ConnectException e) {
					System.out.println("Invalid Address or Server is not online");
				}
				catch(SocketTimeoutException e) {
					System.out.println("Socket time out exception");
				}
				catch(UnknownHostException e) {
					System.out.println("IP address not valid");
				}
				catch(NumberFormatException e) {
					System.out.println("Invalid parameters");
				}
				catch(NoRouteToHostException e) {
					System.out.println("No route to host exception");
				}
				catch(SocketException e) {
					System.out.println("Error in creating or aceessing a socket");
				}
				catch(InterruptedIOException e) {
					System.out.println("InterruptedIOException exception");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public DictionaryClient() {

	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public DictionaryClient(Socket s) throws IOException {
		this.dis = new DataInputStream(s.getInputStream());
		this.dos = new DataOutputStream(s.getOutputStream());
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDictionaryClient = new JFrame();
		frmDictionaryClient.setTitle("Dictionary Client");
		frmDictionaryClient.setBounds(100, 100, 450, 300);
		frmDictionaryClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDictionaryClient.getContentPane().setLayout(null);
		inputField = new JTextField();
		inputField.setToolTipText("Type Input here");
		inputField.setBackground(new Color(245, 245, 220));
		inputField.setBounds(18, 16, 415, 49);
		inputField.setForeground(new Color(0, 0, 0));
		frmDictionaryClient.getContentPane().add(inputField);
		inputField.setColumns(10);

		JLabel label = new JLabel("");
		label.setBounds(150, 1, 150, 92);
		frmDictionaryClient.getContentPane().add(label);

		JLabel label_1 = new JLabel("");
		label_1.setBounds(300, 1, 150, 92);
		frmDictionaryClient.getContentPane().add(label_1);

		JTextArea displayText = new JTextArea();
		displayText.setLineWrap(true);
		displayText.setEditable(false);
		displayText.setBounds(18, 138, 415, 120);
		displayText.setRows(4);
		JScrollPane scroll = new JScrollPane(displayText);
		scroll.setBounds(18, 138, 415, 120);
		frmDictionaryClient.getContentPane().add(scroll);
		JButton btnDeleteWord = new JButton("Delete Word");
		btnDeleteWord.setBounds(321, 77, 112, 49);
		btnDeleteWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String m = null;
				displayText.selectAll();
				displayText.replaceSelection("");
				String word = inputField.getText();
				try {
					m = proceedDeleting(word.trim());
					displayText.append(m + "\n");
				} catch (IOException e1) {
					displayText.append("Server Error" + "\n");
				}

			}
		});

		JButton btnAddWord = new JButton("Add Word");
		btnAddWord.setBounds(172, 77, 128, 49);
		btnAddWord.setToolTipText("Please use word:meaning format");
		btnAddWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String m = null;
				displayText.selectAll();
				displayText.replaceSelection("");
				String word = inputField.getText();
				try {
					m = proceedAdding(word.trim());
					displayText.append(m + "\n");

				} catch (IOException e1) {
					displayText.append("Server Error" + "\n");
				}
			}
		});

		JButton btnSearchWord = new JButton("Search Word");
		btnSearchWord.setLocation(20, 77);
		btnSearchWord.setSize(127, 49);
		btnSearchWord.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String m = null;
				displayText.selectAll();
				displayText.replaceSelection("");
				String word = inputField.getText();
				try {
					m = proceedSearching(word.trim());
					displayText.append(m + "\n");
				} catch (IOException e1) {
					displayText.append("SERVER ERROR" + "\n");
				}
			}
		});
		frmDictionaryClient.getContentPane().add(btnSearchWord);
		frmDictionaryClient.getContentPane().add(btnAddWord);
		frmDictionaryClient.getContentPane().add(btnDeleteWord);

		JLabel label_2 = new JLabel("");
		label_2.setBounds(150, 185, 150, 92);
		frmDictionaryClient.getContentPane().add(label_2);

		JLabel label_3 = new JLabel("");
		label_3.setBounds(300, 185, 150, 92);
		frmDictionaryClient.getContentPane().add(label_3);
	}

	protected String proceedDeleting(String word) throws IOException {
		if (!word.equals("")) {
			sendToServer("Delete#" + word);
			String meaning = readFromServer();
			return meaning;
		}
		return "no word input";
	}

	protected String proceedAdding(String word) throws IOException {
		if (!word.equals("")) {
			if (word.contains(":")) {
				sendToServer("Add#" + word);
				String meaning = readFromServer();
				return meaning;
			} else {
				return "Please provide input in the form word:meaning or word:meaning1;meaning2.. ";
			}

		}
		return "no word input";
	}

	protected String proceedSearching(String word) throws IOException {
		if (!word.equals("")) {
			sendToServer("Search#" + word);
			String meaning = readFromServer();
			return meaning;
		}
		return "no word input";
	}

	private String readFromServer() throws IOException {
		String received = this.dis.readUTF();
		return received;
	}

	private void sendToServer(String str) throws IOException {
		this.dos.writeUTF(str);

	}

}
