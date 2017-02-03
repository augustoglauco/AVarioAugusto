package org.avario;

/**
 * Created by Augusto on 02/02/2017.
 */
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import org.avario.ui.prefs.IllustrativeRSS;
import org.avario.ui.prefs.IllustrativeRSSParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadIntentService extends IntentService {

    private static final String TAG = DownloadIntentService.class.getSimpleName();

    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String URL_EXTRA = "url";
    public static final String RSS_RESULT_EXTRA = "url";

    public static final int RESULT_CODE = 0;
    public static final int INVALID_URL_CODE = 1;
    public static final int ERROR_CODE = 2;

    private IllustrativeRSSParser parser;

    public DownloadIntentService() {
        super(TAG);

        // make one and re-use, in the case where more than one intent is queued
        parser = new IllustrativeRSSParser();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        try {
            try {
                URL url = new URL(intent.getStringExtra(URL_EXTRA));
                //IllustrativeRSS rss = parser.parse(in = url.openStream());
                in = url.openStream();
                if(in != null)
                {
                    BufferedReader br = null;
                    String line;
                    try {

                        br = new BufferedReader(new InputStreamReader(in));
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                {
                    sb.append("<metar>\n" +
                            "<codigo>SBRJ</codigo>\n" +
                            "<atualizacao>Erro</atualizacao>\n" +
                            "</metar>"
                    );
                }

                Intent result = new Intent();
                result.putExtra(RSS_RESULT_EXTRA, sb.toString());
                reply.send(this, RESULT_CODE, result);

            } catch (MalformedURLException exc) {
                reply.send(INVALID_URL_CODE);
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            }
        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }
}