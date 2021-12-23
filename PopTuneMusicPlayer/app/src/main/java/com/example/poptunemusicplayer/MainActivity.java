package com.example.poptunemusicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    List<ItemModel> itemModelList = new ArrayList<>();
    String[] items;
    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewSong);

        runtimePermission();

    }

    public void runtimePermission(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()){
                            displaySongs();
                        }
                        else {
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file){
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();

        for (File singlefile:files){

            if (singlefile.isDirectory() && !singlefile.isHidden()){
                arrayList.addAll(findSong(singlefile));
            }

            else{

                if (singlefile.getName().endsWith(".mp3")){
                    arrayList.add(singlefile);
                }
            }
        }
        return arrayList;
    }

    public void displaySongs(){
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());

        items = new String[mySongs.size()];
        for (int i=0; i<mySongs.size();i++)
        {
            ItemModel itemModel = new ItemModel(mySongs.get(i).getName().toString().replace(".mp3", ""));
            itemModelList.add(itemModel);
        }

        customAdapter = new CustomAdapter(itemModelList, this);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ItemModel itemModel = (ItemModel) listView.getItemAtPosition(i);
                String songName = itemModel.getName();
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("position", i));
            }
        });
    }

    public class CustomAdapter extends BaseAdapter implements Filterable {
        private List<ItemModel> itemsModelsl;
        private List<ItemModel> itemsModelListFiltered;
        private Context context;

        public CustomAdapter(List<ItemModel> itemsModelsl, Context context) {
            this.itemsModelsl = itemsModelsl;
            this.itemsModelListFiltered = itemsModelsl;
            this.context = context;
        }

        @Override
        public int getCount() {
            return itemsModelListFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return itemsModelListFiltered.get(position);
        }

        @Override
        public long getItemId(int position) {
            return itemsModelListFiltered.size();
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textSong = (TextView) myView.findViewById(R.id.textSongName);
            textSong.setSelected(true);
            textSong.setText(itemsModelListFiltered.get(position).getName());
            return myView;
        }

        @Override
        public Filter getFilter(){
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null | constraint.length() == 0){
                        filterResults.count = itemsModelsl.size();
                        filterResults.values = itemsModelsl;
                    }
                    else {
                        List<ItemModel> resultModel = new ArrayList<>();
                        String searchStr = constraint.toString().toLowerCase();
                        for (ItemModel itemModel: itemsModelsl){
                            if (itemModel.getName().toLowerCase().contains(searchStr)){
                                resultModel.add(itemModel);
                            }
                            filterResults.count = resultModel.size();
                            filterResults.values = resultModel;
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    itemsModelListFiltered = (List<ItemModel>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.poptunemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.searchid:

                SearchView searchView = (SearchView) item.getActionView();
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setQueryHint("Type here to search");

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        customAdapter.getFilter().filter(newText);
                        return true;

                    }
                });

                return true;

            case R.id.subitem1:
                btnLightMode();
                Toast.makeText(this, "Light Theme Activated", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.subitem2:
                btnNightMode();
                Toast.makeText(this, "Dark Theme Activated", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.aboutid:
                about();
                Toast.makeText(this, "About Selected", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void btnNightMode() {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void btnLightMode() {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void about(){
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

}