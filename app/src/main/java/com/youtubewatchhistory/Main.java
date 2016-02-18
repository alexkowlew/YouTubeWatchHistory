package com.youtubewatchhistory;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends AppCompatActivity {

    private final String TAG = Main.class.getName();

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private YouTube youTube;
    private GoogleAccountCredential credential;
    private static final int REQUEST_AUTHORIZATION = 3;
    private static final int REQUEST_ACCOUNT_PICKER = 2;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    private String chosenAccountName;

    private ProgressDialog dialog;
    private ListView listView;
    private SimpleListAdapter adapter;
    private List<History> histories = new ArrayList<History>();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        adapter = new SimpleListAdapter(getApplicationContext(), histories);
        listView.setAdapter(adapter);

        credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(Auth.SCOPES));

        if (savedInstanceState != null)
            chosenAccountName = savedInstanceState.getString(PREF_ACCOUNT_NAME);
        else
            chooseAccount();

        credential.setSelectedAccountName(chosenAccountName);

        youTube = new YouTube.Builder(transport, jsonFactory,
                credential).setApplicationName(getResources().getString(R.string.app_name))
                .build();

    }

    private void hideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private void loadWatchHistory() {
        if (chosenAccountName == null) {
            Log.d(TAG, "chosenAccountName: " + chosenAccountName);
            return;
        }

        Log.d(TAG, "Success!");


        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        new AsyncTask<Void, Void, List<History>>() {

            @Override
            protected List<History> doInBackground(Void... params) {

                try {
                    YouTube.Channels.List channelRequest = youTube.channels().list("contentDetails");
                    channelRequest.setMine(true);
                    ChannelListResponse channelResult = channelRequest.execute();
                    String playListId = channelResult.getItems().get(0).getContentDetails().
                            getRelatedPlaylists().getWatchHistory();

                    YouTube.PlaylistItems.List playlistItemRequest =
                            youTube.playlistItems().list("snippet");
                    playlistItemRequest.setPlaylistId(playListId);
                    playlistItemRequest.setMaxResults(50l);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    for (int i = 0; i < playlistItemResult.getItems().size(); i++) {
                        if (playlistItemResult.getItems().get(i).getSnippet().getThumbnails() != null) {
                            histories.add(new History(playlistItemResult
                                    .getItems().get(i).getSnippet().getTitle(),
                                    playlistItemResult.getItems().get(i).getSnippet().getThumbnails()
                                            .getDefault().getUrl()));
                        }
                    }

                    return histories;
                } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                    availabilityException.printStackTrace();
                } catch (UserRecoverableAuthIOException userRecoverableException) {
                    startActivityForResult(
                            userRecoverableException.getIntent(),
                            REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<History> histories) {
                super.onPostExecute(histories);

                if (histories == null) {
                    Log.d(TAG, "histories is null");
                    return;
                } else {
                    hideProgressDialog();
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute((Void) null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        chosenAccountName = accountName;
                        credential.setSelectedAccountName(chosenAccountName);
                        saveAccount();
                        loadWatchHistory();
                    }
                }
                break;
        }
    }

    private void saveAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);

        sp.edit().putString(PREF_ACCOUNT_NAME, chosenAccountName).commit();
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    private void loadAccount() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        chosenAccountName = sp.getString(PREF_ACCOUNT_NAME, null);
        invalidateOptionsMenu();

    }

}
