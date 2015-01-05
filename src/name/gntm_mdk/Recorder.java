package name.gntm_mdk;
import javax.sound.sampled.*;
import javax.xml.ws.handler.PortInfo;

import java.io.*;
 
/**
 * A sample program is to demonstrate how to record sound in Java
 * author: www.codejava.net
 */
public class Recorder {
	public enum LINE_TYPE {
		LINE_OUT,
		SPEAKER,
		SYSTEM
	};
	LINE_TYPE mLineType;
    // record duration, in milliseconds
    long mDuration = 6000; // 0.1 minute
 
    // path to wav file
    File mWavFile = null;
 
    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
 
    // the line from which audio data is captured
    TargetDataLine line;
    
    /** constructor
     * 
     */
    public Recorder( String uri,LINE_TYPE lineType ){
    	mLineType = lineType;
    	if(null != uri){
    		mWavFile = new File(uri);
    	}
    }
    
    public void setUri(String uri){
    	if(null != uri){
    		createFile(uri);
    	}
    }
    
    public void setLineType(LINE_TYPE lineType){
    	mLineType = lineType;
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

		stopper.start();

		// start recording
		recStart();
    }
    /**
     * Captures the sound and record into a WAV file
     * @return success: 0  failed: -1
     */
    private int recStart() {
        try {
        	// format describes only WAV information
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }

            if(!AudioSystem.isLineSupported(Port.Info.LINE_OUT)){
                System.out.println("Info.LINE_OUT is not supported");
            }
            if(!AudioSystem.isLineSupported(Port.Info.SPEAKER)){
                System.out.println("Info.Speaker is not supported");
            }
            
//            line = (TargetDataLine) AudioSystem.getLine(Port.Info.SPEAKER);
            
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing
 
            System.out.println("Start capturing...");
 
            AudioInputStream ais = new AudioInputStream(line);
 
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
     * Closes the target data line to finish capturing and recording
     */
    void finish() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }
}