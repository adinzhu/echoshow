package com.meari.echoshow.util;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName CountryRegionMap
 * @Description TODO
 * @Author huangqh
 * @CreateDate: 2019/5/31 15:29
 **/
public class CountryRegionMap {
    private static final String MAP_STRING = "{" +
            "    \"CA\" : \"US\", \"KZ\" : \"CN\", \"RU\" : \"CN\", \"EG\" : \"EU\", \"ZA\" : \"EU\", \"GR\" : \"EU\", \"NL\" : \"EU\", \"BE\" : \"EU\"," +
            "    \"FR\" : \"EU\", \"ES\" : \"EU\", \"HU\" : \"EU\", \"IT\" : \"EU\", \"RO\" : \"EU\", \"CH\" : \"EU\", \"AT\" : \"EU\", \"IM\" : \"EU\"," +
            "    \"GB\" : \"EU\", \"DK\" : \"EU\", \"SE\" : \"EU\", \"NO\" : \"EU\", \"PL\" : \"EU\", \"DE\" : \"EU\", \"PE\" : \"US\", \"MX\" : \"US\"," +
            "    \"AR\" : \"US\", \"BR\" : \"US\", \"CL\" : \"US\", \"CO\" : \"US\", \"VE\" : \"US\", \"MY\" : \"CN\", \"AU\" : \"CN\", \"ID\" : \"CN\"," +
            "    \"PH\" : \"CN\", \"NZ\" : \"CN\", \"SG\" : \"CN\", \"TH\" : \"CN\", \"JP\" : \"CN\", \"KR\" : \"CN\", \"VN\" : \"CN\", \"CN\" : \"CN\"," +
            "    \"TR\" : \"CN\", \"IN\" : \"CN\", \"PK\" : \"CN\", \"AF\" : \"CN\", \"LK\" : \"CN\", \"MM\" : \"CN\", \"IR\" : \"EU\", \"MA\" : \"EU\"," +
            "    \"DZ\" : \"EU\", \"TN\" : \"EU\", \"LY\" : \"EU\", \"GM\" : \"EU\", \"SN\" : \"EU\", \"MR\" : \"EU\", \"ML\" : \"EU\", \"GN\" : \"EU\"," +
            "    \"CI\" : \"EU\", \"BF\" : \"EU\", \"NE\" : \"EU\", \"TG\" : \"EU\", \"BJ\" : \"EU\", \"MU\" : \"EU\", \"LR\" : \"EU\", \"SL\" : \"EU\"," +
            "    \"GH\" : \"EU\", \"NG\" : \"EU\", \"TD\" : \"EU\", \"CF\" : \"EU\", \"CM\" : \"EU\", \"CV\" : \"EU\", \"GQ\" : \"EU\", \"GA\" : \"EU\"," +
            "    \"CG\" : \"EU\", \"CD\" : \"EU\", \"ZR\" : \"EU\", \"AO\" : \"EU\", \"SD\" : \"EU\", \"RW\" : \"EU\", \"ET\" : \"EU\", \"SO\" : \"EU\"," +
            "    \"DJ\" : \"EU\", \"KE\" : \"EU\", \"TZ\" : \"EU\", \"UG\" : \"EU\", \"BI\" : \"EU\", \"MZ\" : \"EU\", \"ZM\" : \"EU\", \"MG\" : \"EU\"," +
            "    \"RE\" : \"EU\", \"ZW\" : \"EU\", \"NA\" : \"EU\", \"MW\" : \"EU\", \"LS\" : \"EU\", \"BW\" : \"EU\", \"SZ\" : \"EU\", \"KM\" : \"EU\"," +
            "    \"ER\" : \"EU\", \"GL\" : \"US\", \"PT\" : \"EU\", \"LU\" : \"EU\", \"IE\" : \"EU\", \"IS\" : \"EU\", \"AL\" : \"EU\", \"MT\" : \"EU\"," +
            "    \"CY\" : \"CN\", \"FI\" : \"EU\", \"BG\" : \"EU\", \"LT\" : \"EU\", \"LV\" : \"EU\", \"EE\" : \"EU\", \"MD\" : \"EU\", \"AM\" : \"CN\"," +
            "    \"BY\" : \"EU\", \"MC\" : \"EU\", \"SM\" : \"EU\", \"VA\" : \"EU\", \"UA\" : \"EU\", \"RS\" : \"EU\", \"YU\" : \"EU\", \"ME\" : \"EU\"," +
            "    \"HR\" : \"EU\", \"SI\" : \"EU\", \"BA\" : \"EU\", \"MK\" : \"EU\", \"CZ\" : \"EU\", \"SK\" : \"EU\", \"BZ\" : \"US\", \"GT\" : \"US\"," +
            "    \"SV\" : \"US\", \"HN\" : \"US\", \"NI\" : \"US\", \"CR\" : \"US\", \"PA\" : \"US\", \"HT\" : \"US\", \"BO\" : \"US\", \"GY\" : \"US\"," +
            "    \"EC\" : \"US\", \"PY\" : \"US\", \"SR\" : \"US\", \"UY\" : \"US\", \"BN\" : \"EU\", \"TO\" : \"US\", \"FJ\" : \"US\", \"NC\" : \"CN\"," +
            "    \"KP\" : \"EU\", \"HK\" : \"CN\", \"MO\" : \"CN\", \"KH\" : \"CN\", \"LA\" : \"EU\", \"BD\" : \"CN\", \"TW\" : \"CN\", \"MV\" : \"EU\"," +
            "    \"LB\" : \"CN\", \"JO\" : \"CN\", \"SY\" : \"EU\", \"IQ\" : \"EU\", \"KW\" : \"CN\", \"SA\" : \"CN\", \"YE\" : \"EU\", \"OM\" : \"CN\"," +
            "    \"AE\" : \"CN\", \"IL\" : \"CN\", \"BH\" : \"CN\", \"QA\" : \"CN\", \"BT\" : \"EU\", \"MN\" : \"US\", \"NP\" : \"CN\", \"TJ\" : \"EU\"," +
            "    \"TM\" : \"EU\", \"AZ\" : \"CN\", \"GE\" : \"CN\", \"KG\" : \"EU\", \"UZ\" : \"US\", \"VG\" : \"US\", \"VI\" : \"US\", \"KY\" : \"US\"," +
            "    \"PR\" : \"US\", \"DO\" : \"US\", \"TT\" : \"US\", \"JM\" : \"US\", \"AD\" : \"EU\", \"AC\" : \"EU\", \"AW\" : \"US\", \"CU\" : \"US\"," +
            "    \"US\" : \"US\"" +
            "}";
    private static final JSONObject countryMap = JSONObject.parseObject(MAP_STRING);

    public static String getRegionByCountry(String country){
        if(StringUtil.isNull(country)){
            return null;
        }
        return countryMap.getString(country);
    }
}
