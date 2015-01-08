package name.gntm_mdk;
import javax.sound.sampled.*;
import javax.xml.ws.handler.PortInfo;

import java.io.*;
import java.util.Hashtable;
 
/**
 * A sample program is to demonstrate how to record sound in Java
 * @author medaka.mp3@gmail.com
 * refer: www.codejava.net
 */
public class Recorder {
	@Deprecated
	public enum LINE_TYPE {
		LINE_OUT,
		SPEAKER,
		SYSTEM,
		NONE
	};
	@Deprecated
	LINE_TYPE mLineType = LINE_TYPE.NONE;
	String mLineName = null;
    // record duration, in milliseconds
    long mDuration = 6000; // 0.1 minute
 
    // path to wav file
    File mWavFile = null;
 
    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
 
    // the line from which audio data is captured
    TargetDataLine mLine;
    
    // contains available device list
    Hashtable<String,TargetDataLine> mTargetDataLineHash;
    
    RecordStateListener mRecordStateListener;
    
    /** 
     * constructor. 
     * this method is deprecated due to the issue #1.
     * @see also issue #1
     */
    @Deprecated
    public Recorder( String uri,LINE_TYPE lineType ){
    	mLineType = lineType;
    	if(null != uri){
    		mWavFile = new File(uri);
    	}
    	initTargetDataLine();
    }
    
    /** 
     * constructor
     * @param uri contains the location to save wave file.
     * @lineType is the name of an audio device. This can be got from getTargetDataLine()
     */
    @Deprecated
    public Recorder( String uri,String lineType ){
    	mLineName = lineType;
    	if(null != uri){
    		mWavFile = new File(uri);
    	}
    	initTargetDataLine();
    }
    

    /** 
     * constructor
     */
    public Recorder(){
    	initTargetDataLine();
    }
    
    public void setUri(String uri){
    	if(null != uri){
    		createFile(uri);
    	}
    }
    public void setListener(RecordStateListener listener){
    	mRecordStateListener = listener;
    }
    
    @Deprecated
    public void setLineType(LINE_TYPE lineType){
    	mLineType = lineType;
    }
    
    /**
     * setter
     * @param lineType is the name of an audio device. This can be got from getTargetDataLine()
     */
    public void setLineType(String lineType){
    	mLineName = lineType;
    }
    
    public void setDuration(long durationInSecond) {
    	mDuration = durationInSecond;
    }
    
    private void createFile(String uri){
    	mWavFile = new File(uri);
    }
 
    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
 
    public void start(){
    	// creates a new thread to record sound
    	Thread recordingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				recStart();
			}
		});
		// creates a new thread that waits for a specified
		// of time before stopping
		Thread stopper = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(mDuration);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				finish();
			}
		});

		recordingThread.start();
		stopper.start();

		// start recording
		//recStart();
    }
    
    /**
     * Captures the sound and record into a WAV file
     * @return success: 0  failed: -1
     */
    private int recStart() {
        try {
        	// format describes only WAV information
            AudioFormat format = getAudioFormat();
            
            // todo:delete START ----
            DataLine.Info info;
            switch(mLineType) {
            case LINE_OUT:
                if(!AudioSystem.isLineSupported(Port.Info.LINE_OUT)){
                    System.out.println("Info.LINE_OUT is not supported");
                    return -1;
                }
                mLine = (TargetDataLine) AudioSystem.getLine(Port.Info.LINE_OUT);
            	break;
            case SPEAKER:
                if(!AudioSystem.isLineSupported(Port.Info.SPEAKER)){
                    System.out.println("Info.Speaker is not supported");
                    return -1;
                }
                mLine = (TargetDataLine) AudioSystem.getLine(Port.Info.SPEAKER);
            	break;
            case SYSTEM:
           	    info = new DataLine.Info(TargetDataLine.class, format);
           	    if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("Line not supported");
                    return -1;
           	    }
            	mLine = (TargetDataLine) AudioSystem.getLine(info);
            	break;
            default: 
            	// do nothing when type is NONE
            }
            // todo: delete END ---
            if( mLineName != null && mTargetDataLineHash.containsKey( mLineName ) ) {
                mLine = mTargetDataLineHash.get( mLineName );
            } else if(mTargetDataLineHash != null && !mTargetDataLineHash.isEmpty()){
            	String[] array = (String[])mTargetDataLineHash.keySet().toArray();
            	mLine = mTargetDataLineHash.get(array[0]);
            } else {
            	return -1;
            }
            mLine.open(format);
            mLine.start();   // start capturing
 
            System.out.println("Start capturing...");
 
            AudioInputStream ais = new AudioInputStream(mLine);
 
            System.out.println("Start recording...");
 
            // start recording
            AudioSystem.write(ais, fileType, mWavFile);
 
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            return -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    /**
     * stops recording.
     */
    public void stop() {
    	finish();
    }
 
    /**
     * Closes the target data line to finish capturing and recording
     */
    void finish() {
        mLine.stop();
        mLine.close();
    	mRecordStateListener.onRecordFinished();
        System.out.println("Finished");
    }
    
    /**
     * creates mTargetDataLineHash.
     */
    private void initTargetDataLine( ) {
        mTargetDataLineHash = new Hashtable<String,TargetDataLine>();
        Mixer.Info[] mixerInfoList = AudioSystem.getMixerInfo();
        for( Mixer.Info info : mixerInfoList ) {
            //String name = info.getName();
            //System.out.println( name );
            Mixer mixer = AudioSystem.getMixer(info);
            //System.out.println( mixer );
            //for( Line.Info i : mixer.getSourceLineInfo() ) {
            //  System.out.println( "- " + i ); // output
            //}
            for( Line.Info i : mixer.getTargetLineInfo() ) {
                //System.out.println( "+ " + i ); // input
                try {
                    Line line = mixer.getLine(i);
                    if( line instanceof TargetDataLine ) {
                        //System.out.println( "\tOK" ); // input
                        mTargetDataLineHash.put( info.getName(), (TargetDataLine)line );
                    }
                } catch ( LineUnavailableException e ) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println( "supported devices:" );
        for(String name : mTargetDataLineHash.keySet()){
        	System.out.println(name);
        }
    }
    
    /**
     * gets an array that contains supported device name.
     * @return names
     */
    public String[] getTargetDataLine() {
    	return (String[]) mTargetDataLineHash.keySet().toArray(new String[0]);
    }
    
}