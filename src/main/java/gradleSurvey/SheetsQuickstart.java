package gradleSurvey;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SheetsQuickstart extends JFrame implements ActionListener {

	JTextField tf1, tf1b, tf2;
	JLabel l1, l1b, l1c, l3, l4, l4b, l4c;
	JButton b1, b2, bCheckbox, bMultiChoice, bLinearScale, b4;
	JSlider s1;
	Student[] student;
	String qName;
	int qNum;
	int currentColumn;
	int questionType;
	static String[] questions;
	static int numm;
	List<List<Object>> spreadsheetData;
	final int WIDTH = 600;
	final int HEIGHT = 400;

	SheetsQuickstart() {
		l1 = new JLabel("Welcome to Jacob's and Rory's Medway Polling System!");
		l1.setBounds(WIDTH / 2 - 175, HEIGHT / 8, 350, 25);
		l1b = new JLabel(
				"Make sure to make the sheet public, and not apart of the TVDSB school board so anyone can access it");
		l1b.setBounds(WIDTH / 2 - 300, HEIGHT * 3 / 4, 600, 25);
		tf1 = new JTextField("Enter your URL for the Google Sheet");
		tf1.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		tf1b = new JTextField("Additionally, Provide the number of questions, Including the username Question");
		tf1b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 50, 500, 25);
		l1c = new JLabel("Make sure to copy update.txt into the folder, GoogleSurvey");
		l1c.setBounds(WIDTH / 2 - 250, HEIGHT / 3 + 50, 500, 25);
		b1 = new JButton("Confirm");
		b1.setBounds((WIDTH / 2 - 50), (HEIGHT / 2) + 25, 100, 50);
		b1.addActionListener(this);
		add(l1b);
		add(l1);
		add(b1);
		add(tf1);
		add(tf1b);
		add(l1c);
		setSize(WIDTH, HEIGHT);
		setLayout(null);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == b1) {
			String[] items = tf1.getText().split("/");
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals("d")) {
					// optional test link?? - here
					try {
						spreadsheetData = collectData(items[i + 1], tf1b.getText());
					} catch (GeneralSecurityException | IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					tf1.setText("Success!");
					try {
						student = validateEmails(spreadsheetData, student);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					columnScreen();
				}
			}
			tf1.setText("That link was not valid, check that everything should work.");

		} else if (e.getSource() == b2) {
			boolean runOnce = true;
			for (int i = 0; i < questions.length; i++) {
				if (questions[i].equals(tf2.getText().toLowerCase()) && runOnce) {
					currentColumn = i;
					qName = tf2.getText();
					qNum = i +1;
					runOnce = false;
					questionTypeScreen();
				} else if (tf2.getText().equals("continue")) {
					currentColumn = i;
					runOnce = false;
					questionTypeScreen();
				}
			}
			tf2.setText("There was no Question with that name");

		} else if (e.getSource() == bCheckbox) {
			questionType = 0;
			System.out.println("hit2");
			checkBoxScreen(student);
		} else if (e.getSource() == bMultiChoice) {
			questionType = 1;
			System.out.println("hit2");
			multiChoiceScreen(student, spreadsheetData);
		} else if (e.getSource() == bLinearScale) {
			questionType = 2;
			System.out.println("hit2");
			linearScaleScreen(student);
		} else if (e.getSource() == b4) {
			columnScreen();
			remove(b4);
			remove(l4);
			remove(l4b);
			remove(l4c);
		}

	}

	public List<List<Object>> collectData(String spreadsheetID, String num)
			throws GeneralSecurityException, IOException {
		numm = Integer.parseInt(num);
		questions = new String[numm];
		char rangeEnd = checkLetter(numm);
		String rangeEndString = "" + rangeEnd;
		rangeEndString = rangeEndString.toUpperCase();

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		String range = "Form Responses 1!B1:";
		range += rangeEndString;
		System.out.print(range);

		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ValueRange response = service.spreadsheets().values().get(spreadsheetID, range).execute();
		List<List<Object>> values = response.getValues();
		// if (values == null || values.isEmpty()) {
		// tf1.setText("The sheet is empty");
		// } else {
		// tf1.setText("Success");
		for (List row : values) {
			// Print columns A and E, which correspond to indices 0 and 3.
			System.out.printf("%s, %s\n", row.get(0), row.get(3));
		}
		//
		// }
		return values;
	}
