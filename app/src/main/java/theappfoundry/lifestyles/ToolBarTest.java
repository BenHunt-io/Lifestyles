package theappfoundry.lifestyles;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

public class ToolBarTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_bar_test);

        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar); // sets toolbar as app bar for the activity
        // Returns reference to an appcompat ActionBar object
        ActionBar myActionBar = getSupportActionBar();

        myActionBar.setDisplayHomeAsUpEnabled(true); // sets the up button on the action bar


    }




    // The MenuInflator class allows to inflate actions defined in an XML file (Into a java object)
    // and adds them to the action bar. MenuInflator can get accessed via the getMenuInflator()
    // method from your activity. The following example code demonstrates the creation of actions.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);
        return true;
    }





}



