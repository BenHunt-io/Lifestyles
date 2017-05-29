package theappfoundry.lifestyles;

/**
 * Created by Ben on 5/22/2017.
 */

public class Profile {

    public String firstName;
    public String lastName;


    Profile(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    Profile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
