package com.example.todocards;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantosello.todolistforbabes.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.widget.DynamicListView;


public class MainActivity extends Activity implements OnDismissCallback, DeleteItemCallback {

	private static final int TASK_UNCHECKED = 0;
	private static final int TASK_CHECKED = 1;
	private static final int DELETE_DELAY = 4000;
	// implememts special ListView from listviewanimations library to enable drag and drop functionality
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
        //setDivider(null) would this be better in the xml?
        // listView.setDivider(null);
        
        // this ArrayList feeds the adapter that puts the strings into the dynamiclistview
        db = new DbHelper(this);
        items = db.getAllTasks();
        adapter = createListAdapter();
        
        setOnItemClickListener(); // only logs position of clicked item at the moment
        
        setSwipeDismissAdapter();
        
        
        // animation adapter applied to the listview sets the delay before the listview is 
        // populated via a cascading animation
        AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(adapter);
        animAdapter.setInitialDelayMillis(300);
        animAdapter.setAbsListView(listView);
        listView.setAdapter(animAdapter);
        
        setContextualUndoAdapter();
        
        // fix actionbar shadow bug
        setWindowContentOverlayCompat();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
    }
   
	private void setOnItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	TextView descriptionView = (TextView) view.findViewById(R.id.task_description);
            	CheckBox checkBox = (CheckBox) view.findViewById(R.id.taskCheckBox);
            	
            	Log.d("Clicked item position", " "+ position);
            	Log.d("Clicked item id", " "+ items.get(position).getId());
            	Log.d("Clicked item status", " " + items.get(position).getStatus());
            	
            	Task clickedTask = items.get(position);
            	
            	if(clickedTask.getStatus() == TASK_UNCHECKED) {
            			items.get(position).setStatus(TASK_CHECKED);
            			db.updateTask(items.get(position));
            			descriptionView.setPaintFlags(descriptionView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            			checkBox.setChecked(true);
            		} else {
            			items.get(position).setStatus(TASK_UNCHECKED);
            			db.updateTask(items.get(position));
                        descriptionView.setPaintFlags(descriptionView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        checkBox.setChecked(false);
            		}
            	
            }});

	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    		for (int i = 0; i < items.size(); i++) {
    			items.get(i).setPosition(i);
    			db.updateTask(items.get(i)); // update the db with the current position of all items
    			Log.d("added " + items.get(i).getTaskName(), " to db with position: " + String.valueOf(items.get(i).getPosition()));
		 }
    	undoAdapter.removePendingItem();
    }
    
    private void setSwipeDismissAdapter() {
		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(adapter, this);
        swipeAdapter.setAbsListView(getListView());
        getListView().setAdapter(swipeAdapter);
	}
    
	private void setContextualUndoAdapter() {
    	undoAdapter = new ContextualUndoAdapter(adapter, 
    			R.layout.undo_row, R.id.undo_row_undobutton, DELETE_DELAY, this);
    	undoAdapter.setAbsListView(getListView());
    	getListView().setAdapter(undoAdapter);
	}
    
    // dont actually need a reference object for the add button, just used the xml onclick "addTaskNow"
    public void addTaskNow(View v) {
    	addTaskText = (EditText) findViewById(R.id.editTextAddTask);
		String s = addTaskText.getText().toString();
		Task task = new Task(s, TASK_UNCHECKED); 
		Log.d("Created item position", " "+ String.valueOf(items.size()));
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
    	Toast.makeText(this, "enter the task description, you beautiful creature", Toast.LENGTH_LONG).show();
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
        
     
        // set the text of each textview to contents of the EditText
        // change to:  protected void onListItemClick(ListView l, View v, int position, long id)
        @Override
        public View getView(final int position, View convertView,
                final ViewGroup parent) {
        	if(convertView == null){
                LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
                convertView = mLayoutInflater.inflate(R.layout.list_row, null);
            }
        	
            final Task current = items.get(position);
            
            final TextView descriptionView = (TextView) convertView.findViewById(R.id.task_description);
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.taskCheckBox);
            
            // makes clicked status of checkbox save on view recycle and cross out text on checked
            checkBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(checkBox.isChecked()) {
						current.setStatus(TASK_CHECKED);
						descriptionView.setPaintFlags(descriptionView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					} else {
						current.setStatus(TASK_UNCHECKED);
						descriptionView.setPaintFlags(descriptionView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
					}
					
				}
            		
            });
            descriptionView.setText(current.getTaskName());
            // Log.d("here!", getItem(position));
            
            if(current.getStatus() == TASK_CHECKED){
                descriptionView.setPaintFlags(descriptionView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                checkBox.setChecked(true);
            }else{
                descriptionView.setPaintFlags(descriptionView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                checkBox.setChecked(false);
            }
            
            return convertView;
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
	 

	 private void setWindowContentOverlayCompat() {
		    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
		        // Get the content view
		        View contentView = findViewById(android.R.id.content);

		        // Make sure it's a valid instance of a FrameLayout
		        if (contentView instanceof FrameLayout) {
		            TypedValue tv = new TypedValue();

		            // Get the windowContentOverlay value of the current theme
		            if (getTheme().resolveAttribute(
		                    android.R.attr.windowContentOverlay, tv, true)) {

		                // If it's a valid resource, set it as the foreground drawable
		                // for the content view
		                if (tv.resourceId != 0) {
		                    ((FrameLayout) contentView).setForeground(
		                            getResources().getDrawable(tv.resourceId));
		                }
		            }
		        }
		    }
		}

}