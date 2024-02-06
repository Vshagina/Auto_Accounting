package com.example.auto_accounting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;


public class NoteFragment extends Fragment {
    private RecyclerView recyclerViewNote;
    private NoteAdapter noteAdapter;
    private DBHelper dbHelper;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public NoteFragment() {
        // Required empty public constructor
    }

    public static NoteFragment newInstance(String param1, String param2) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);

        recyclerViewNote = view.findViewById(R.id.recyclerViewNote);
        recyclerViewNote.setLayoutManager(new LinearLayoutManager(requireContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteNoteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerViewNote);

        noteAdapter = new NoteAdapter(getNoteData());
        recyclerViewNote.setAdapter(noteAdapter);

        Button addButton = view.findViewById(R.id.buttonNote);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(noteAdapter);
            }
        });

        return view;
    }

    private void showNoteDialog(NoteAdapter noteAdapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Добавить заметку");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteText = input.getText().toString().trim();
                if (!TextUtils.isEmpty(noteText)) {
                    // Использование переданного в метод NoteAdapter
                    noteAdapter.addNoteItem(createNoteData(noteText, false));
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private NoteData createNoteData(String noteText, boolean noteChecked) {
        return new NoteData(0, noteText, noteChecked);
    }



    private class SwipeToDeleteNoteCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeToDeleteNoteCallback() {
            super(0, ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            noteAdapter.removeNoteItem(position);
        }


        // Добавьте этот метод для установки различных цветов фона для свайпов
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; // отрегулируйте это значение для увеличения/уменьшения радиуса угла

            // Установите разные цвета фона для свайпов вправо (редактирование) и влево (удаление)
            if (dX < 0) { // Свайп влево (удаление)
                Drawable deleteBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDeleteSwipeBackground));
                deleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                deleteBackground.draw(c);

                // Отобразите надпись "Удаление"
                drawText(c, "Удаление", itemView.getRight() - 250, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            }
        }

        // Добавьте этот метод для отображения текста при свайпе внутри элемента
        private void drawText(Canvas canvas, String text, float x, float y, int color) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(40);
            paint.setAntiAlias(true);
            canvas.drawText(text, x, y, paint);
        }
    }

    private List<NoteData> getNoteData() {
        List<NoteData> noteDataList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (db == null) {
            return noteDataList;
        }

        String[] projection = {
                "_id",
                DBHelper.COLUMN_NOTE_TEXT,
                DBHelper.COLUMN_NOTE_CHECKED
        };

        Cursor cursor = db.query(
                DBHelper.TABLE_NOTES,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long noteId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String noteText = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOTE_TEXT));
            int noteChecked = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOTE_CHECKED));

            NoteData noteData = new NoteData(noteId, noteText, noteChecked == 1);
            noteDataList.add(noteData);
        }

        cursor.close();
        db.close();

        return noteDataList;
    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private List<NoteData> noteDataList;

        public NoteAdapter(List<NoteData> noteDataList) {
            this.noteDataList = noteDataList;
        }

        @Override
        public int getItemCount() {
            return noteDataList.size();
        }

        public void removeNoteItem(int position) {
            long noteId = noteDataList.get(position).getId();

            // Удалите элемент из списка
            noteDataList.remove(position);
            notifyItemRemoved(position);

            // Обновите базу данных
            int deletedRows = dbHelper.deleteNoteById(noteId);
        }

        public void addNoteItem(NoteData noteData) {
            noteDataList.add(noteData);
            notifyItemInserted(noteDataList.size() - 1);

            // Вставьте новую заметку в базу данных
            dbHelper.insertNote(noteData.getNoteText());
        }


        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            NoteData noteData = noteDataList.get(position);
            holder.bind(noteData);
        }

        private class NoteViewHolder extends RecyclerView.ViewHolder {
            private CheckBox checkBoxNote;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBoxNote = itemView.findViewById(R.id.checkBoxNote);

                // Добавить обработчик щелчка для CheckBox
                checkBoxNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Обработка события щелчка по CheckBox, при необходимости
                    }
                });
            }
            public void bind(NoteData noteData) {
                // Установить текст и статус отмеченности для CheckBox
                checkBoxNote.setText(noteData.getNoteText());
                // Предполагается, что у вас есть метод, например, isNoteChecked() в NoteData
                checkBoxNote.setChecked(noteData.isNoteChecked());
            }
        }
    }
}