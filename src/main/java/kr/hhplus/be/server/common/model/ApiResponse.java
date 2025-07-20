package kr.hhplus.be.server.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse implements Serializable {

    private static final long serialVersionUID = -1206151647868602861L;

    protected ApiResponse(){}

    @Builder.Default
    String resultCode = "200";
    @Builder.Default
    String msg = "OK";

    @Builder.Default
    Map<String, Object> data = new HashMap<String, Object>();

    @Builder.Default
    TimeZone timeZone = TimeZone.getDefault();

    @Builder.Default
    Long timeStamp = System.currentTimeMillis();

    @Builder.Default
    String languageCode = Locale.getDefault().getLanguage();

}
