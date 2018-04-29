package com.example.android.noteappsqlitequery;

import android.content.Intent;
import com.example.android.noteappsqlitequery.adapter.NoteAdapter;
import com.example.android.noteappsqlitequery.model.Note;
import com.example.android.noteappsqlitequery.utils.FakeDataUtils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NoteAdapter.NoteClickListener {
    private List<Note> notes;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get fake data
        notes = FakeDataUtils.getFakeNotes();
        recyclerView = findViewById(R.id.rv_note_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Set adapter
        NoteAdapter adapter = new NoteAdapter(notes, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(Note note) {
        // Create an intent to start DetailActivity
        Intent intent = new Intent(this, DetailActivity.class);
        // Add note as an intent extra
        intent.putExtra(DetailActivity.EXTRA_NOTE, note);
        startActivity(intent);
    }
}
