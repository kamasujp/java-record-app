package name.gntm_mdk;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class RecordMain extends Frame implements ActionListener{
	private static final String DEFAULT_FILE_NAME = "record.wav";
	private static final String SETTING_FILE_NAME = "property.conf";
	private static final String SETTING_KEY_URI = "setting_key_uri";
	private static final String SETTING_KEY_TYPE = "setting_key_type";
	private static final String SETTING_KEY_DURATION = "setting_key_duration";
	private enum RecState{
		Prepare, Standby , Rec
	}

	private Recorder mRecorder;
	private Thread mThread;
	private String[] inputs;
	Label mHourLabel;
	Label mMinuteLabel;
	Label mPathLabel;
	Choice mMinuteChoice;
	Choice mHourChoice;
	Button mRecordButton;
	Checkbox mImmediateCheck;
	boolean mIsImmediate = false;

	private Properties prop;
	private RecState state = RecState.Prepare;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RecordMain record = new RecordMain();
		record.setVisible(true);
	}

	public RecordMain(){
		// Window initialization
		super();
		// load default settings
		prop = new Properties();
		load();

		//ウィンドウを閉じる時
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(600, 200);
		buildUi();
		// other initialization
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

		
		/* Line Select */
		Label lineLabel = new Label("Line:");
		add(lineLabel);
		Choice lineChoice = new Choice();
		final ArrayList<String> list = new ArrayList<String>(Arrays.asList(mRecorder.getTargetDataLine()));
		for (String s : list){
			lineChoice.add(s);
		}
		int def = Integer.parseInt(prop.getProperty(SETTING_KEY_TYPE));
		if (list.size() <= def){
			lineChoice.select(0);			
		}else{
			lineChoice.select(def);
		}
		lineChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				int order =  list.indexOf(e.getItem().toString());
				System.out.println("selected :" + order);
				prop.setProperty(SETTING_KEY_TYPE, order+"");
				save();
			}
		});
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

		mImmediateCheck = new Checkbox("Immediately");
		mImmediateCheck.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateCheckBox(e.getStateChange()==1);
			}
		});
		add(mImmediateCheck);


		mPathLabel = new Label("保存先:"+prop.getProperty(SETTING_KEY_URI));
		add(mPathLabel);
		Button srcButton = new Button("保存先を変更");
		srcButton.setSize(300, 50);
		srcButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectURI();
			}
		});
		add(srcButton);

		mRecordButton = new Button("Rec");
		state = RecState.Prepare;
		updateState();
		mRecordButton.setSize(300, 50);
		mRecordButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (state == RecState.Prepare){
					StartRec();
				}else if (state == RecState.Standby){
					StopWait();
				}else{
					StopRec();
				}
			}
		});
		add(mRecordButton);

		Label durationLabel = new Label("duration");
		add(durationLabel);

		Choice durationChoice = new Choice();
		int selected = 0;
		int defaultDuration = Integer.parseInt(prop.getProperty(SETTING_KEY_DURATION));
		int now = 0;
		for(int d = 15; d < 60; d+=15){
			durationChoice.add(String.valueOf(d).concat(" mins"));
			if (d == defaultDuration){
				selected = now;
			}
			now ++;
		}
		for(int d = 60; d <= 300; d+=60){
			durationChoice.add(String.valueOf(d).concat(" mins"));
			if (d == defaultDuration){
				selected = now;
			}
			now ++;
		}
		durationChoice.select(selected);
		add(durationChoice);
		durationChoice.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				int min = Integer.parseInt(e.getItem().toString().replace(" mins", ""));
				System.out.println("duration :"+ min);
				prop.setProperty(SETTING_KEY_DURATION, min + "");
				save();
			}
		});
	}


	private void updateState(){
		if (state == RecState.Prepare){
			mRecordButton.setLabel("録音開始");
		}else if(state == RecState.Standby){
			mRecordButton.setLabel("予約取消");
		}else{
			mRecordButton.setLabel("録音終了");
		}
	}
	private void StartRec(){
		if (mImmediateCheck.getState()){
			state = RecState.Rec;
			updateState();
			mRecorder.start();
		}else{
			state = RecState.Standby;
		}
		updateState();
	}
	private void StopRec(){
		mRecorder.stop();
		state = RecState.Prepare;
		updateState();
	}
	private void StopWait(){
		state = RecState.Prepare;
		updateState();
	}
	
	
	private void updateCheckBox(boolean isChecked){
		System.out.println("changed" + isChecked);
		mHourChoice.setEnabled(!isChecked);
		mMinuteChoice.setEnabled(!isChecked);
		mIsImmediate = isChecked;
	}


	/*
	 * util
	 */

	private void selectURI(){
		FileDialog fileDialog=new FileDialog(this , "保存先を選択" , FileDialog.SAVE );//FileDialogの作成
		fileDialog.setVisible(true);//表示する
		String dir=fileDialog.getDirectory();//ディレクトリーの取得
		String fileName=fileDialog.getFile();//File名の取得
		if (fileName!=null){
			String path = dir + fileName;
			System.out.println(path);
			prop.setProperty(SETTING_KEY_URI, path );
			mPathLabel.setText("保存先:"+path);
			save();
		}
	}

	private void save(){
		try {
			prop.store(new FileOutputStream(SETTING_FILE_NAME), "settings");
		} catch (FileNotFoundException e1) {
			System.out.println("save failed : not found");
		} catch (IOException e1) {
			System.out.println("save failed : IO blocked");
		}

		mRecorder.setUri(prop.getProperty(SETTING_KEY_URI));
		int index = Integer.parseInt(prop.getProperty(SETTING_KEY_TYPE));
		try {
			mRecorder.setLineType(inputs[index]);
			System.out.println("[Mic] setLineType : "+inputs[index]);
		}catch (ArrayIndexOutOfBoundsException e){
			System.out.println("[Mic] line index "+index+" is out of range. setLineType : "+inputs[0]);
			mRecorder.setLineType(inputs[0]);
		}
		mRecorder.setDuration(60*1000*Integer.parseInt(prop.getProperty(SETTING_KEY_DURATION)));
	}
	private void load(){
		try {
			prop.load(new FileInputStream(SETTING_FILE_NAME));
		} catch (IOException e) {
			System.out.println("setting is not found.");

			String path = new File(".").getAbsoluteFile().getParent() + "/"+DEFAULT_FILE_NAME;
			System.out.println("default file path :"+path);			
			prop.setProperty(SETTING_KEY_URI, path);		
			prop.setProperty(SETTING_KEY_TYPE, "0");
			prop.setProperty(SETTING_KEY_DURATION, "180");
		}
		mRecorder = new Recorder();
		inputs = mRecorder.getTargetDataLine();
		save();
	}

	public static <E extends Enum<E>> E fromOrdinal(Class<E> enumClass, int ordinal) {
		E[] enumArray = enumClass.getEnumConstants();
		return enumArray[ordinal];
	}
	
	public static void showDialog (String sentence){
		/*
        Dialog alert = new Dialog(new Frame() , "確認");
        alert.setVisible(true);
        */
	}

}
