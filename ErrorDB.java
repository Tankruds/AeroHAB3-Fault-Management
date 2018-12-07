import java.util.ArrayList;

public class ErrorDB {
	//The ArrayList notSent is the list of all errors that have not been checked yet.
	private static ArrayList<Error> notSent;
	
	//Method Name: ErrorDB()
	//Description: A constructor for ErrorDB, sets up the initial ArrayLists
	//Precondition: N/a
	//Postcondition: N/a
	public ErrorDB() {
		notSent = new ArrayList<Error>();
	}
	
	//Method Name: addError(Error error)
	//Description: Adds a new Error to the notSent ArrayList.
	//Precondition: Receives a Error error.
	//Postcondition: N/a
	public void addError(Error error) {
		notSent.add(error);
	}
	
	//Method Name: getLatestErrors()
	//Description: Copies all Error objects from notSent into a buffer ArrayList
	//			   and alreadySent, and then returns the buffer, meaning that it 
	//             only sends the errors that haven't been retrieved yet.
	//Precondition: N/a
	//Postcondition: Returns a ArrayList<Error>.
	public ArrayList<Error> getLatestErrors() {
		ArrayList<Error> buffer = new ArrayList<Error>();
		while (notSent.size() != 0) {
			buffer.add(notSent.get(0));
			notSent.remove(0);
		}
		return buffer;
	}
}