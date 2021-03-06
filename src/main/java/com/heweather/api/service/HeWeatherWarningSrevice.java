package com.heweather.api.service;

import com.heweather.api.dto.response.HeWeatherResponse;

/**
 * 天气灾害预警
 */
public interface HeWeatherWarningSrevice {
    //灾害预警
    HeWeatherResponse getWeatherWarning(String location, String lang);

    //灾害预警
    HeWeatherResponse getWeatherWarning(String location);

    //灾害预警城市列表
    HeWeatherResponse getWarningLocList(String range);
}
