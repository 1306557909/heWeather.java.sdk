package com.heweather.api.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heweather.api.InitializeSign;
import com.heweather.api.dto.ApiEnum;
import com.heweather.api.dto.response.AirQuality;
import com.heweather.api.dto.response.HeWeatherResponse;
import com.heweather.api.dto.response.air.Daily;
import com.heweather.api.dto.response.air.Now;
import com.heweather.api.dto.response.air.Station;
import com.heweather.api.dto.response.weatherinfo.Refer;
import com.heweather.api.service.HeWeatherAirService;
import com.heweather.utils.SignUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 空气质量请求处理
 * add by djc
 */
public class HeWeatherAirServiceImpl implements HeWeatherAirService {

    @Override
    public HeWeatherResponse getWeatherAir(String location, String lang, ApiEnum apiEnum) {

        HeWeatherResponse heWeatherResponse = new HeWeatherResponse();
        String URL = "https://api.heweather.net/v7/air/";
        HashMap<String, String> params = new HashMap<>();
        String key;
        String sign;
        try {
            key = InitializeSign.getKey();
            sign = InitializeSign.getSign();
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("4001");
            return heWeatherResponse;
        }
        if (apiEnum != null) {
            URL = URL + apiEnum.getValue();
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        if (location != null && !location.equals("")) {
            URL = URL + "location" + location;
            params.put("location", location);
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        if (key != null && !key.equals("")) {
            URL = URL + "&username" + InitializeSign.getKey();
            params.put("username", InitializeSign.getKey());
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        if (lang != null && !lang.equals("")) {
            URL = URL + "&lang" + lang;
            params.put("lang", lang);
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        String t = String.valueOf(System.currentTimeMillis() / 1000);
        URL = URL + "&t" + t;
        params.put("t", t);
        String secret = sign;
        try {
            String signature = SignUtils.getSignature(params, secret);
            URL = URL + "&sign" + signature;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String json = httpResponse.getEntity().toString();
            JSONObject response = (JSONObject) JSONObject.parse(json);
            if (response.get("code").equals("200")) {
                heWeatherResponse.setStatus("200");
                heWeatherResponse.setUpdateTime(response.getString("updateTime"));
                heWeatherResponse.setFxLink(response.getString("fxLink"));
                AirQuality airQuality = new AirQuality();
                if (response.containsKey("now")) {
                    JSONObject nowobj = response.getJSONObject("now");
                    Now now = new Now();
                    now.setAqi(nowobj.getString("aqi"));
                    now.setCategory(nowobj.getString("category"));
                    now.setCo(nowobj.getString("co"));
                    now.setNo2(nowobj.getString("no2"));
                    now.setO3(nowobj.getString("o3"));
                    now.setPm2p5(nowobj.getString("pm2p5"));
                    now.setPm10(nowobj.getString("pm10"));
                    now.setPrimary(nowobj.getString("primary"));
                    now.setPubTime(nowobj.getString("pubTime"));
                    now.setSo2(nowobj.getString("so2"));
                    JSONArray stationes = response.getJSONArray("station");
                    List<Station> stationList = new ArrayList<Station>();
                    List<JSONObject> list = JSONObject.parseArray(stationes.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Station station = new Station();
                        station.setAqi(jsonObject.getString("api"));
                        station.setCategory(jsonObject.getString("category"));
                        station.setCo(jsonObject.getString("co"));
                        station.setId(jsonObject.getString("stationId"));
                        station.setLevel(jsonObject.getString("level"));
                        station.setName(jsonObject.getString("stationName"));
                        station.setNo2(jsonObject.getString("no2"));
                        station.setO3(jsonObject.getString("o3"));
                        station.setPm2p5(jsonObject.getString("pm2p5"));
                        station.setPm10(jsonObject.getString("pm10"));
                        station.setPrimary(jsonObject.getString("primary"));
                        station.setPubTime(jsonObject.getString("pubTime"));
                        station.setSo2(jsonObject.getString("so2"));
                        stationList.add(station);
                    }
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    airQuality.setNow(now);
                    airQuality.setStation(stationList);
                    airQuality.setRefer(refer);
                    heWeatherResponse.setAirQuality(airQuality);
                } else if (response.containsKey("daily")) {
                    List<Daily> dailyList = new ArrayList<Daily>();
                    JSONArray dailies = response.getJSONArray("station");
                    List<JSONObject> list = JSONObject.parseArray(dailies.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Daily daily = new Daily();
                        daily.setAqi(jsonObject.getString("aqi"));
                        daily.setCategory(jsonObject.getString("category"));
                        daily.setFxDate(jsonObject.getString("fxDate"));
                        daily.setPrimary(jsonObject.getString("primary"));
                        dailyList.add(daily);
                    }
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    airQuality.setDaily(dailyList);
                    airQuality.setRefer(refer);
                }
            } else {
                heWeatherResponse.setStatus("400");
            }
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("400");
        }
        return heWeatherResponse;
    }

    @Override
    public HeWeatherResponse getWeatherAir(String location, ApiEnum apiEnum) {

        HeWeatherResponse heWeatherResponse = new HeWeatherResponse();
        String URL = "https://api.heweather.net/v7/air/";
        HashMap<String, String> params = new HashMap<>();
        if (apiEnum != null) {
            URL = URL + apiEnum.getValue();
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        String key;
        String sign;
        try {
            key = InitializeSign.getKey();
            sign = InitializeSign.getSign();
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("4001");
            return heWeatherResponse;
        }

        if (location != null && !location.equals("")) {
            URL = URL + "location" + location;
            params.put("location", location);
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        if (key != null && !key.equals("")) {
            URL = URL + "&username" + key;
            params.put("username", key);
        } else {
            heWeatherResponse.setStatus("401");
            return heWeatherResponse;
        }
        String t = String.valueOf(System.currentTimeMillis() / 1000);
        URL = URL + "&t" + t;
        params.put("t", t);
        String secret = sign;
        try {
            String signature = SignUtils.getSignature(params, secret);
            URL = URL + "&sign" + signature;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URL);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String json = httpResponse.getEntity().toString();
            JSONObject response = (JSONObject) JSONObject.parse(json);
            if (response.get("code").equals("200")) {
                heWeatherResponse.setStatus("200");
                heWeatherResponse.setUpdateTime(response.getString("updateTime"));
                heWeatherResponse.setFxLink(response.getString("fxLink"));
                AirQuality airQuality = new AirQuality();
                if (response.containsKey("now")) {
                    JSONObject nowobj = response.getJSONObject("now");
                    Now now = new Now();
                    now.setAqi(nowobj.getString("aqi"));
                    now.setCategory(nowobj.getString("category"));
                    now.setCo(nowobj.getString("co"));
                    now.setNo2(nowobj.getString("no2"));
                    now.setO3(nowobj.getString("o3"));
                    now.setPm2p5(nowobj.getString("pm2p5"));
                    now.setPm10(nowobj.getString("pm10"));
                    now.setPrimary(nowobj.getString("primary"));
                    now.setPubTime(nowobj.getString("pubTime"));
                    now.setSo2(nowobj.getString("so2"));
                    JSONArray stationes = response.getJSONArray("station");
                    List<Station> stationList = new ArrayList<Station>();
                    List<JSONObject> list = JSONObject.parseArray(stationes.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Station station = new Station();
                        station.setAqi(jsonObject.getString("api"));
                        station.setCategory(jsonObject.getString("category"));
                        station.setCo(jsonObject.getString("co"));
                        station.setId(jsonObject.getString("stationId"));
                        station.setLevel(jsonObject.getString("level"));
                        station.setName(jsonObject.getString("stationName"));
                        station.setNo2(jsonObject.getString("no2"));
                        station.setO3(jsonObject.getString("o3"));
                        station.setPm2p5(jsonObject.getString("pm2p5"));
                        station.setPm10(jsonObject.getString("pm10"));
                        station.setPrimary(jsonObject.getString("primary"));
                        station.setPubTime(jsonObject.getString("pubTime"));
                        station.setSo2(jsonObject.getString("so2"));
                        stationList.add(station);
                    }
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    airQuality.setNow(now);
                    airQuality.setStation(stationList);
                    airQuality.setRefer(refer);
                    heWeatherResponse.setAirQuality(airQuality);
                } else if (response.containsKey("daily")) {
                    List<Daily> dailyList = new ArrayList<Daily>();
                    JSONArray dailies = response.getJSONArray("station");
                    List<JSONObject> list = JSONObject.parseArray(dailies.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Daily daily = new Daily();
                        daily.setAqi(jsonObject.getString("aqi"));
                        daily.setCategory(jsonObject.getString("category"));
                        daily.setFxDate(jsonObject.getString("fxDate"));
                        daily.setPrimary(jsonObject.getString("primary"));
                        dailyList.add(daily);
                    }
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    airQuality.setDaily(dailyList);
                    airQuality.setRefer(refer);
                }
            } else {
                heWeatherResponse.setStatus("400");
            }
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("400");
        }
        return heWeatherResponse;
    }
}
