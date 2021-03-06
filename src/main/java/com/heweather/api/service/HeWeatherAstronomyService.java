package com.heweather.api.service;

import com.heweather.api.dto.response.HeWeatherResponse;

/**
 * 太阳和月亮
 * add by djc
 */
public interface HeWeatherAstronomyService {
    //日出日落、月升月落和月相
    HeWeatherResponse getSunmoon(String location,String date);
    //日出日落、月升月落和月相
    HeWeatherResponse getSunmoon(String location,String date, String lang);
}
