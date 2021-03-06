package com.heweather.api.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heweather.api.InitializeSign;
import com.heweather.api.dto.UnitEnum;
import com.heweather.api.dto.ApiEnum;
import com.heweather.api.dto.response.HeWeatherResponse;
import com.heweather.api.dto.response.WeatherPoiInfo;
import com.heweather.api.dto.response.weatherinfo.Refer;
import com.heweather.api.dto.response.weatherpoi.Daily;
import com.heweather.api.dto.response.weatherpoi.Now;
import com.heweather.api.service.HeWeatherPoiService;
import com.heweather.utils.SignUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 中国景点天气预报请求处理
 * add by djc
 * @author djc
 */
public class HeWeatherPoiServiceImpl implements HeWeatherPoiService {

    @Override
    public HeWeatherResponse getWeatherPoi(ApiEnum apiEnum, UnitEnum enum_unit_code, String location, String lang) {

        HeWeatherResponse heWeatherResponse = new HeWeatherResponse();
        String URL = "https://api.heweather.net/v7/weather-poi/";
        String key;
        String sign;
        try{
            key = InitializeSign.getKey();
            sign = InitializeSign.getSign();
        }catch (Exception e){
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
        HashMap<String, String> params = new HashMap<>();
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
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        if (enum_unit_code != null) {
            URL = URL + "&unit" + enum_unit_code.getValue();
            params.put("unit", enum_unit_code.getValue());
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
                WeatherPoiInfo weatherPoiInfo = new WeatherPoiInfo();
                if (response.containsKey("now")) {
                    JSONObject jsonObject = response.getJSONObject("now");
                    Now now = new Now();
                    now.setFeelsLike(jsonObject.getString("feelsLike"));
                    now.setHumidity(jsonObject.getString("humidity"));
                    now.setIcon(jsonObject.getString("icon"));
                    now.setObsTime(jsonObject.getString("obsTime"));
                    now.setPrecip(jsonObject.getString("precip"));
                    now.setPressure(jsonObject.getString("pressure"));
                    now.setTemp(jsonObject.getString("temp"));
                    now.setText(jsonObject.getString("text"));
                    now.setWindDir(jsonObject.getString("windDir"));
                    now.setWindScale(jsonObject.getString("windScale"));
                    weatherPoiInfo.setNow(now);
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    weatherPoiInfo.setRefer(refer);
                    heWeatherResponse.setWeatherPoiInfo(weatherPoiInfo);
                } else if (response.containsKey("daily")) {
                    JSONArray jsonArray = response.getJSONArray("daily");
                    List<Daily> dailyList = new ArrayList<Daily>();
                    List<JSONObject> list = JSONObject.parseArray(jsonArray.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Daily daily = new Daily();
                        daily.setFxDate(jsonObject.getString("fxDate"));
                        daily.setIconDay(jsonObject.getString("iconDay"));
                        daily.setIconNight(jsonObject.getString("iconNight"));
                        daily.setTempMax(jsonObject.getString("tempMax"));
                        daily.setTempMin(jsonObject.getString("tempMin"));
                        daily.setTextDay(jsonObject.getString("textDay"));
                        daily.setTextNight(jsonObject.getString("textNight"));
                        daily.setWindDirDay(jsonObject.getString("windDirDay"));
                        daily.setWindDirNight(jsonObject.getString("windDirNight"));
                        daily.setWindScaleDay(jsonObject.getString("windScaleDay"));
                        daily.setWindScaleNight(jsonObject.getString("windScaleNight"));
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
                    weatherPoiInfo.setRefer(refer);
                    weatherPoiInfo.setDailyList(dailyList);
                    heWeatherResponse.setWeatherPoiInfo(weatherPoiInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("400");

        }
        return heWeatherResponse;
    }

    @Override
    public HeWeatherResponse getWeatherPoi(ApiEnum apiEnum, String location) {

        HeWeatherResponse heWeatherResponse = new HeWeatherResponse();
        String URL = "https://api.heweather.net/v7/weather-poi/";
        String key;
        String sign;
        try{
            key = InitializeSign.getKey();
            sign = InitializeSign.getSign();
        }catch (Exception e){
            e.printStackTrace();
            heWeatherResponse.setStatus("4001");
            return heWeatherResponse;
        }
        if (apiEnum != null && !apiEnum.getName().equals("")) {
            URL = URL + apiEnum.getValue();
        } else {
            heWeatherResponse.setStatus("400");
            return heWeatherResponse;
        }
        HashMap<String, String> params = new HashMap<>();
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
                WeatherPoiInfo weatherPoiInfo = new WeatherPoiInfo();
                if (response.containsKey("now")) {
                    JSONObject jsonObject = response.getJSONObject("now");
                    Now now = new Now();
                    now.setFeelsLike(jsonObject.getString("feelsLike"));
                    now.setHumidity(jsonObject.getString("humidity"));
                    now.setIcon(jsonObject.getString("icon"));
                    now.setObsTime(jsonObject.getString("obsTime"));
                    now.setPrecip(jsonObject.getString("precip"));
                    now.setPressure(jsonObject.getString("pressure"));
                    now.setTemp(jsonObject.getString("temp"));
                    now.setText(jsonObject.getString("text"));
                    now.setWindDir(jsonObject.getString("windDir"));
                    now.setWindScale(jsonObject.getString("windScale"));
                    weatherPoiInfo.setNow(now);
                    Refer refer = new Refer();
                    JSONObject refer1 = response.getJSONObject("refer");
                    JSONArray sources = refer1.getJSONArray("sources");
                    List<String> sourcesList = JSONObject.parseArray(sources.toJSONString(), String.class);
                    JSONArray license = refer1.getJSONArray("license");
                    List<String> licenseList = JSONObject.parseArray(license.toJSONString(), String.class);
                    refer.setLicense(sourcesList);
                    refer.setSources(licenseList);
                    weatherPoiInfo.setRefer(refer);
                    heWeatherResponse.setWeatherPoiInfo(weatherPoiInfo);
                } else if (response.containsKey("daily")) {
                    JSONArray jsonArray = response.getJSONArray("daily");
                    List<Daily> dailyList = new ArrayList<Daily>();
                    List<JSONObject> list = JSONObject.parseArray(jsonArray.toJSONString(), JSONObject.class);
                    for (JSONObject jsonObject : list) {
                        Daily daily = new Daily();
                        daily.setFxDate(jsonObject.getString("fxDate"));
                        daily.setIconDay(jsonObject.getString("iconDay"));
                        daily.setIconNight(jsonObject.getString("iconNight"));
                        daily.setTempMax(jsonObject.getString("tempMax"));
                        daily.setTempMin(jsonObject.getString("tempMin"));
                        daily.setTextDay(jsonObject.getString("textDay"));
                        daily.setTextNight(jsonObject.getString("textNight"));
                        daily.setWindDirDay(jsonObject.getString("windDirDay"));
                        daily.setWindDirNight(jsonObject.getString("windDirNight"));
                        daily.setWindScaleDay(jsonObject.getString("windScaleDay"));
                        daily.setWindScaleNight(jsonObject.getString("windScaleNight"));
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
                    weatherPoiInfo.setRefer(refer);
                    weatherPoiInfo.setDailyList(dailyList);
                    heWeatherResponse.setWeatherPoiInfo(weatherPoiInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            heWeatherResponse.setStatus("400");
        }
        return heWeatherResponse;
    }
}
