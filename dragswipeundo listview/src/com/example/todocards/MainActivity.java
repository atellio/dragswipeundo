package com.example.todocards;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todocards.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.widget.DynamicListView;


public class MainActivity extends Activity implements OnDismissCallback, DeleteItemCallback {

	// implememts special ListView from listviewanimations library
    private DynamicListView listView;
    private EditText addTaskText;
    // for the task strings
    private static List<Task> items;
    // for the swipe to delete
    ArrayAdapter<Task> adapter;
    protected DbHelper db;
    private Task tempTaskHolder;
    private ContextualUndoAdapter undoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asign DynamicListView to the listView object
        listView = (DynamicListView) findViewById(R.id.activity_draganddrop_listview);
        //setDivider(null) is set in the xml file so not sure why it's here as well
        listView.setDivider(null);
        
        // this ArrayList feeds the adapter that puts the strings into the dynamiclistview
        db = new DbHelper(this);
        items = db.getAllTasks();
        adapter = createListAdapter();
        
        
        setSwipeDismissAdapter();
        
        
        // animation adapter applied to the listview sets the delay before the listview is 
        // populated via a cascading animation
        AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(adapter);
        animAdapter.setInitialDelayMillis(300);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
        
        setContextualUndoAdapter();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	undoAdapter.removePendingItem();
    	
    }
    
    private void setSwipeDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(adapter, this);
        swipeAdapter.setAbsListView(getListView());
        getListView().setAdapter(swipeAdapter);
	}
    
	private void setContextualUndoAdapter() {
    	undoAdapter = new ContextualUndoAdapter(adapter, 
    			R.layout.undo_row, R.id.undo_row_undobutton, 3000, this);
    	undoAdapter.setAbsListView(getListView());
    	getListView().setAdapter(undoAdapter);
	}
    
    // dont actually need a reference object for the add button, just used the xml onclick "addTaskNow"
    public void addTaskNow(View v) {
    	addTaskText = (EditText) findViewById(R.id.editTextAddTask);
		String s = addTaskText.getText().toString();
		Task task = new Task(s, 0);
		if (s.equalsIgnoreCase("")) {
				noTextEnteredToast();
			} else {
				items.add(task);
				db.addTask(task);
				Log.d("tasker", task.getTaskName() + " added");
				addTaskText.setText("");
				adapter.notifyDataSetChanged();
			}
      }

	protected void noTextEnteredToast() {
    	Toast.makeText(this, "enter the task description, idiot", Toast.LENGTH_LONG).show();
	}

	// return a MyListAdapter object with MainActivity.java as the context (this)
	// and the ArrayList that contains our task strings, "items".
    protected ArrayAdapter<Task> createListAdapter() {
        return new MyListAdapter(this, items);
    }

    private static class MyListAdapter extends ArrayAdapter<Task> {
        Context mContext;
        
        public MyListAdapter(Context context,
                List<Task> items) {
            super(items);
            mContext = context;
        }
        
        // is this the int position in the arraylist or in the adapter? is it the same thing?
        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
        
        // populate views of the listview with the textview defined in list_row.xml, 
        // set the text of each textview to "this is row number" + whichever position the view occupies.
        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            Task current = items.get(position);
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(
                        R.layout.list_row, parent, false);
            }
            
            tv.setText(current.getTaskName());
            // Log.d("here!", getItem(position));
            return tv;
        }
    }

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
			for(int i : reverseSortedPositions) {
					db.deleteTask(adapter.getItem(i));
					adapter.remove(adapter.getItem(i));
				}
			}
	
	@Override
	public void deleteItem(int position) {
		db.deleteTask(adapter.getItem(position));
		Log.d("deleted", adapter.getItem(position).getTaskName());
		
		adapter.remove(adapter.getItem(position));
		adapter.notifyDataSetChanged();
	}

	 private AbsListView getListView() {
			return listView;
		}

}