////////////////////////////////////Not Working Need to fix//////////////////////////////////
	public static Student[] validateEmails(List<List<Object>> values, Student[] students) throws FileNotFoundException {
		students = getEmails("update.txt");
		boolean ranOnce = false;
		for (List row : values) {
			if (!ranOnce) {
				for (int i = 0; i < numm; i++) {
					row.set(i, row.get(i).toString().toLowerCase());
					questions[i] = (String) row.get(i);
					System.out.println(row.get(i));
				}
				ranOnce = true;
			}
			for (int i = 0; i < students.length; i++) {
				if (row.get(0).equals(students[i].lName.substring(0, 3) + students[i].fName.substring(0, 3)
						+ students[i].sNum.substring(students[i].sNum.length() - 3, students[i].sNum.length() - 1))) {
					students[i].valid = false;
				}
				System.out.println(students[i].fName + students[i].lName + students[i].sNum +students[i].valid);
			}

		}
		return students;
	}

	public static Student[] getEmails(String fileName) throws FileNotFoundException {

		File file = new File(fileName);
		Scanner parser = new Scanner(new FileReader(file));

		int length = 0;
		String[] item = new String[length];
		String[] item2 = new String[length];

		// checks if there is another entry, and puts it in the array
		while (parser.hasNextLine()) {
			length++;
			for (int i = 0; i < item.length; i++) {
				item2[i] = item[i];
			}
			item = new String[length];
			for (int i = 0; i < item2.length; i++) {
				item[i] = item2[i];
			}
			item2 = new String[length];
			item[length - 1] = parser.nextLine();
		}
		Student[] stu = new Student[length];
		for (int i = 0; i < length; i++) {
			stu[i] = new Student();
			String[] items = item[i].split(",");
			stu[i].lName = items[0].toLowerCase();
			stu[i].fName = items[1].toLowerCase();
			stu[i].sNum = items[3];
		}
		return stu;
	}

	public void columnScreen() { // displays the second screen, where the user picks the column

		System.out.println("hit0");
		b1.setVisible(false);
		tf1.setVisible(false);
		tf1b.setVisible(false);
		l1b.setVisible(false);
		l1c.setVisible(false);
		remove(b1);
		remove(tf1);
		remove(tf1b);
		remove(l1b);
		remove(l1c);

		tf2 = new JTextField("Enter the title of the Question you would like data for.");
		tf2.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		b2 = new JButton("Confirm");
		b2.setBounds((WIDTH / 2 - 50), (HEIGHT / 2) - 25, 100, 50);
		b2.addActionListener(this);
		add(tf2);
		add(b2);
	}

	public void questionTypeScreen() {
		b2.setVisible(false);
		tf2.setVisible(false);
		tf2.setText("invalid");
		remove(b2);
		remove(tf2);

		l3 = new JLabel("What type of Question is this?");
		l3.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		// bCheckbox, bMultiChoice, bLinearScale;
		bCheckbox = new JButton("Checkbox");
		bCheckbox.setBounds((WIDTH / 2 - 250), (HEIGHT / 2) - 25, 150, 50);
		bCheckbox.addActionListener(this);

		bMultiChoice = new JButton("MutliChoice");
		bMultiChoice.setBounds((WIDTH / 2 - 75), (HEIGHT / 2) - 25, 150, 50);
		bMultiChoice.addActionListener(this);

		bLinearScale = new JButton("LinearScale");
		bLinearScale.setBounds((WIDTH / 2 + 100), (HEIGHT / 2) - 25, 150, 50);
		bLinearScale.addActionListener(this);
		add(l3);
		add(bCheckbox);
		add(bMultiChoice);
		add(bLinearScale);
		System.out.println("hit1");
	}

	public void multiChoiceScreen(Student[] students, List<List<Object>> values) {
		System.out.println("hit3");
		int totalValid = 0;

		l3.setVisible(false);
		bCheckbox.setVisible(false);
		bMultiChoice.setVisible(false);
		bLinearScale.setVisible(false);

		remove(l3);
		remove(bCheckbox);
		remove(bMultiChoice);
		remove(bLinearScale);
		
		Vector <String> responses = new Vector();

		for (List row : values) {
				responses.add((String) row.get(qNum-1)+"x");
		}
		for (int i = 0; i <students.length;i++) {
			System.out.println(students[i].valid);
		}
		
		System.out.println(responses);
		l4 = new JLabel(qName);
		l4.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 25);

		b4 = new JButton("Done");
		b4.setBounds((WIDTH / 2 - 50), (HEIGHT / 2) + 25, 100, 50);
		b4.addActionListener(this);

		add(l4);
		add(l4b);
		add(b4);

	}

	public void checkBoxScreen(Student[] students) {
		int totalValid = 0;

		l3.setVisible(false);
		bCheckbox.setVisible(false);
		bMultiChoice.setVisible(false);
		bLinearScale.setVisible(false);

		remove(l3);
		remove(bCheckbox);
		remove(bMultiChoice);
		remove(bLinearScale);

		l4 = new JLabel(qName);
		l4.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 25);

		b4 = new JButton("Done");
		b4.setBounds((WIDTH / 2 + 100), (HEIGHT / 2) + 25, 100, 50);
		b4.addActionListener(this);

		add(l4);
		add(l4b);
		add(b4);

	}

	public void linearScaleScreen(Student[] students) {
		int totalValid = 0;

		l3.setVisible(false);
		bCheckbox.setVisible(false);
		bMultiChoice.setVisible(false);
		bLinearScale.setVisible(false);

		remove(l3);
		remove(bCheckbox);
		remove(bMultiChoice);
		remove(bLinearScale);

		l4 = new JLabel(qName);
		l4.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 25);

		b4 = new JButton("Done");
		b4.setBounds((WIDTH / 2 + 100), (HEIGHT / 2) + 25, 100, 50);
		b4.addActionListener(this);

		add(l4);
		add(l4b);
		add(b4);

	}

	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT
	 *            The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException
	 *             If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	/**
	 * Prints the names and majors of students in a sample spreadsheet:
	 * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
	 */
	public static void main(String... args) throws IOException, GeneralSecurityException, FileNotFoundException {
		// Build a new authorized API client service.
		SwingUtilities.invokeLater(() -> new SheetsQuickstart());

	}

	public static char checkLetter(int letter) {
		switch (letter) {
		case 0:
			return 'a';
		case 1:
			return 'b';
		case 2:
			return 'c';
		case 3:
			return 'd';
		case 4:
			return 'e';
		case 5:
			return 'f';
		case 6:
			return 'g';
		case 7:
			return 'h';
		case 8:
			return 'i';
		case 9:
			return 'j';
		case 10:
			return 'k';
		case 11:
			return 'l';
		case 12:
			return 'm';
		case 13:
			return 'n';
		case 14:
			return 'o';
		case 15:
			return 'p';
		case 16:
			return 'q';
		case 17:
			return 'r';
		case 18:
			return 's';
		case 19:
			return 't';
		case 20:
			return 'u';
		case 21:
			return 'v';
		case 22:
			return 'w';
		case 23:
			return 'x';
		case 24:
			return 'y';
		case 25:
			return 'z';
		default:
			return 'a';
		}

	}

}