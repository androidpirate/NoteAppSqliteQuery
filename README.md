# Previously on Building A Note Taking App

In the [**previous tutorial**](https://androidpirate.github.io/NoteAppIntents/ "**previous tutorial**"), we displayed details of a note on a separate activity, all thanks to **Intents**.
Also learned a little about how the navigation works within our app and setup a **Up Navigation**.



## NoteApp SQLiteQuery – Tutorial 4

Start by cloning/forking the app from the link below:

[**NoteApp SQLiteQuery - Tutorial 4**](https://github.com/androidpirate/NoteAppSqliteQuery "**NoteApp SQLiteQuery - Tutorial 4**")



### Goal of This Tutorial

The goal of this tutorial is to create our own database for our app to store and manipulate our notes! This is going to be the end of our
static data!

**This is going to be a huge one!**

Most apps these days, work with both local, remote databases and even cloud storages to store their data. **SQLite** is the local database that is shipped with every Android OS to store data locally on your device.



### What’s in Starter Module?

Starter module already has a fully functional **RecyclerView** which displays static notes provided by **FakeDataUtils class** as cards and responds to user clicks by displaying the details of a note in **DetailActivity**.

**If you have never implemented an SQLite database before, I highly recommend to follow this tutorial closely.**

You can follow the steps below and give it a shot yourself, and if you stuck at some point, check out the **solution module** or the rest of the tutorial.



### Steps to Build

1. Add a new field: **int _id** to **Note class**
2. Add a new filed: **int _id** to **Parcel interface** in **Note class**
3. Add a new package: **db** under project pane
4. Add a new class under **db** package: **NoteContract.java**
5. Add static constant fields to **ItemContract.java** which are used to define an SQLite database table
6. Add a new class under **db** package: **NoteDbHelper.java**
7. Implement necessary callbacks to create and update an SQLite database using **Singleton Pattern**
8. Implement **Query method**
9. Remove **FakeDataUtils class** from project files
10. Display an empty list message in **MainActivity** to be displayed when the database is empty



### ID

Every database uses some sort of unique identification to keep track of the underlying data and **SQLite** is no difference. It uses a primary key to
keep track entries which are automatically assigned to each entry as we insert them into the database. Since our goal is to create a database to store
our notes, our model class should have a field to store that information. Open up **Note class** and add an **int _id** field:


```java
public class Note implements Parcelable {
    private int _id;
    // Fields and methods are excluded for simplicity

    public Note() {
    }

    public Note(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Add _id field to Parcel
        dest.writeInt(this._id);
        dest.writeString(this.title);
        dest.writeString(this.description);
    }

    protected Note(Parcel in) {
        // Set _id field from Parcel
        this._id = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
    }
}
```


Here we created **_id** field and also added necessary **getter and setter methods**  for it. Also we are adding it, to **Parcel interface constructor** and **writeToParcel()** method.



### Database Schema and Contract Class

A schema is a formal way of declaring how a database is organized. For example, your contact list. The list has a bunch of contacts which have a unique identifier, and some other fields (such as name, or phone number).

A **Contract class** is a class which is used to contain a bunch of constants that will define names for URIs, tables, and columns on your database, used to define a schema. The advantage of using a **Contract class** is that it allows you the use same constants across all the other classes in the same package. If a constant is updated, the change will be applied to all classes that use it.

First, create a new package **db** and then create a new class **NoteContract.java** under the **db** package. Let's define the constants that we will use to create our database in **NoteContract.java**:


```java
/**
 * Contract class that defines the table and columns
 * for Note database.
 */
public final class NoteContract {
    /**
     * Private constructor prevents
     * accidentally instantiating NoteContract class.
     */
    private NoteContract() {
    }

    public static class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String NOTE_TITLE = "title";
        public static final String NOTE_DESCRIPTION = "description";
    }
}
```


Here we define database **table name, title and description columns**. Our **NoteEntry class** implements **BaseColumns interface** which defines **_ID** and **_COUNT** constants out of the box so we don't need to create constants for them. (More on **BaseColumns**  [**here**](https://stackoverflow.com/questions/7899720/what-is-the-use-of-basecolumns-in-android " **here**"))



### Database Helper

The **SQLiteOpenHelper class** is part of your Android framework, which uses a set of useful APIs that allows us to manage your **SQLite database**. We are going to create our own custom helper class, which inherits from **SQLiteOpenHelper**. Start with adding **NoteDbHelper.java class**  under **db** package an make sure, it extends **SQLiteOpenHelper**. Then override two major callbacks **onCreate()** and **onUpdate()**:

```java
public class NoteDbHelper extends SQLiteOpenHelper {

    public NoteDbHelper() {

    }

    @Override
    public void onCreate(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
```


One thing for sure is the **SQLiteOpenHelper** doesn't come with much out of the box, which means we need to implement most of the database callbacks and methods ourselves.

In order to instantiate **NoteDbHelper** we need the following:
* Context
* Database Name
* Database Version

The **Context** is passed by the class instantiating **NoteDbHelper**. It is a good idea to assign a static constant for other parameters since they don't change frequently, plus it is easy to update the value and refer to the same constant every time, plus it is a best practice to not to hardcode these parameters, which is proved to be error-prone.


```java
public class NoteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    public NoteDbHelper() {

    }
    // Callbacks are excluded for simplicity
}
```


You can name your database whatever you like, just make sure it has a **"*.db"** extension in the end. Since it is the first time we are implementing our database, the version number is set to **1**. The **version number** increases as you keep updating your database with new columns and tables.

**NoteDbHelper** is our access point to the app's database and as you will see in this tutorial we will implement some methods that will allow us to manipulate the data stored in the database. Which means we need to access an instance of **NoteDbHelper** from different parts of our application, as the user keeps interacting with our app. Once you have multiple access points to your database, it means multiple actions can be performed at the same time, which may cause conflicts, such as inconsistency updating the data, if not losing the data...(**We are doomed!!**) But wait... There is a way to prevent it!



### Singleton Pattern

A **singleton pattern** is a software design pattern which restricts the instantiation of a class to **only one** object. Basically, it is one of a kind object, which means it can not be presented to different parts of the application at the same. (**There can be only one!**)

The way to turn a class into a **singleton** is fairly easy. Start with switching the access modifier from **public** to **private** for **NoteDbHelper's constructor**. That means, **constructor** is only accessible within **NoteDbHelper class**. (**Good job, how are we going to get the instance of NoteDbHelper anymore???**) So the only way to access the constructor is through a static **getInstance() method**, and return the static instance of **NoteDbHelper** (**static method <-> static field**):

```java
public class NoteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    private static NoteDbHelper sInstance;

    public static NoteDbHelper getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new NoteDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Callbacks are excluded for simplicity
}
```


Whenever we call **getInstance() method**, we will pass a **Context** as an argument. We already learned keeping **Context reference in a static field is never a good idea because it causes memory leaks!**. The trick is to get the **Application Context (which is a singleton itself)** , so the **Context** we passed as an argument, such as an **activity**, won't leak.

The **super() method** of **NoteDbHelper** accepts four arguments; a **Context**, **database name**, **cursor factory** and **database version**. We use it to instantiate **NoteDbHelper**.

The database though is not created yet, next we are going to implement that using the following callbacks.

**onCreate()** is called when the app is running for the first time on your device. This is where the creation and initialization of your database tables happens.

**onUpgrade()** however, is called when a new version of the database is available and the existing database needs to be updated. This is where dropping, updating or creating new tables are handled.

Unfortunately, we have to use **SQLite commands** to create an **SQLite database.(At least this was the case the Room library, which is introduced as part of the Android Architecture Components**) As mentioned above, it is **not** a good practice to write **raw SQL commands** so it is better to use constants:


```java
public class NoteDbHelper extends SQLiteOpenHelper {
    private static final String COMMA_SEPARATOR = ",";
    // SQL command to create notes table
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + NoteEntry.TABLE_NAME + "(" +
            NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEPARATOR +
            NoteEntry.NOTE_TITLE + " TEXT NOT NULL" + COMMA_SEPARATOR +
            NoteEntry.NOTE_DESCRIPTION + " TEXT NOT NULL " + ");";
    // SQL command to drop a table
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME;
    // Fields and callbacks are excluded for simplicity
}
```


**SQL_CREATE_TABLE** command wraps up the entire **raw SQL command** to create a table in the database. Everything you see in **CAPS** is a **raw SQL command** and the rest are names for fields. We are using the constants we defined in **NoteContract** for field names so nothing is hard-coded! (**We are even using a constant for comma!**)

For every column in notes table, there is a **type**, which is specified right after the column name. (e.g. **INTEGER** for **_ID**) Also notice **PRIMARY KEY** which is used to specify that **_ID** is the unique key for each row in the table and **AUTOINCREMENT** which is used to specify each time a new row is added **_ID** gets incremented automatically.

Likewise, **TEXT** is the type of **NOTE_TITLE** column and **NOT NULL** is used to specify that this column cannot be empty.

Below we execute **SQL_CREATE_TABLE** command to create our database:


```java
public class NoteDbHelper extends SQLiteOpenHelper {
    // Fields and callbacks are excluded for simplicity
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }
}
```


And we drop the table (**a fancy name for delete**), if it exists whenever the app is upgraded and recreate it:


```java
public class NoteDbHelper extends SQLiteOpenHelper {
    // Fields and callbacks are excluded for simplicity
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}
```


Of course, the above command doesn't drop the existing table and creates a new one as the **database version** increases, **it does that every time you re-install the app!** That is not the best way to handle upgrading a database, but since you have read this far, I will do you a favor... (Check [**this**](https://medium.com/@elye.project/android-sqlite-database-migration-b9ad47811d34 " **this**") out for how to properly handle upgrading databases.)



### Query the Database

Once the database is created, we will pull the data from the database to display notes. To do so, we simply **query the database** and the result is a **Cursor** object, which points to the rows of the query result, starting with the very first row. The query itself can be modified to return different results, but in our case, we are going to return all the notes from the database:


```java
public class NoteDbHelper extends SQLiteOpenHelper {
    // Fields and callbacks are excluded for simplicity
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(NoteEntry.TABLE_NAME,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null);
        try {
            if(cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.set_id(cursor.getInt(cursor.getColumnIndex(NoteEntry._ID)));
                    note.setTitle(cursor.getString(cursor.getColumnIndex(NoteEntry.NOTE_TITLE)));
                    note.setDescription(cursor.getString(cursor.getColumnIndex(NoteEntry.NOTE_DESCRIPTION)));
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error fetching notes from database.");
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }
}
```


Here we implement the **public getAllNotes() method** to query the database for all notes. It is a good idea to wrap the query in a method which adds an abstraction layer to actual query and returns the results as a **list**, so we don't have to worry about the details of the query.

Pay attention that we are getting a **SQLiteDatabase** instance to perform any database related functionality. **SQLiteDatabase** instance we get, can be either **readable or writable**, the latter is to used to update the data. The **query() method** only specifies the table (**notes table**), and the rest of the parameters are **null**, which means we don't point to any particular row or group. (Check out how other parameters are used in a query: [**official documentation**](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase#query(java.lang.String,%20java.lang.String%5B%5D,%20java.lang.String,%20java.lang.String%5B%5D,%20java.lang.String,%20java.lang.String,%20java.lang.String%29))

As mentioned above, the query returns a **Cursor** object as a result, and using a **try-catch block** along with a **do while loop**, it can be moved through the results of the query one by one and get the information for each row stored in different columns. We close the cursor after inserting the notes to the list and return the list!  



### Nothing is Static... Everything Falls

It is time to say goodbye to our fancy class **FakeDataUtils**, no more static data. Open up your project pane and delete the entire **utils** package and that should take care of it.



### There is No List

Once this tutorial is over, you will get to try out the app. Only to realize the truth... There is no list!

As mentioned above, this will be the first time your database is going to be created and there won't be any data. Instead of showing the user an empty screen, which is more or less confusing, let's try to show a friendly message.

To do so, all we need to do is to display a warning message if the result of our database query returns with an empty list. The message is going to be part of **activity_main.xml**:


``` xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.android.noteappsqlite.MainActivity">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv_note_list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <TextView
        android:id="@+id/tv_empty_list"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:text="@string/empty_list_message"
        android:textSize="22sp"
        android:layout_centerInParent="true"
        android:gravity="center"
        android:visibility="invisible"
        tools:text="@string/empty_list_message"/>

</RelativeLayout>
```


We give our **TextView** an **id** to refer it later on, and we create a **string resource** for the message. We also set the **visibility** property to **invisible** to start with. Our message will **only be visible if the list is empty**.



### Getting Notes

Switch to **MainActivity**, and replace the static data with a query to the database and get a reference for **empty list TextView**:

``` java
public class MainActivity extends AppCompatActivity
    implements NoteAdapter.NoteClickListener {
    private TextView emptyListMessage;
    // Fields and callbacks are excluded for simplicity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get fake data (Remove)
        // notes = FakeDataUtils.getFakeNotes(); (Remove)
        NoteDbHelper noteDbHelper = NoteDbHelper.getInstance(this);
        // Get all notes from database
        List<Note> notes = noteDbHelper.getAllNotes();
        recyclerView = findViewById(R.id.rv_note_list);
        emptyListMessage = findViewById(R.id.tv_empty_list);
        .
        .
    }
}
```


Now we can easily handle both situations depending on the size of the list:


``` java
public class MainActivity extends AppCompatActivity
    implements NoteAdapter.NoteClickListener {
    // Fields and callbacks are excluded for simplicity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        .
        .
        emptyListMessage = findViewById(R.id.tv_empty_list);
        if(notes.size() == 0) {
            displayEmptyListMessage();
        } else {
            displayRecyclerView();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Set adapter
            NoteAdapter adapter = new NoteAdapter(notes, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void displayRecyclerView() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void displayEmptyListMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyListMessage.setVisibility(View.VISIBLE);
    }
}
```

We used two private methods **displayRecyclerView() and displayEmptyListMessage()** to set the visibility of either.

That's it, you survived this gigantic tutorial! (**Believe me if there was an achievement for it, I would give it to you!**) Run the app... Only to see the message that tells you, your list is empty.  



### What's In Next Tutorial

In our [**next tutorial**](https://androidpirate.github.io/NoteAppSqliteInsert/ "**next tutorial**"), we are going to implement the insert functionality in **NoteDbHelper** and finally able to add new notes. Stay tuned!



### Resources
1. [Android Developer Guides](https://developer.android.com/guide/ "Android Developer Guides") by Google
2. [Elye on Medium](https://medium.com/@elye.project "Elye on Medium") by Elye
