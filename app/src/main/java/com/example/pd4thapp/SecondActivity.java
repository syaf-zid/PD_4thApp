package com.example.pd4thapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

public class SecondActivity extends AppCompatActivity {
    TextView tvTitle;
    ListView lvInfo;
    Button btnBack;

    String title, xmlUrl;

    CustomArrayAdapter aa;
    ArrayList<ReadXMLFile> articleInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        xmlUrl = intent.getStringExtra("url");

        Log.i("SecondActivity", title + ": " + xmlUrl);

        tvTitle = findViewById(R.id.textViewCategory);
        lvInfo = findViewById(R.id.listViewInfo);
        btnBack = findViewById(R.id.buttonBack);

        tvTitle.setText(title);

        articleInfoList = new ArrayList<>();
        aa = new CustomArrayAdapter(this, R.layout.row, articleInfoList);
        lvInfo.setAdapter(aa);

        String[] articleArray = new String[articleInfoList.size()];
        for(int i = 0; i < articleInfoList.size(); i++) {
            articleArray[i] = articleInfoList.get(i).getTitle();
        }

        ArticleInfoGrabber grabber = new ArticleInfoGrabber();
        grabber.execute(articleArray);

        lvInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ReadXMLFile link = articleInfoList.get(position);
                String url = link.getLink();

                Intent intent = new Intent(SecondActivity.this, ThirdActivity.class);
                intent.putExtra("url", url);

                Log.i("MainActivity", url);

                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SecondActivity", "Going back to MainActivity");
                finish();
            }
        });
    }

    private class ArticleInfoGrabber extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... articles) {
            ArrayList<ReadXMLFile> al = new ArrayList<>();
            URL url;

            try {
                url = new URL(xmlUrl);

                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                int responseCode = httpConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = httpConnection.getInputStream();

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document document = db.parse(in);
                    Element channelElement = document.getDocumentElement();

                    NodeList itemNodeList = channelElement.getElementsByTagName("item");
                    for(int i = 0; i < itemNodeList.getLength(); i++) {
                        if(itemNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            String pubDate = null, title = null, link = null, desc = null;

                            Node itemNode = itemNodeList.item(i);
                            Element itemElement = (Element) itemNode;

                            NodeList pubDateNodeList = channelElement.getElementsByTagName("pubDate");
                            if(pubDateNodeList.item(0) != null) {
                                Element pubDateElement = (Element) pubDateNodeList.item(0);
                                pubDate = pubDateElement.getTextContent();
                            }

                            NodeList titleNodeList = itemElement.getElementsByTagName("title");
                            if(titleNodeList.item(0) != null) {
                                Element titleElement = (Element) titleNodeList.item(0);
                                title = titleElement.getTextContent();
                            }

                            NodeList linkNodeList = itemElement.getElementsByTagName("link");
                            if(linkNodeList.item(0) != null) {
                                Element linkElement = (Element) linkNodeList.item(0);
                                link = linkElement.getTextContent();
                            }

                            NodeList descNodeList = itemElement.getElementsByTagName("description");
                            if(descNodeList.item(0) != null) {
                                Element descElement = (Element) descNodeList.item(0);
                                Log.i("SecondActivity", descElement.getTextContent().substring(33, descElement.getTextContent().length() - 1));
                                desc = descElement.getTextContent().substring(33, descElement.getTextContent().length() - 1);
                            }

                            if(pubDate != null && title != null && desc != null && link != null) {
                                al.add(new ReadXMLFile(pubDate, title, desc, link));
                            } else {
                                al.add(new ReadXMLFile(pubDate, title, "", link));
                            }
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

            articleInfoList.clear();
            for(int i = 0; i < articles.length; i++) {
                String article = articles[i];
                for(ReadXMLFile obj : al) {
                    if(obj.getTitle().equals(article)) {
                        articleInfoList.add(obj);
                    }
                }
            }

            if(articleInfoList.size() == 0) {
                articleInfoList.addAll(al);
            }

            return null;
        }
    }
}