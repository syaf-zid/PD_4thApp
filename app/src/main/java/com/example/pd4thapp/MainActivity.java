package com.example.pd4thapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> selectedRSSTopic, list4Spinner, allRSSTopicList;
    ArrayList<AllRSSFeed> allRSSObjs;
    ArrayAdapter<String> aa4Spinner, aa;

    Spinner spinner;
    Button btnAdd, btnRefresh;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = this.findViewById(R.id.spinner);
        btnAdd = this.findViewById(R.id.btnAdd);
        btnRefresh = this.findViewById(R.id.btnRefresh);
        lv = this.findViewById(R.id.lvRSS);

        selectedRSSTopic = new ArrayList<>();
        allRSSTopicList = new ArrayList<>();
        list4Spinner = new ArrayList<>();
        allRSSObjs = new ArrayList<>();

        aa4Spinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list4Spinner);
        spinner.setAdapter(aa4Spinner);

        aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allRSSTopicList);
        lv.setAdapter(aa);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected = (String) spinner.getSelectedItem();
                selectedRSSTopic.add(selected);
                allRSSTopicList.add(selected);
                allRSSObjs.add(new AllRSSFeed(selected, "Please refresh"));
                aa.notifyDataSetChanged();
            }
        });

        getTopic();

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allRSSObjs.clear();
                allRSSTopicList.clear();
                aa.notifyDataSetChanged();

                String[] topicArray = new String[selectedRSSTopic.size()];
                for(int i = 0; i < selectedRSSTopic.size(); i++) {
                    topicArray[i] = selectedRSSTopic.get(i);
                }

                RSSInfoGrabber grabber = new RSSInfoGrabber();
                grabber.execute(topicArray);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AllRSSFeed category = allRSSObjs.get(position);
                String catTitle = category.getTitle();
                String catUrl = category.getXmlUrl();

                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("title", catTitle);
                intent.putExtra("url", catUrl);

                Log.i("MainActivity", catTitle + ": " + catUrl);

                startActivity(intent);
            }
        });
    }

    private void getTopic() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        boolean value = settings.getBoolean("hasrun", false);

        if(!value) {
            selectedRSSTopic.add("Singapore");
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("hasrun", true);
            editor.apply();
        } else {
            int num = settings.getInt("num", 0);
            for(int i = 0; i < num; i++) {
                String topic = settings.getString("topic" + i, "Singapore");
                selectedRSSTopic.add(topic);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        allRSSObjs.clear();
        allRSSTopicList.clear();
        aa.notifyDataSetChanged();

        String[] topicArray = new String[selectedRSSTopic.size()];
        for(int i = 0; i < selectedRSSTopic.size(); i++) {
            topicArray[i] = selectedRSSTopic.get(i);
        }

        RSSInfoGrabber grabber = new RSSInfoGrabber();
        grabber.execute(topicArray);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("num", selectedRSSTopic.size());
        editor.apply();

        for(int i = 0; i < selectedRSSTopic.size(); i++) {
            String topic = selectedRSSTopic.get(i);
            editor = settings.edit();
            editor.putString("topic" + i, topic);
            editor.commit();
        }
    }

    private class RSSInfoGrabber extends AsyncTask<String, Integer, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(MainActivity.this, "ST RSS Reader", "Retrieving data...");
            pd.setProgress(0);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            aa.notifyDataSetChanged();
            aa4Spinner.notifyDataSetChanged();
            pd.dismiss();

            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... topics) {
            ArrayList<AllRSSFeed> al = new ArrayList<>();
            URL url;

            try {
                String strUrl = "https://www.straitstimes.com/sites/default/files/rss_breaking_news.opml";
                url = new URL(strUrl);

                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                int responseCode = httpConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = httpConnection.getInputStream();

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document document = db.parse(in);
                    Element rootElement = document.getDocumentElement();

                    NodeList bodyNodeList = rootElement.getElementsByTagName("body");
                    Node bodyNode = bodyNodeList.item(0);
                    Element bodyElement = (Element) bodyNode;

                    NodeList outlineNodeList = bodyElement.getElementsByTagName("outline");
                    for(int i = 0; i < outlineNodeList.getLength(); i++) {
                        if(outlineNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Node outlineNode = outlineNodeList.item(i);
                            Element outlineElement = (Element) outlineNode;

                            String title = outlineElement.getAttribute("title");
                            String link = outlineElement.getAttribute("xmlUrl");

                            Log.i("MainActivity", title);
                            Log.i("Main Activity", link);

                            allRSSTopicList.add(title);
                            allRSSObjs.add(new AllRSSFeed(title, link));
                            al.add(new AllRSSFeed(title, link));
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            allRSSObjs.clear();
            allRSSTopicList.clear();
            for(int i = 0; i < topics.length; i ++) {
                String topic = topics[i];
                for(AllRSSFeed obj:al) {
                    if(obj.getTitle().equals(topic)) {
                        allRSSObjs.add(obj);
                        allRSSTopicList.add(obj.getTitle());
                    }
                }
            }

            if(list4Spinner.size() == 0) {
                for(AllRSSFeed obj: al) {
                    list4Spinner.add(obj.getTitle());
                }
            }

            return null;
        }
    }
}