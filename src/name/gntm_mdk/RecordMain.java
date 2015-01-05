package name.gntm_mdk;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RecordMain extends Frame implements ActionListener{
	private Recorder mRecorder;
	private Thread mThread;
	Label mHourLabel;
	Label mMinuteLabel;
	Choice mMinuteChoice;
	Choice mHourChoice;
	boolean mIsImmediate = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Recorder recorder = new Recorder();
		RecordMain record = new RecordMain();
		record.setVisible(true);
	}
	
	public RecordMain(){
		// Window initialization
		super();
	    //ウィンドウを閉じる時
	    addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	        System.exit(0);
	      }
	    });
	    setSize(300, 300);
	    buildUi();
	    // other initialization
	    mRecorder = new Recorder();
	    mThread = new Thread();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		startRecording();
	}
	
	public void startRecording(){
		//mRecorder.start();
	}
	
	public void stopRecording(){
		// todo: implement
	}
	
	private void buildUi() {
	    // menubar settings
	    MenuBar menubar = new MenuBar();
	    Menu fileMenu = new Menu("File");
	    fileMenu.add(new MenuItem("open"));
	    menubar.add(fileMenu);
	    Menu recordMenu = new Menu("Record");
	    MenuItem startRecordItem = new MenuItem("startRecording");
	    startRecordItem.addActionListener(this);
	   
	    recordMenu.add(startRecordItem);
	    menubar.add(recordMenu);
	    setMenuBar(menubar);
	    
	    // UI layouting
	    setLayout(new FlowLayout());

	    Label lineLabel = new Label("Line:");
	    add(lineLabel);
        Choice lineChoice = new Choice();
        lineChoice.add("LINE_OUT");
        lineChoice.add("SPEAKER");
        lineChoice.add("Line");
        add(lineChoice);
        
        mHourLabel = new Label("hour:");
        add(mHourLabel);
        mHourChoice = new Choice();
        for (int h=0; h<13 ;h++) {
        	mHourChoice.add(String.valueOf(h));
        }
        add(mHourChoice);
        
        mMinuteLabel = new Label();
        mMinuteLabel.setText("minute");
        add(mMinuteLabel);
        mMinuteChoice = new Choice();
        for (int m=0; m<60; m++) {
        	mMinuteChoice.add(String.valueOf(m));
        }
		add(mMinuteChoice);
        
		Checkbox checkBox = new Checkbox("Immediately");
		checkBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateCheckBox(e.getStateChange()==1);
					}
				});
		add(checkBox);
		
		Button recordButton = new Button("Rec");
		recordButton.setSize(300, 50);
		recordButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		add(recordButton);
		
		Label durationLabel = new Label("duration");
		add(durationLabel);
		
		Choice durationChoice = new Choice();
		for(int d = 15; d < 100; d+=15){
			durationChoice.add(String.valueOf(d).concat(" mins"));
		}
		durationChoice.add("infinite");
		add(durationChoice);
	}
	
	private void updateCheckBox(boolean isChecked){
		System.out.println("changed" + isChecked);
		mHourChoice.setEnabled(!isChecked);
		mMinuteChoice.setEnabled(!isChecked);
		mIsImmediate = isChecked;
	}
	
}
