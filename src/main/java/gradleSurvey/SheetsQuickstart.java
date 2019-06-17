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

import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SheetsQuickstart extends JFrame {

	Display one;

	//sets up the jframe, and the panel
	public SheetsQuickstart() {
		setTitle("Google Survey");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		one = new Display();
		add(one);
		// setLayout(null);
		pack();

		setVisible(true);

	}
	//puts the Jframe on the EDT, and calls the frame
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new SheetsQuickstart());
	}

}

class Display extends JPanel implements ActionListener {

	JTextField tf1, tf1b, tf2;
	JLabel l1, l1b, l1c, l3, l4, l4b, l4c, l4d;
	JButton b1, b2, bCheckbox, bMultiChoice, bLinearScale, b4, b4b;
	JSlider s1;
	Student[] student;
	usernames[] user;
	String qName;
	int qNum;
	int currentColumn;
	int questionType;
	static String[] questions;
	static int numm;
	List<List<Object>> spreadsheetData;
	final int WIDTH = 600;
	final int HEIGHT = 400;

	public Display() {
		//loads the first page
		l1 = new JLabel("Welcome to Jacob and Rory's Medway Polling System!");
		l1.setBounds(WIDTH / 2 - 175, HEIGHT / 8, 350, 25);
		l1b = new JLabel(
				"Make sure to make the sheet public, and not apart of the TVDSB school board so anyone can access it");
		l1b.setBounds(WIDTH / 2 - 300, HEIGHT * 3 / 4, 600, 25);
		tf1 = new JTextField(
				"https://docs.google.com/spreadsheets/d/1rrV69Qc-gmfagLKP2AII1xjrWqui6m1ejPeUHuvbrY0/edit#gid=1206381474");
		tf1.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		tf1b = new JTextField("5");
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
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setLayout(null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// checks the source to see what button called the method;
		if (e.getSource() == b1) { // searches for the url, by splitting by /d/
			String[] items = tf1.getText().split("/");
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals("d")) {
					try {
						spreadsheetData = collectData(items[i + 1], tf1b.getText());
					} catch (GeneralSecurityException | IOException e2) {
						e2.printStackTrace();
					}
					tf1.setText("Success!");
					//if the spreadsheet exists, get logins, and validate the emails
					try {
						user = userLogin(spreadsheetData);
						user = validateEmails(spreadsheetData, student, user);
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					//runs the second screen
					columnScreen();
				}
			}
			tf1.setText("That link was not valid, check that everything should work.");

		} else if (e.getSource() == b2) {
			boolean runOnce = true;
			//searches for the question name, then continues onto next screen
			for (int i = 0; i < questions.length; i++) {
				if (questions[i].equals(tf2.getText().toLowerCase()) && runOnce) {
					currentColumn = i;
					qName = tf2.getText();
					qNum = i + 1;
					runOnce = false;
					questionTypeScreen();
				} 
			}
			tf2.setText("There was no Question with that name");

			/*
			 * the next if statements, all do the same, and just run the next screen respectivly
			 */
		} else if (e.getSource() == bCheckbox) {
			questionType = 0;
			answerScreenPrep();
			checkBoxScreen(student, spreadsheetData);
		} else if (e.getSource() == bMultiChoice) {
			questionType = 1;
			answerScreenPrep();
			multiChoiceScreen(user, spreadsheetData);

		} else if (e.getSource() == bLinearScale) {
			questionType = 2;
			answerScreenPrep();
			linearScaleScreen(student, spreadsheetData);
		} else if (e.getSource() == b4) {
			//loops the program back to the column screen so the user can pick a new question to look at data for
			columnScreen();
			b4.setVisible(false);
			l4.setVisible(false);
			l4b.setVisible(false);
			l4c.setVisible(false);
			remove(b4);
			remove(l4);
			remove(l4b);
			remove(l4c);
			if (questionType == 2) {
				l4d.setVisible(false);
				remove(l4d);
			}
		}
	}

	public List<List<Object>> collectData(String spreadsheetID, String num)
			throws GeneralSecurityException, IOException {
		//first the ending letter of the spreadsheet is found through collecting the number of questions at the begining of the program
		numm = Integer.parseInt(num);
		questions = new String[numm];
		char rangeEnd = checkLetter(numm);
		//its then added to the range string
		String rangeEndString = "" + rangeEnd;
		rangeEndString = rangeEndString.toUpperCase();

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		String range = "Form Responses 1!B1:";
		range += rangeEndString;
		
		
		//this retrieves the spreadsheet and returns a List<List<Object>>
		Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		ValueRange response = service.spreadsheets().values().get(spreadsheetID, range).execute();
		List<List<Object>> values = response.getValues();
		return values;
	}

