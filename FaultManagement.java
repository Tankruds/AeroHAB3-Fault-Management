import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class FaultManagement {
	//All info received from the sensor group.
	private static double lastAltitude = 0;
	private static double altitude;
	private static double lastLatitude = 0;
	private static double latitude;
	private static double lastLongitude = 0;
	private static double longitude;
	private static double lastPitch = 0;
	private static double pitch;
	private static double lastRoll = 0;
	private static double roll;
	private static double lastYaw = 0;
	private static double yaw;
	private static double lastPitchRate = 0;
	private static double pitchRate;
	private static double lastRollRate = 0;
	private static double rollRate;
	private static double lastYawRate = 0;
	private static double yawRate;
	private static double lastTemperature = 0;
	private static double temperature;
	private static double lastHumidity = 0;
	private static double humidity;
	private static double lastPressure = 0;
	private static double pressure;
	
	//used to check the camera
	private static long cameraDirectorySize;
	private static long lastCameraDirectorySize = 0;
	private static final File cameraDirectory = new File("../AeroHABSensor/SensorCollection/Stills");
	
	//These three are going to be received from another group I don't know right now.
	private static int timeToDisconnect = 0;
	private static int altitudeToDisconnect = 0;
	
	//This will be used to find out when to disconnect.
	private static double elapsedTime = 0;
	
	//These are for our connections.
	private static final String IP = "localhost";
	private static final int PORT = 50050;
	
	//These are for our Error database and file.
	private static ErrorDB errors = new ErrorDB();
	private static final File errorFile = new File("Error.txt");
	private static BufferedWriter errorWriter;
	
	//This is the path to the python file that will turn pin 17 to high
	private static final String disconnectPath = "Disconnect.py";
	private static boolean disconnected = false;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		//args[0] is the timeToDisconnect in seconds, args[1] is the altitude to disconnect
		if (args.length != 0) {
			timeToDisconnect = Integer.parseInt(args[0]);
			altitudeToDisconnect = Integer.parseInt(args[1]);
		}
		
		//If Error.txt doesn't exist, this will make the file. It then makes sure the file is empty.
		if (!errorFile.exists())
			errorFile.createNewFile();
		errorWriter = new BufferedWriter(new FileWriter(errorFile));
		errorWriter.close();
		
		//This sleep is to give time for all the sensors to start collecting data and for the servers to get set up.
		Thread.sleep(1000);
		try {
			//The socket we need to communicate with the sensor group.
			Socket sensorSocket;
			DataOutputStream outToSensor;
			BufferedReader inFromSensor;
			try {
				sensorSocket = new Socket(IP, PORT);
				outToSensor = new DataOutputStream(sensorSocket.getOutputStream());
				inFromSensor = new BufferedReader(new InputStreamReader(sensorSocket.getInputStream()));
			} catch (Exception e) {
				errors.addError(new Error("Connection error.", "Sensor Group"));
			}

			// Temporary setup to not cause a permanent loop.
			int i = 1;
			while (i == 1) {
				//Gets the current time (used in tracking elapsedTime)
				long startTime = System.currentTimeMillis();
				
				//Set up all of the sockets again.
				String sensorInformation = "";
				try {
					sensorSocket = new Socket(IP, PORT);
					outToSensor = new DataOutputStream(sensorSocket.getOutputStream());
					inFromSensor = new BufferedReader(new InputStreamReader(sensorSocket.getInputStream()));
					// SensorInformation will be the data we receive from sensor
					// We can mess with the string to get whatever information we need from there
					// Write f to sensor group to request data from them.
					outToSensor.writeBytes("f\n");
					sensorInformation = inFromSensor.readLine();
					
					sensorSocket.close();
					inFromSensor.close();
					outToSensor.close();
				} catch (Exception e) {
					errors.addError(new Error("Connection error.", "Sensor Group"));
					sensorInformation = ",,,,,,,,,,,,";
				}
				
				if (sensorInformation.isEmpty() || sensorInformation.contains("Error")) {
					errors.addError(new Error("Error receiving data.", "Sensor Group"));
					sensorInformation = ",,,,,,,,,,,,";
				}
				
				//This entire section gets all of the info from the data from the sensor group
				//and does some error checking to see if we should have any errors.
				String tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty())
					tempString = "0";
				String time = tempString;
				//System.out.println("Time: " + time);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Altitude (GPS Error).", "Sensor Group"));
					altitude = 0;
				} else 
					altitude = Double.parseDouble(tempString);
				if (altitude == 0 && !tempString.isEmpty())
					errors.addError(new Error("Altitude is giving a strange value (Possible GPS Error).", "Sensor Group"));
				if (altitude == lastAltitude && !tempString.isEmpty() && altitude != 0)
					errors.addError(new Error("Altitude has not changed (Possible GPS Error).", "Sensor Group"));
				lastAltitude = altitude;
				//System.out.println("Altitude: " + altitude);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Latitude (GPS Error).", "Sensor Group"));
					latitude = 0;
				} else 
					latitude = Double.parseDouble(tempString);
				if (latitude == 0 && !tempString.isEmpty())
					errors.addError(new Error("Latitude is giving a strange value (Possible GPS Error).", "Sensor Group"));
				if (latitude == lastLatitude && !tempString.isEmpty() && latitude != 0)
					errors.addError(new Error("Latitude has not changed (Possible GPS Error).", "Sensor Group"));
				lastLatitude = latitude;
				//System.out.println("Latitude: " + latitude);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Longitude (GPS Error).", "Sensor Group"));
					longitude = 0;
				} else 
					longitude = Double.parseDouble(tempString);
				if (longitude == 0 && !tempString.isEmpty())
					errors.addError(new Error("Longitude is giving a strange value (Possible GPS Error).", "Sensor Group"));
				if (longitude == lastLongitude && !tempString.isEmpty() && longitude != 0)
					errors.addError(new Error("Longitude has not changed (Possible GPS Error).", "Sensor Group"));
				lastLongitude = longitude;
				//System.out.println("Longitude: " + longitude);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Pitch (Sensor Error).", "Sensor Group"));
					pitch = 0;
				} else 
					pitch = Double.parseDouble(tempString);
				if (pitch == 0 && !tempString.isEmpty())
					errors.addError(new Error("Pitch is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (pitch == lastPitch && !tempString.isEmpty() && pitch != 0)
					errors.addError(new Error("Pitch has not changed (Possible Sensor Error).", "Sensor Group"));
				lastPitch = pitch;
				//System.out.println("Pitch: " + pitch);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Roll (Sensor Error).", "Sensor Group"));
					roll = 0;
				} else 
					roll = Double.parseDouble(tempString);
				if (roll == 0 && !tempString.isEmpty())
					errors.addError(new Error("Roll is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (roll == lastRoll && !tempString.isEmpty() && roll != 0)
					errors.addError(new Error("Roll has not changed (Possible Sensor Error).", "Sensor Group"));
				lastRoll = roll;
				//System.out.println("Roll: " + roll);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Yaw (Sensor Error).", "Sensor Group"));
					yaw = 0;
				} else 
					yaw = Double.parseDouble(tempString);
				if (yaw == 0 && !tempString.isEmpty())
					errors.addError(new Error("Yaw is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (yaw == lastYaw && !tempString.isEmpty() && yaw != 0)
					errors.addError(new Error("Yaw has not changed (Possible Sensor Error).", "Sensor Group"));
				lastYaw = yaw;
				//System.out.println("Yaw: " + yaw);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Pitch Rate (Sensor Error).", "Sensor Group"));
					pitchRate = 0;
				} else 
					pitchRate = Double.parseDouble(tempString);
				if (pitchRate == 0 && !tempString.isEmpty())
					errors.addError(new Error("Pitch Rate is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (pitchRate == lastPitchRate && !tempString.isEmpty() && pitchRate != 0)
					errors.addError(new Error("Pitch Rate has not changed (Possible Sensor Error).", "Sensor Group"));
				lastPitchRate = pitchRate;
				//System.out.println("Pitch Rate: " + pitchRate);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Roll Rate (Sensor Error).", "Sensor Group"));
					rollRate = 0;
				} else 
					rollRate = Double.parseDouble(tempString);
				if (rollRate == 0 && !tempString.isEmpty())
					errors.addError(new Error("Roll Rate is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (rollRate == lastRollRate && !tempString.isEmpty() && rollRate != 0)
					errors.addError(new Error("Roll Rate has not changed (Possible Sensor Error).", "Sensor Group"));
				lastRollRate = rollRate;
				//System.out.println("Roll Rate: " + rollRate);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Yaw Rate (Sensor Error).", "Sensor Group"));
					yawRate = 0;
				} else 
					yawRate = Double.parseDouble(tempString);
				if (yawRate == 0 && !tempString.isEmpty())
					errors.addError(new Error("Yaw Rate is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (yawRate == lastYawRate && !tempString.isEmpty() && yawRate != 0)
					errors.addError(new Error("Yaw Rate has not changed (Possible Sensor Error).", "Sensor Group"));
				lastYawRate = yawRate;
				//System.out.println("Yaw Rate: " + pitch);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Temperature (Sensor Error).", "Sensor Group"));
					temperature = 0;
				} else 
					temperature = Double.parseDouble(tempString);
				if (temperature == 0 && !tempString.isEmpty())
					errors.addError(new Error("Temperature is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (temperature == lastTemperature && !tempString.isEmpty() && temperature != 0)
					errors.addError(new Error("Temperature has not changed (Possible Sensor Error).", "Sensor Group"));
				lastTemperature = temperature;
				//System.out.println("Temperature: " + temperature);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0, sensorInformation.indexOf(','));
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Humidity (Sensor Error).", "Sensor Group"));
					humidity = 0;
				} else 
					humidity = Double.parseDouble(tempString);
				if (humidity == 0 && !tempString.isEmpty())
					errors.addError(new Error("Humidity is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (humidity == lastHumidity && !tempString.isEmpty() && humidity != 0)
					errors.addError(new Error("Humidity has not changed (Possible Sensor Error).", "Sensor Group"));
				lastHumidity = humidity;
				//System.out.println("Humidity: " + humidity);
				
				sensorInformation = sensorInformation.substring(sensorInformation.indexOf(',') + 1);
				tempString = sensorInformation.substring(0);
				if (tempString.isEmpty()) {
					errors.addError(new Error("Not receiving Pressure (Sensor Error).", "Sensor Group"));
					pressure = 0;
				} else 
					pressure = Double.parseDouble(tempString);
				if (pressure == 0 && !tempString.isEmpty())
					errors.addError(new Error("Pressure is giving a strange value (Possible Sensor Error).", "Sensor Group"));
				if (pressure == lastPressure && !tempString.isEmpty() && pressure != 0)
					errors.addError(new Error("Pressure has not changed (Possible Sensor Error).", "Sensor Group"));
				lastPressure = pressure;
				//System.out.println("Pressure: " + pressure);
				
				//This section covers if the directory holding the photos has changed size.
				if (cameraDirectory.exists()) {
					cameraDirectorySize = cameraDirectory.listFiles().length;
					if (cameraDirectorySize == lastCameraDirectorySize)
						errors.addError(new Error("Camera Directory hasn't changed size (Possible Camera Error).", "Sensor Group"));
					lastCameraDirectorySize = cameraDirectorySize;
					//System.out.println(cameraDirectorySize);
				} else {
					errors.addError(new Error("Camera Directory does not exist (Possible Camera Error).", "Sensor Group"));
				}
				
				//Sets up the errorFile to be written to in append mode, writes to it, then closes it.
				errorWriter = new BufferedWriter(new FileWriter(errorFile, true));
				for (Error error : errors.getLatestErrors()) {
					errorWriter.write(error.toString());
					errorWriter.newLine();
				}
				errorWriter.close();
				
				//endTime will calculate the time after the bulk of the loop has passed (used in calculating elapsedTime)
				long endTime = System.currentTimeMillis();
				//ProgramTime is how long it took to get to this point.
				long programTime = endTime - startTime;
				//SleepTime is how long we should sleep (based on our hz)
				long sleepTime = 1000 - (endTime - startTime);
				if (sleepTime >= 0)
					elapsedTime += ((endTime - startTime + sleepTime) / 1000.0);
				else
					elapsedTime += ((endTime - startTime) / 1000.0);
				
				//System.out.println("Program Time: " + programTime + "\nSleep Time: " + sleepTime);
				//System.out.println("Elapsed Time: " + elapsedTime + "\n");
				
				//if current altitude is greater then our set disconnecting altitude or
				//if the elapsed time is greater than our set time to disconnect then we will disconnect.
				if (((altitude >= altitudeToDisconnect && altitudeToDisconnect != 0) || (elapsedTime >= timeToDisconnect && timeToDisconnect != 0)) && !disconnected) {
					System.out.println("Disconnect");
					disconnect();
				}

				if (sleepTime > 0)
					Thread.sleep(sleepTime);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void disconnect() throws IOException {
		Runtime.getRuntime().exec("python " + disconnectPath);
		disconnected = true;
	}
}
