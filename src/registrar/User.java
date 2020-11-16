package registrar;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class User {
	private String phoneNumber;
	private Map<LocalDate, List<byte[]>> tokensIssued;
	
	private static List<User> allUsers;
	private static int criticalPeriod;
	private static SecureRandom rng;
	
	/** uses the phoneNumber field to initialise a User object if phone number is not yet in use. Uses .equals to check: compares on phoneNumber only.
	 * @param phoneNumber: phone number to register
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 * @throws UserAlreadyRegisteredException: if the phoneNumber is already in use.
	 */
	public User(String phoneNumber) throws NotInitialisedException, UserAlreadyRegisteredException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		this.phoneNumber = phoneNumber;
		this.tokensIssued = new HashMap<LocalDate, List<byte[]>>();
		
		if(User.allUsers.contains(this)) throw new UserAlreadyRegisteredException(this.phoneNumber);
		User.allUsers.add(this);
	}
	
	/** register a list of tokens to a specific user for a specific date.
	 * @param today: date for which the tokens are to be registered
	 * @param tokens: (array)list of tokens
	 * @return List<byte[]> if key was already associated with an item, returns null if not.
	 */
	public List<byte[]> addTokens(LocalDate today, List<byte[]> tokens) {
		List<byte[]> result = tokensIssued.put(today, tokens);
		return result;
	}
	
	/** checks whether or not a certain token was issued to this user
	 * @param token: byte[]: the token against which to compare
	 * @return if no match is found, return null. Else, return the LocalDate object matching the day the token was issued.
	 */
	public LocalDate checkToken(byte[] token) {
		Set<LocalDate> dates = this.tokensIssued.keySet();
		for(LocalDate ld: dates) {
			List<byte[]> tokensOnDayLD = this.tokensIssued.get(ld);
			for(byte[] currentToken: tokensOnDayLD) {
				if(Arrays.equals(token, currentToken)) {
					return ld;
				}
			}
		}
		return null;
	}
	
	/**
	 *  remove all tokens (from a specified user) that are older than the critical period.
	 */
	public void removeOldEntries() {
		Set<LocalDate> dates = this.tokensIssued.keySet();
		List<LocalDate> toRemove = new ArrayList<>();
		for(LocalDate ld : dates) {
			//if the difference in days between now & the registered date, remove all tokens associated with that date.
			if(ChronoUnit.DAYS.between(LocalDate.now(), ld) > User.criticalPeriod) {
				toRemove.add(ld);
			}
		}
		toRemove.forEach(date -> {
			if(this.tokensIssued.remove(date) == null) {
				System.err.println("date removed from tokensIssued that was not present. Flaw in 'removeOldEntries()'-method. User " + this.phoneNumber + ".");
			};
		});
	}
	
	public String getPhoneNumber() {
		return this.phoneNumber;
	}
	
	/** get the User object associated with a certain phoneNumber
	 * @param phoneNumber: String
	 * @return User if found, null if not.
	 */
	public static User findUser(String phoneNumber) {
		for(User u: User.allUsers) {
			if(u.phoneNumber.equals(phoneNumber)) {
				return u;
			}
		}
		return null;
	}
	
	/** method to initialise the UserSystem as needed: create the allUsers-list & set the criticalPeriod (in days).
	 * @param criticalPeriod: integer value that determines how long (in days) tokens are saved for. 
	 * @return List<User> allUsers: a reference to the empty ArrayList<User> allUsers, which can be used to perform actions on all users.
	 */
	public static List<User> initialiseUserSystem(int criticalPeriod){
		User.allUsers = new ArrayList<>();
		User.criticalPeriod = criticalPeriod;
		User.rng = new SecureRandom();
		
		return User.allUsers;
	}
	
	/**
	 * @return
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 */
	public static List<User> getUserList() throws NotInitialisedException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		return User.allUsers;
	}
	
	/**
	 * @return Random rng: the static Random instance used to get the randomly generated integers for the user.random field.
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 */
	public static Random getRNG() throws NotInitialisedException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		return User.rng;
	}
	
	/**
	 * @return
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 */
	public static int getCriticalPeriod() throws NotInitialisedException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		return User.criticalPeriod;
	}
	
	/** register a list of tokens for a specific user for use on a specific date.
	 * @param user: user for whom to register the tokens
	 * @param ld: date for which the tokens are issued
	 * @param tokens: (array)list of the issued tokens
	 * @return true if successful, false if user not enrolled or ld already had tokens associated with it.
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 */
	public static boolean addTokens(User user, LocalDate ld, List<byte[]> tokens) throws NotInitialisedException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		if(!User.allUsers.contains(user)) return false;
		if(user.addTokens(ld, tokens) != null) {
			System.out.println("User " + user.phoneNumber + "already had tokens for this day. Error in function addTokens(User, LocalDate, List<byte[]>)");
			return false;
		}
		return true;
	}
	
	/** For all registered users, remove all tokens older than the set criticalPeriod (as specified in the initialise(int criticalPeriod)-method).
	 * @throws NotInitialisedException: if the User.initialiseUserSystem(int criticalPeriod) has not yet been called.
	 */
	public static void removeAllOldEntries() throws NotInitialisedException{
		if(User.allUsers == null) throw new NotInitialisedException("UserSystem");
		User.allUsers.forEach(user -> {
			user.removeOldEntries();
		});
	}
	
	public static void setAllUsersList(List<User> users) {
		User.allUsers = users;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * USES ONLY THE PHONENUMBER FIELD!
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		return true;
	}
	
	
}
