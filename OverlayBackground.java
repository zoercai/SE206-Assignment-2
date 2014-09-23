package mediaPlayer;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

class OverlayBackground extends SwingWorker<Integer, Integer> {
	private int status;
	private String originVideo;
	private String originAudio;
	private String destURL;

	public OverlayBackground(String originVideo,String originAudio,String destURL) {
		this.originVideo = originVideo;
		this.originAudio = originAudio;
		this.destURL = destURL;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		
		//TODO have a progress bar for overlay in a dialog that can be minimized.
		//TODO give warning if no audio signal. -> perhaps in main.
		
		String chkFileExistsCmd = "test -e " + destURL;
		ProcessBuilder checkFileBuilder = new ProcessBuilder("bash", "-c",
				chkFileExistsCmd);
		checkFileBuilder.redirectErrorStream(true);
		Process checkFileProcess = checkFileBuilder.start();
		if (!isCancelled()) {
			status = checkFileProcess.waitFor();
		}
		if (checkFileProcess.exitValue() == 0) { // file exists already
			Object[] confirm = { "Override", "Cancel" };
			int a = JOptionPane
					.showOptionDialog(
							null,
							"Output file name already exists! Would you like to override existing file? Click Cancel if you would like to specify another output file name.",
							"Output File Name Exists!",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, confirm,
							confirm[1]);
			if (a == JOptionPane.YES_OPTION) { // override
				String avconvCmd = "avconv -y -i "+originVideo+" -i "+originAudio+" -filter_complex amix=inputs=2 -crf 18 "+destURL;
				ProcessBuilder avconvBuilder = new ProcessBuilder("bash", "-c",
						avconvCmd);
				avconvBuilder.redirectErrorStream(true);
				Process avconvProcess = avconvBuilder.start();
				if (!isCancelled()) {
					status = avconvProcess.waitFor();
				}
				if (avconvProcess.exitValue() != 0) {
					this.cancel(true);
				} else {
					// checkLog("EXTRACT");
				}
			} else {
				this.cancel(true);
				JOptionPane
						.showMessageDialog(
								null,
								"Error! Overlay was not successful. Please check output file name and make sure it contains the appropriate extension.");
			}

		} else { // file doesn't exist
			// avconv it
			String avconvCmd = "avconv -i "+originVideo+" -i "+originAudio+" -filter_complex amix=inputs=2 -crf 18 "+destURL;
			System.out.println(avconvCmd);
			ProcessBuilder avconvBuilder = new ProcessBuilder("bash", "-c",
					avconvCmd);
			avconvBuilder.redirectErrorStream(true);
			Process avconvProcess = avconvBuilder.start();
			if (!isCancelled()) {
				status = avconvProcess.waitFor();
			}
			if (avconvProcess.exitValue() != 0) {
				this.cancel(true);
				JOptionPane
						.showMessageDialog(
								null,
								"Error! Overlay was not successful. Please check output file name and make sure it contains the appropriate extension.");
			} else {
				// checkLog("EXTRACT");
			}
		}

		return null;
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			JOptionPane.showMessageDialog(null, "Overlay completed!");
		} else if (this.isCancelled()) {
			JOptionPane.showMessageDialog(null, "Overlay not completed.");
		}
		// progressBar.setValue(0);
		// progressBar.setStringPainted(false);
	}

}