	public static usernames[] userLogin(List<List<Object>> values) {
		Vector<String> names = new Vector();

		for (List row : values) {
			names.add((String) row.get(0));
		}
		names.remove(0);
		usernames[] user = new usernames[names.size()];
		for (int i = 0; i < user.length; i++) {
			user[i] = new usernames();
			user[i].Username = names.get(i);
		}
		return user;
	}

	public static usernames[] validateEmails(List<List<Object>> values, Student[] students, usernames[] user)
			throws FileNotFoundException {
		students = getEmails("update.txt");
		boolean ranOnce = false;

		for (List row : values) {
			if (!ranOnce) {
				for (int i = 0; i < numm; i++) {
					row.set(i, row.get(i).toString().toLowerCase());
					questions[i] = (String) row.get(i);
				}
				ranOnce = true;
			}
			String email;
			for (int i = 0; i < user.length; i++) {
				for (int j = 0; j < students.length; j++) {
					int fNameLength = 4;
					if (students[j].fName.length() < 4) {
						fNameLength = students[j].fName.length();
					}
					int lNameLength = 4;
					if (students[j].lName.length() < 4) {
						lNameLength = students[j].lName.length();
					}

					if (user[i].Username.contentEquals(students[j].lName.substring(0, lNameLength)
							+ students[j].fName.substring(0, fNameLength)
							+ students[j].sNum.substring(students[j].sNum.length() - 3, students[j].sNum.length()))
							&& students[j].used == false) {
						user[i].valid = true;
						students[j].used = true;
					}
				}
			}

		}
		return user;
	}

	public static Student[] getEmails(String fileName) throws FileNotFoundException {
		//this method searches for each email in update.txt
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
		
		//splits up the line, into last name, first name, then student number
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

		//removing the old screen
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

		//adding the new screen
		tf2 = new JTextField("Enter the title of the Question you would like data for.");
		tf2.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		b2 = new JButton("Confirm");
		b2.setBounds((WIDTH / 2 - 50), (HEIGHT / 2) - 25, 100, 50);
		b2.addActionListener(this);
		add(tf2);
		add(b2);
	}

	public void answerScreenPrep() {
		//a general cleanup method, as all of the question types use these lines, the code is just condensed
		//removing old screen
		l3.setVisible(false);
		bCheckbox.setVisible(false);
		bMultiChoice.setVisible(false);
		bLinearScale.setVisible(false);

		remove(l3);
		remove(bCheckbox);
		remove(bMultiChoice);
		remove(bLinearScale);

		//adding new screen
		l4 = new JLabel(qName);
		l4.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		b4 = new JButton("Done");
		b4.setBounds((WIDTH / 2 + 100), (HEIGHT / 2) + 25, 100, 50);
		b4.addActionListener(this);

		add(l4);
		add(b4);

		validate();
	}

	public void questionTypeScreen() {
		//removing old screen
		b2.setVisible(false);
		tf2.setVisible(false);
		tf2.setText("invalid");
		remove(b2);
		remove(tf2);
		
		//adding new screen
		l3 = new JLabel("What type of Question is this?");
		l3.setBounds(WIDTH / 2 - 250, HEIGHT / 4, 500, 25);
		
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
	}

	public void multiChoiceScreen(usernames[] user, List<List<Object>> values) {
		int totalValid = 0;
		for (int i = 0; i < user.length; i++) {
			if (user[i].valid == true) {
				totalValid++;
			}
		}

		
		Vector<String> responses = new Vector();
		Vector<String> titles = new Vector();
		Vector<Integer> total = new Vector();

		for (List row : values) {
			responses.add((String) row.get(qNum - 1));
		}

		responses.remove(0);

		for (int i = 0; i < responses.size(); i++) {
			if (!titles.contains(responses.get(i))) {
				titles.add(responses.get(i));
			}
		}

		for (int j = 0; j < titles.size(); j++) {
			total.add(0);
		}

		for (int i = 0; i < user.length; i++) {
			for (int j = 0; j < titles.size(); j++) {
				if (user[i].valid == true) {
					if (responses.get(i).equals(titles.get(j))) {
						total.set(j, total.get(j) + 1);
					}
				}
			}
		}

		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 25);

		String answer = titles.get(0) + ": " + total.get(0);
		for (int i = 1; i < titles.size(); i++) {
			answer = answer + " | " + titles.get(i) + ": " + total.get(i);
		}

