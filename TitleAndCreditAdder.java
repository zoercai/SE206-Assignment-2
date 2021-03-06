package mediaPlayer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class TitleAndCreditAdder {

	/*
	 * Change the setout to make it better TODO
	 */


	private JFrame frame = new JFrame("Add Title Scene");

	private JLabel title = new JLabel("Enter your text: ");
	private JTextArea text;
	private JPanel textPanel = new JPanel(new FlowLayout());

	private JLabel size = new JLabel("Size ");
	private String[] sizeStrings = {"Small","Medium","Large"};
	private JComboBox<String> sizeChoice = new JComboBox<String>(sizeStrings);
	private JPanel sizePanel = new JPanel(new FlowLayout());

	private JLabel font = new JLabel("Font ");
	private String[] fontStrings = {"Font 1","Font 2","Font 3","Font 4","Font 5"};
	private JComboBox<String> fontChoice = new JComboBox<String>(fontStrings);
	private JPanel fontPanel = new JPanel(new FlowLayout());

	private JLabel colour = new JLabel("Colour ");
	private String[] colourStrings = {"White","Blue","Black","Purple","Red"};
	private JComboBox<String> colourChoice = new JComboBox<String>(colourStrings);
	private JPanel colourPanel = new JPanel(new FlowLayout());

	private JLabel position = new JLabel("Position ");
	private String[] vertical = {"Top","Centre","Bottom"};
	private JComboBox<String> positionChoiceVertical = new JComboBox<String>(vertical); // Always centred horizontally
	private JPanel posPanel = new JPanel(new FlowLayout());

	private JLabel duration = new JLabel("Duration (seconds) ");  // Limit of 10
	private Integer[] durationStrings = {1,2,3,4,5,6,7,8,9,10};
	private JComboBox<Integer> durationChoice = new JComboBox<Integer>(durationStrings);
	private JPanel durPanel = new JPanel(new FlowLayout());

	private JButton enter = new JButton("Enter");
	private JButton preview = new JButton("Preview");
	private JPanel buttons = new JPanel(new FlowLayout());

	private final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	private final EmbeddedMediaPlayer previewArea = mediaPlayerComponent.getMediaPlayer();   // Maybe have a video player that will play for their chosen duration

	private JPanel panelCont = new JPanel();

	private String videoLocation;
	private String saveLocation;
	private Boolean _isTitle;
	private Boolean _isEdit;

	public TitleAndCreditAdder(boolean isTitle, boolean edit, String original, String editText, String name) { // TODO check input is video?
		_isTitle = isTitle;
		_isEdit = edit;
		if (_isTitle == false && _isEdit == false) {
			frame.setTitle("Add Credit Scene");
		} else if (_isEdit == true ) {
			frame.setTitle("Edit Text");
			videoLocation = original;
			saveLocation = name;
			File del = new File(saveLocation);
			Boolean dod = del.delete();
			if (dod == false) {
			}
			text = new JTextArea(editText);
		}

		if (_isEdit == false) {
			text = new JTextArea(1,10);
		}

		panelCont.setLayout(new BoxLayout(panelCont,BoxLayout.Y_AXIS));
		panelCont.add(Box.createVerticalGlue());




		if (_isEdit == false) {
			JFileChooser fc = new JFileChooser();
			int result = fc.showOpenDialog(null);
			if (result == JFileChooser.CANCEL_OPTION) {
				return;
			}
			File file1 = fc.getSelectedFile(); 
			videoLocation = file1.getAbsolutePath();

			JFileChooser fileSaver = new JFileChooser();
			fileSaver.setSelectedFile(new File(""));
			int res = fileSaver.showDialog(null,"Save");
			if (res == JFileChooser.CANCEL_OPTION) {
				return;
			}
			File file2 = fileSaver.getSelectedFile();
			saveLocation = file2.getAbsolutePath();
			saveLocation = checkSaveName(saveLocation);
			checkExists(saveLocation);
		}

		textPanel.add(title);
		textPanel.add(text); 

		sizePanel.add(size);
		sizePanel.add(sizeChoice);

		fontPanel.add(font);
		fontPanel.add(fontChoice);

		colourPanel.add(colour);
		colourPanel.add(colourChoice);

		posPanel.add(position);
		posPanel.add(positionChoiceVertical);

		durPanel.add(duration);
		durPanel.add(durationChoice);
		buttons.add(preview);
		buttons.add(enter);

		panelCont.add(textPanel);
		panelCont.add(sizePanel);
		panelCont.add(fontPanel);
		panelCont.add(colourPanel);
		panelCont.add(posPanel);
		panelCont.add(durPanel);
		panelCont.add(buttons);

		enter.addActionListener(new EnterListener());
		preview.addActionListener(new PreviewListener());

		frame.add(panelCont,BorderLayout.NORTH);
		frame.add(mediaPlayerComponent,BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}

	private class EnterListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			previewArea.stop();
			String instruction = "avconv -i " + videoLocation + " -strict experimental -vf ";
			instruction = instructionCreator(instruction);

			Object instructduration = durationChoice.getSelectedItem();
			String num = instructduration.toString();
			if (_isTitle == true) {
				instruction+=":draw='lt(t,"+num+")'\" -crf 18 ";
			} else {
				int n = Integer.parseInt(num);
				int end = getLength(videoLocation);
				int t = end - n;
				instruction+=":draw='gt(t,"+t+")'\" -crf 18 ";
			}

			String homeDir = System.getProperty("user.home");
			String logname = homeDir + "/VAMIXlog.txt";
			File log = new File(logname);
			addToLog(log);

			instruction = instruction + saveLocation;
			int frames = getFrameCount();
			TitleAndCreditBackground titleCreator = new TitleAndCreditBackground(instruction,frames);
			titleCreator.execute();
			frame.setVisible(false);
		}
	}

	private class PreviewListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String homeDir = System.getProperty("user.home");
			String temp = homeDir + "/temp.mp4";
			File file = new File(temp);
			PreviewSwing hog = new PreviewSwing();
			hog.execute();
		}
	}

	public int getLength(String vid) {
		try {
			ProcessBuilder titleAdder = new ProcessBuilder("bash", "-c", "avconv -i " + vid + " 2>&1 | grep 'Duration' | awk '{print $2}' | sed s/,//");
			titleAdder.redirectErrorStream(true);
			Process downloadProcess = titleAdder.start();
			BufferedReader stdoutDownload = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
			String line = stdoutDownload.readLine();
			int num = stringTimetToInt(line);
			return num;
		} catch (Exception e) {
			return 0;
		}
	}

	private void addToLog(File log){
		try {
			FileWriter fileWritter = new FileWriter(log.getAbsolutePath(),true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(videoLocation + " " + _isTitle + " " + saveLocation + " " + text.getText() + "\n");
			bufferWritter.close();
		} catch (Exception e) {
		}
	}

	public int stringTimetToInt(String inTime) {
		String[] nums = inTime.split(":");

		String[] seconds = nums[2].split("\\.");
		String sec = seconds[0];

		int hour = Integer.parseInt(nums[0]);
		int minute = Integer.parseInt(nums[1]);
		int second = Integer.parseInt(sec);

		int totalTime = (hour*60*60) + minute * 60 + second;

		return totalTime;
	}

	public String checkSaveName(String save) {
		save.replaceAll("\\s+","");
		String[] ext = videoLocation.split("\\.");
		String extension = ".mp4";
		if (ext.length > 1) {
			extension = "." + ext[ext.length-1];
		}
		save = save + extension;;

		return save;
	}

	public void checkExists(String filename) {
		int status;
		try {
			String chkFileExistsCmd = "test -e " + saveLocation;
			ProcessBuilder checkFileBuilder = new ProcessBuilder(
					"bash", "-c", chkFileExistsCmd);
			checkFileBuilder.redirectErrorStream(true);
			Process checkFileProcess = checkFileBuilder.start();
			status = checkFileProcess.waitFor();
			if (checkFileProcess.exitValue() == 0) { 
				// Option Pane code from http://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
				Object[] options = {"Override",
				"Choose New Name"};
				int n = JOptionPane.showOptionDialog(frame,
						"File name alreay in use. How would you like to continue?","File Exists",
						JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,
						null,options,options[0]);
				if (n == JOptionPane.YES_OPTION) { // Override
					File file = new File(saveLocation);
					file.delete();
				} else if (n == JOptionPane.NO_OPTION) { // New Name
					JFileChooser fileSaver = new JFileChooser();
					fileSaver.setSelectedFile(new File(""));
					fileSaver.showDialog(null,"Save");
					File file2 = fileSaver.getSelectedFile();
					saveLocation = file2.getAbsolutePath();
					saveLocation = checkSaveName(saveLocation);
					checkExists(saveLocation);
				}			
			}
		} catch (Exception e) {

		}
	}

	public int getFrameCount() {
		int time = getLength(videoLocation);
		int frames = getFPS(videoLocation);
		int frameCount = time * frames;
		return frameCount;
	}

	public int getFPS(String vid) {
		try { 
			ProcessBuilder titleAdder = new ProcessBuilder("bash", "-c", "avconv -i " + vid + "  2>&1| grep \",* fps\" | cut -d \",\" -f 5 | cut -d \" \" -f 2");
			titleAdder.redirectErrorStream(true);
			Process downloadProcess = titleAdder.start();
			BufferedReader stdoutDownload = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
			String line = stdoutDownload.readLine();
			String[] f = line.split("\\.");
			int num = Integer.parseInt(f[0]);
			int point = Integer.parseInt(f[1]);
			if (point > 0.5) {
				num++;
			}
			return num;
		} catch (Exception e) {
			return 0;
		}
	}

	public String instructionCreator(String instruction) {
		// colour reading
		Object instructcolour = colourChoice.getSelectedItem();
		if (instructcolour.equals("Blue")) {
			instruction+="\"drawtext=fontcolor=blue:";
		} else if (instructcolour.equals("Black")){
			instruction+="\"drawtext=fontcolor=black:";
		} else if (instructcolour.equals("Purple")){
			instruction+="\"drawtext=fontcolor=purple:";
		} else if (instructcolour.equals("Red")){
			instruction+="\"drawtext=fontcolor=red:";
		} else {
			instruction+="\"drawtext=fontcolor=white:";
		}

		// size reading => Sizes are 30,50,70
		Object instructsize = sizeChoice.getSelectedItem();
		if (instructsize.equals("Small")) {
			instruction+="fontsize=30:";
		} else if (instructsize.equals("Medium")){
			instruction+="fontsize=45:";
		} else {
			instruction+="fontsize=60:";
		}

		// font reading TODO Change names
		Object instructfont = fontChoice.getSelectedItem();
		if (instructfont.equals("Font 1")) {
			instruction+="fontfile=/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-C.ttf:";
		} else if (instructfont.equals("Font 2")){
			instruction+="fontfile=/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-M.ttf:";
		} else if (instructfont.equals("Font 3")){
			instruction+="fontfile=/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-R.ttf:";
		} else if (instructfont.equals("Font 4")){
			instruction+="fontfile=/usr/share/fonts/truetype/ubuntu-font-family/UbuntuMono-B.ttf:";
		} else {
			instruction+="fontfile=/usr/share/fonts/truetype/ubuntu-font-family/Ubuntu-B.ttf:";
		}

		// text reading
		String words = text.getText();
		instruction+="text='"+words+"':";


		// Position reading 
		instruction+="x=(main_w/2-text_w/2):";
		Object instructYposition = positionChoiceVertical.getSelectedItem();
		if (instructYposition.equals("Top")) {
			instruction+="y=h-text_h-30";
		} else if (instructYposition.equals("Centre")){
			instruction+="y=main_h/2-text_h/2";
		} else {
			instruction+="y=main_h+30";
		} 
		return instruction;
	}

	public String intToStringTime(int time) {
		String timeInput = "";
		int hour = time / 3600;
		time = time % 3600;
		int minute = time / 60;
		time = time % 60;
		int second = time;
		if (hour < 10) {
			timeInput = "0" + hour +":";
		} else {
			timeInput = hour +":";
		}
		if (minute < 10) {
			timeInput = timeInput + "0" + minute +":";
		} else {
			timeInput = timeInput + minute +":";
		}
		if (second < 10) {
			timeInput = timeInput + "0" + second;
		} else {
			timeInput = timeInput + second;
		}
		return timeInput;
	}

	public class PreviewSwing extends SwingWorker<Integer, Integer> {
		@Override
		protected Integer doInBackground() throws Exception {
			String homeDir = System.getProperty("user.home");
			String cuttemp = homeDir + "/cuttemp.mp4";
			File cutfile = new File(cuttemp);
			cutfile.delete();
			ProcessBuilder cut ;
			if (_isTitle == true) {
				cut = new ProcessBuilder("bash","-c","avconv -ss 00:00:00 -i "+ videoLocation + 
						" -strict experimental -t 00:00:10 -c:v libx264 -crf 23 " + cutfile.getAbsolutePath());
			} else {
				int len = getLength(videoLocation);
				int lenLess10 = len - 10;
				if (lenLess10 < 0) {
					lenLess10 = 0;
				}
				String end = intToStringTime(len);
				String endLess10 = intToStringTime(lenLess10 );

				cut = new ProcessBuilder("bash","-c","avconv -ss " + endLess10 + " -i " + videoLocation + 
						" -strict experimental -t "+ end + " -c:v libx264 -crf 23 " + cutfile.getAbsolutePath());
			}
			cut.redirectErrorStream(true);
			Process slice = cut.start();
			BufferedReader cutty = new BufferedReader(new InputStreamReader(slice.getInputStream()));
			String linecut = cutty.readLine();
			while (linecut != null) {
				linecut = cutty.readLine();
			}


			String temp = homeDir + "/temp.mp4";
			File file = new File(temp);
			file.delete();
			String instruction = "avconv -i " + cutfile.getAbsolutePath() + " -strict experimental -vf ";
			instruction = instructionCreator(instruction);
			instruction = instruction + "\" " + file.getAbsolutePath();
			ProcessBuilder titleAdder = new ProcessBuilder("bash", "-c", instruction);
			titleAdder.redirectErrorStream(true);
			Process downloadProcess = titleAdder.start();
			BufferedReader stdoutDownload = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
			String line = stdoutDownload.readLine();
			while (line != null) {
				line = stdoutDownload.readLine();
			}
			cutfile.delete();

			previewArea.playMedia(file.getAbsolutePath());
			previewArea.parseMedia();
			file.delete();
			return null;
		}
	}
}