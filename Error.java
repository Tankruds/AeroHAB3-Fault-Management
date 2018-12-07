import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Error {
	//The DateTimeFormatter simplifies taking the current date and time and putting it into a String.
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS");
	//The errorMessage is the full error message that is made up of the below values.
	private String errorMessage = "";
	//The error is what the actual error that happened was, eg. "The battery voltage was too low"
	private String error = "";
	//The location is where the error occurred, eg. "Stabilization"
	private String location = "";
	//The time is what time the error was created in the format of "month/day/year hour:minute:second.millisecond
	private String time = "";

	//Method Name: Error(String error, String location)
	//Description: A constructor for Error. It will take an error message and a
	//			   location, it will then put it into the form of
	//
	//			   Error: **error**
	//			   Location: **location**
	//			   Time: **time**
	//Precondition: Receives a String error, String location.
	//Postcondition: N/a
	public Error(String error, String location) {
		time = dtf.format(LocalDateTime.now());
		this.location = location;
		this.error = error;
		this.errorMessage += "Error: " + error + " \nLocation: " + location + ". \nTime: " + time + "\n";
	}

	//Method Name: getError()
	//Description: Returns the error that was determined by the constructor.
	//Precondition: N/a
	//Postcondition: Returns a String.
	public String getError() {
		return error;
	}
	
	//Method Name: getTime()
	//Description: Returns the time at which the error was first created, decided
	//			   when the constructor was run.
	//Precondition: N/a
	//Postcondition: Returns a String.
	public String getTime() {
		return time;
	}
	
	//Method Name: getLocation()
	//Description: Returns the location that was determined by the constructor.
	//Precondition: N/a
	//Postcondition: Returns a String.
	public String getLocation() {
		return location;
	}
	
	//Method Name: getErrorMessage()
	//Description: Returns the full errorMessage that this Error is storing.
	//Precondition: N/a
	//Postcondition: Returns a String.
	public String getErrorMessage() {
		return errorMessage;
	}

	//Method Name: toString()
	//Description: Overwritten Object class to simplify the printing of errors.
	//Precondition: N/a
	//Postcondition: Returns a String.
	public String toString() {
		return errorMessage;
	}
}