		l4c = new JLabel(answer);
		l4c.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 50, 500, 25);

		add(l4b);
		add(l4c);
	}

	public void checkBoxScreen(Student[] students, List<List<Object>> values) {
		int totalValid = 0;
		for (int i = 0; i < user.length; i++) {
			if (user[i].valid == true) {
				totalValid++;
			}
		}

		Vector<String> data = new Vector();
		Vector<String> responses = new Vector();
		Vector<String> titles = new Vector();
		Vector<Integer> total = new Vector();
		Vector<Integer> remove = new Vector();

		for (List row : values) {
			data.add((String) row.get(qNum - 1));
		}
		data.remove(0);
		for (int i = 0; i < data.size(); i++) {
			if (user[i].valid == false) {
				remove.add(i);
			}
		}
		for (int i = 0; i < remove.size(); i++) {
			data.remove(remove.get(i)-i);
		}
		String[] split = null;
		String data1 = null;
		for (int i = 0; i < data.size(); i++) {
			data1 = data.toString();
		}
		String data2 = data1.substring(1, data1.length() - 1);
		split = data2.split(", ");

		for (int i = 0; i < split.length; i++) {
			responses.add(split[i]);
			System.out.println(responses.get(i));
		}

		for (int i = 0; i < responses.size(); i++) {
			if (!titles.contains(responses.get(i))) {
				titles.add(responses.get(i));
			}
		}

		for (int j = 0; j < titles.size(); j++) {
			total.add(0);
		}

		for (int i = 0; i < responses.size(); i++) {
			for (int j = 0; j < titles.size(); j++) {

				if (responses.get(i).equals(titles.get(j))) {
					total.set(j, total.get(j) + 1);

				}
			}
		}

		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 25);

		String answer = titles.get(0) + ": " + total.get(0);
		for (int i = 1; i < titles.size(); i++) {
			answer = answer + " | " + titles.get(i) + ": " + total.get(i);
		}

		l4c = new JLabel(answer);
		l4c.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 50, 500, 25);

		add(l4b);
		add(l4c);
	}

	public void linearScaleScreen(Student[] students, List<List<Object>> values) {
		int totalValid = 0;
		double average = 0;

		Vector<String> responses = new Vector();
		Vector<Integer> numbers = new Vector();
		Vector<Integer> amount = new Vector();
		boolean notFound = true;
		//changes the List<List<Object>> into a Vector<String>
		for (List row : values) {
			responses.add((String) row.get(qNum - 1));
		}
		//removes the question in the vector of responses
		responses.remove(0);

		for (int i = 0; i < user.length; i++) {
			//if the user is valid, 
			if (user[i].valid) {
				//add to total valid users
				totalValid++;
				//change response into double, and sum it
				double answer = Double.parseDouble(responses.get(i));
				average += answer;

				//if the number has already occured, add it to total
				for (int j = 0; j < numbers.size(); j++) {
					if (answer == numbers.get(j)) {
						notFound = false;
						amount.set(j, amount.get(j) + 1);
					}
				}
				//if it hasn't, make a new int to represent it
				if (notFound) {
					numbers.add((int) answer);
					amount.add(1);
				}
			}
		}
		average = average / totalValid;

		//adding new screen
		l4b = new JLabel("Total Valid Entries: " + totalValid);
		l4b.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 25, 500, 50);
		l4c = new JLabel("Average Response" + average);
		l4c.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 50, 500, 25);

		l4d = new JLabel("Responses:");
		l4d.setBounds(WIDTH / 2 - 250, HEIGHT / 4 + 75, 500, 25);

		String l4dS = "";

		//sorting the list of responses
		for (int i = 0; i < numbers.size() - 1; i++) {
			if (numbers.get(i) > numbers.get(i + 1)) {
				int storage = numbers.get(i);
				numbers.set(i, numbers.get(i + 1));
				numbers.set(i + 1, storage);

				storage = amount.get(i);
				amount.set(i, amount.get(i + 1));
				amount.set(i + 1, storage);
				i = -1;
			}
		}
		
		//printing out each response catagory
		for (int i = 0; i < numbers.size(); i++) {
			l4dS = l4dS + numbers.get(i) + ": " + amount.get(i) + " responses";
			if (i + 1 != numbers.size()) {
				l4dS = l4dS + " | ";
			}
		}

		//more adding to screen
		l4d.setText(l4dS);
		add(l4b);
		add(l4d);
		add(l4c);

	}

	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets
		InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String... args) throws IOException, GeneralSecurityException, FileNotFoundException {
		// Build a new authorized API client service.
		SwingUtilities.invokeLater(() -> new SheetsQuickstart());

	}

	//simply changes a num to a char that is matched, alphanumerically
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
