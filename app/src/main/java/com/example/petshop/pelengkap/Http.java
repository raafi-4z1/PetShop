package com.example.petshop.pelengkap;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

public class Http {
    private String method = "GET", data = null, response = null, url;
    private int statusCode = 0;
    private Boolean token = false;
    private LocalStorage localStorage;

    public Http(Context context, String url) {
        localStorage = new LocalStorage(context);
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase(Locale.getDefault());
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setToken(Boolean token) {
        this.token = token;
    }

    public String getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void send() {
        HttpURLConnection connection = null;
        String boundary = "*****"; // Sesuaikan dengan boundary yang sesuai

        try {
            URL sUrl = new URL(url);
            connection = (HttpURLConnection) sUrl.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            if (token) {
                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + localStorage.getToken()
                );
            }

            if (!method.equals("GET")) {
                connection.setDoOutput(true);
            }

            // for https://github.com/raafi-4z1/Petshop-API-PHP-Natif-main.git
            if (data != null) {
                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                JSONObject jsonObject = new JSONObject(data);

                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jsonObject.getString(key);

                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
                    writer.append(value).append("\r\n");
                }

                writer.flush();
                writer.close();
            }

            statusCode = connection.getResponseCode();
            InputStreamReader isr;
            if (statusCode >= 200 && statusCode <= 299) {
                // if success response
                isr = new InputStreamReader(connection.getInputStream());
            } else {
                isr = new InputStreamReader(connection.getErrorStream());
            }

            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            response = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            // Disconnect koneksi setelah selesai
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
