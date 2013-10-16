package phil.projects.mongo.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import phil.projects.mongo.MongoLoadConfig;

/**
 * The GUI for the Generator, based on JPanel. 
 * 
 * @author ptaprogg
 *
 */
public class MongoLoadUI extends JPanel implements ActionListener {
	
	private static Logger logger = Logger.getLogger(MongoLoadUI.class);
	
	public static final long serialVersionUID = 2013101001;

	//The UI components used in actionPerformed()
	private JLabel label;
	private JButton startButton;
	private JProgressBar progressBar;
	private JTextField textField;
	
	//The config object used for the runner
	private MongoLoadConfig config;
	
	//The runner used for generating the documents
	private MongoRunner runner;

	/**
	 * SwingWorker used to update the progress bar. It monitors the runner.
	 * 
	 * @author ptaprogg
	 *
	 */
	private class ProgressUpdater extends SwingWorker<Void, Void> {
		
		//The runner to monitor
		private MongoRunner runner;
		
		//The UI components used during the update cycle
		private JProgressBar progressBar;
		private int progress;
		
		@SuppressWarnings("unused")
		private ProgressUpdater() {};
		
		public ProgressUpdater(JProgressBar progressBar, MongoRunner runner) {
			this.progressBar = progressBar;
			this.runner = runner;
			this.progress = 0;
		}

		//Worker will run this in the background
		@Override
		protected Void doInBackground() throws Exception {
			logger.debug("Updater thread started");
			//Continue updating until progress reaches 100
			while (progress < 100) {
				//Get progress from runner
				progress = runner.getProgress();
				//Update progress bar with value and string
				progressBar.setValue(progress);
				progressBar.setString(Integer.toString(progress) + "%");

				//Sleep for a bit to let the runner run
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException ie) {
					//ignore
				}
			}
			return null;
		}
		
		//Called once the thread finishes. Displays confirmation dialog and restores UI to interactive state
		@Override
		public void done() {
			//Dislpay the dialog
			JOptionPane.showMessageDialog(null, "Operation complete", "Complete", JOptionPane.INFORMATION_MESSAGE);
			
			//Restore UI and repaint
			restoreUI();
			logger.debug("Updater thread finished");
		}
		
	}

	/**
	 * Standard constructor to initialise UI
	 * 
	 * @param config The MongoLoadConfig to use for the loader thread
	 */
	public MongoLoadUI(MongoLoadConfig config) {
		//Set up the GridLayout for this
		super(new GridLayout(3,1));
		this.config = config;
		
		//Create and initialize UI components
		startButton = new JButton("Start Generation");
		startButton.addActionListener(this);
		label = new JLabel("Please enter number of documents to create below");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("0%");
		textField = new JTextField();
		textField.setText(String.valueOf(config.getNumdocs()));
		add(label);
		add(textField);
		add(startButton);

		//Set up the runner
		runner = new MongoRunner(config);
		logger.info("UI initialized");
	}

	/**
	 * Entry method for the UI.
	 */
	public void createAndShow() {
		JFrame frame = new JFrame("Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	protected void restoreUI() {
		label.setText("Please enter number of documents to create below");
		remove(progressBar);
		add(textField,1);
		startButton.setEnabled(true);
		repaint();
	}

	//When the button is clicked, start work. There is only one button, so no need to filter events.
	@Override
	public void actionPerformed(ActionEvent e) {
		logger.trace("Button clicked");
		int numDocs = 0;
		//Check if the input field is a number
		try {
			numDocs = Integer.parseInt(textField.getText());
		}
		catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Please enter a numeric value", "Invalid value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (numDocs == 0) {
			JOptionPane.showMessageDialog(null, "Please enter value greater than 0", "Invalid value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		//Not what we have a number, update config
		config.setNumdocs(numDocs);
		
		//Set UI to non-interactive
		label.setText("In Progress...");
		startButton.setEnabled(false);
		remove(textField);
		add(progressBar, 1);

		//The worker thread that will run the load
		Thread worker = new Thread(runner);
		
		//The progress updater thread
		ProgressUpdater updater = new ProgressUpdater(progressBar, runner);
		
		//Add uncaught exception handler to worker thread
		worker.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error in Worker Thread", JOptionPane.ERROR_MESSAGE);
				logger.error("Error in worker thread: " + e.getMessage());
				e.printStackTrace();
				restoreUI();
			}
		});
		
		//Start both
		updater.execute();
		worker.start();
	}
	
}
